package com.his.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.dto.request.MedicalRecordRequest;
import com.his.exception.GlobalExceptionHandler;
import com.his.mapper.MedicalRecordMapper;
import com.his.security.AuthorizationService;
import com.his.security.CustomUserDetailsService;
import com.his.security.JwtUtils;
import com.his.service.MedicalRecordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MedicalRecordController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class,
        excludeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = com.his.security.JwtAuthFilter.class)
        })
@Import({MethodSecurityTestConfig.class, GlobalExceptionHandler.class})
@DisplayName("MedicalRecordController Method Security Tests")
class MedicalRecordControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MedicalRecordService medicalRecordService;

    @MockBean
    private MedicalRecordMapper medicalRecordMapper;

    @MockBean(name = "authorizationService")
    private AuthorizationService authorizationService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("403: Hasta baska hastanin tibbi kaydini goremez")
    void patientCannotReadAnotherPatientsMedicalRecord() throws Exception {
        when(authorizationService.canAccessMedicalRecord(70L)).thenReturn(false);

        mockMvc.perform(get("/api/medical-records/{id}", 70L))
                .andExpect(status().isForbidden());

        verifyNoInteractions(medicalRecordService);
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("403: Doktor yetkili olmadigi tibbi kaydi guncelleyemez")
    void doctorCannotUpdateUnownedMedicalRecord() throws Exception {
        MedicalRecordRequest request = new MedicalRecordRequest();
        request.setAppointmentId(30L);
        request.setDiagnosis("Diagnosis");
        when(authorizationService.canUpdateMedicalRecord(80L)).thenReturn(false);

        mockMvc.perform(put("/api/medical-records/{id}", 80L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(medicalRecordService);
    }
}
