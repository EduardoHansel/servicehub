package com.servicehub.servicehubapi.dto.auth;

import com.servicehub.servicehubapi.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token de acesso emitido após autenticação")
public record LoginResponseDTO(
        @Schema(description = "JWT assinado (HS256)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Tipo do token", example = "Bearer")
        String tokenType,

        @Schema(description = "Validade do token em segundos", example = "3600")
        long expiresIn,

        @Schema(description = "ID do usuário autenticado", example = "1")
        Long userId,

        @Schema(description = "Perfil do usuário autenticado", example = "CLIENT")
        Role role
) {
}
