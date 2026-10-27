package com.servicehub.servicehubapi.util;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.LookupTranslator;

import java.util.Map;

/**
 * Sanitização de textos livres contra XSS (Semana 08), baseada no Apache Commons Text.
 * <p>
 * Converte os caracteres significativos em HTML ({@code & < > " '}) em entidades seguras.
 * Diferente do {@code escapeHtml4}, preserva acentos (ex.: "João" não vira "Jo&amp;atilde;o").
 * A operação é <b>idempotente</b>: o texto é normalizado (unescape) antes de ser escapado,
 * evitando escape duplo quando o cliente reenvia um valor já sanitizado em um PUT.
 */
public final class InputSanitizer {

    private static final CharSequenceTranslator HTML_ESCAPER = new LookupTranslator(Map.of(
            "&", "&amp;",
            "<", "&lt;",
            ">", "&gt;",
            "\"", "&quot;",
            "'", "&#39;"
    ));

    private InputSanitizer() {
    }

    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        String normalized = StringEscapeUtils.unescapeHtml4(input.strip());
        return HTML_ESCAPER.translate(normalized);
    }
}
