package com.his.mapper;

import com.his.dto.request.DepartmentRequest;
import com.his.dto.response.DepartmentResponse;
import com.his.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    /**
     * DepartmentRequest DTO -> Department entity
     * Yeni kayıt oluştururken kullanılır (id yok).
     */
    public Department toEntity(DepartmentRequest request) {
        if (request == null) return null;

        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        return department;
    }

    /**
     * Department entity -> DepartmentResponse DTO
     */
    public DepartmentResponse toResponse(Department department) {
        if (department == null) return null;

        DepartmentResponse response = new DepartmentResponse();
        response.setId(department.getId());
        response.setName(department.getName());
        response.setDescription(department.getDescription());
        response.setCreatedAt(department.getCreatedAt());
        response.setUpdatedAt(department.getUpdatedAt());
        return response;
    }

    /**
     * Mevcut entity'yi request verileriyle günceller (UPDATE işlemi).
     * Entity'nin id ve audit alanları korunur.
     */
    public void updateEntityFromRequest(DepartmentRequest request, Department department) {
        if (request == null || department == null) return;

        department.setName(request.getName());
        department.setDescription(request.getDescription());
    }
}
