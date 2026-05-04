package com.his.controller;

import com.his.dto.ApiResponse;
import com.his.dto.request.MedicalRecordRequest;
import com.his.dto.response.MedicalRecordResponse;
import com.his.entity.MedicalRecord;
import com.his.mapper.MedicalRecordMapper;
import com.his.service.MedicalRecordService;
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
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
@Tag(name = "Tıbbi Kayıtlar", description = "Randevuya bağlı tıbbi kayıt görüntüleme ve yönetme işlemleri")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final MedicalRecordMapper medicalRecordMapper;

    // DOCTOR, ADMIN veya ilgili hasta
    @GetMapping("/{id}")
    @Operation(summary = "Tıbbi kayıt detayı getir", description = "Tıbbi kayıt detayını yetki kurallarına göre görüntüler.")
    @PreAuthorize("hasRole('ADMIN') or @authorizationService.canAccessMedicalRecord(#id)")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> getMedicalRecordById(@PathVariable Long id) {
        MedicalRecord record = medicalRecordService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Tıbbi kayıt bulundu", medicalRecordMapper.toResponse(record)));
    }

    // DOCTOR, ADMIN veya ilgili hasta
    @GetMapping("/appointment/{appointmentId}")
    @Operation(summary = "Randevuya ait tıbbi kaydı getir", description = "Belirli randevuya bağlı tıbbi kaydı getirir.")
    @PreAuthorize("hasRole('ADMIN') or @authorizationService.canAccessMedicalRecordByAppointment(#appointmentId)")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> getByAppointment(@PathVariable Long appointmentId) {
        MedicalRecord record = medicalRecordService.findByAppointmentId(appointmentId);
        return ResponseEntity.ok(ApiResponse.success("Randevuya ait tıbbi kayıt bulundu", medicalRecordMapper.toResponse(record)));
    }

    // ADMIN veya hastanın kendisi
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Hastanın tıbbi kayıtlarını listele", description = "Belirli hastaya ait tıbbi kayıtları listeler. Hasta sadece kendi kayıtlarını görebilir.")
    @PreAuthorize("hasRole('ADMIN') or @authorizationService.isCurrentPatient(#patientId)")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getByPatient(@PathVariable Long patientId) {
        List<MedicalRecordResponse> records = medicalRecordService.findByPatientId(patientId)
                .stream()
                .map(medicalRecordMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Hastanın tıbbi kayıtları listelendi", records));
    }

    // ADMIN veya doktorun kendisi
    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Doktorun yazdığı kayıtları listele", description = "Belirli doktorun oluşturduğu tıbbi kayıtları listeler.")
    @PreAuthorize("hasRole('ADMIN') or @authorizationService.isCurrentDoctor(#doctorId)")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getByDoctor(@PathVariable Long doctorId) {
        List<MedicalRecordResponse> records = medicalRecordService.findByDoctorId(doctorId)
                .stream()
                .map(medicalRecordMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Doktorun yazdığı tıbbi kayıtlar listelendi", records));
    }

    // DOCTOR only
    @PostMapping
    @Operation(summary = "Tıbbi kayıt oluştur", description = "Tamamlanan veya ilgili randevu için doktor tarafından tıbbi kayıt oluşturur.")
    @PreAuthorize("@authorizationService.canCreateMedicalRecordForAppointment(#request.appointmentId)")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> createMedicalRecord(
            @Valid @RequestBody MedicalRecordRequest request) {
        MedicalRecord record = medicalRecordMapper.toEntity(request);
        MedicalRecord saved = medicalRecordService.createMedicalRecord(record);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tıbbi kayıt oluşturuldu", medicalRecordMapper.toResponse(saved)));
    }

    // DOCTOR only
    @PutMapping("/{id}")
    @Operation(summary = "Tıbbi kayıt güncelle", description = "Doktorun yetkili olduğu tıbbi kaydı günceller.")
    @PreAuthorize("@authorizationService.canUpdateMedicalRecord(#id)")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> updateMedicalRecord(
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordRequest request) {
        MedicalRecord existing = medicalRecordService.findById(id);
        medicalRecordMapper.updateEntityFromRequest(request, existing);
        MedicalRecord updated = medicalRecordService.updateMedicalRecord(id, existing);
        return ResponseEntity.ok(ApiResponse.success("Tıbbi kayıt güncellendi", medicalRecordMapper.toResponse(updated)));
    }
}
