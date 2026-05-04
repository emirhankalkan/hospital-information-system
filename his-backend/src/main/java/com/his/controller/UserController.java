package com.his.controller;

import com.his.dto.ApiResponse;
import com.his.dto.response.UserResponse;
import com.his.entity.User;
import com.his.mapper.UserMapper;
import com.his.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Kullanıcılar", description = "Kullanıcı hesaplarını listeleme ve pasifleştirme işlemleri")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    // ADMIN only
    @GetMapping
    @Operation(summary = "Kullanıcıları listele", description = "Aktif kullanıcı hesaplarını listeler. Sadece ADMIN rolü kullanabilir.")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.findAllActiveUsers()
                .stream()
                .map(userMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Kullanıcılar listelendi", users));
    }

    // ADMIN only
    @GetMapping("/{id}")
    @Operation(summary = "Kullanıcı detayı getir", description = "Kullanıcı hesabını ID değerine göre getirir. Sadece ADMIN rolü kullanabilir.")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Kullanıcı bulundu", userMapper.toResponse(user)));
    }

    // ADMIN only
    @DeleteMapping("/{id}")
    @Operation(summary = "Kullanıcı hesabını pasifleştir", description = "Kullanıcı hesabını soft delete mantığıyla pasif duruma alır.")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Hesap başarıyla pasif duruma alındı."));
    }
}
