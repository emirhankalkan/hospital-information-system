package com.his.service;

import com.his.entity.Appointment;
import com.his.enums.AppointmentStatus;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    Appointment findById(Long id);

    List<Appointment> findAll();

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByDoctorIdAndDate(Long doctorId, LocalDate date);

    Appointment bookAppointment(Appointment appointment);

    Appointment updateAppointment(Long id, Appointment appointmentDetails);

    Appointment updateAppointmentStatus(Long id, AppointmentStatus status);

    void cancelAppointment(Long id);
}
