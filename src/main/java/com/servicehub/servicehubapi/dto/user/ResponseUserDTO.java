package com.servicehub.servicehubapi.dto.user;

import com.servicehub.servicehubapi.model.Role;
import com.servicehub.servicehubapi.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representa os dados de um usuário retornados pela API")
public class ResponseUserDTO {
    @Schema(description = "ID do usuário", example = "1")
    private Long id;
    
    @Schema(description = "Nome completo do usuário", example = "João da Silva")
    private String fullName;
    
    @Schema(description = "Email do usuário", example = "joao.silva@example.com")
    private String email;
    
    @Schema(description = "Telefone de contato", example = "11999999999")
    private String phone;
    
    @Schema(description = "Biografia curta", example = "Especialista em manutenções.")
    private String bio;
    
    @Schema(description = "URL da foto de perfil", example = "https://example.com/avatar.jpg")
    private String avatarUrl;
    
    @Schema(description = "Status indicando se o usuário está ativo", example = "true")
    private Boolean active;

    @Schema(description = "Perfil de acesso do usuário", example = "CLIENT")
    private Role role;

    public ResponseUserDTO(User user){
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.bio = user.getBio();
        this.avatarUrl = user.getAvatarUrl();
        this.active = user.getActive();
        this.role = user.getRole();
    }
}
