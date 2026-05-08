package com.his.service;

import com.his.dto.request.RegisterRequest;
import com.his.entity.Role;
import com.his.entity.User;
import com.his.enums.RoleName;
import com.his.exception.ResourceAlreadyExistsException;
import com.his.repository.AccountTokenRepository;
import com.his.repository.RefreshTokenRepository;
import com.his.repository.RoleRepository;
import com.his.repository.UserRepository;
import com.his.security.JwtUtils;
import com.his.service.EmailService;
import com.his.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthServiceImpl Unit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AccountTokenRepository accountTokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest validRegisterRequest;
    private Role patientRole;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("testuser");
        validRegisterRequest.setEmail("test@test.com");
        validRegisterRequest.setPassword("password123");
        // roles null → default PATIENT alınır

        patientRole = new Role();
        patientRole.setId(1L);
        patientRole.setName(RoleName.PATIENT);
    }

    // =========================================================================
    // register
    // =========================================================================
    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("Başarılı: Yeni kullanıcı PATIENT rolüyle kaydedilir")
        void whenNewUser_thenSaveWithPatientRole() {
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(roleRepository.findByName(RoleName.PATIENT)).thenReturn(Optional.of(patientRole));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            authService.register(validRegisterRequest);

            verify(userRepository).save(argThat(user ->
                    user.getUsername().equals("testuser") &&
                    user.getEmail().equals("test@test.com") &&
                    user.getPassword().equals("encodedPassword") &&
                    user.getRoles().contains(patientRole) &&
                    !user.getEmailVerified()
            ));
            verify(emailService).sendEmailVerification(any(User.class), anyString());
        }

        @Test
        @DisplayName("Başarılı: Roles olarak 'admin' verilirse ADMIN rolü atanır")
        void whenAdminRoleRequested_thenSaveWithAdminRole() {
            validRegisterRequest.setRoles(Set.of("admin"));

            Role adminRole = new Role();
            adminRole.setName(RoleName.ADMIN);

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPwd");
            when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.of(adminRole));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            authService.register(validRegisterRequest);

            verify(userRepository).save(argThat(user ->
                    user.getRoles().contains(adminRole)
            ));
            verify(emailService).sendEmailVerification(any(User.class), anyString());
        }

        @Test
        @DisplayName("Hata: Kullanıcı adı zaten varsa → ResourceAlreadyExistsException")
        void whenUsernameExists_thenThrowAlreadyExists() {
            when(userRepository.existsByUsername("testuser")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(validRegisterRequest))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .satisfies(ex -> assertThat(ex.getMessage().toLowerCase())
                            .contains("kullanıcı"));
        }

        @Test
        @DisplayName("Hata: E-posta zaten varsa → ResourceAlreadyExistsException")
        void whenEmailExists_thenThrowAlreadyExists() {
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(validRegisterRequest))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .satisfies(ex -> assertThat(ex.getMessage().toLowerCase())
                            .contains("e-posta"));
        }

        @Test
        @DisplayName("Başarılı: Kayıt sırasında şifre encode edilir (plain text DB'ye gitmez)")
        void whenRegister_thenPasswordIsEncoded() {
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$HASHED");
            when(roleRepository.findByName(RoleName.PATIENT)).thenReturn(Optional.of(patientRole));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            authService.register(validRegisterRequest);

            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(argThat(u -> u.getPassword().equals("$2a$10$HASHED")));
        }
    }
}
