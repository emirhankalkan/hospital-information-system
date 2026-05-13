package com.his.controller;

import com.his.dto.response.PatientResponse;
import com.his.entity.Patient;
import com.his.exception.GlobalExceptionHandler;
import com.his.mapper.PatientMapper;
import com.his.security.AuthorizationService;
import com.his.security.CustomUserDetailsService;
import com.his.security.JwtUtils;
import com.his.service.PatientService;
import com.his.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PatientController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class,
        excludeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = com.his.security.JwtAuthFilter.class)
        })
@Import({MethodSecurityTestConfig.class, GlobalExceptionHandler.class})
@DisplayName("PatientController Method Security Tests")
class PatientControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @MockBean
    private PatientMapper patientMapper;

    @MockBean
    private UserService userService;

    @MockBean(name = "authorizationService")
    private AuthorizationService authorizationService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("403: Hasta baska hastanin profil detayini goremez")
    void patientCannotReadAnotherPatientProfile() throws Exception {
        when(authorizationService.isCurrentPatient(99L)).thenReturn(false);

        mockMvc.perform(get("/api/patients/{id}", 99L))
                .andExpect(status().isForbidden());

        verifyNoInteractions(patientService);
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("200: Hasta kendi profil detayini gorebilir")
    void patientCanReadOwnProfile() throws Exception {
        Patient patient = new Patient();
        patient.setId(10L);
        PatientResponse response = new PatientResponse();
        response.setId(10L);

        when(authorizationService.isCurrentPatient(10L)).thenReturn(true);
        when(patientService.findById(10L)).thenReturn(patient);
        when(patientMapper.toResponse(patient)).thenReturn(response);

        mockMvc.perform(get("/api/patients/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10L));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("403: Hasta tum hasta listesini goremez")
    void patientCannotListAllPatients() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(patientService);
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    @DisplayName("200: Receptionist hasta listesini gorebilir")
    void receptionistCanListPatients() throws Exception {
        when(patientService.findAllActive()).thenReturn(List.of());

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
