package com.servicehub.servicehubapi.controller;

import com.servicehub.servicehubapi.dto.service.CreateServiceDTO;
import com.servicehub.servicehubapi.dto.service.ResponseServiceDTO;
import com.servicehub.servicehubapi.dto.service.UpdateServiceDTO;
import com.servicehub.servicehubapi.security.UserPrincipal;
import com.servicehub.servicehubapi.service.ServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Serviços", description = "Operações de gerenciamento de serviços cadastrados na plataforma ServiceHub.")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService){
        this.serviceService = serviceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Cria um novo serviço",
            description = "Cria um novo serviço associado ao provedor autenticado ou especificado por ADMIN.",
            operationId = "createService"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Serviço criado com sucesso.",
            content = @Content(schema = @Schema(implementation = ResponseServiceDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos.", content = @Content),
        @ApiResponse(responseCode = "403", description = "Acesso proibido.", content = @Content)
    })
    public ResponseServiceDTO create(
            @Parameter(description = "Dados para criação do serviço", required = true)
            @RequestBody @Valid CreateServiceDTO dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal actor){
        return serviceService.createService(dto, actor);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lista serviços (Paginado)", description = "Retorna uma página com todos os serviços disponíveis na plataforma.")
    @ApiResponse(responseCode = "200", description = "Página de serviços retornada com sucesso.")
    public Page<ResponseServiceDTO> findAll(@ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable){
        return serviceService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Busca serviço por ID", description = "Retorna os detalhes de um serviço cadastrado através de seu ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Serviço encontrado com sucesso.",
            content = @Content(schema = @Schema(implementation = ResponseServiceDTO.class))),
        @ApiResponse(responseCode = "404", description = "Serviço não encontrado.", content = @Content)
    })
    public ResponseServiceDTO findById(
            @Parameter(description = "ID do serviço a ser buscado", required = true)
            @PathVariable long id){
        return serviceService.findById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Atualiza serviço", description = "Atualiza os dados de um serviço existente. Permitido ao provedor proprietário ou ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso.",
            content = @Content(schema = @Schema(implementation = ResponseServiceDTO.class))),
        @ApiResponse(responseCode = "404", description = "Serviço não encontrado.", content = @Content),
        @ApiResponse(responseCode = "400", description = "Dados inválidos.", content = @Content),
        @ApiResponse(responseCode = "403", description = "Acesso negado para modificar serviço de outro usuário.", content = @Content)
    })
    public ResponseServiceDTO updateService(
            @Parameter(description = "ID do serviço a ser atualizado", required = true)
            @PathVariable long id, 
            @Parameter(description = "Novos dados do serviço", required = true)
            @RequestBody @Valid UpdateServiceDTO dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal actor){
        return serviceService.updateService(id, dto, actor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Deleta serviço", description = "Remove um serviço do sistema. Permitido ao provedor proprietário ou ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Serviço deletado com sucesso."),
        @ApiResponse(responseCode = "404", description = "Serviço não encontrado.", content = @Content),
        @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content)
    })
    public void deleteService (
            @Parameter(description = "ID do serviço a ser deletado", required = true)
            @PathVariable long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal actor){
        serviceService.deleteService(id, actor);
    }
}
