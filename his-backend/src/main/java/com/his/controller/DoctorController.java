package com.his.controller;

import com.his.dto.request.DoctorRequest;
import com.his.dto.response.DoctorResponse;
import com.his.entity.Department;
import com.his.entity.Doctor;
import com.his.entity.User;
import com.his.mapper.DoctorMapper;
import com.his.service.DepartmentService;
import com.his.service.DoctorService;
import com.his.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorMapper doctorMapper;
    private final UserService userService;
    private final DepartmentService departmentService;

    // PUBLIC — tüm roller görebilir
    @GetMapping
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        List<DoctorResponse> doctors = doctorService.findAll()
                .stream()
                .map(doctorMapper::toResponse)
                .toList();
        return ResponseEntity.ok(doctors);
    }

    // PUBLIC
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable Long id) {
        Doctor doctor = doctorService.findById(id);
        return ResponseEntity.ok(doctorMapper.toResponse(doctor));
    }

    // PUBLIC
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<DoctorResponse>> getDoctorsByDepartment(@PathVariable Long departmentId) {
        List<DoctorResponse> doctors = doctorService.findByDepartmentId(departmentId)
                .stream()
                .map(doctorMapper::toResponse)
                .toList();
        return ResponseEntity.ok(doctors);
    }

    // PUBLIC
    @GetMapping("/search")
    public ResponseEntity<List<DoctorResponse>> searchDoctors(@RequestParam String keyword) {
        List<DoctorResponse> results = doctorService.searchDoctors(keyword)
                .stream()
                .map(doctorMapper::toResponse)
                .toList();
        return ResponseEntity.ok(results);
    }

    // ADMIN only
    @PostMapping
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody DoctorRequest request) {
        User user = userService.findById(request.getUserId());
        Department department = departmentService.findById(request.getDepartmentId());
        Doctor doctor = doctorMapper.toEntity(request, user, department);
        Doctor saved = doctorService.createDoctor(doctor);
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorMapper.toResponse(saved));
    }

    // ADMIN only
    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequest request) {
        Doctor existing = doctorService.findById(id);
        Department department = departmentService.findById(request.getDepartmentId());
        doctorMapper.updateEntityFromRequest(request, existing, department);
        Doctor updated = doctorService.updateDoctor(id, existing);
        return ResponseEntity.ok(doctorMapper.toResponse(updated));
    }

    // ADMIN only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }
}
