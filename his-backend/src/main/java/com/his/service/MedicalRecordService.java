package com.his.service;

import com.his.entity.MedicalRecord;
import java.util.List;

public interface MedicalRecordService {

    MedicalRecord findById(Long id);

    MedicalRecord findByAppointmentId(Long appointmentId);

    List<MedicalRecord> findByPatientId(Long patientId);

    List<MedicalRecord> findByDoctorId(Long doctorId);

    MedicalRecord createMedicalRecord(MedicalRecord medicalRecord);

    MedicalRecord updateMedicalRecord(Long id, MedicalRecord recordDetails);
}
