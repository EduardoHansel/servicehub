package com.servicehub.servicehubapi.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes Unitários - LegacySha256PasswordEncoder")
class LegacySha256PasswordEncoderTest {

    private final LegacySha256PasswordEncoder encoder = new LegacySha256PasswordEncoder();

    @Test
    @DisplayName("Deve validar hash SHA-256 legado com sucesso")
    void matches_Success() {
        // SHA-256 de "secret" = 2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b
        String raw = "secret";
        String sha256Hex = "2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b";

        assertThat(encoder.matches(raw, sha256Hex)).isTrue();
        assertThat(encoder.matches("wrong", sha256Hex)).isFalse();
        assertThat(encoder.matches(null, sha256Hex)).isFalse();
        assertThat(encoder.matches(raw, null)).isFalse();
        assertThat(encoder.upgradeEncoding(sha256Hex)).isTrue();
    }

    @Test
    @DisplayName("encode() deve lançar UnsupportedOperationException")
    void encode_ThrowsException() {
        assertThatThrownBy(() -> encoder.encode("password"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
