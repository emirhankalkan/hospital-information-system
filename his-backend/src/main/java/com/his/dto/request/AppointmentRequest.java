package com.his.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {

    @NotNull(message = "Hasta ID bos olamaz")
    private Long patientId;

    @NotNull(message = "Doktor ID bos olamaz")
    private Long doctorId;

    @NotNull(message = "Randevu tarihi bos olamaz")
    @FutureOrPresent(message = "Randevu tarihi gecmis olamaz")
    private LocalDate appointmentDate;

    @NotNull(message = "Randevu saati bos olamaz")
    private LocalTime appointmentTime;

    private String notes;
}
