package com.his.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordRequest {

    @NotNull(message = "Randevu ID boş olamaz")
    private Long appointmentId;

    private String diagnosis;

    private String treatmentNotes;

    private String prescriptionNotes;
}
