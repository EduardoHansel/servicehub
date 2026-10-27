package com.servicehub.servicehubapi.security;

import com.servicehub.servicehubapi.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes Unitários - JwtService")
class JwtServiceTest {

    private JwtService jwtService;
    private final String validSecret = "c2VydmljZWh1Yi1zZWNyZXQta2V5LWZvci1qd3Qtc2lnbmluZy1hbmQtdmFsaWRhdGlvbi10ZXN0LTEyMzQ1Ng==";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(validSecret, 3600000L, "servicehub-test");
    }

    @Test
    @DisplayName("Deve gerar token JWT e fazer parse com sucesso")
    void generateAndParseToken_Success() {
        UserPrincipal principal = UserPrincipal.fromToken(10L, "user@test.com", Role.PROVIDER);

        String token = jwtService.generateToken(principal);

        assertThat(token).isNotBlank();
        Optional<UserPrincipal> parsedOpt = jwtService.parseToken(token);

        assertThat(parsedOpt).isPresent();
        UserPrincipal parsed = parsedOpt.get();
        assertThat(parsed.id()).isEqualTo(10L);
        assertThat(parsed.email()).isEqualTo("user@test.com");
        assertThat(parsed.role()).isEqualTo(Role.PROVIDER);
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(3600L);
    }

    @Test
    @DisplayName("Deve retornar vazio ao tentar fazer parse de token inválido")
    void parseToken_InvalidToken_ReturnsEmpty() {
        Optional<UserPrincipal> result = jwtService.parseToken("invalid.token.structure");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar vazio ao tentar fazer parse de token expirado")
    void parseToken_ExpiredToken_ReturnsEmpty() {
        JwtService expiredService = new JwtService(validSecret, -1000L, "servicehub-test");
        UserPrincipal principal = UserPrincipal.fromToken(10L, "user@test.com", Role.CLIENT);
        String token = expiredService.generateToken(principal);

        Optional<UserPrincipal> result = jwtService.parseToken(token);
        assertThat(result).isEmpty();
    }
}
