package com.his.service.impl;

import com.his.entity.Doctor;
import com.his.exception.ResourceAlreadyExistsException;
import com.his.exception.ResourceNotFoundException;
import com.his.repository.DoctorRepository;
import com.his.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    private boolean isDoctorUserActive(Doctor doctor) {
        return doctor.getUser() != null && Boolean.TRUE.equals(doctor.getUser().getIsActive());
    }

    @Override
    public Doctor findById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doktor bulunamadı, id: " + id));
                
        // Eğer bağlı olduğu User pasif durumdaysa, dışarıya aktif bir doktor gibi görünmemeli
        if (!isDoctorUserActive(doctor)) {
            throw new ResourceNotFoundException("Doktora bağlı kullanıcı hesabı pasif durumda");
        }
        return doctor;
    }

    @Override
    public Doctor findByUserId(Long userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doktor bulunamadı, user id: " + userId));
                
        if (!isDoctorUserActive(doctor)) {
            throw new ResourceNotFoundException("Doktora bağlı kullanıcı hesabı pasif durumda");
        }
        return doctor;
    }

    @Override
    public List<Doctor> findAll() {
        // Sadece bağlı olduğu kullanıcı hesabı aktif olanları getir
        return doctorRepository.findAll().stream()
                .filter(this::isDoctorUserActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<Doctor> findByDepartmentId(Long departmentId) {
        return doctorRepository.findByDepartmentId(departmentId).stream()
                .filter(this::isDoctorUserActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<Doctor> searchDoctors(String keyword) {
        return doctorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(keyword, keyword).stream()
                .filter(this::isDoctorUserActive)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Doctor createDoctor(Doctor doctor) {
        // Validation kontrolleri
        if (doctor.getUser() == null || doctor.getUser().getId() == null) {
            throw new IllegalArgumentException("Doktor geçerli bir kullanıcı hesabıyla ilişkilendirilmeli");
        }
        if (doctor.getDepartment() == null || doctor.getDepartment().getId() == null) {
            throw new IllegalArgumentException("Doktor geçerli bir departmanla ilişkilendirilmeli");
        }
        
        if (doctorRepository.existsByUserId(doctor.getUser().getId())) {
            throw new ResourceAlreadyExistsException("Bu kullanıcı kimliğine bağlı bir doktor profili zaten mevcut: " + doctor.getUser().getId());
        }
        
        return doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public Doctor updateDoctor(Long id, Doctor doctorDetails) {
        Doctor existingDoctor = findById(id);
        
        // Sadece doktora ait alanlar güncellenir
        existingDoctor.setFirstName(doctorDetails.getFirstName());
        existingDoctor.setLastName(doctorDetails.getLastName());
        existingDoctor.setSpecialization(doctorDetails.getSpecialization());
        existingDoctor.setPhone(doctorDetails.getPhone());
        
        if (doctorDetails.getDepartment() != null) {
            existingDoctor.setDepartment(doctorDetails.getDepartment());
        }
        
        return doctorRepository.save(existingDoctor);
    }

    @Override
    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doktor bulunamadı, id: " + id));
                
        // Eğer ileride Doctor için de isActive alanı (soft delete) eklenirse burası güncellenmeli.
        // Şimdilik FK kısıtlamalarını göz ardı edip hard delete yapıyoruz. Geçmiş verilerin bozulmaması
        // için doktoru silmek yerine User nesnesini pasife çekmek pratik bir çözüm olabilir.
        doctorRepository.delete(doctor);
    }
}
