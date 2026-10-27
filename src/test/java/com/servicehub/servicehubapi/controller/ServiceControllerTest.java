package com.servicehub.servicehubapi.controller;

import com.servicehub.servicehubapi.dto.service.CreateServiceDTO;
import com.servicehub.servicehubapi.dto.service.ResponseServiceDTO;
import com.servicehub.servicehubapi.dto.service.UpdateServiceDTO;
import com.servicehub.servicehubapi.exception.ResourceNotFoundException;
import com.servicehub.servicehubapi.model.Role;
import com.servicehub.servicehubapi.model.Service_;
import com.servicehub.servicehubapi.model.User;
import com.servicehub.servicehubapi.security.JwtService;
import com.servicehub.servicehubapi.service.ServiceService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes da Camada Web - ServiceController")
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ServiceService serviceService;

    @MockitoBean
    private JwtService jwtService;

    private ResponseServiceDTO sampleResponse;

    @BeforeEach
    void setUp() {
        User provider = new User();
        provider.setId(10L);
        provider.setFullName("Provedor Teste");
        provider.setEmail("provedor@test.com");
        provider.setRole(Role.PROVIDER);

        Service_ s = new Service_();
        s.setId(100L);
        s.setTitle("Chaveiro 24h");
        s.setDescription("Abertura de portas");
        s.setPrice(new BigDecimal("150.00"));
        s.setCategory(Service_.ServiceCategory.SERVIÇO_1);
        s.setProvider(provider);
        s.setActive(true);

        sampleResponse = new ResponseServiceDTO(s);
    }

    @Test
    @DisplayName("GET /api/v1/services - Deve retornar página de serviços")
    void findAll_Returns200() throws Exception {
        when(serviceService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/v1/services?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100))
                .andExpect(jsonPath("$.content[0].title").value("Chaveiro 24h"));
    }

    @Test
    @DisplayName("GET /api/v1/services/{id} - Deve retornar serviço por ID")
    void findById_Success() throws Exception {
        when(serviceService.findById(100L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/services/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @DisplayName("GET /api/v1/services/{id} - Deve retornar 404 para serviço inexistente")
    void findById_NotFound() throws Exception {
        when(serviceService.findById(999L))
                .thenThrow(new ResourceNotFoundException("Serviço não encontrado: 999"));

        mockMvc.perform(get("/api/v1/services/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/v1/services - Deve criar serviço com sucesso")
    void createService_Success() throws Exception {
        CreateServiceDTO dto = new CreateServiceDTO();
        dto.setTitle("Chaveiro 24h");
        dto.setDescription("Abertura de portas");
        dto.setPrice(new BigDecimal("150.00"));
        dto.setCategory(Service_.ServiceCategory.SERVIÇO_1);

        when(serviceService.createService(any(CreateServiceDTO.class), any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @DisplayName("POST /api/v1/services - Deve retornar 400 quando campos obrigatórios estiverem ausentes")
    void createService_Invalid_Returns400() throws Exception {
        CreateServiceDTO dto = new CreateServiceDTO();
        // Dados vazios

        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/v1/services/{id} - Deve atualizar serviço")
    void updateService_Success() throws Exception {
        UpdateServiceDTO dto = new UpdateServiceDTO();
        dto.setTitle("Chaveiro Atualizado");
        dto.setDescription("Nova descrição");
        dto.setPrice(new BigDecimal("200.00"));
        dto.setCategory(Service_.ServiceCategory.SERVIÇO_2);
        dto.setActive(true);

        when(serviceService.updateService(eq(100L), any(UpdateServiceDTO.class), any())).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/services/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/services/{id} - Deve deletar serviço")
    void deleteService_Success() throws Exception {
        doNothing().when(serviceService).deleteService(eq(100L), any());

        mockMvc.perform(delete("/api/v1/services/100"))
                .andExpect(status().isNoContent());
    }
}
