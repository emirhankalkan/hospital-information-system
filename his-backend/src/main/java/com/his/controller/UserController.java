package com.his.controller;

import com.his.dto.ApiResponse;
import com.his.dto.response.UserResponse;
import com.his.entity.User;
import com.his.mapper.UserMapper;
import com.his.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    // ADMIN only
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.findAllActiveUsers()
                .stream()
                .map(userMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Kullanıcılar listelendi", users));
    }

    // ADMIN only
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Kullanıcı bulundu", userMapper.toResponse(user)));
    }

    // ADMIN only — soft delete
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Hesap başarıyla pasif duruma alındı."));
    }
}
