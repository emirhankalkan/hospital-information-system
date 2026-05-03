package com.his.service;

import com.his.entity.Appointment;
import com.his.entity.Doctor;
import com.his.entity.Patient;
import com.his.entity.User;
import com.his.enums.AppointmentStatus;
import com.his.exception.ResourceAlreadyExistsException;
import com.his.exception.ResourceNotFoundException;
import com.his.repository.AppointmentRepository;
import com.his.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AppointmentServiceImpl Unit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService Unit Tests")
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientService patientService;

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Patient patient;
    private Doctor doctor;
    private Appointment futureAppointment;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setIsActive(true);

        patient = new Patient();
        patient.setId(10L);
        patient.setUser(user);

        doctor = new Doctor();
        doctor.setId(20L);

        // Yarın sabah 10:00 → her zaman geçerli olacak
        futureAppointment = new Appointment();
        futureAppointment.setPatient(patient);
        futureAppointment.setDoctor(doctor);
        futureAppointment.setAppointmentDate(LocalDate.now().plusDays(1));
        futureAppointment.setAppointmentTime(LocalTime.of(10, 0));
        futureAppointment.setStatus(AppointmentStatus.SCHEDULED);
    }

    // =========================================================================
    // bookAppointment
    // =========================================================================
    @Nested
    @DisplayName("bookAppointment")
    class BookAppointment {

        @Test
        @DisplayName("Başarılı: Geçerli randevu kaydedilir ve SCHEDULED statüsü atanır")
        void whenValidAppointment_thenSaveAndReturnScheduled() {
            when(patientService.findById(10L)).thenReturn(patient);
            when(doctorService.findById(20L)).thenReturn(doctor);
            when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                    anyLong(), any(), any())).thenReturn(false);
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(futureAppointment);

            Appointment result = appointmentService.bookAppointment(futureAppointment);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
            verify(appointmentRepository).save(any(Appointment.class));
        }

        @Test
        @DisplayName("Hata: Geçmiş tarih için randevu alınamaz → IllegalArgumentException")
        void whenPastDate_thenThrowIllegalArgument() {
            futureAppointment.setAppointmentDate(LocalDate.now().minusDays(1));

            assertThatThrownBy(() -> appointmentService.bookAppointment(futureAppointment))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Geçmiş bir tarih");
        }

        @Test
        @DisplayName("Hata: Doktor meşgulse → ResourceAlreadyExistsException")
        void whenDoctorHasConflict_thenThrowAlreadyExists() {
            when(patientService.findById(10L)).thenReturn(patient);
            when(doctorService.findById(20L)).thenReturn(doctor);
            when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                    anyLong(), any(), any())).thenReturn(true);

            assertThatThrownBy(() -> appointmentService.bookAppointment(futureAppointment))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("randevusu mevcut");
        }

        @Test
        @DisplayName("Hata: Hasta null ise → IllegalArgumentException")
        void whenPatientIsNull_thenThrowIllegalArgument() {
            futureAppointment.setPatient(null);

            assertThatThrownBy(() -> appointmentService.bookAppointment(futureAppointment))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("hasta");
        }

        @Test
        @DisplayName("Hata: Randevu tarihi null ise → IllegalArgumentException")
        void whenDateIsNull_thenThrowIllegalArgument() {
            futureAppointment.setAppointmentDate(null);

            assertThatThrownBy(() -> appointmentService.bookAppointment(futureAppointment))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }
    }

    // =========================================================================
    // updateAppointmentStatus
    // =========================================================================
    @Nested
    @DisplayName("updateAppointmentStatus")
    class UpdateAppointmentStatus {

        @Test
        @DisplayName("Başarılı: SCHEDULED → COMPLETED geçişi yapılabilir")
        void whenScheduledThenCompleted_thenSuccess() {
            futureAppointment.setId(1L);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(futureAppointment));
            when(appointmentRepository.save(any())).thenReturn(futureAppointment);

            Appointment result = appointmentService.updateAppointmentStatus(1L, AppointmentStatus.COMPLETED);

            assertThat(result.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        }

        @Test
        @DisplayName("Hata: Zaten CANCELED randevunun durumu değiştirilemez → IllegalStateException")
        void whenAlreadyCanceled_thenThrowIllegalState() {
            futureAppointment.setId(1L);
            futureAppointment.setStatus(AppointmentStatus.CANCELED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(futureAppointment));

            assertThatThrownBy(() ->
                    appointmentService.updateAppointmentStatus(1L, AppointmentStatus.SCHEDULED))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("iptal edilmiş");
        }

        @Test
        @DisplayName("Hata: Zaten COMPLETED randevunun durumu değiştirilemez → IllegalStateException")
        void whenAlreadyCompleted_thenThrowIllegalState() {
            futureAppointment.setId(1L);
            futureAppointment.setStatus(AppointmentStatus.COMPLETED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(futureAppointment));

            assertThatThrownBy(() ->
                    appointmentService.updateAppointmentStatus(1L, AppointmentStatus.SCHEDULED))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("tamamlanmış");
        }

        @Test
        @DisplayName("Hata: Aynı statü geçişi yapılamaz → IllegalStateException")
        void whenSameStatus_thenThrowIllegalState() {
            futureAppointment.setId(1L);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(futureAppointment));

            assertThatThrownBy(() ->
                    appointmentService.updateAppointmentStatus(1L, AppointmentStatus.SCHEDULED))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Hata: Randevu bulunamazsa → ResourceNotFoundException")
        void whenNotFound_thenThrowNotFoundException() {
            when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    appointmentService.updateAppointmentStatus(999L, AppointmentStatus.CANCELED))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    // cancelAppointment
    // =========================================================================
    @Nested
    @DisplayName("cancelAppointment")
    class CancelAppointment {

        @Test
        @DisplayName("Başarılı: SCHEDULED randevu iptal edilebilir")
        void whenScheduled_thenCancelSucceeds() {
            futureAppointment.setId(1L);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(futureAppointment));
            when(appointmentRepository.save(any())).thenReturn(futureAppointment);

            appointmentService.cancelAppointment(1L);

            assertThat(futureAppointment.getStatus()).isEqualTo(AppointmentStatus.CANCELED);
        }
    }
}
