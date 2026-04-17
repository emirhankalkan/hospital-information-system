package com.his.service.impl;

import com.his.entity.Appointment;
import com.his.entity.MedicalRecord;
import com.his.enums.AppointmentStatus;
import com.his.exception.ResourceAlreadyExistsException;
import com.his.exception.ResourceNotFoundException;
import com.his.repository.MedicalRecordRepository;
import com.his.service.AppointmentService;
import com.his.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentService appointmentService;

    @Override
    public MedicalRecord findById(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tıbbi kayıt bulunamadı, id: " + id));
    }

    @Override
    public MedicalRecord findByAppointmentId(Long appointmentId) {
        return medicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bu randevuya ait tıbbi kayıt bulunamadı, randevu id: " + appointmentId));
    }

    @Override
    public List<MedicalRecord> findByPatientId(Long patientId) {
        return medicalRecordRepository.findByAppointment_Patient_IdOrderByCreatedAtDesc(patientId);
    }

    @Override
    public List<MedicalRecord> findByDoctorId(Long doctorId) {
        return medicalRecordRepository.findByAppointment_Doctor_IdOrderByCreatedAtDesc(doctorId);
    }

    private boolean isStringEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    @Override
    @Transactional
    public MedicalRecord createMedicalRecord(MedicalRecord medicalRecord) {
        if (medicalRecord.getAppointment() == null || medicalRecord.getAppointment().getId() == null) {
            throw new IllegalArgumentException("Tıbbi kayıt geçerli bir randevu ile ilişkilendirilmelidir");
        }
        
        // Boş medikal kayıt oluşturulmasını engelleyen iş kuralı
        if (isStringEmpty(medicalRecord.getDiagnosis()) && 
            isStringEmpty(medicalRecord.getTreatmentNotes()) && 
            isStringEmpty(medicalRecord.getPrescriptionNotes())) {
            throw new IllegalArgumentException("Tıbbi kaydın oluşturulması için Teşhis, Tedavi Notu veya Reçete bilgilerinden en az biri doldurulmalıdır");
        }

        Long appointmentId = medicalRecord.getAppointment().getId();

        // 1. Randevunun gerçekten var olup olmadığı ve silinmiş/pasif hesaplara ait olup olmadığı
        // AppointmentService üzerinden doğrulanıyor (İş kuralı mirası)
        Appointment appointment = appointmentService.findById(appointmentId);

        if (appointment.getStatus() == AppointmentStatus.CANCELED) {
            throw new IllegalStateException("İptal edilmiş bir randevuya tıbbi kayıt oluşturulamaz");
        }

        // 2. Bu randevu için zaten bir kayıt var mı kontrolü (Birebir ilişki)
        if (medicalRecordRepository.existsByAppointmentId(appointmentId)) {
            throw new ResourceAlreadyExistsException("Bu randevu için halihazırda bir tıbbi kayıt oluşturulmuş");
        }

        medicalRecord.setAppointment(appointment);
        MedicalRecord savedRecord = medicalRecordRepository.save(medicalRecord);

        // 3. Otomatik Randevu Durumu Güncellemesi: Eklenebilecek yeni statüleri ezmemek ve akışı bozmamak 
        // için sadece randevu SCHEDULED ise durumu güncelliyoruz.
        if (appointment.getStatus() == AppointmentStatus.SCHEDULED) {
            appointmentService.updateAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED);
        }

        return savedRecord;
    }

    @Override
    @Transactional
    public MedicalRecord updateMedicalRecord(Long id, MedicalRecord recordDetails) {
        MedicalRecord existingRecord = findById(id);

        // Null kontrolü: Kullanıcı API'den yanlışlıkla null gönderirse eski veriyi uçurmayı engelliyoruz.
        // Eğer veriyi silmek isterse açıkça boş string ("") yollamalı.
        if (recordDetails.getDiagnosis() != null) {
            existingRecord.setDiagnosis(recordDetails.getDiagnosis());
        }
        if (recordDetails.getTreatmentNotes() != null) {
            existingRecord.setTreatmentNotes(recordDetails.getTreatmentNotes());
        }
        if (recordDetails.getPrescriptionNotes() != null) {
            existingRecord.setPrescriptionNotes(recordDetails.getPrescriptionNotes());
        }

        // Not: Tıbbi kayıtların bağlı olduğu Randevu (Appointment) update işlemiyle değiştirilemez.
        // Bu güvenlik için bir iş kuralıdır. Hastanın geçmiş teşhislerinin başka randevuya kaydırılması yasaktır.

        return medicalRecordRepository.save(existingRecord);
    }
}
