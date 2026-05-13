package com.his.service;

import com.his.entity.Department;
import com.his.entity.Doctor;
import com.his.entity.User;
import com.his.exception.ResourceAlreadyExistsException;
import com.his.exception.ResourceNotFoundException;
import com.his.repository.DoctorRepository;
import com.his.service.impl.DoctorServiceImpl;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DoctorService Unit Tests")
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private Doctor activeDoctor;
    private Doctor inactiveDoctor;

    @BeforeEach
    void setUp() {
        User activeUser = new User();
        activeUser.setId(1L);
        activeUser.setIsActive(true);

        User inactiveUser = new User();
        inactiveUser.setId(2L);
        inactiveUser.setIsActive(false);

        Department department = new Department();
        department.setId(10L);

        activeDoctor = new Doctor();
        activeDoctor.setId(20L);
        activeDoctor.setUser(activeUser);
        activeDoctor.setDepartment(department);
        activeDoctor.setFirstName("Aylin");
        activeDoctor.setLastName("Kaya");

        inactiveDoctor = new Doctor();
        inactiveDoctor.setId(21L);
        inactiveDoctor.setUser(inactiveUser);
        inactiveDoctor.setDepartment(department);
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Basarili: Aktif user'a bagli doktor bulunur")
        void whenActiveDoctorExists_thenReturnDoctor() {
            when(doctorRepository.findById(20L)).thenReturn(Optional.of(activeDoctor));

            Doctor result = doctorService.findById(20L);

            assertThat(result).isSameAs(activeDoctor);
        }

        @Test
        @DisplayName("Hata: Pasif user'a bagli doktor disariya gosterilmez")
        void whenDoctorUserInactive_thenThrowNotFound() {
            when(doctorRepository.findById(21L)).thenReturn(Optional.of(inactiveDoctor));

            assertThatThrownBy(() -> doctorService.findById(21L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("pasif");
        }
    }

    @Test
    @DisplayName("findAll: Sadece aktif user'a bagli doktorlari dondurur")
    void findAllReturnsOnlyActiveDoctors() {
        when(doctorRepository.findAll()).thenReturn(List.of(activeDoctor, inactiveDoctor));

        List<Doctor> result = doctorService.findAll();

        assertThat(result).containsExactly(activeDoctor);
    }

    @Nested
    @DisplayName("createDoctor")
    class CreateDoctor {

        @Test
        @DisplayName("Basarili: Gecerli doktor kaydedilir")
        void whenValidDoctor_thenSave() {
            when(doctorRepository.existsByUserId(1L)).thenReturn(false);
            when(doctorRepository.save(activeDoctor)).thenReturn(activeDoctor);

            Doctor result = doctorService.createDoctor(activeDoctor);

            assertThat(result).isSameAs(activeDoctor);
            verify(doctorRepository).save(activeDoctor);
        }

        @Test
        @DisplayName("Hata: Kullanici hesabi yoksa doktor olusturulamaz")
        void whenUserMissing_thenThrowIllegalArgument() {
            activeDoctor.setUser(null);

            assertThatThrownBy(() -> doctorService.createDoctor(activeDoctor))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("kullan");
        }

        @Test
        @DisplayName("Hata: Ayni user icin ikinci doktor profili olusturulamaz")
        void whenUserAlreadyHasDoctor_thenThrowAlreadyExists() {
            when(doctorRepository.existsByUserId(1L)).thenReturn(true);

            assertThatThrownBy(() -> doctorService.createDoctor(activeDoctor))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("1");

            verify(doctorRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("deleteDoctor: Var olan doktor hard delete edilir")
    void deleteDoctorRemovesExistingDoctor() {
        when(doctorRepository.findById(20L)).thenReturn(Optional.of(activeDoctor));

        doctorService.deleteDoctor(20L);

        verify(doctorRepository).delete(activeDoctor);
    }
}
