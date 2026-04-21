package com.his.dto.response;

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
public class MedicalRecordResponse {

    private Long id;

    // Randevu özeti
    private Long appointmentId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    // Hasta özeti
    private Long patientId;
    private String patientFirstName;
    private String patientLastName;

    // Doktor özeti
    private Long doctorId;
    private String doctorFirstName;
    private String doctorLastName;

    private String diagnosis;
    private String treatmentNotes;
    private String prescriptionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
