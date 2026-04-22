package com.his.controller;

import com.his.dto.response.MessageResponse;
import com.his.dto.response.UserResponse;
import com.his.entity.User;
import com.his.mapper.UserMapper;
import com.his.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    // ADMIN only
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.findAllActiveUsers()
                .stream()
                .map(userMapper::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    // ADMIN only
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    // ADMIN only — soft delete
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new MessageResponse("Hesap başarıyla pasif duruma alındı."));
    }
}
