package com.his.service;

import com.his.entity.Appointment;
import com.his.entity.MedicalRecord;
import com.his.enums.AppointmentStatus;
import com.his.exception.ResourceAlreadyExistsException;
import com.his.exception.ResourceNotFoundException;
import com.his.repository.MedicalRecordRepository;
import com.his.service.impl.MedicalRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicalRecordService Unit Tests")
class MedicalRecordServiceImplTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private MedicalRecordServiceImpl medicalRecordService;

    private Appointment scheduledAppointment;
    private MedicalRecord medicalRecord;

    @BeforeEach
    void setUp() {
        scheduledAppointment = new Appointment();
        scheduledAppointment.setId(30L);
        scheduledAppointment.setStatus(AppointmentStatus.SCHEDULED);

        medicalRecord = new MedicalRecord();
        medicalRecord.setId(40L);
        medicalRecord.setAppointment(scheduledAppointment);
        medicalRecord.setDiagnosis("Influenza");
        medicalRecord.setTreatmentNotes("Rest");
    }

    @Nested
    @DisplayName("createMedicalRecord")
    class CreateMedicalRecord {

        @Test
        @DisplayName("Basarili: Gecerli kayit olusturulur ve scheduled randevu completed olur")
        void whenValidRecord_thenSaveAndCompleteAppointment() {
            when(appointmentService.findById(30L)).thenReturn(scheduledAppointment);
            when(medicalRecordRepository.existsByAppointmentId(30L)).thenReturn(false);
            when(medicalRecordRepository.save(medicalRecord)).thenReturn(medicalRecord);

            MedicalRecord result = medicalRecordService.createMedicalRecord(medicalRecord);

            assertThat(result).isSameAs(medicalRecord);
            verify(appointmentService).updateAppointmentStatus(30L, AppointmentStatus.COMPLETED);
        }

        @Test
        @DisplayName("Hata: Bos tibbi kayit olusturulamaz")
        void whenRecordContentEmpty_thenThrowIllegalArgument() {
            medicalRecord.setDiagnosis(" ");
            medicalRecord.setTreatmentNotes(null);
            medicalRecord.setPrescriptionNotes("");

            assertThatThrownBy(() -> medicalRecordService.createMedicalRecord(medicalRecord))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("en az biri");

            verifyNoInteractions(appointmentService, medicalRecordRepository);
        }

        @Test
        @DisplayName("Hata: Iptal edilmis randevuya tibbi kayit acilamaz")
        void whenAppointmentCanceled_thenThrowIllegalState() {
            scheduledAppointment.setStatus(AppointmentStatus.CANCELED);
            when(appointmentService.findById(30L)).thenReturn(scheduledAppointment);

            assertThatThrownBy(() -> medicalRecordService.createMedicalRecord(medicalRecord))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("randevuya");

            verify(medicalRecordRepository, never()).save(any());
        }

        @Test
        @DisplayName("Hata: Ayni randevu icin ikinci tibbi kayit olusturulamaz")
        void whenRecordAlreadyExists_thenThrowAlreadyExists() {
            when(appointmentService.findById(30L)).thenReturn(scheduledAppointment);
            when(medicalRecordRepository.existsByAppointmentId(30L)).thenReturn(true);

            assertThatThrownBy(() -> medicalRecordService.createMedicalRecord(medicalRecord))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("halihaz");
        }
    }

    @Nested
    @DisplayName("updateMedicalRecord")
    class UpdateMedicalRecord {

        @Test
        @DisplayName("Basarili: Null alanlar mevcut veriyi ezmez")
        void whenFieldNull_thenKeepExistingValue() {
            MedicalRecord patch = new MedicalRecord();
            patch.setDiagnosis(null);
            patch.setTreatmentNotes("Hydration");
            when(medicalRecordRepository.findById(40L)).thenReturn(Optional.of(medicalRecord));
            when(medicalRecordRepository.save(medicalRecord)).thenReturn(medicalRecord);

            MedicalRecord result = medicalRecordService.updateMedicalRecord(40L, patch);

            assertThat(result.getDiagnosis()).isEqualTo("Influenza");
            assertThat(result.getTreatmentNotes()).isEqualTo("Hydration");
        }

        @Test
        @DisplayName("Hata: Bulunamayan tibbi kayit guncellenemez")
        void whenRecordNotFound_thenThrowNotFound() {
            when(medicalRecordRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medicalRecordService.updateMedicalRecord(99L, new MedicalRecord()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
