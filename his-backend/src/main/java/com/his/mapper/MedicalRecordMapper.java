package com.his.mapper;

import com.his.dto.request.MedicalRecordRequest;
import com.his.dto.response.MedicalRecordResponse;
import com.his.entity.Appointment;
import com.his.entity.Doctor;
import com.his.entity.MedicalRecord;
import com.his.entity.Patient;
import org.springframework.stereotype.Component;

@Component
public class MedicalRecordMapper {

    /**
     * MedicalRecordRequest DTO -> MedicalRecord entity
     * Appointment nesnesinin önceden yüklenmiş (fetched) olması gerekir.
     * MedicalRecordServiceImpl.createMedicalRecord() appointment'ı validate edip set eder.
     */
    public MedicalRecord toEntity(MedicalRecordRequest request) {
        if (request == null) return null;

        MedicalRecord record = new MedicalRecord();

        // Appointment sadece id'si ile set edilir; Service katmanı gerçek nesneyi yükler
        Appointment appointmentRef = new Appointment();
        appointmentRef.setId(request.getAppointmentId());
        record.setAppointment(appointmentRef);

        record.setDiagnosis(request.getDiagnosis());
        record.setTreatmentNotes(request.getTreatmentNotes());
        record.setPrescriptionNotes(request.getPrescriptionNotes());
        return record;
    }

    /**
     * MedicalRecord entity -> MedicalRecordResponse DTO
     * Appointment zinciri üzerinden hasta ve doktor bilgileri düz alanlar olarak aktarılır.
     */
    public MedicalRecordResponse toResponse(MedicalRecord record) {
        if (record == null) return null;

        MedicalRecordResponse response = new MedicalRecordResponse();
        response.setId(record.getId());
        response.setDiagnosis(record.getDiagnosis());
        response.setTreatmentNotes(record.getTreatmentNotes());
        response.setPrescriptionNotes(record.getPrescriptionNotes());
        response.setCreatedAt(record.getCreatedAt());
        response.setUpdatedAt(record.getUpdatedAt());

        // Randevu bilgileri — MedicalRecord her zaman bir Appointment'a bağlıdır
        if (record.getAppointment() != null) {
            Appointment appointment = record.getAppointment();
            response.setAppointmentId(appointment.getId());
            response.setAppointmentDate(appointment.getAppointmentDate());
            response.setAppointmentTime(appointment.getAppointmentTime());

            // Hasta bilgileri (Appointment -> Patient zinciri)
            Patient patient = appointment.getPatient();
            if (patient != null) {
                response.setPatientId(patient.getId());
                response.setPatientFirstName(patient.getFirstName());
                response.setPatientLastName(patient.getLastName());
            }

            // Doktor bilgileri (Appointment -> Doctor zinciri)
            Doctor doctor = appointment.getDoctor();
            if (doctor != null) {
                response.setDoctorId(doctor.getId());
                response.setDoctorFirstName(doctor.getFirstName());
                response.setDoctorLastName(doctor.getLastName());
            }
        }

        return response;
    }
}
