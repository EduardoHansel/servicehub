package com.servicehub.servicehubapi.controller;

import com.servicehub.servicehubapi.dto.service.CreateServiceDTO;
import com.servicehub.servicehubapi.dto.service.ResponseServiceDTO;
import com.servicehub.servicehubapi.dto.user.CreateUserDTO;
import com.servicehub.servicehubapi.dto.user.ResponseUserDTO;
import com.servicehub.servicehubapi.model.Role;
import com.servicehub.servicehubapi.model.Service_;
import com.servicehub.servicehubapi.model.User;
import com.servicehub.servicehubapi.security.UserPrincipal;
import com.servicehub.servicehubapi.service.ServiceService;
import com.servicehub.servicehubapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - GraphQLController")
class GraphQLControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ServiceService serviceService;

    @InjectMocks
    private GraphQLController graphQLController;

    private ResponseUserDTO sampleUser;
    private ResponseServiceDTO sampleService;
    private UserPrincipal actor;

    @BeforeEach
    void setUp() {
        User u = new User();
        u.setId(1L);
        u.setFullName("User Teste");
        u.setEmail("test@test.com");
        u.setRole(Role.CLIENT);
        sampleUser = new ResponseUserDTO(u);

        Service_ s = new Service_();
        s.setId(10L);
        s.setTitle("Serviço GraphQL");
        s.setDescription("Desc");
        s.setPrice(new BigDecimal("120.00"));
        s.setCategory(Service_.ServiceCategory.SERVIÇO_1);
        s.setProvider(u);
        sampleService = new ResponseServiceDTO(s);

        actor = UserPrincipal.fromToken(1L, "test@test.com", Role.CLIENT);
    }

    @Test
    @DisplayName("Query users - Deve retornar lista de usuários")
    void usersQuery() {
        when(userService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(sampleUser)));

        List<ResponseUserDTO> result = graphQLController.users(0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("Query userById - Deve retornar usuário por ID")
    void userByIdQuery() {
        when(userService.findById(1L)).thenReturn(sampleUser);

        ResponseUserDTO result = graphQLController.userById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Query services - Deve retornar lista de serviços")
    void servicesQuery() {
        when(serviceService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(sampleService)));

        List<ResponseServiceDTO> result = graphQLController.services(0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Serviço GraphQL");
    }

    @Test
    @DisplayName("Query serviceById - Deve retornar serviço por ID")
    void serviceByIdQuery() {
        when(serviceService.findById(10L)).thenReturn(sampleService);

        ResponseServiceDTO result = graphQLController.serviceById(10L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Mutation createUser - Deve criar usuário")
    void createUserMutation() {
        Map<String, Object> input = Map.of(
                "fullName", "Novo Usuário",
                "email", "novo@teste.com",
                "password", "senha123",
                "role", "CLIENT"
        );
        when(userService.createUser(any(CreateUserDTO.class))).thenReturn(sampleUser);

        ResponseUserDTO result = graphQLController.createUser(input);

        assertThat(result).isNotNull();
        verify(userService, times(1)).createUser(any(CreateUserDTO.class));
    }

    @Test
    @DisplayName("Mutation createService - Deve criar serviço")
    void createServiceMutation() {
        Map<String, Object> input = Map.of(
                "title", "Novo Serviço",
                "description", "Nova descrição",
                "price", 150.0,
                "category", "SERVIÇO_1",
                "provider", 1L
        );
        when(serviceService.createService(any(CreateServiceDTO.class), eq(actor))).thenReturn(sampleService);

        ResponseServiceDTO result = graphQLController.createService(input, actor);

        assertThat(result).isNotNull();
        verify(serviceService, times(1)).createService(any(CreateServiceDTO.class), eq(actor));
    }
}
