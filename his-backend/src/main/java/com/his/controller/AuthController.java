package com.his.controller;

import com.his.dto.ApiResponse;
import com.his.dto.request.ForgotPasswordRequest;
import com.his.dto.request.LoginRequest;
import com.his.dto.request.RefreshTokenRequest;
import com.his.dto.request.RegisterRequest;
import com.his.dto.request.ResendVerificationRequest;
import com.his.dto.request.ResetPasswordRequest;
import com.his.dto.response.JwtResponse;
import com.his.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Kimlik Doğrulama", description = "Kullanıcı kaydı, JWT token, refresh token ve hesap doğrulama işlemleri")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Giriş yap", description = "Kullanıcı adı ve şifre ile access token ve refresh token üretir.")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse jwtResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Giriş başarılı", jwtResponse));
    }

    @PostMapping("/register")
    @Operation(summary = "Kullanıcı kaydı oluştur", description = "Yeni kullanıcı oluşturur ve e-posta doğrulama tokenı üretir.")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Kullanıcı kaydı başarıyla oluşturuldu. Lütfen e-posta adresinizi doğrulayın.", null));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Access token yenile", description = "Geçerli refresh token ile yeni access token ve refresh token üretir.")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        JwtResponse jwtResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token yenilendi", jwtResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "Çıkış yap", description = "Refresh token değerini iptal eder.")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Çıkış başarılı", null));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "E-posta doğrula", description = "E-posta doğrulama tokenı ile kullanıcı hesabını doğrular.")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam @NotBlank String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("E-posta adresi doğrulandı", null));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Doğrulama e-postasını yeniden gönder", description = "Doğrulanmamış kullanıcı için yeni doğrulama tokenı üretir.")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request);
        return ResponseEntity.ok(ApiResponse.success("Eğer kayıtlı ve doğrulanmamış bir e-posta ise doğrulama bağlantısı gönderildi.", null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Şifre sıfırlama iste", description = "Kayıtlı e-posta için şifre sıfırlama tokenı üretir.")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Eğer e-posta kayıtlıysa şifre sıfırlama bağlantısı gönderildi.", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Şifre sıfırla", description = "Şifre sıfırlama tokenı ile yeni şifre belirler.")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Şifre başarıyla güncellendi", null));
    }
}
