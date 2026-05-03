package com.his.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * JwtUtils Unit Tests
 * ReflectionTestUtils ile @Value alanları test ortamında enjekte edilir.
 */
@DisplayName("JwtUtils Unit Tests")
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    // Geçerli 256-bit Base64 secret (test için sabit)
    private static final String TEST_SECRET = "aYp2s8vD4gK9fX1mP4jL7wN0zB3sC6xV9aT2hN5yR8E=";
    private static final long EXPIRATION_MS = 3_600_000L; // 1 saat

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", EXPIRATION_MS);
    }

    private Authentication buildAuthentication(String username) {
        CustomUserDetails userDetails = buildUserDetails(username);
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    private CustomUserDetails buildUserDetails(String username) {
        com.his.entity.User user = new com.his.entity.User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setPassword("encoded");
        user.setIsActive(true);
        user.setRoles(new java.util.HashSet<>());
        return new CustomUserDetails(user);
    }

    @Test
    @DisplayName("generateJwtToken: Authentication'dan geçerli token üretilir")
    void whenAuthentication_thenGenerateValidToken() {
        Authentication auth = buildAuthentication("johndoe");

        String token = jwtUtils.generateJwtToken(auth);

        assertThat(token).isNotBlank();
        // JWT formatı: header.payload.signature (3 parça, nokta ile ayrılmış)
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("getUserNameFromJwtToken: Token'dan doğru kullanıcı adı çözülür")
    void whenValidToken_thenExtractUsername() {
        Authentication auth = buildAuthentication("janedoe");
        String token = jwtUtils.generateJwtToken(auth);

        String username = jwtUtils.getUserNameFromJwtToken(token);

        assertThat(username).isEqualTo("janedoe");
    }

    @Test
    @DisplayName("validateJwtToken: Geçerli token true döner")
    void whenValidToken_thenReturnTrue() {
        Authentication auth = buildAuthentication("validuser");
        String token = jwtUtils.generateJwtToken(auth);

        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateJwtToken: Bozuk (malformed) token false döner")
    void whenMalformedToken_thenReturnFalse() {
        assertThat(jwtUtils.validateJwtToken("not.a.valid.token")).isFalse();
    }

    @Test
    @DisplayName("validateJwtToken: Süresi dolmuş token false döner")
    void whenExpiredToken_thenReturnFalse() {
        // Geçmişte süresi dolmuş bir token üretmek için expiration = 0 ms
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 0L);
        Authentication auth = buildAuthentication("expireduser");
        String expiredToken = jwtUtils.generateJwtToken(auth);

        assertThat(jwtUtils.validateJwtToken(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("validateJwtToken: Boş string false döner")
    void whenEmptyToken_thenReturnFalse() {
        assertThat(jwtUtils.validateJwtToken("")).isFalse();
    }
}
