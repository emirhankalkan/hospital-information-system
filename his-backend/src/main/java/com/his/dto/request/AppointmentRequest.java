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

    @NotNull(message = "Hasta ID boş olamaz")
    private Long patientId;

    @NotNull(message = "Doktor ID boş olamaz")
    private Long doctorId;

    @NotNull(message = "Randevu tarihi boş olamaz")
    @FutureOrPresent(message = "Randevu tarihi geçmiş olamaz")
    private LocalDate appointmentDate;

    @NotNull(message = "Randevu saati boş olamaz")
    private LocalTime appointmentTime;

    private String notes;
}
