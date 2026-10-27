package com.servicehub.servicehubapi.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes Unitários - InputSanitizer")
class InputSanitizerTest {

    @Test
    @DisplayName("Deve escapar tags HTML perigosas para prevenir XSS")
    void sanitize_EscapesHtml() {
        String xss = "<script>alert('xss')</script>";
        String sanitized = InputSanitizer.sanitize(xss);

        assertThat(sanitized).isEqualTo("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;");
    }

    @Test
    @DisplayName("Deve preservar caracteres acentuados em português")
    void sanitize_PreservesAccents() {
        String text = "João & Maria em São Paulo";
        String sanitized = InputSanitizer.sanitize(text);

        assertThat(sanitized).isEqualTo("João &amp; Maria em São Paulo");
    }

    @Test
    @DisplayName("Deve retornar null quando entrada for null")
    void sanitize_Null() {
        assertThat(InputSanitizer.sanitize(null)).isNull();
    }

    @Test
    @DisplayName("Operação deve ser idempotente (evita escape duplo)")
    void sanitize_Idempotent() {
        String raw = "Texto <simples>";
        String once = InputSanitizer.sanitize(raw);
        String twice = InputSanitizer.sanitize(once);

        assertThat(twice).isEqualTo(once);
    }
}
