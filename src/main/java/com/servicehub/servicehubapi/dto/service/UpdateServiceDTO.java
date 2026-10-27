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
@Schema(description = "Dados para atualização de um serviço")
public class UpdateServiceDTO {

    @Schema(description = "Nome do serviço", example = "Serviço de chaveiro 24h", maxLength = 150)
    @NotBlank(message = "O título é obrigatório.")
    @Size(max = 150, message = "O título deve ter no máximo 150 caracteres.")
    private String title;

    @Schema(description = "Descrição atualizada do serviço", example = "Abertura de portas e troca de fechaduras.", maxLength = 5000)
    @NotBlank(message = "A descrição é obrigatória.")
    @Size(max = 5000, message = "A descrição deve ter no máximo 5000 caracteres.")
    private String description;

    @Schema(description = "Preço atualizado do serviço", example = "160.00")
    @NotNull(message = "O preço deve ser informado.")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
    @Digits(integer = 8, fraction = 2, message = "O preço deve ter até 8 dígitos inteiros e 2 decimais.")
    private BigDecimal price;

    @Schema(description = "Nova categoria do serviço", example = "SERVIÇO_1")
    @NotNull(message = "A categoria deve ser informada.")
    private Service_.ServiceCategory category;

    /**
     * Antes recebia a entidade {@code User} inteira (risco de Mass Assignment - OWASP API3).
     * Agora recebe apenas o ID; somente ADMIN pode transferir o serviço para outro provedor.
     */
    @Schema(description = "ID do provedor. Opcional: se omitido, mantém o atual. Somente ADMIN pode alterá-lo.",
            example = "1")
    @Positive(message = "O ID do provedor deve ser positivo.")
    private Long provider;

    @Schema(description = "Indica se o serviço está ativo ou não", example = "true")
    @NotNull(message = "O status deve ser informado.")
    private Boolean active;
}
