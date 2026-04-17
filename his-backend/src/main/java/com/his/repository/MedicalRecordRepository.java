package com.his.repository;

import com.his.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);

    List<MedicalRecord> findByAppointment_Patient_IdOrderByCreatedAtDesc(Long patientId);

    List<MedicalRecord> findByAppointment_Doctor_IdOrderByCreatedAtDesc(Long doctorId);
}
