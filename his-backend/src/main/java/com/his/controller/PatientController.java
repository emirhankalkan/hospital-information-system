package com.his.controller;

import com.his.dto.ApiResponse;
import com.his.dto.request.PatientRequest;
import com.his.dto.response.PatientResponse;
import com.his.entity.Patient;
import com.his.entity.User;
import com.his.mapper.PatientMapper;
import com.his.service.PatientService;
import com.his.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Tag(name = "Hastalar", description = "Hasta profillerini listeleme, arama ve yönetme işlemleri")
public class PatientController {

    private final PatientService patientService;
    private final PatientMapper patientMapper;
    private final UserService userService;

    // ADMIN, RECEPTIONIST, DOCTOR
    @GetMapping
    @Operation(summary = "Hastaları listele", description = "ADMIN, RECEPTIONIST ve DOCTOR rolleri aktif hastaları listeler.")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> getAllPatients() {
        List<PatientResponse> patients = patientService.findAllActive()
                .stream()
                .map(patientMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Hastalar listelendi", patients));
    }

    // ADMIN, RECEPTIONIST, DOCTOR veya hastanın kendisi
    @GetMapping("/{id}")
    @Operation(summary = "Hasta detayı getir", description = "Yetkili kullanıcılar veya hastanın kendisi hasta detayını görüntüler.")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR') or @authorizationService.isCurrentPatient(#id)")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(@PathVariable Long id) {
        Patient patient = patientService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Hasta bulundu", patientMapper.toResponse(patient)));
    }

    // ADMIN, RECEPTIONIST, DOCTOR
    @GetMapping("/tc/{tcNo}")
    @Operation(summary = "TC kimlik numarasına göre hasta getir", description = "TC kimlik numarası ile hasta profilini getirir.")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientByTcNo(@PathVariable String tcNo) {
        Patient patient = patientService.findByTcNo(tcNo);
        return ResponseEntity.ok(ApiResponse.success("Hasta bulundu", patientMapper.toResponse(patient)));
    }

    // ADMIN, RECEPTIONIST, DOCTOR
    @GetMapping("/search")
    @Operation(summary = "Hasta ara", description = "Hasta adı, soyadı veya ilgili arama alanlarına göre hasta araması yapar.")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> searchPatients(@RequestParam String keyword) {
        List<PatientResponse> results = patientService.searchPatients(keyword)
                .stream()
                .map(patientMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Arama sonuçları getirildi", results));
    }

    // ADMIN, RECEPTIONIST
    @PostMapping
    @Operation(summary = "Hasta profili oluştur", description = "Mevcut bir kullanıcı hesabına bağlı hasta profili oluşturur.")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PatientResponse>> createPatient(@Valid @RequestBody PatientRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("Hasta oluşturmak için userId zorunludur.");
        }
        User user = userService.findById(request.getUserId());
        Patient patient = patientMapper.toEntity(request, user);
        Patient saved = patientService.createPatient(patient);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Hasta oluşturuldu", patientMapper.toResponse(saved)));
    }

    // ADMIN, RECEPTIONIST veya hastanın kendisi
    @PutMapping("/{id}")
    @Operation(summary = "Hasta profili güncelle", description = "Yetkili kullanıcılar veya hastanın kendisi hasta profilini günceller.")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @authorizationService.isCurrentPatient(#id)")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {
        Patient existing = patientService.findById(id);
        patientMapper.updateEntityFromRequest(request, existing);
        Patient updated = patientService.updatePatient(id, existing);
        return ResponseEntity.ok(ApiResponse.success("Hasta güncellendi", patientMapper.toResponse(updated)));
    }

    // ADMIN only
    @DeleteMapping("/{id}")
    @Operation(summary = "Hasta profilini sil", description = "Hasta profilini siler. Sadece ADMIN rolü kullanabilir.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.success("Hasta silindi"));
    }
}
