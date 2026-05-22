package com.his.dto.request;

import com.his.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    // Required only while creating a profile; ignored during update.
    private Long userId;

    @NotBlank(message = "Ad bos olamaz")
    @Size(max = 50, message = "Ad en fazla 50 karakter olabilir")
    private String firstName;

    @NotBlank(message = "Soyad bos olamaz")
    @Size(max = 50, message = "Soyad en fazla 50 karakter olabilir")
    private String lastName;

    // Not updated by PatientMapper. It may be null for demo/self-registered patients.
    @Size(min = 11, max = 11, message = "TC Kimlik No 11 karakter olmalidir")
    private String tcNo;

    private LocalDate birthDate;

    private Gender gender;

    @Size(max = 15)
    private String phone;

    @Email(message = "Gecerli bir e-posta adresi giriniz")
    @Size(max = 100)
    private String email;

    private String address;

    @Size(max = 100)
    private String emergencyContact;

    @Size(max = 5)
    private String bloodType;
}
