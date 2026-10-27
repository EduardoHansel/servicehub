package com.servicehub.servicehubapi.dto.user;

import com.servicehub.servicehubapi.model.Role;
import com.servicehub.servicehubapi.validation.AllowedRoles;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Dados para criação de um novo usuário")
public class CreateUserDTO {

    @Schema(description = "Nome completo do usuário", example = "João da Silva", maxLength = 100)
    @NotBlank(message = "O nome completo é obrigatório")
    @Size(max = 100, message = "O nome completo deve ter no máximo 100 caracteres")
    private String fullName;

    @Schema(description = "Email do usuário", example = "joao.silva@example.com")
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Formato de email inválido")
    @Size(max = 255, message = "O email deve ter no máximo 255 caracteres")
    private String email;

    @Schema(description = "Senha de acesso (8 a 72 caracteres, com letras e números)",
            example = "senhaForte123", minLength = 8, maxLength = 72)
    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "A senha deve conter letras e números")
    private String password;

    @Schema(description = "Telefone para contato (somente dígitos, opcionalmente com +)", example = "11999999999")
    @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Telefone inválido: informe de 10 a 15 dígitos")
    private String phone;

    @Schema(description = "Biografia curta do usuário", example = "Especialista em manutenções.", maxLength = 1000)
    @Size(max = 1000, message = "A biografia deve ter no máximo 1000 caracteres")
    private String bio;

    @Schema(description = "URL da foto de perfil (http/https)", example = "https://example.com/avatar.jpg")
    @Size(max = 500, message = "A URL do avatar deve ter no máximo 500 caracteres")
    @Pattern(regexp = "^https?://\\S+$", message = "A URL do avatar deve começar com http:// ou https://")
    private String avatarUrl;

    @Schema(description = "Perfil do usuário. Padrão: CLIENT. ADMIN não pode ser auto-atribuído.",
            example = "CLIENT", allowableValues = {"CLIENT", "PROVIDER"})
    @AllowedRoles(value = {Role.CLIENT, Role.PROVIDER}, message = "Perfil permitido no cadastro: CLIENT ou PROVIDER")
    private Role role;
}