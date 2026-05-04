package com.his.controller;

import com.his.dto.ApiResponse;
import com.his.dto.request.AppointmentRequest;
import com.his.dto.request.AppointmentStatusRequest;
import com.his.dto.response.AppointmentResponse;
import com.his.entity.Appointment;
import com.his.entity.Doctor;
import com.his.entity.Patient;
import com.his.entity.User;
import com.his.mapper.AppointmentMapper;
import com.his.service.AppointmentService;
import com.his.service.DoctorService;
import com.his.service.PatientService;
import com.his.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final UserService userService;

    // ADMIN, RECEPTIONIST, DOCTOR
    // ADMIN, RECEPTIONIST, DOCTOR
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAllAppointments() {
        List<AppointmentResponse> appointments = appointmentService.findAll()
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Randevular listelendi", appointments));
    }

    // ADMIN, RECEPTIONIST, DOCTOR
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @authorizationService.canAccessAppointment(#id)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Randevu bulundu", appointmentMapper.toResponse(appointment)));
    }

    // ADMIN, RECEPTIONIST, DOCTOR — hastaya ait randevular
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @authorizationService.isCurrentPatient(#patientId)")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointmentsByPatient(@PathVariable Long patientId) {
        List<AppointmentResponse> appointments = appointmentService.findByPatientId(patientId)
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Hastanın randevuları listelendi", appointments));
    }

    // DOCTOR — kendi randevuları
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @authorizationService.isCurrentDoctor(#doctorId)")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        List<AppointmentResponse> appointments = appointmentService.findByDoctorId(doctorId)
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Doktorun randevuları listelendi", appointments));
    }

    // DOCTOR — belirli güne ait randevular (günlük takvim)
    @GetMapping("/doctor/{doctorId}/date")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @authorizationService.isCurrentDoctor(#doctorId)")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointmentsByDoctorAndDate(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AppointmentResponse> appointments = appointmentService.findByDoctorIdAndDate(doctorId, date)
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Doktorun günlük randevuları listelendi", appointments));
    }

    // RECEPTIONIST, PATIENT — randevu oluştur
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @authorizationService.isCurrentPatient(#request.patientId)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> bookAppointment(@Valid @RequestBody AppointmentRequest request) {
        Patient patient = patientService.findById(request.getPatientId());
        Doctor doctor = doctorService.findById(request.getDoctorId());

        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User createdByUser = userService.findByUsername(username);

        Appointment appointment = appointmentMapper.toEntity(request, patient, doctor, createdByUser);
        Appointment saved = appointmentService.bookAppointment(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Randevu oluşturuldu", appointmentMapper.toResponse(saved)));
    }

    // RECEPTIONIST, DOCTOR — tarih/saat/notlar güncelle
    // TODO JWT sonrası: sadece randevuyu oluşturan veya ADMIN güncelleyebilmeli
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @authorizationService.canManageAppointment(#id)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request) {
        Appointment existing = appointmentService.findById(id);
        appointmentMapper.updateEntityFromRequest(request, existing);
        Appointment updated = appointmentService.updateAppointment(id, existing);
        return ResponseEntity.ok(ApiResponse.success("Randevu güncellendi", appointmentMapper.toResponse(updated)));
    }

    // DOCTOR, RECEPTIONIST — durum güncelle (SCHEDULED → COMPLETED / CANCELLED)
    // TODO JWT sonrası: @PreAuthorize("hasAnyRole('DOCTOR','RECEPTIONIST','ADMIN')")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @authorizationService.canManageAppointment(#id)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusRequest request) {
        Appointment updated = appointmentService.updateAppointmentStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Randevu durumu güncellendi", appointmentMapper.toResponse(updated)));
    }

    // RECEPTIONIST, PATIENT — randevu iptal et
    // TODO JWT sonrası: hasta sadece kendi randevusunu iptal edebilmeli
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @authorizationService.canCancelAppointment(#id)")
    public ResponseEntity<ApiResponse<Void>> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(ApiResponse.success("Randevu iptal edildi"));
    }
}
