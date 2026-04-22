package com.his.controller;

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
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        List<AppointmentResponse> appointments = appointmentService.findAll()
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(appointments);
    }

    // ADMIN, RECEPTIONIST, DOCTOR
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.findById(id);
        return ResponseEntity.ok(appointmentMapper.toResponse(appointment));
    }

    // ADMIN, RECEPTIONIST, DOCTOR — hastaya ait randevular
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByPatient(@PathVariable Long patientId) {
        List<AppointmentResponse> appointments = appointmentService.findByPatientId(patientId)
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(appointments);
    }

    // DOCTOR — kendi randevuları
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        List<AppointmentResponse> appointments = appointmentService.findByDoctorId(doctorId)
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(appointments);
    }

    // DOCTOR — belirli güne ait randevular (günlük takvim)
    @GetMapping("/doctor/{doctorId}/date")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByDoctorAndDate(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AppointmentResponse> appointments = appointmentService.findByDoctorIdAndDate(doctorId, date)
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(appointments);
    }

    // RECEPTIONIST, PATIENT — randevu oluştur
    @PostMapping
    public ResponseEntity<AppointmentResponse> bookAppointment(@Valid @RequestBody AppointmentRequest request) {
        Patient patient = patientService.findById(request.getPatientId());
        Doctor doctor = doctorService.findById(request.getDoctorId());

        // createdByUserId opsiyoneldir (hastanın kendisi rezerve edebilir)
        User createdByUser = null;
        if (request.getCreatedByUserId() != null) {
            createdByUser = userService.findById(request.getCreatedByUserId());
        }

        Appointment appointment = appointmentMapper.toEntity(request, patient, doctor, createdByUser);
        Appointment saved = appointmentService.bookAppointment(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentMapper.toResponse(saved));
    }

    // RECEPTIONIST, DOCTOR — tarih/saat/notlar güncelle
    // TODO JWT sonrası: sadece randevuyu oluşturan veya ADMIN güncelleyebilmeli
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request) {
        Appointment existing = appointmentService.findById(id);
        appointmentMapper.updateEntityFromRequest(request, existing);
        Appointment updated = appointmentService.updateAppointment(id, existing);
        return ResponseEntity.ok(appointmentMapper.toResponse(updated));
    }

    // DOCTOR, RECEPTIONIST — durum güncelle (SCHEDULED → COMPLETED / CANCELLED)
    // TODO JWT sonrası: @PreAuthorize("hasAnyRole('DOCTOR','RECEPTIONIST','ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusRequest request) {
        Appointment updated = appointmentService.updateAppointmentStatus(id, request.getStatus());
        return ResponseEntity.ok(appointmentMapper.toResponse(updated));
    }

    // RECEPTIONIST, PATIENT — randevu iptal et
    // TODO JWT sonrası: hasta sadece kendi randevusunu iptal edebilmeli
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
