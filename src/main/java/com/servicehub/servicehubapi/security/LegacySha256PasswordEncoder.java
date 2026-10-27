package com.servicehub.servicehubapi.security;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Encoder <b>somente leitura</b> para os hashes SHA-256 (hex, sem salt) gravados pela
 * versão anterior da API. É usado pelo {@code DelegatingPasswordEncoder} apenas para
 * validar senhas legadas; após um login bem-sucedido o hash é automaticamente
 * migrado para BCrypt.
 */
public class LegacySha256PasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        throw new UnsupportedOperationException("SHA-256 não deve ser usado para novas senhas; utilize BCrypt.");
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        byte[] expected = encodedPassword.toLowerCase().getBytes(StandardCharsets.UTF_8);
        byte[] actual = sha256Hex(rawPassword).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return true;
    }

    static String sha256Hex(CharSequence raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
