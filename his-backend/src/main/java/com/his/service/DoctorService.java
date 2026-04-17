package com.his.service;

import com.his.entity.Doctor;
import java.util.List;

public interface DoctorService {

    Doctor findById(Long id);

    Doctor findByUserId(Long userId);

    List<Doctor> findAll();

    List<Doctor> findByDepartmentId(Long departmentId);

    List<Doctor> searchDoctors(String keyword);

    Doctor createDoctor(Doctor doctor);

    Doctor updateDoctor(Long id, Doctor doctorDetails);

    void deleteDoctor(Long id);
}
