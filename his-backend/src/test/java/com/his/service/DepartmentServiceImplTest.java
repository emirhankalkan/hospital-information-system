package com.his.service;

import com.his.entity.Department;
import com.his.exception.ResourceAlreadyExistsException;
import com.his.exception.ResourceNotFoundException;
import com.his.repository.DepartmentRepository;
import com.his.repository.DoctorRepository;
import com.his.service.impl.DepartmentServiceImpl;
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
@DisplayName("DepartmentService Unit Tests")
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department cardiology;

    @BeforeEach
    void setUp() {
        cardiology = new Department();
        cardiology.setId(1L);
        cardiology.setName("Cardiology");
        cardiology.setDescription("Heart clinic");
    }

    @Nested
    @DisplayName("createDepartment")
    class CreateDepartment {

        @Test
        @DisplayName("Basarili: Isim trimlenir ve departman kaydedilir")
        void whenValidDepartment_thenTrimAndSave() {
            cardiology.setName("  Cardiology  ");
            when(departmentRepository.existsByNameIgnoreCase("Cardiology")).thenReturn(false);
            when(departmentRepository.save(cardiology)).thenReturn(cardiology);

            Department result = departmentService.createDepartment(cardiology);

            assertThat(result.getName()).isEqualTo("Cardiology");
            verify(departmentRepository).save(cardiology);
        }

        @Test
        @DisplayName("Hata: Ayni isimli departman tekrar olusturulamaz")
        void whenNameExists_thenThrowAlreadyExists() {
            when(departmentRepository.existsByNameIgnoreCase("Cardiology")).thenReturn(true);

            assertThatThrownBy(() -> departmentService.createDepartment(cardiology))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("Cardiology");

            verify(departmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteDepartment")
    class DeleteDepartment {

        @Test
        @DisplayName("Basarili: Doktoru olmayan departman silinir")
        void whenNoDoctors_thenDeleteDepartment() {
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(cardiology));
            when(doctorRepository.existsByDepartmentId(1L)).thenReturn(false);

            departmentService.deleteDepartment(1L);

            verify(departmentRepository).delete(cardiology);
        }

        @Test
        @DisplayName("Hata: Bagli doktor varsa departman silinemez")
        void whenDoctorsExist_thenThrowIllegalState() {
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(cardiology));
            when(doctorRepository.existsByDepartmentId(1L)).thenReturn(true);

            assertThatThrownBy(() -> departmentService.deleteDepartment(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("doktor");

            verify(departmentRepository, never()).delete(any());
        }
    }

    @Test
    @DisplayName("findByName: Bos isim reddedilir")
    void whenBlankName_thenThrowIllegalArgument() {
        assertThatThrownBy(() -> departmentService.findByName(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("searchDepartments: Bos arama tum departmanlari dondurur")
    void whenBlankSearchKeyword_thenReturnAllDepartments() {
        when(departmentRepository.findAll()).thenReturn(List.of(cardiology));

        List<Department> result = departmentService.searchDepartments(" ");

        assertThat(result).containsExactly(cardiology);
    }

    @Test
    @DisplayName("findById: Bulunamayan departman icin hata atar")
    void whenDepartmentNotFound_thenThrowNotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
