package com.his.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {

    private Long id;
    private Long userId;
    private String username;
    private String email;

    // Departman özeti - circular reference'ı önlemek için tüm DepartmentResponse yerine sadece özet
    private Long departmentId;
    private String departmentName;

    private String firstName;
    private String lastName;
    private String specialization;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
