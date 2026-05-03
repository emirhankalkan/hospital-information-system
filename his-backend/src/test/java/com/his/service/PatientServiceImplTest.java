package com.his.service;

import com.his.entity.Patient;
import com.his.entity.User;
import com.his.exception.ResourceAlreadyExistsException;
import com.his.exception.ResourceNotFoundException;
import com.his.repository.PatientRepository;
import com.his.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PatientServiceImpl Unit Tests
 * Mockito kullanılarak repository bağımlılıkları izole edildi.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PatientService Unit Tests")
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    // --- Test Fixture Nesneleri ---
    private User activeUser;
    private User inactiveUser;
    private Patient activePatient;
    private Patient inactiveUserPatient;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setUsername("johndoe");
        activeUser.setIsActive(true);

        inactiveUser = new User();
        inactiveUser.setId(2L);
        inactiveUser.setIsActive(false);

        activePatient = new Patient();
        activePatient.setId(10L);
        activePatient.setFirstName("John");
        activePatient.setLastName("Doe");
        activePatient.setTcNo("12345678901");
        activePatient.setIsDeleted(false);
        activePatient.setUser(activeUser);

        inactiveUserPatient = new Patient();
        inactiveUserPatient.setId(11L);
        inactiveUserPatient.setTcNo("99999999999");
        inactiveUserPatient.setIsDeleted(false);
        inactiveUserPatient.setUser(inactiveUser);
    }

    // =========================================================================
    // findById
    // =========================================================================
    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Başarılı: Aktif hasta ID ile bulunur")
        void whenValidId_thenReturnPatient() {
            when(patientRepository.findByIdAndIsDeletedFalse(10L))
                    .thenReturn(Optional.of(activePatient));

            Patient result = patientService.findById(10L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getFirstName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Hata: Silinmiş (deleted) hasta bulunamaz → ResourceNotFoundException")
        void whenDeletedPatient_thenThrowNotFoundException() {
            when(patientRepository.findByIdAndIsDeletedFalse(99L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> patientService.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("Hata: Pasif kullanıcıya bağlı hasta → ResourceNotFoundException")
        void whenInactiveUserPatient_thenThrowNotFoundException() {
            when(patientRepository.findByIdAndIsDeletedFalse(11L))
                    .thenReturn(Optional.of(inactiveUserPatient));

            assertThatThrownBy(() -> patientService.findById(11L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("pasif");
        }
    }

    // =========================================================================
    // findAllActive
    // =========================================================================
    @Nested
    @DisplayName("findAllActive")
    class FindAllActive {

        @Test
        @DisplayName("Başarılı: Sadece aktif kullanıcıya bağlı hastalar döner")
        void whenCalled_thenReturnOnlyActiveUserPatients() {
            when(patientRepository.findByIsDeleted(false))
                    .thenReturn(List.of(activePatient, inactiveUserPatient));

            List<Patient> result = patientService.findAllActive();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Başarılı: Hiç hasta yoksa boş liste döner")
        void whenNoPatients_thenReturnEmptyList() {
            when(patientRepository.findByIsDeleted(false)).thenReturn(List.of());

            List<Patient> result = patientService.findAllActive();

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // createPatient
    // =========================================================================
    @Nested
    @DisplayName("createPatient")
    class CreatePatient {

        @Test
        @DisplayName("Başarılı: Geçerli hasta kaydedilir ve ID atanır")
        void whenValidPatient_thenSaveAndReturn() {
            when(patientRepository.existsByUserId(1L)).thenReturn(false);
            when(patientRepository.existsByTcNo("12345678901")).thenReturn(false);
            when(patientRepository.save(activePatient)).thenReturn(activePatient);

            Patient result = patientService.createPatient(activePatient);

            assertThat(result).isNotNull();
            assertThat(result.getTcNo()).isEqualTo("12345678901");
            verify(patientRepository).save(activePatient);
        }

        @Test
        @DisplayName("Hata: Kullanıcı null ise → IllegalArgumentException")
        void whenUserIsNull_thenThrowIllegalArgument() {
            Patient patientWithNoUser = new Patient();
            patientWithNoUser.setUser(null);

            assertThatThrownBy(() -> patientService.createPatient(patientWithNoUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("kullanıcı");
        }

        @Test
        @DisplayName("Hata: Aynı user_id ile hasta zaten varsa → ResourceAlreadyExistsException")
        void whenUserIdAlreadyExists_thenThrowAlreadyExists() {
            when(patientRepository.existsByUserId(1L)).thenReturn(true);

            assertThatThrownBy(() -> patientService.createPatient(activePatient))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("1");
        }

        @Test
        @DisplayName("Hata: Aynı TC No ile hasta zaten varsa → ResourceAlreadyExistsException")
        void whenTcNoAlreadyExists_thenThrowAlreadyExists() {
            when(patientRepository.existsByUserId(1L)).thenReturn(false);
            when(patientRepository.existsByTcNo("12345678901")).thenReturn(true);

            assertThatThrownBy(() -> patientService.createPatient(activePatient))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("12345678901");
        }
    }

    // =========================================================================
    // deletePatient (Soft Delete)
    // =========================================================================
    @Nested
    @DisplayName("deletePatient (Soft Delete)")
    class DeletePatient {

        @Test
        @DisplayName("Başarılı: Hasta soft-delete ile işaretlenir")
        void whenValidId_thenMarkAsDeleted() {
            when(patientRepository.findById(10L)).thenReturn(Optional.of(activePatient));
            when(patientRepository.save(any(Patient.class))).thenReturn(activePatient);

            patientService.deletePatient(10L);

            assertThat(activePatient.getIsDeleted()).isTrue();
            verify(patientRepository).save(activePatient);
        }

        @Test
        @DisplayName("Hata: Zaten silinmiş hasta tekrar silinemez → IllegalStateException")
        void whenAlreadyDeleted_thenThrowIllegalState() {
            activePatient.setIsDeleted(true);
            when(patientRepository.findById(10L)).thenReturn(Optional.of(activePatient));

            assertThatThrownBy(() -> patientService.deletePatient(10L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("zaten silinmiş");
        }

        @Test
        @DisplayName("Hata: Hasta bulunamazsa → ResourceNotFoundException")
        void whenNotFound_thenThrowNotFoundException() {
            when(patientRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> patientService.deletePatient(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
