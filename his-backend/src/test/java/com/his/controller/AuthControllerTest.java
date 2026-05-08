package com.his.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.dto.request.LoginRequest;
import com.his.dto.request.RegisterRequest;
import com.his.dto.response.JwtResponse;
import com.his.exception.GlobalExceptionHandler;
import com.his.exception.ResourceAlreadyExistsException;
import com.his.security.CustomUserDetailsService;
import com.his.security.JwtUtils;
import com.his.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController Web Layer (Slice) Tests
 *
 * - @WebMvcTest: Yalnızca Web katmanı (Controller + MVC) yüklenir.
 * - TestSecurityConfig: Tüm endpoint'leri açık bırakır (JWT filtresi devre dışı).
 * - GlobalExceptionHandler: Validation ve custom exception response'larını etkinleştirir.
 */
@WebMvcTest(controllers = AuthController.class,
        excludeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = com.his.security.JwtAuthFilter.class)
        })
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("AuthController Web Layer Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    // =========================================================================
    // POST /api/auth/login
    // =========================================================================
    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("200 OK: Geçerli kimlik bilgileriyle JWT token döner")
        void whenValidCredentials_thenReturnJwt() throws Exception {
            LoginRequest loginRequest = new LoginRequest("testuser", "password123");
            JwtResponse jwtResponse = new JwtResponse(
                    "mocked.jwt.token", 1L, "testuser", "test@test.com", List.of("ROLE_PATIENT")
            );
            when(authService.login(any(LoginRequest.class))).thenReturn(jwtResponse);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.token").value("mocked.jwt.token"))
                    .andExpect(jsonPath("$.data.username").value("testuser"))
                    .andExpect(jsonPath("$.data.roles[0]").value("ROLE_PATIENT"));
        }

        @Test
        @DisplayName("400 Bad Request: Kullanıcı adı boş → validation hatası")
        void whenUsernameIsBlank_thenReturn400() throws Exception {
            LoginRequest invalidRequest = new LoginRequest("", "password123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("400 Bad Request: Şifre boş → validation hatası")
        void whenPasswordIsBlank_thenReturn400() throws Exception {
            LoginRequest invalidRequest = new LoginRequest("testuser", "");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // =========================================================================
    // POST /api/auth/register
    // =========================================================================
    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {

        private RegisterRequest buildValidRequest() {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("newuser");
            req.setPassword("secret123");
            req.setEmail("newuser@example.com");
            return req;
        }

        @Test
        @DisplayName("200 OK: Geçerli kayıt isteği kabul edilir")
        void whenValidRequest_thenReturn200() throws Exception {
            doNothing().when(authService).register(any(RegisterRequest.class));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildValidRequest())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Kullanıcı kaydı başarıyla oluşturuldu. Lütfen e-posta adresinizi doğrulayın."));
        }

        @Test
        @DisplayName("409 Conflict: Kullanıcı adı zaten varsa conflict döner")
        void whenUsernameExists_thenReturn409() throws Exception {
            doThrow(new ResourceAlreadyExistsException("Hata: Kullanıcı adı zaten kullanımda!"))
                    .when(authService).register(any(RegisterRequest.class));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildValidRequest())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Hata: Kullanıcı adı zaten kullanımda!"));
        }

        @Test
        @DisplayName("400 Bad Request: Geçersiz e-posta formatı")
        void whenInvalidEmail_thenReturn400() throws Exception {
            RegisterRequest invalidReq = buildValidRequest();
            invalidReq.setEmail("not-an-email");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidReq)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("400 Bad Request: Kısa şifre (3 karakter) reddedilir")
        void whenPasswordTooShort_thenReturn400() throws Exception {
            RegisterRequest invalidReq = buildValidRequest();
            invalidReq.setPassword("abc");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidReq)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("400 Bad Request: Çok kısa kullanıcı adı (2 karakter) reddedilir")
        void whenUsernameTooShort_thenReturn400() throws Exception {
            RegisterRequest invalidReq = buildValidRequest();
            invalidReq.setUsername("ab");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidReq)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
