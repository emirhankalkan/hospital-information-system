package com.his.controller;

import com.his.dto.request.MedicalRecordRequest;
import com.his.dto.response.MedicalRecordResponse;
import com.his.entity.MedicalRecord;
import com.his.mapper.MedicalRecordMapper;
import com.his.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final MedicalRecordMapper medicalRecordMapper;

    // DOCTOR, ADMIN
    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecordResponse> getMedicalRecordById(@PathVariable Long id) {
        MedicalRecord record = medicalRecordService.findById(id);
        return ResponseEntity.ok(medicalRecordMapper.toResponse(record));
    }

    // DOCTOR, ADMIN — randevuya ait kayıt
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<MedicalRecordResponse> getByAppointment(@PathVariable Long appointmentId) {
        MedicalRecord record = medicalRecordService.findByAppointmentId(appointmentId);
        return ResponseEntity.ok(medicalRecordMapper.toResponse(record));
    }

    // DOCTOR, ADMIN — hastanın tüm kayıtları
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecordResponse>> getByPatient(@PathVariable Long patientId) {
        List<MedicalRecordResponse> records = medicalRecordService.findByPatientId(patientId)
                .stream()
                .map(medicalRecordMapper::toResponse)
                .toList();
        return ResponseEntity.ok(records);
    }

    // DOCTOR — doktorun yazdığı tüm kayıtlar
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<MedicalRecordResponse>> getByDoctor(@PathVariable Long doctorId) {
        List<MedicalRecordResponse> records = medicalRecordService.findByDoctorId(doctorId)
                .stream()
                .map(medicalRecordMapper::toResponse)
                .toList();
        return ResponseEntity.ok(records);
    }

    // DOCTOR only — tıbbi kayıt oluştur
    @PostMapping
    public ResponseEntity<MedicalRecordResponse> createMedicalRecord(
            @Valid @RequestBody MedicalRecordRequest request) {
        MedicalRecord record = medicalRecordMapper.toEntity(request);
        MedicalRecord saved = medicalRecordService.createMedicalRecord(record);
        return ResponseEntity.status(HttpStatus.CREATED).body(medicalRecordMapper.toResponse(saved));
    }

    // DOCTOR only — tıbbi kayıt güncelle
    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecordResponse> updateMedicalRecord(
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordRequest request) {
        MedicalRecord existing = medicalRecordService.findById(id);
        medicalRecordMapper.updateEntityFromRequest(request, existing);
        MedicalRecord updated = medicalRecordService.updateMedicalRecord(id, existing);
        return ResponseEntity.ok(medicalRecordMapper.toResponse(updated));
    }
}
