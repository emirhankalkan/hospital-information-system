package com.his.dto.response;

import com.his.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {

    private Long id;
    private Long userId;
    private String username;

    private String firstName;
    private String lastName;
    private String tcNo;
    private LocalDate birthDate;
    private Gender gender;
    private String phone;
    private String email;
    private String address;
    private String emergencyContact;
    private String bloodType;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
