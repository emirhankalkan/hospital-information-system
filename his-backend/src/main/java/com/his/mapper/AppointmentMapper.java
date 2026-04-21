package com.his.mapper;

import com.his.dto.request.AppointmentRequest;
import com.his.dto.response.AppointmentResponse;
import com.his.entity.Appointment;
import com.his.entity.Doctor;
import com.his.entity.Patient;
import com.his.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    /**
     * AppointmentRequest DTO -> Appointment entity
     * Patient, Doctor ve (opsiyonel) createdByUser nesnelerinin
     * önceden yüklenmiş (fetched) olması gerekir.
     */
    public Appointment toEntity(AppointmentRequest request, Patient patient, Doctor doctor, User createdByUser) {
        if (request == null) return null;

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setNotes(request.getNotes());
        appointment.setCreatedByUser(createdByUser); // null olabilir (self-booked ise)
        // Status, AppointmentServiceImpl.bookAppointment() içinde SCHEDULED olarak set edilir
        return appointment;
    }

    /**
     * Appointment entity -> AppointmentResponse DTO
     * Hasta, doktor ve oluşturan kullanıcı bilgileri düz alanlar (flat) olarak aktarılır.
     */
    public AppointmentResponse toResponse(Appointment appointment) {
        if (appointment == null) return null;

        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setAppointmentTime(appointment.getAppointmentTime());
        response.setStatus(appointment.getStatus());
        response.setNotes(appointment.getNotes());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());

        // Hasta bilgileri
        if (appointment.getPatient() != null) {
            Patient patient = appointment.getPatient();
            response.setPatientId(patient.getId());
            response.setPatientFirstName(patient.getFirstName());
            response.setPatientLastName(patient.getLastName());
            response.setPatientTcNo(patient.getTcNo());
        }

        // Doktor bilgileri
        if (appointment.getDoctor() != null) {
            Doctor doctor = appointment.getDoctor();
            response.setDoctorId(doctor.getId());
            response.setDoctorFirstName(doctor.getFirstName());
            response.setDoctorLastName(doctor.getLastName());
            response.setDoctorSpecialization(doctor.getSpecialization());

            if (doctor.getDepartment() != null) {
                response.setDepartmentName(doctor.getDepartment().getName());
            }
        }

        // Randevuyu oluşturan kullanıcı bilgileri (Receptionist veya Patient kullanıcısı)
        if (appointment.getCreatedByUser() != null) {
            response.setCreatedByUserId(appointment.getCreatedByUser().getId());
            response.setCreatedByUsername(appointment.getCreatedByUser().getUsername());
        }

        return response;
    }
}
