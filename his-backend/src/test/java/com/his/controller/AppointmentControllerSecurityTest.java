package com.his.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.dto.request.AppointmentRequest;
import com.his.exception.GlobalExceptionHandler;
import com.his.mapper.AppointmentMapper;
import com.his.security.AuthorizationService;
import com.his.security.CustomUserDetailsService;
import com.his.security.JwtUtils;
import com.his.service.AppointmentService;
import com.his.service.DoctorService;
import com.his.service.PatientService;
import com.his.service.UserService;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AppointmentController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class,
        excludeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = com.his.security.JwtAuthFilter.class)
        })
@Import({MethodSecurityTestConfig.class, GlobalExceptionHandler.class})
@DisplayName("AppointmentController Method Security Tests")
class AppointmentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private AppointmentMapper appointmentMapper;

    @MockBean
    private PatientService patientService;

    @MockBean
    private DoctorService doctorService;

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
    @DisplayName("403: Hasta baska hastanin randevu detayini goremez")
    void patientCannotReadAnotherPatientsAppointment() throws Exception {
        when(authorizationService.canAccessAppointment(50L)).thenReturn(false);

        mockMvc.perform(get("/api/appointments/{id}", 50L))
                .andExpect(status().isForbidden());

        verifyNoInteractions(appointmentService);
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("403: Doktor baska doktorun randevusunu guncelleyemez")
    void doctorCannotUpdateAnotherDoctorsAppointment() throws Exception {
        AppointmentRequest request = new AppointmentRequest();
        request.setPatientId(10L);
        request.setDoctorId(20L);
        request.setAppointmentDate(LocalDate.now().plusDays(1));
        request.setAppointmentTime(LocalTime.of(10, 0));
        when(authorizationService.canManageAppointment(60L)).thenReturn(false);

        mockMvc.perform(put("/api/appointments/{id}", 60L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(appointmentService);
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("403: Hasta tum randevu listesini goremez")
    void patientCannotListAllAppointments() throws Exception {
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(appointmentService);
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    @DisplayName("200: Receptionist tum randevu listesini gorebilir")
    void receptionistCanListAppointments() throws Exception {
        when(appointmentService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
