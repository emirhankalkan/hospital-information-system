package com.his.controller;

import com.his.dto.ApiResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getAllDoctors() {
        List<DoctorResponse> doctors = doctorService.findAll()
                .stream()
                .map(doctorMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Doktorlar listelendi", doctors));
    }

    // PUBLIC
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorResponse>> getDoctorById(@PathVariable Long id) {
        Doctor doctor = doctorService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Doktor bulundu", doctorMapper.toResponse(doctor)));
    }

    // PUBLIC
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getDoctorsByDepartment(@PathVariable Long departmentId) {
        List<DoctorResponse> doctors = doctorService.findByDepartmentId(departmentId)
                .stream()
                .map(doctorMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Departmana göre doktorlar listelendi", doctors));
    }

    // PUBLIC
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> searchDoctors(@RequestParam String keyword) {
        List<DoctorResponse> results = doctorService.searchDoctors(keyword)
                .stream()
                .map(doctorMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Arama sonuçları getirildi", results));
    }

    // ADMIN only
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponse>> createDoctor(@Valid @RequestBody DoctorRequest request) {
        User user = userService.findById(request.getUserId());
        Department department = departmentService.findById(request.getDepartmentId());
        Doctor doctor = doctorMapper.toEntity(request, user, department);
        Doctor saved = doctorService.createDoctor(doctor);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Doktor oluşturuldu", doctorMapper.toResponse(saved)));
    }

    // ADMIN only
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponse>> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequest request) {
        Doctor existing = doctorService.findById(id);
        Department department = departmentService.findById(request.getDepartmentId());
        doctorMapper.updateEntityFromRequest(request, existing, department);
        Doctor updated = doctorService.updateDoctor(id, existing);
        return ResponseEntity.ok(ApiResponse.success("Doktor güncellendi", doctorMapper.toResponse(updated)));
    }

    // ADMIN only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok(ApiResponse.success("Doktor silindi"));
    }
}
