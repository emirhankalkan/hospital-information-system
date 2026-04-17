package com.his.service.impl;

import com.his.entity.Department;
import com.his.exception.ResourceAlreadyExistsException;
import com.his.exception.ResourceNotFoundException;
import com.his.repository.DepartmentRepository;
import com.his.repository.DoctorRepository;
import com.his.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository; // Bağlı doktor kontrolü için

    @Override
    public Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departman (Poliklinik) bulunamadı, id: " + id));
    }

    @Override
    public Department findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Departman adı boş olamaz");
        }
        String normalizedName = name.trim();
        return departmentRepository.findByNameIgnoreCase(normalizedName)
                .orElseThrow(() -> new ResourceNotFoundException("Belirtilen isimde departman bulunamadı: " + normalizedName));
    }

    @Override
    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    @Override
    public List<Department> searchDepartments(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return departmentRepository.findByNameContainingIgnoreCase(keyword.trim());
    }

    @Override
    @Transactional
    public Department createDepartment(Department department) {
        if (department.getName() == null || department.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Oluşturulacak departmanın adı zorunludur");
        }
        
        String normalizedName = department.getName().trim();
        department.setName(normalizedName);

        if (departmentRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ResourceAlreadyExistsException("Bu isme sahip bir departman zaten mevcut: " + normalizedName);
        }

        return departmentRepository.save(department);
    }

    @Override
    @Transactional
    public Department updateDepartment(Long id, Department departmentDetails) {
        Department existingDepartment = findById(id);

        if (departmentDetails.getName() == null || departmentDetails.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Departman adı boş bırakılamaz");
        }
        
        String normalizedName = departmentDetails.getName().trim();

        // Eğer isim değiştiriliyorsa ve yeni isim başkası tarafından kullanılıyorsa çakışmayı önle
        if (!existingDepartment.getName().equalsIgnoreCase(normalizedName) &&
                departmentRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ResourceAlreadyExistsException("Bu isimde başka bir departman zaten mevcut: " + normalizedName);
        }

        existingDepartment.setName(normalizedName);
        existingDepartment.setDescription(departmentDetails.getDescription() != null ? departmentDetails.getDescription().trim() : null);

        return departmentRepository.save(existingDepartment);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = findById(id);
        
        // İş Kuralı: İçerisinde doktor barındıran bir departman silinemez.
        // Silinmesi için önce doktorların başka departmanlara aktarılması ya da onların silinmesi gerekir.
        if (doctorRepository.existsByDepartmentId(id)) {
            throw new IllegalStateException("Bu departmana bağlı doktorlar bulunduğu için departman silinemez. Lütfen önce doktorları başka bir departmana taşıyın.");
        }

        departmentRepository.delete(department);
    }
}
