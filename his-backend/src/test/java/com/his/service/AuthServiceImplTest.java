package com.his.service;

import com.his.dto.request.LoginRequest;
import com.his.dto.request.RefreshTokenRequest;
import com.his.dto.request.RegisterRequest;
import com.his.dto.request.ResetPasswordRequest;
import com.his.dto.response.JwtResponse;
import com.his.entity.AccountToken;
import com.his.entity.RefreshToken;
import com.his.entity.Role;
import com.his.entity.User;
import com.his.enums.AccountTokenType;
import com.his.enums.RoleName;
import com.his.exception.ResourceAlreadyExistsException;
import com.his.repository.AccountTokenRepository;
import com.his.repository.RefreshTokenRepository;
import com.his.repository.RoleRepository;
import com.his.repository.UserRepository;
import com.his.security.CustomUserDetails;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
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
    private User verifiedPatientUser;
    private CustomUserDetails verifiedPatientDetails;

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

        verifiedPatientUser = new User();
        verifiedPatientUser.setId(10L);
        verifiedPatientUser.setUsername("patient");
        verifiedPatientUser.setEmail("patient@test.com");
        verifiedPatientUser.setPassword("encodedPassword");
        verifiedPatientUser.setIsActive(true);
        verifiedPatientUser.setEmailVerified(true);
        verifiedPatientUser.setRoles(Set.of(patientRole));
        verifiedPatientDetails = new CustomUserDetails(verifiedPatientUser);
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

    // =========================================================================
    // login
    // =========================================================================
    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("Hata: E-posta doğrulanmadan giriş yapılamaz")
        void whenEmailIsNotVerified_thenThrowIllegalStateException() {
            User unverifiedUser = new User();
            unverifiedUser.setId(11L);
            unverifiedUser.setUsername("unverified");
            unverifiedUser.setEmail("unverified@test.com");
            unverifiedUser.setPassword("encodedPassword");
            unverifiedUser.setIsActive(true);
            unverifiedUser.setEmailVerified(false);
            unverifiedUser.setRoles(Set.of(patientRole));

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    new CustomUserDetails(unverifiedUser),
                    null,
                    new CustomUserDetails(unverifiedUser).getAuthorities()
            );
            when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
            when(jwtUtils.generateJwtToken(authentication)).thenReturn("access-token");

            assertThatThrownBy(() -> authService.login(new LoginRequest("unverified@test.com", "password123")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("E-posta");

            verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Başarılı: Doğrulanmış kullanıcı login olunca access ve refresh token döner")
        void whenEmailIsVerified_thenReturnAccessAndRefreshToken() {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    verifiedPatientDetails,
                    null,
                    verifiedPatientDetails.getAuthorities()
            );
            when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
            when(jwtUtils.generateJwtToken(authentication)).thenReturn("access-token");
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

            JwtResponse response = authService.login(new LoginRequest("patient@test.com", "password123"));

            assertThat(response.getToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isNotBlank();
            assertThat(response.getUsername()).isEqualTo("patient");
            assertThat(response.getRoles()).containsExactly("ROLE_PATIENT");
            verify(refreshTokenRepository).save(argThat(token ->
                    token.getUser().equals(verifiedPatientUser) &&
                    token.getTokenHash() != null &&
                    !token.getTokenHash().isBlank() &&
                    Boolean.FALSE.equals(token.getRevoked())
            ));
        }
    }

    // =========================================================================
    // refresh token / logout
    // =========================================================================
    @Nested
    @DisplayName("refreshToken and logout")
    class RefreshTokenFlow {

        @Test
        @DisplayName("Başarılı: Refresh token tek kullanımlık olarak rotate edilir")
        void whenRefreshTokenIsValid_thenRotateToken() {
            RefreshToken existingToken = buildRefreshToken("old-refresh-token", false, LocalDateTime.now().plusDays(1));
            when(refreshTokenRepository.findByTokenHash(hashTokenForTest("old-refresh-token")))
                    .thenReturn(Optional.of(existingToken));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtUtils.generateJwtToken("patient")).thenReturn("new-access-token");

            JwtResponse response = authService.refreshToken(new RefreshTokenRequest("old-refresh-token"));

            assertThat(existingToken.getRevoked()).isTrue();
            assertThat(response.getToken()).isEqualTo("new-access-token");
            assertThat(response.getRefreshToken()).isNotBlank();
            assertThat(response.getRefreshToken()).isNotEqualTo("old-refresh-token");
            verify(refreshTokenRepository).save(argThat(token ->
                    token.getUser().equals(verifiedPatientUser) &&
                    Boolean.FALSE.equals(token.getRevoked())
            ));
        }

        @Test
        @DisplayName("Hata: Revoked refresh token tekrar kullanılamaz")
        void whenRefreshTokenIsRevoked_thenThrowIllegalArgumentException() {
            RefreshToken revokedToken = buildRefreshToken("revoked-refresh-token", true, LocalDateTime.now().plusDays(1));
            when(refreshTokenRepository.findByTokenHash(hashTokenForTest("revoked-refresh-token")))
                    .thenReturn(Optional.of(revokedToken));

            assertThatThrownBy(() -> authService.refreshToken(new RefreshTokenRequest("revoked-refresh-token")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Refresh token");

            verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Hata: Süresi dolmuş refresh token reddedilir ve revoked yapılır")
        void whenRefreshTokenIsExpired_thenRevokeAndThrowIllegalArgumentException() {
            RefreshToken expiredToken = buildRefreshToken("expired-refresh-token", false, LocalDateTime.now().minusMinutes(1));
            when(refreshTokenRepository.findByTokenHash(hashTokenForTest("expired-refresh-token")))
                    .thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> authService.refreshToken(new RefreshTokenRequest("expired-refresh-token")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Refresh token");

            assertThat(expiredToken.getRevoked()).isTrue();
            verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Başarılı: Logout refresh tokenı iptal eder")
        void whenLogout_thenRefreshTokenIsRevoked() {
            RefreshToken refreshToken = buildRefreshToken("logout-refresh-token", false, LocalDateTime.now().plusDays(1));
            when(refreshTokenRepository.findByTokenHash(hashTokenForTest("logout-refresh-token")))
                    .thenReturn(Optional.of(refreshToken));

            authService.logout(new RefreshTokenRequest("logout-refresh-token"));

            assertThat(refreshToken.getRevoked()).isTrue();
        }
    }

    // =========================================================================
    // password reset
    // =========================================================================
    @Nested
    @DisplayName("resetPassword")
    class ResetPassword {

        @Test
        @DisplayName("Başarılı: Şifre resetlenince token kullanılmış işaretlenir ve refresh tokenlar silinir")
        void whenResetPassword_thenMarkAccountTokenUsedAndDeleteRefreshTokens() {
            AccountToken accountToken = new AccountToken();
            accountToken.setUser(verifiedPatientUser);
            accountToken.setTokenType(AccountTokenType.PASSWORD_RESET);
            accountToken.setTokenHash(hashTokenForTest("reset-token"));
            accountToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));

            when(accountTokenRepository.findByTokenHashAndTokenType(
                    hashTokenForTest("reset-token"),
                    AccountTokenType.PASSWORD_RESET
            )).thenReturn(Optional.of(accountToken));
            when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

            authService.resetPassword(new ResetPasswordRequest("reset-token", "newPassword123"));

            assertThat(accountToken.getUsedAt()).isNotNull();
            assertThat(verifiedPatientUser.getPassword()).isEqualTo("encodedNewPassword");
            verify(refreshTokenRepository).deleteByUser(verifiedPatientUser);
        }

        @Test
        @DisplayName("Hata: Süresi dolmuş şifre reset tokenı kullanılamaz")
        void whenResetTokenIsExpired_thenThrowIllegalArgumentException() {
            AccountToken expiredToken = new AccountToken();
            expiredToken.setUser(verifiedPatientUser);
            expiredToken.setTokenType(AccountTokenType.PASSWORD_RESET);
            expiredToken.setTokenHash(hashTokenForTest("expired-reset-token"));
            expiredToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));

            when(accountTokenRepository.findByTokenHashAndTokenType(
                    hashTokenForTest("expired-reset-token"),
                    AccountTokenType.PASSWORD_RESET
            )).thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("expired-reset-token", "newPassword123")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Token");

            verify(passwordEncoder, never()).encode(anyString());
            verify(refreshTokenRepository, never()).deleteByUser(any(User.class));
        }
    }

    private RefreshToken buildRefreshToken(String rawToken, boolean revoked, LocalDateTime expiresAt) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(verifiedPatientUser);
        refreshToken.setTokenHash(hashTokenForTest(rawToken));
        refreshToken.setRevoked(revoked);
        refreshToken.setExpiresAt(expiresAt);
        return refreshToken;
    }

    private String hashTokenForTest(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
