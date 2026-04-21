package com.his.mapper;

import com.his.dto.request.PatientRequest;
import com.his.dto.response.PatientResponse;
import com.his.entity.Patient;
import com.his.entity.User;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    /**
     * PatientRequest DTO -> Patient entity
     * User nesnesinin önceden yüklenmiş (fetched) olması gerekir.
     */
    public Patient toEntity(PatientRequest request, User user) {
        if (request == null) return null;

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setTcNo(request.getTcNo());
        patient.setBirthDate(request.getBirthDate());
        patient.setGender(request.getGender());
        patient.setPhone(request.getPhone());
        patient.setEmail(request.getEmail());
        patient.setAddress(request.getAddress());
        patient.setEmergencyContact(request.getEmergencyContact());
        patient.setBloodType(request.getBloodType());
        return patient;
    }

    /**
     * Patient entity -> PatientResponse DTO
     */
    public PatientResponse toResponse(Patient patient) {
        if (patient == null) return null;

        PatientResponse response = new PatientResponse();
        response.setId(patient.getId());
        response.setFirstName(patient.getFirstName());
        response.setLastName(patient.getLastName());
        response.setTcNo(patient.getTcNo());
        response.setBirthDate(patient.getBirthDate());
        response.setGender(patient.getGender());
        response.setPhone(patient.getPhone());
        response.setEmail(patient.getEmail());
        response.setAddress(patient.getAddress());
        response.setEmergencyContact(patient.getEmergencyContact());
        response.setBloodType(patient.getBloodType());
        response.setIsDeleted(patient.getIsDeleted());
        response.setCreatedAt(patient.getCreatedAt());
        response.setUpdatedAt(patient.getUpdatedAt());

        // Bağlı kullanıcı bilgileri
        if (patient.getUser() != null) {
            response.setUserId(patient.getUser().getId());
            response.setUsername(patient.getUser().getUsername());
        }

        return response;
    }

    /**
     * Mevcut Patient entity'sini request verileriyle günceller (UPDATE işlemi).
     * User ve TcNo değiştirilemez — bu bir iş kuralıdır.
     */
    public void updateEntityFromRequest(PatientRequest request, Patient patient) {
        if (request == null || patient == null) return;

        // tcNo kasıtlı olarak güncellenmez — kimlik numarası değişmez
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setBirthDate(request.getBirthDate());
        patient.setGender(request.getGender());
        patient.setPhone(request.getPhone());
        patient.setEmail(request.getEmail());
        patient.setAddress(request.getAddress());
        patient.setEmergencyContact(request.getEmergencyContact());
        patient.setBloodType(request.getBloodType());
    }
}
