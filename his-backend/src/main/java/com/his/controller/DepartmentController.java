package com.his.controller;

import com.his.dto.ApiResponse;
import com.his.dto.request.DepartmentRequest;
import com.his.dto.response.DepartmentResponse;
import com.his.entity.Department;
import com.his.mapper.DepartmentMapper;
import com.his.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Departmanlar", description = "Hastane departmanlarını listeleme ve yönetme işlemleri")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DepartmentMapper departmentMapper;

    // PUBLIC — tüm roller görebilir
    @GetMapping
    @Operation(summary = "Departmanları listele", description = "Sistemde kayıtlı tüm departmanları listeler.")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
        List<DepartmentResponse> departments = departmentService.findAll()
                .stream()
                .map(departmentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Departmanlar listelendi", departments));
    }

    // PUBLIC
    @GetMapping("/{id}")
    @Operation(summary = "Departman detayı getir", description = "Verilen departman ID değerine göre departman bilgisini getirir.")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(@PathVariable Long id) {
        Department department = departmentService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Departman bulundu", departmentMapper.toResponse(department)));
    }

    // PUBLIC
    @GetMapping("/search")
    @Operation(summary = "Departman ara", description = "Departman adında anahtar kelimeye göre arama yapar.")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> searchDepartments(@RequestParam String keyword) {
        List<DepartmentResponse> results = departmentService.searchDepartments(keyword)
                .stream()
                .map(departmentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Arama sonuçları getirildi", results));
    }

    // ADMIN only
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Departman oluştur", description = "Yeni departman oluşturur. Sadece ADMIN rolü kullanabilir.")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        Department department = departmentMapper.toEntity(request);
        Department saved = departmentService.createDepartment(department);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Departman oluşturuldu", departmentMapper.toResponse(saved)));
    }

    // ADMIN only
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Departman güncelle", description = "Mevcut departman bilgisini günceller. Sadece ADMIN rolü kullanabilir.")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        Department existing = departmentService.findById(id);
        departmentMapper.updateEntityFromRequest(request, existing);
        Department updated = departmentService.updateDepartment(id, existing);
        return ResponseEntity.ok(ApiResponse.success("Departman güncellendi", departmentMapper.toResponse(updated)));
    }

    // ADMIN only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Departman sil", description = "Departmanı siler. Bağlı doktor varsa silme işlemi reddedilir.")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Departman silindi"));
    }
}
