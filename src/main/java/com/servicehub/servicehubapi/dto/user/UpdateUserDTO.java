package com.servicehub.servicehubapi.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Dados para atualização de um usuário existente")
public class UpdateUserDTO {
    
    @Schema(description = "Nome completo atualizado", example = "João da Silva Sauro", maxLength = 100)
    @NotBlank(message = "O nome completo é obrigatório")
    @Size(max = 100, message = "O nome completo deve ter no máximo 100 caracteres")
    private String fullName;

    @Schema(description = "Email atualizado", example = "joao.silva@example.com")
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Formato de email inválido")
    @Size(max = 255, message = "O email deve ter no máximo 255 caracteres")
    private String email;

    @Schema(description = "Telefone para contato", example = "11999999999")
    @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Telefone inválido: informe de 10 a 15 dígitos")
    private String phone;
    
    @Schema(description = "Biografia curta do usuário", example = "Especialista em manutenções.", maxLength = 1000)
    @Size(max = 1000, message = "A biografia deve ter no máximo 1000 caracteres")
    private String bio;
    
    @Schema(description = "URL da foto de perfil (http/https)", example = "https://example.com/avatar.jpg")
    @Size(max = 500, message = "A URL do avatar deve ter no máximo 500 caracteres")
    @Pattern(regexp = "^https?://\\S+$", message = "A URL do avatar deve começar com http:// ou https://")
    private String avatarUrl;
}
