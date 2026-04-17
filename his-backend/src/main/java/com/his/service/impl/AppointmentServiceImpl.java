package com.his.service.impl;

import com.his.entity.Appointment;
import com.his.entity.Doctor;
import com.his.entity.Patient;
import com.his.enums.AppointmentStatus;
import com.his.exception.ResourceAlreadyExistsException;
import com.his.exception.ResourceNotFoundException;
import com.his.repository.AppointmentRepository;
import com.his.service.AppointmentService;
import com.his.service.DoctorService;
import com.his.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;
    private final DoctorService doctorService;

    // Geçmiş tarih/saat için randevu verilmesini engelleyen kontrol metodu
    private void validateFutureDate(LocalDate date, LocalTime time) {
        if (date == null || time == null) {
            throw new IllegalArgumentException("Randevu tarihi ve saati boş (null) olamaz");
        }
        
        LocalDate today = LocalDate.now();
        // Saniye ve milisaniye faktöründen kaynaklanan hatalı sapmaları önlemek için sıfırlanıyor
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        
        if (date.isBefore(today)) {
            throw new IllegalArgumentException("Geçmiş bir tarih için randevu alınamaz");
        }
        
        if (date.isEqual(today) && time.isBefore(now)) {
            throw new IllegalArgumentException("Bugün için geçmiş bir saate randevu alınamaz");
        }
    }

    @Override
    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Randevu bulunamadı, id: " + id));
    }

    @Override
    public List<Appointment> findAll() {
        return appointmentRepository.findAllByOrderByAppointmentDateAscAppointmentTimeAsc();
    }

    @Override
    public List<Appointment> findByPatientId(Long patientId) {
        // Hastanın randevuları ters kornolojik listeyle, en yeni güncel randevusu en başta olacak şekilde listelenir
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(patientId);
    }

    @Override
    public List<Appointment> findByDoctorId(Long doctorId) {
        // Doktor randevuları yaklaşan ajandasına göre düz sırayla listelenir
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(doctorId);
    }

    @Override
    public List<Appointment> findByDoctorIdAndDate(Long doctorId, LocalDate date) {
        return appointmentRepository.findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(doctorId, date);
    }

    @Override
    @Transactional
    public Appointment bookAppointment(Appointment appointment) {
        if (appointment.getPatient() == null || appointment.getPatient().getId() == null) {
            throw new IllegalArgumentException("Randevu geçerli bir hasta ile ilişkilendirilmeli");
        }
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            throw new IllegalArgumentException("Randevu geçerli bir doktor ile ilişkilendirilmeli");
        }
        
        // Null koruması ve Geçmiş tarih analizi
        validateFutureDate(appointment.getAppointmentDate(), appointment.getAppointmentTime());
        // Servisleri kullanarak varlık (ve aktiflik) kontrolü yapıyoruz. Bu sayede
        // silinmiş bir hastaya veya pasif bir doktora randevu verilemez.
        Patient patient = patientService.findById(appointment.getPatient().getId());
        Doctor doctor = doctorService.findById(appointment.getDoctor().getId());
        
        // Randevu çakışma (Duplicate) kontrolü
        boolean isConflict = appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                doctor.getId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
        );
        
        if (isConflict) {
            throw new ResourceAlreadyExistsException("Doktorun talep edilen tarih ve saatte başka bir randevusu mevcut");
        }
        
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStatus(AppointmentStatus.SCHEDULED); // Yeni randevular her zaman SCHEDULED başlar
        
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment updateAppointment(Long id, Appointment appointmentDetails) {
        Appointment existingAppointment = findById(id);
        
        if (appointmentDetails.getAppointmentDate() == null || appointmentDetails.getAppointmentTime() == null) {
            throw new IllegalArgumentException("Randevu tarihi ve saati boş (null) olamaz");
        }
        
        boolean isRescheduling = !existingAppointment.getAppointmentDate().equals(appointmentDetails.getAppointmentDate()) || 
                                 !existingAppointment.getAppointmentTime().equals(appointmentDetails.getAppointmentTime());
                                 
        if (isRescheduling) {
            validateFutureDate(appointmentDetails.getAppointmentDate(), appointmentDetails.getAppointmentTime());
            
            // NotId metodu sayesinde, eğer kullanıcı sadece "Notes" alanını değiştirip tarihi aynı bırakırsa
            // kendi id'si harici diğer kayıtlarda bu saat boş mu diye bakıldığında false döner, hatasız güncellenir.
            // Fakat performans için zaten bu sadece rescheduling varsa çağırılıyor
            boolean isConflict = appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndIdNot(
                    existingAppointment.getDoctor().getId(),
                    appointmentDetails.getAppointmentDate(),
                    appointmentDetails.getAppointmentTime(),
                    existingAppointment.getId()
            );
            
            if (isConflict) {
                throw new ResourceAlreadyExistsException("Erteleme başarısız: Talep edilen zaman dilimi zaten dolu");
            }
        }
        
        // Doctor and Patient fields are intentionally NOT changed during appointment update to secure records
        existingAppointment.setAppointmentDate(appointmentDetails.getAppointmentDate());
        existingAppointment.setAppointmentTime(appointmentDetails.getAppointmentTime());
        existingAppointment.setNotes(appointmentDetails.getNotes());
        
        // Durum (Status) kontrolü
        if (appointmentDetails.getStatus() != null) {
            existingAppointment.setStatus(appointmentDetails.getStatus());
        }
        
        return appointmentRepository.save(existingAppointment);
    }

    @Override
    @Transactional
    public Appointment updateAppointmentStatus(Long id, AppointmentStatus status) {
        Appointment appointment = findById(id);
        
        if (status == null) {
            throw new IllegalArgumentException("Randevu durumu boş (null) olamaz");
        }
        
        if (appointment.getStatus() == status) {
            throw new IllegalStateException("Randevu zaten bu durumda");
        }
        
        if (appointment.getStatus() == AppointmentStatus.CANCELED && status != AppointmentStatus.CANCELED) {
            throw new IllegalStateException("Durum değiştirilemez: Randevu zaten iptal edilmiş");
        }
        
        if (appointment.getStatus() == AppointmentStatus.COMPLETED && status != AppointmentStatus.COMPLETED) {
             throw new IllegalStateException("Durum değiştirilemez: Randevu zaten tamamlanmış");
        }
        
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public void cancelAppointment(Long id) {
        // Zaten gerekli Exception / Geçiş kontrolleri UpdateAppointmentStatus metodunda
        // kapsamlı şekilde olduğu için DRY kuralı (Don't Repeat Yourself) gereği onu çağırıyoruz.
        updateAppointmentStatus(id, AppointmentStatus.CANCELED);
    }
}
