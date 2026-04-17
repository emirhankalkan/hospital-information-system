package com.his.service;

import com.his.entity.Patient;
import java.util.List;

public interface PatientService {

    Patient findById(Long id);

    Patient findByUserId(Long userId);

    Patient findByTcNo(String tcNo);

    List<Patient> findAllActive();

    List<Patient> searchPatients(String keyword);

    Patient createPatient(Patient patient);

    Patient updatePatient(Long id, Patient patientDetails);

    void deletePatient(Long id);
}
