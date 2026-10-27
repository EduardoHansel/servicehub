package com.servicehub.servicehubapi.controller;

import com.servicehub.servicehubapi.dto.user.CreateUserDTO;
import com.servicehub.servicehubapi.dto.user.ResponseUserDTO;
import com.servicehub.servicehubapi.dto.user.UpdateUserDTO;
import com.servicehub.servicehubapi.exception.ResourceNotFoundException;
import com.servicehub.servicehubapi.model.Role;
import com.servicehub.servicehubapi.model.User;
import com.servicehub.servicehubapi.security.JwtService;
import com.servicehub.servicehubapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes da Camada Web - UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    private ResponseUserDTO sampleResponse;

    @BeforeEach
    void setUp() {
        User u = new User();
        u.setId(1L);
        u.setFullName("João da Silva");
        u.setEmail("joao@example.com");
        u.setPhone("11999998888");
        u.setRole(Role.CLIENT);
        u.setActive(true);
        sampleResponse = new ResponseUserDTO(u);
    }

    @Test
    @DisplayName("POST /api/v1/users - Deve cadastrar usuário com sucesso e retornar 201")
    void createUser_Success() throws Exception {
        CreateUserDTO dto = new CreateUserDTO();
        dto.setFullName("Carlos Prestador");
        dto.setEmail("carlos@example.com");
        dto.setPassword("senhaForte123");
        dto.setPhone("11988887777");
        dto.setRole(Role.PROVIDER);

        when(userService.createUser(any(CreateUserDTO.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("joao@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/users - Deve retornar 400 quando campos obrigatórios estiverem ausentes")
    void createUser_Invalid_Returns400() throws Exception {
        CreateUserDTO dto = new CreateUserDTO();
        // email e senha ausentes

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/users - Deve retornar lista paginada com status 200")
    void findAll_Returns200() throws Exception {
        when(userService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/v1/users?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("João da Silva"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - Deve retornar usuário existente com status 200")
    void findById_Success() throws Exception {
        when(userService.findById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("João da Silva"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - Deve retornar 404 quando usuário não existir")
    void findById_NotFound() throws Exception {
        when(userService.findById(999L))
                .thenThrow(new ResourceNotFoundException("Usuário não encontrado: 999"));

        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} - Deve atualizar usuário existente")
    void updateUser_Success() throws Exception {
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setFullName("João Atualizado");
        dto.setEmail("joao.atualizado@example.com");

        when(userService.updateUser(eq(1L), any(UpdateUserDTO.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} - Deve remover usuário e retornar 204")
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());
    }
}
