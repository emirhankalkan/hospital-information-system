package com.his.service.impl;

import com.his.dto.request.ForgotPasswordRequest;
import com.his.dto.request.LoginRequest;
import com.his.dto.request.RefreshTokenRequest;
import com.his.dto.request.RegisterRequest;
import com.his.dto.request.ResendVerificationRequest;
import com.his.dto.request.ResetPasswordRequest;
import com.his.dto.response.JwtResponse;
import com.his.entity.AccountToken;
import com.his.entity.RefreshToken;
import com.his.entity.Role;
import com.his.entity.User;
import com.his.enums.AccountTokenType;
import com.his.enums.RoleName;
import com.his.exception.ResourceAlreadyExistsException;
import com.his.exception.ResourceNotFoundException;
import com.his.repository.AccountTokenRepository;
import com.his.repository.RefreshTokenRepository;
import com.his.repository.RoleRepository;
import com.his.repository.UserRepository;
import com.his.security.CustomUserDetails;
import com.his.security.JwtUtils;
import com.his.service.AuthService;
import com.his.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountTokenRepository accountTokenRepository;
    private final EmailService emailService;

    @Value("${his.refresh-token.expiration:604800000}")
    private long refreshTokenExpirationMs;

    @Value("${his.account.email-verification-expiration:86400000}")
    private long emailVerificationExpirationMs;

    @Value("${his.account.password-reset-expiration:900000}")
    private long passwordResetExpirationMs;

    @Override
    @Transactional
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalStateException("E-posta adresinizi doğrulamadan giriş yapamazsınız.");
        }

        String refreshToken = createRefreshToken(user);
        return buildJwtResponse(jwt, refreshToken, userDetails);
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Hata: Kullanıcı adı zaten kullanımda!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Hata: E-posta adresi zaten kullanımda!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        user.setEmailVerified(false);
        user.setRoles(resolveRoles(request.getRoles()));

        User savedUser = userRepository.save(user);
        String verificationToken = createAccountToken(
                savedUser,
                AccountTokenType.EMAIL_VERIFICATION,
                emailVerificationExpirationMs
        );
        emailService.sendEmailVerification(savedUser, verificationToken);
    }

    @Override
    @Transactional
    public JwtResponse refreshToken(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.getRefreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token geçersiz."));

        if (Boolean.TRUE.equals(refreshToken.getRevoked()) || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshToken.setRevoked(true);
            throw new IllegalArgumentException("Refresh token süresi dolmuş veya iptal edilmiş.");
        }

        User user = refreshToken.getUser();
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalStateException("Kullanıcı hesabı aktif değil.");
        }

        refreshToken.setRevoked(true);
        String newRefreshToken = createRefreshToken(user);
        String newAccessToken = jwtUtils.generateJwtToken(user.getUsername());
        List<String> roles = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName().name())
                .collect(Collectors.toList());

        return new JwtResponse(newAccessToken, newRefreshToken, user.getId(), user.getUsername(), user.getEmail(), roles);
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByTokenHash(hashToken(request.getRefreshToken()))
                .ifPresent(refreshToken -> refreshToken.setRevoked(true));
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        AccountToken accountToken = findUsableAccountToken(token, AccountTokenType.EMAIL_VERIFICATION);
        User user = accountToken.getUser();
        user.setEmailVerified(true);
        accountToken.setUsedAt(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void resendVerification(ResendVerificationRequest request) {
        userRepository.findByEmail(request.getEmail())
                .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
                .filter(user -> !Boolean.TRUE.equals(user.getEmailVerified()))
                .ifPresent(user -> {
                    String token = createAccountToken(user, AccountTokenType.EMAIL_VERIFICATION, emailVerificationExpirationMs);
                    emailService.sendEmailVerification(user, token);
                });
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail())
                .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
                .ifPresent(user -> {
                    String token = createAccountToken(user, AccountTokenType.PASSWORD_RESET, passwordResetExpirationMs);
                    emailService.sendPasswordReset(user, token);
                });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        AccountToken accountToken = findUsableAccountToken(request.getToken(), AccountTokenType.PASSWORD_RESET);
        User user = accountToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountToken.setUsedAt(LocalDateTime.now());
        refreshTokenRepository.deleteByUser(user);
    }

    private JwtResponse buildJwtResponse(String jwt, String refreshToken, CustomUserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        User user = userDetails.getUser();
        return new JwtResponse(jwt, refreshToken, user.getId(), userDetails.getUsername(), user.getEmail(), roles);
    }

    private Set<Role> resolveRoles(Set<String> strRoles) {
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role patientRole = roleRepository.findByName(RoleName.PATIENT)
                    .orElseThrow(() -> new ResourceNotFoundException("Hata: Rol bulunamadı (PATIENT)."));
            roles.add(patientRole);
            return roles;
        }

        strRoles.forEach(role -> {
            switch (role.toLowerCase()) {
                case "admin":
                    roles.add(roleRepository.findByName(RoleName.ADMIN)
                            .orElseThrow(() -> new ResourceNotFoundException("Hata: Rol bulunamadı (ADMIN).")));
                    break;
                case "receptionist":
                    roles.add(roleRepository.findByName(RoleName.RECEPTIONIST)
                            .orElseThrow(() -> new ResourceNotFoundException("Hata: Rol bulunamadı (RECEPTIONIST).")));
                    break;
                case "doctor":
                    roles.add(roleRepository.findByName(RoleName.DOCTOR)
                            .orElseThrow(() -> new ResourceNotFoundException("Hata: Rol bulunamadı (DOCTOR).")));
                    break;
                default:
                    roles.add(roleRepository.findByName(RoleName.PATIENT)
                            .orElseThrow(() -> new ResourceNotFoundException("Hata: Rol bulunamadı (PATIENT).")));
            }
        });

        return roles;
    }

    private String createRefreshToken(User user) {
        String rawToken = generateSecureToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpirationMs)));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    private String createAccountToken(User user, AccountTokenType tokenType, long expirationMs) {
        accountTokenRepository.deleteByUserAndTokenType(user, tokenType);

        String rawToken = generateSecureToken();
        AccountToken accountToken = new AccountToken();
        accountToken.setUser(user);
        accountToken.setTokenType(tokenType);
        accountToken.setTokenHash(hashToken(rawToken));
        accountToken.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(expirationMs)));
        accountTokenRepository.save(accountToken);

        return rawToken;
    }

    private AccountToken findUsableAccountToken(String rawToken, AccountTokenType tokenType) {
        AccountToken accountToken = accountTokenRepository.findByTokenHashAndTokenType(hashToken(rawToken), tokenType)
                .orElseThrow(() -> new IllegalArgumentException("Token geçersiz."));

        if (accountToken.getUsedAt() != null || accountToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token süresi dolmuş veya daha önce kullanılmış.");
        }

        return accountToken;
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Token hash algoritması bulunamadı.", ex);
        }
    }
}
