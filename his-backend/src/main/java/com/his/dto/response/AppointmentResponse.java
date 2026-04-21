package com.his.dto.response;

import com.his.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private Long id;

    // Hasta özeti
    private Long patientId;
    private String patientFirstName;
    private String patientLastName;
    private String patientTcNo;

    // Doktor özeti
    private Long doctorId;
    private String doctorFirstName;
    private String doctorLastName;
    private String doctorSpecialization;
    private String departmentName;

    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private AppointmentStatus status;
    private String notes;

    // Oluşturan kullanıcı
    private Long createdByUserId;
    private String createdByUsername;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
