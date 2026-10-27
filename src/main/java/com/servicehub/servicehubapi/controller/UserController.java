package com.servicehub.servicehubapi.controller;

import com.servicehub.servicehubapi.dto.user.CreateUserDTO;
import com.servicehub.servicehubapi.dto.user.ResponseUserDTO;
import com.servicehub.servicehubapi.dto.user.UpdateUserDTO;
import com.servicehub.servicehubapi.service.UserService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuários", description = "Operações de gerenciamento de usuários da plataforma.")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo usuário", description = "Cadastra um novo usuário no sistema.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso",
            content = @Content(schema = @Schema(implementation = ResponseUserDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos", content = @Content),
        @ApiResponse(responseCode = "409", description = "Email já cadastrado", content = @Content)
    })
    public ResponseUserDTO create(
            @Parameter(description = "Objeto com os dados de criação do usuário", required = true) 
            @RequestBody @Valid CreateUserDTO dto){
        return userService.createUser(dto);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lista todos os usuários (Paginado)", description = "Retorna uma página com usuários cadastrados. Acesso restrito a ADMIN.")
    @ApiResponse(responseCode = "200", description = "Página de usuários recuperada com sucesso")
    public Page<ResponseUserDTO> findAll(@ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable){
        return userService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Busca usuário por ID", description = "Retorna os detalhes de um usuário específico. Permitido ao próprio usuário ou ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso",
            content = @Content(schema = @Schema(implementation = ResponseUserDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    public ResponseUserDTO findById(
            @Parameter(description = "ID do usuário a ser buscado", required = true)
            @PathVariable Long id){
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Atualiza usuário", description = "Atualiza os dados de um usuário existente. Permitido ao próprio usuário ou ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = ResponseUserDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content),
        @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
        @ApiResponse(responseCode = "409", description = "Email já em uso", content = @Content)
    })
    public ResponseUserDTO update(
            @Parameter(description = "ID do usuário a ser atualizado", required = true)
            @PathVariable Long id, 
            @Parameter(description = "Dados para atualização do usuário", required = true)
            @RequestBody @Valid UpdateUserDTO dto){
        return userService.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Deleta usuário", description = "Remove um usuário do sistema pelo ID. Permitido ao próprio usuário ou ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    public void delete(
            @Parameter(description = "ID do usuário a ser deletado", required = true)
            @PathVariable Long id){
        userService.deleteUser(id);
    }
}
