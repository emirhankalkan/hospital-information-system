package com.his.dto.request;

import com.his.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequest {

    @NotNull(message = "Kullanıcı ID boş olamaz")
    private Long userId;

    @NotBlank(message = "Ad boş olamaz")
    @Size(max = 50, message = "Ad en fazla 50 karakter olabilir")
    private String firstName;

    @NotBlank(message = "Soyad boş olamaz")
    @Size(max = 50, message = "Soyad en fazla 50 karakter olabilir")
    private String lastName;

    @NotBlank(message = "TC Kimlik No boş olamaz")
    @Size(min = 11, max = 11, message = "TC Kimlik No 11 karakter olmalıdır")
    private String tcNo;

    private LocalDate birthDate;

    private Gender gender;

    @Size(max = 15)
    private String phone;

    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @Size(max = 100)
    private String email;

    private String address;

    @Size(max = 100)
    private String emergencyContact;

    @Size(max = 5)
    private String bloodType;
}
