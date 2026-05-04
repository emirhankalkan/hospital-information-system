package com.his.security;

import com.his.entity.Appointment;
import com.his.entity.MedicalRecord;
import com.his.entity.User;
import com.his.repository.AppointmentRepository;
import com.his.repository.DoctorRepository;
import com.his.repository.MedicalRecordRepository;
import com.his.repository.PatientRepository;
import com.his.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("authorizationService")
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public boolean isCurrentPatient(Long patientId) {
        User user = currentUser();
        return user != null && patientRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .map(patient -> patient.getId().equals(patientId))
                .orElse(false);
    }

    public boolean isCurrentDoctor(Long doctorId) {
        User user = currentUser();
        return user != null && doctorRepository.findByUserId(user.getId())
                .map(doctor -> doctor.getId().equals(doctorId))
                .orElse(false);
    }

    public boolean canAccessAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .map(this::canAccessAppointment)
                .orElse(false);
    }

    public boolean canManageAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .map(appointment -> isAppointmentDoctor(appointment) || isAppointmentCreator(appointment))
                .orElse(false);
    }

    public boolean canCancelAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .map(appointment -> isAppointmentPatient(appointment) || isAppointmentCreator(appointment))
                .orElse(false);
    }

    public boolean canAccessMedicalRecord(Long recordId) {
        return medicalRecordRepository.findById(recordId)
                .map(this::canAccessMedicalRecord)
                .orElse(false);
    }

    public boolean canAccessMedicalRecordByAppointment(Long appointmentId) {
        return medicalRecordRepository.findByAppointmentId(appointmentId)
                .map(this::canAccessMedicalRecord)
                .orElse(false);
    }

    public boolean canCreateMedicalRecordForAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .map(this::isAppointmentDoctor)
                .orElse(false);
    }

    public boolean canUpdateMedicalRecord(Long recordId) {
        return medicalRecordRepository.findById(recordId)
                .map(record -> isAppointmentDoctor(record.getAppointment()))
                .orElse(false);
    }

    private boolean canAccessAppointment(Appointment appointment) {
        return isAppointmentPatient(appointment) || isAppointmentDoctor(appointment) || isAppointmentCreator(appointment);
    }

    private boolean canAccessMedicalRecord(MedicalRecord record) {
        Appointment appointment = record.getAppointment();
        return isAppointmentPatient(appointment) || isAppointmentDoctor(appointment);
    }

    private boolean isAppointmentPatient(Appointment appointment) {
        return appointment != null
                && appointment.getPatient() != null
                && appointment.getPatient().getId() != null
                && isCurrentPatient(appointment.getPatient().getId());
    }

    private boolean isAppointmentDoctor(Appointment appointment) {
        return appointment != null
                && appointment.getDoctor() != null
                && appointment.getDoctor().getId() != null
                && isCurrentDoctor(appointment.getDoctor().getId());
    }

    private boolean isAppointmentCreator(Appointment appointment) {
        User user = currentUser();
        return user != null
                && appointment != null
                && appointment.getCreatedByUser() != null
                && user.getId().equals(appointment.getCreatedByUser().getId());
    }

    private User currentUser() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        if (username == null || "anonymousUser".equals(username)) {
            return null;
        }

        return userRepository.findByUsername(username).orElse(null);
    }
}
