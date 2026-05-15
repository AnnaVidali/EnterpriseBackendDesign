package com.application.enterprisebackenddesign.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(
                "4A6F5B6D7C8E9F0A1B2C3D4E5F6A7B8C9D0E1F2A3B4C5D6E7F8A9B0C1D2E3F",
                86400000
        );
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String token = jwtProvider.generateToken("testuser");
        assertThat(token).isNotBlank();
        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String token = jwtProvider.generateToken("john");
        assertThat(jwtProvider.getUsernameFromToken(token)).isEqualTo("john");
    }

    @Test
    void shouldRejectInvalidToken() {
        assertThat(jwtProvider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    void shouldRejectNullToken() {
        assertThat(jwtProvider.validateToken(null)).isFalse();
    }

    @Test
    void shouldRejectEmptyToken() {
        assertThat(jwtProvider.validateToken("")).isFalse();
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtProvider shortLived = new JwtProvider(
                "4A6F5B6D7C8E9F0A1B2C3D4E5F6A7B8C9D0E1F2A3B4C5D6E7F8A9B0C1D2E3F",
                -1
        );
        String token = shortLived.generateToken("testuser");
        assertThat(shortLived.validateToken(token)).isFalse();
    }
}
