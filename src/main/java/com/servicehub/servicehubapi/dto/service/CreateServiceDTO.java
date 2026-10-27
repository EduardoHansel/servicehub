package com.servicehub.servicehubapi.dto.service;

import com.servicehub.servicehubapi.model.Service_;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Dados necessários para a criação de um novo serviço.")
public class CreateServiceDTO {

    @Schema(description = "Nome do serviço", example = "Serviço de chaveiro 24h", maxLength = 150)
    @NotBlank(message = "O título é obrigatório.")
    @Size(max = 150, message = "O título deve ter no máximo 150 caracteres.")
    private String title;

    @Schema(description = "Descrição detalhada do serviço", example = "Abertura de portas e troca de fechaduras.", maxLength = 5000)
    @NotBlank(message = "A descrição é obrigatória.")
    @Size(max = 5000, message = "A descrição deve ter no máximo 5000 caracteres.")
    private String description;

    @Schema(description = "Preço base do serviço", example = "150.00")
    @NotNull(message = "O preço é obrigatório.")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
    @Digits(integer = 8, fraction = 2, message = "O preço deve ter até 8 dígitos inteiros e 2 decimais.")
    private BigDecimal price;

    @Schema(description = "Categoria do serviço", example = "SERVIÇO_1")
    @NotNull(message = "A categoria é obrigatória.")
    private Service_.ServiceCategory category;

    @Schema(description = "ID do usuário provedor do serviço. Opcional: se omitido, usa o usuário autenticado. "
            + "Somente ADMIN pode informar outro provedor.", example = "1")
    @Positive(message = "O ID do provedor deve ser positivo.")
    private Long provider;
}
