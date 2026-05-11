package com.his.security;

import com.his.HisBackendApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = HisBackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Security error response tests")
class SecurityErrorResponseTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("401 Unauthorized: Kimlik doğrulama yoksa standart ApiResponse döner")
    void whenUnauthenticated_thenReturnStandard401Response() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Kimlik doğrulama gerekli. Lütfen giriş yapın."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("403 Forbidden: Yetkisiz rol standart ApiResponse alır")
    void whenAuthenticatedWithoutRole_thenReturnStandard403Response() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Erişim reddedildi. Bu kaynağa erişim izniniz yok."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("CORS preflight: Angular dev origin izinli olur")
    void whenCorsPreflightFromAngularDevOrigin_thenAllowOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
    }
}
