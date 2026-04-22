package com.his.controller;

import com.his.dto.request.LoginRequest;
import com.his.dto.request.RegisterRequest;
import com.his.dto.response.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — Kimlik doğrulama (login/register) endpoint'leri.
 *
 * NOT: Bu controller şu an iskelet (placeholder) halindedir.
 * JWT + Spring Security kurulduktan sonra tam implementasyon buraya gelecek:
 *   - login  → JWT token üret, JwtResponse döndür
 *   - register → User oluştur, rol ata, başarı mesajı döndür
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // TODO: JWT Security kurulunca AuthService inject edilecek

    /**
     * POST /api/auth/login
     * Kullanıcı girişi — username/password alır, JWT token döner.
     */
    @PostMapping("/login")
    public ResponseEntity<MessageResponse> login(@Valid @RequestBody LoginRequest request) {
        // TODO: AuthService.login(request) → JwtResponse
        return ResponseEntity.ok(new MessageResponse("Login endpoint — JWT Security kurulunca aktive edilecek."));
    }

    /**
     * POST /api/auth/register
     * Yeni kullanıcı kaydı — ADMIN yetkisi gerektirir.
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        // TODO: AuthService.register(request) → UserResponse
        return ResponseEntity.ok(new MessageResponse("Register endpoint — JWT Security kurulunca aktive edilecek."));
    }
}
