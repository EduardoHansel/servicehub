package com.servicehub.servicehubapi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Credenciais de acesso")
public record LoginRequestDTO(
        @Schema(description = "Email cadastrado", example = "joao.silva@example.com")
        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Formato de email inválido")
        @Size(max = 255)
        String email,

        @Schema(description = "Senha", example = "senhaForte123")
        @NotBlank(message = "A senha é obrigatória")
        @Size(max = 72)
        String password
) {
}
