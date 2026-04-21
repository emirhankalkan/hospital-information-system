package com.his.dto.request;

import com.his.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatusRequest {

    @NotNull(message = "Randevu durumu boş olamaz")
    private AppointmentStatus status;
}
