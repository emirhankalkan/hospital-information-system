package com.his.repository;

import com.his.entity.Patient;
import com.his.enums.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByTcNo(String tcNo);

    Optional<Patient> findByUserId(Long userId);

    boolean existsByTcNo(String tcNo);

    boolean existsByUserId(Long userId);

    List<Patient> findByIsDeleted(Boolean isDeleted);

    List<Patient> findByGender(Gender gender);

    List<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    List<Patient> findByBloodType(String bloodType);
    
    Optional<Patient> findByIdAndIsDeletedFalse(Long id);
    
    Optional<Patient> findByUserIdAndIsDeletedFalse(Long userId);
    
    Optional<Patient> findByTcNoAndIsDeletedFalse(String tcNo);
}
