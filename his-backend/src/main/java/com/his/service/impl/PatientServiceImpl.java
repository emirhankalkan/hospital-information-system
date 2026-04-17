package com.his.service.impl;

import com.his.entity.Patient;
import com.his.exception.ResourceAlreadyExistsException;
import com.his.exception.ResourceNotFoundException;
import com.his.repository.PatientRepository;
import com.his.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    private boolean isPatientUserActive(Patient patient) {
        return patient.getUser() != null && Boolean.TRUE.equals(patient.getUser().getIsActive());
    }

    @Override
    public Patient findById(Long id) {
        Patient patient = patientRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hasta bulunamadı, id: " + id));
        
        if (!isPatientUserActive(patient)) {
            throw new ResourceNotFoundException("Hastaya bağlı kullanıcı hesabı pasif durumda");
        }
        return patient;
    }

    @Override
    public Patient findByUserId(Long userId) {
        Patient patient = patientRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hasta bulunamadı, user id: " + userId));
                
        if (!isPatientUserActive(patient)) {
            throw new ResourceNotFoundException("Hastaya bağlı kullanıcı hesabı pasif durumda");
        }
        return patient;
    }

    @Override
    public Patient findByTcNo(String tcNo) {
        Patient patient = patientRepository.findByTcNoAndIsDeletedFalse(tcNo)
                .orElseThrow(() -> new ResourceNotFoundException("Hasta bulunamadı, T.C. Kimlik No: " + tcNo));
                
        if (!isPatientUserActive(patient)) {
            throw new ResourceNotFoundException("Hastaya bağlı kullanıcı hesabı pasif durumda");
        }
        return patient;
    }

    @Override
    public List<Patient> findAllActive() {
        return patientRepository.findByIsDeleted(false).stream()
                .filter(this::isPatientUserActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<Patient> searchPatients(String keyword) {
        return patientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(keyword, keyword).stream()
                .filter(patient -> Boolean.FALSE.equals(patient.getIsDeleted()))
                .filter(this::isPatientUserActive)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Patient createPatient(Patient patient) {
        if (patient.getUser() == null || patient.getUser().getId() == null) {
            throw new IllegalArgumentException("Hasta profili geçerli bir kullanıcı hesabıyla ilişkilendirilmeli");
        }
        
        if (patientRepository.existsByUserId(patient.getUser().getId())) {
            throw new ResourceAlreadyExistsException("Bu kullanıcı kimliğine (user id) bağlı bir hasta profili zaten mevcut: " + patient.getUser().getId());
        }
        
        if (patientRepository.existsByTcNo(patient.getTcNo())) {
            throw new ResourceAlreadyExistsException("Bu T.C. Kimlik numarasına sahip bir hasta profili zaten mevcut: " + patient.getTcNo());
        }
        
        patient.setIsDeleted(false); // Varsayılan olarak silinmemiş başlasın
        return patientRepository.save(patient);
    }

    @Override
    @Transactional
    public Patient updatePatient(Long id, Patient patientDetails) {
        Patient existingPatient = findById(id);
        
        if (!existingPatient.getTcNo().equals(patientDetails.getTcNo()) && patientRepository.existsByTcNo(patientDetails.getTcNo())) {
             throw new ResourceAlreadyExistsException("Bu T.C. Kimlik numarası başka bir hasta tarafından kullanılıyor: " + patientDetails.getTcNo());
        }
        
        existingPatient.setFirstName(patientDetails.getFirstName());
        existingPatient.setLastName(patientDetails.getLastName());
        existingPatient.setTcNo(patientDetails.getTcNo());
        existingPatient.setBirthDate(patientDetails.getBirthDate());
        existingPatient.setGender(patientDetails.getGender());
        existingPatient.setPhone(patientDetails.getPhone());
        existingPatient.setEmail(patientDetails.getEmail());
        existingPatient.setAddress(patientDetails.getAddress());
        existingPatient.setEmergencyContact(patientDetails.getEmergencyContact());
        existingPatient.setBloodType(patientDetails.getBloodType());
        
        return patientRepository.save(existingPatient);
    }

    @Override
    @Transactional
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hasta bulunamadı, id: " + id));
                
        if (Boolean.TRUE.equals(patient.getIsDeleted())) {
            throw new IllegalStateException("Hasta profili zaten silinmiş durumda, id: " + id);
        }
        
        patient.setIsDeleted(true); // Soft delete applied here
        patientRepository.save(patient);
    }
}
