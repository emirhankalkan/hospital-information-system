package com.his.mapper;

import com.his.dto.request.DoctorRequest;
import com.his.dto.response.DoctorResponse;
import com.his.entity.Department;
import com.his.entity.Doctor;
import com.his.entity.User;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {

    /**
     * DoctorRequest DTO -> Doctor entity
     * User ve Department nesnelerinin önceden yüklenmiş (fetched) olması gerekir.
     * Bu nesneler Controller/Service katmanında sağlanır.
     */
    public Doctor toEntity(DoctorRequest request, User user, Department department) {
        if (request == null) return null;

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setDepartment(department);
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setPhone(request.getPhone());
        return doctor;
    }

    /**
     * Doctor entity -> DoctorResponse DTO
     * Dairesel referansı (circular reference) önlemek için User ve Department
     * bilgileri düz alanlar olarak (flat) aktarılır.
     */
    public DoctorResponse toResponse(Doctor doctor) {
        if (doctor == null) return null;

        DoctorResponse response = new DoctorResponse();
        response.setId(doctor.getId());
        response.setFirstName(doctor.getFirstName());
        response.setLastName(doctor.getLastName());
        response.setSpecialization(doctor.getSpecialization());
        response.setPhone(doctor.getPhone());
        response.setCreatedAt(doctor.getCreatedAt());
        response.setUpdatedAt(doctor.getUpdatedAt());

        // Bağlı kullanıcı bilgileri
        if (doctor.getUser() != null) {
            response.setUserId(doctor.getUser().getId());
            response.setUsername(doctor.getUser().getUsername());
            response.setEmail(doctor.getUser().getEmail());
        }

        // Bağlı departman bilgileri
        if (doctor.getDepartment() != null) {
            response.setDepartmentId(doctor.getDepartment().getId());
            response.setDepartmentName(doctor.getDepartment().getName());
        }

        return response;
    }

    /**
     * Mevcut Doctor entity'sini request verileriyle günceller (UPDATE işlemi).
     * User değiştirilemez. Department isteğe bağlı olarak güncellenir.
     */
    public void updateEntityFromRequest(DoctorRequest request, Doctor doctor, Department department) {
        if (request == null || doctor == null) return;

        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setPhone(request.getPhone());

        if (department != null) {
            doctor.setDepartment(department);
        }
    }
}
