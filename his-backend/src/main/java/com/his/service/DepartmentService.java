package com.his.service;

import com.his.entity.Department;

import java.util.List;

public interface DepartmentService {

    Department findById(Long id);

    Department findByName(String name);

    List<Department> findAll();

    List<Department> searchDepartments(String keyword);

    Department createDepartment(Department department);

    Department updateDepartment(Long id, Department departmentDetails);

    void deleteDepartment(Long id);
}
