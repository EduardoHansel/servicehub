package com.servicehub.servicehubapi.dto.service;

import com.servicehub.servicehubapi.dto.user.ResponseUserDTO;
import com.servicehub.servicehubapi.model.Service_;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representa os dados de um serviço retornados pela API")
public class ResponseServiceDTO {
    @Schema(description = "ID do serviço", example = "1")
    private Long id;
    
    @Schema(description = "Título do serviço", example = "Serviço de chaveiro 24h")
    private String title;
    
    @Schema(description = "Descrição do serviço", example = "Abertura de portas e troca de fechaduras.")
    private String description;
    
    @Schema(description = "Preço do serviço", example = "150.00")
    private BigDecimal price;
    
    @Schema(description = "Categoria", example = "MANUTENCAO")
    private Service_.ServiceCategory category;
    
    @Schema(description = "Dados do provedor que oferece o serviço")
    private ResponseUserDTO provider;
    
    @Schema(description = "Status do serviço", example = "true")
    private Boolean active;
    
    @Schema(description = "Data e hora de criação", example = "2023-10-25T10:00:00")
    private LocalDateTime createdAt;

    public ResponseServiceDTO(Service_ service) {
        this.id = service.getId();
        this.title = service.getTitle();
        this.description = service.getDescription();
        this.price = service.getPrice();
        this.category = service.getCategory();
        this.provider = new ResponseUserDTO(service.getProvider());
        this.active = service.getActive();
        this.createdAt = service.getCreatedAt();
    }
}
