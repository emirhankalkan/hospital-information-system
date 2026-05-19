package com.his.service;

import com.his.entity.Patient;
import com.his.entity.User;
import java.util.List;

public interface PatientService {

    Patient findById(Long id);

    Patient findByUserId(Long userId);

    Patient findOrCreateByUser(User user);

    Patient findByTcNo(String tcNo);

    List<Patient> findAllActive();

    List<Patient> searchPatients(String keyword);

    Patient createPatient(Patient patient);

    Patient updatePatient(Long id, Patient patientDetails);

    void deletePatient(Long id);
}
