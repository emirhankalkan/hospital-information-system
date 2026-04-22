package com.his.controller;

import com.his.dto.request.PatientRequest;
import com.his.dto.response.PatientResponse;
import com.his.entity.Patient;
import com.his.entity.User;
import com.his.mapper.PatientMapper;
import com.his.service.PatientService;
import com.his.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final PatientMapper patientMapper;
    private final UserService userService;

    // ADMIN, RECEPTIONIST, DOCTOR
    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        List<PatientResponse> patients = patientService.findAllActive()
                .stream()
                .map(patientMapper::toResponse)
                .toList();
        return ResponseEntity.ok(patients);
    }

    // ADMIN, RECEPTIONIST, DOCTOR
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long id) {
        Patient patient = patientService.findById(id);
        return ResponseEntity.ok(patientMapper.toResponse(patient));
    }

    // ADMIN, RECEPTIONIST, DOCTOR
    @GetMapping("/tc/{tcNo}")
    public ResponseEntity<PatientResponse> getPatientByTcNo(@PathVariable String tcNo) {
        Patient patient = patientService.findByTcNo(tcNo);
        return ResponseEntity.ok(patientMapper.toResponse(patient));
    }

    // ADMIN, RECEPTIONIST, DOCTOR
    @GetMapping("/search")
    public ResponseEntity<List<PatientResponse>> searchPatients(@RequestParam String keyword) {
        List<PatientResponse> results = patientService.searchPatients(keyword)
                .stream()
                .map(patientMapper::toResponse)
                .toList();
        return ResponseEntity.ok(results);
    }

    // ADMIN, RECEPTIONIST
    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("Hasta oluşturmak için userId zorunludur.");
        }
        User user = userService.findById(request.getUserId());
        Patient patient = patientMapper.toEntity(request, user);
        Patient saved = patientService.createPatient(patient);
        return ResponseEntity.status(HttpStatus.CREATED).body(patientMapper.toResponse(saved));
    }

    // ADMIN, RECEPTIONIST
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {
        Patient existing = patientService.findById(id);
        patientMapper.updateEntityFromRequest(request, existing);
        Patient updated = patientService.updatePatient(id, existing);
        return ResponseEntity.ok(patientMapper.toResponse(updated));
    }

    // ADMIN only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
