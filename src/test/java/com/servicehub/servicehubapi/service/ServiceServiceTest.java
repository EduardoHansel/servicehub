package com.servicehub.servicehubapi.service;

import com.servicehub.servicehubapi.dto.service.CreateServiceDTO;
import com.servicehub.servicehubapi.dto.service.ResponseServiceDTO;
import com.servicehub.servicehubapi.dto.service.UpdateServiceDTO;
import com.servicehub.servicehubapi.exception.BusinessRuleException;
import com.servicehub.servicehubapi.exception.ResourceNotFoundException;
import com.servicehub.servicehubapi.model.Role;
import com.servicehub.servicehubapi.model.Service_;
import com.servicehub.servicehubapi.model.User;
import com.servicehub.servicehubapi.repository.ServiceRepository;
import com.servicehub.servicehubapi.repository.UserRepository;
import com.servicehub.servicehubapi.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - ServiceService")
class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ServiceService serviceService;

    private User providerUser;
    private User clientUser;
    private Service_ sampleService;
    private UserPrincipal providerPrincipal;
    private UserPrincipal adminPrincipal;
    private UserPrincipal anotherProviderPrincipal;

    @BeforeEach
    void setUp() {
        providerUser = new User();
        providerUser.setId(10L);
        providerUser.setFullName("Carlos Prestador");
        providerUser.setEmail("carlos@example.com");
        providerUser.setRole(Role.PROVIDER);
        providerUser.setActive(true);

        clientUser = new User();
        clientUser.setId(20L);
        clientUser.setFullName("Ana Cliente");
        clientUser.setEmail("ana@example.com");
        clientUser.setRole(Role.CLIENT);
        clientUser.setActive(true);

        sampleService = new Service_();
        sampleService.setId(100L);
        sampleService.setTitle("Instalação Elétrica");
        sampleService.setDescription("Serviço de instalação");
        sampleService.setPrice(new BigDecimal("250.00"));
        sampleService.setCategory(Service_.ServiceCategory.SERVIÇO_1);
        sampleService.setProvider(providerUser);
        sampleService.setActive(true);

        providerPrincipal = UserPrincipal.from(providerUser);
        adminPrincipal = UserPrincipal.fromToken(999L, "admin@servicehub.com", Role.ADMIN);
        anotherProviderPrincipal = UserPrincipal.fromToken(30L, "outro@example.com", Role.PROVIDER);
    }

    @Test
    @DisplayName("Deve criar serviço com sucesso pelo próprio prestador")
    void createService_Success() {
        CreateServiceDTO dto = new CreateServiceDTO();
        dto.setTitle("Conserto Elétrico");
        dto.setDescription("Descrição");
        dto.setPrice(new BigDecimal("150.00"));
        dto.setCategory(Service_.ServiceCategory.SERVIÇO_1);

        when(userRepository.findById(10L)).thenReturn(Optional.of(providerUser));
        when(serviceRepository.save(any(Service_.class))).thenAnswer(i -> {
            Service_ s = i.getArgument(0);
            s.setId(101L);
            return s;
        });

        ResponseServiceDTO response = serviceService.createService(dto, providerPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(101L);
        assertThat(response.getTitle()).isEqualTo("Conserto Elétrico");
        verify(serviceRepository, times(1)).save(any(Service_.class));
    }

    @Test
    @DisplayName("Prestador não deve conseguir criar serviço para outro usuário (BFLA/BOLA)")
    void createService_ThrowsAccessDenied_WhenCreatingForOther() {
        CreateServiceDTO dto = new CreateServiceDTO();
        dto.setTitle("Teste");
        dto.setDescription("Desc");
        dto.setPrice(new BigDecimal("100.00"));
        dto.setCategory(Service_.ServiceCategory.SERVIÇO_1);
        dto.setProvider(50L); // ID diferente do principal

        assertThatThrownBy(() -> serviceService.createService(dto, providerPrincipal))
                .isInstanceOf(AccessDeniedException.class);

        verify(serviceRepository, never()).save(any());
    }

    @Test
    @DisplayName("ADMIN pode criar serviço indicando outro prestador")
    void createService_AdminCanCreateForOtherProvider() {
        CreateServiceDTO dto = new CreateServiceDTO();
        dto.setTitle("Serviço via Admin");
        dto.setDescription("Desc");
        dto.setPrice(new BigDecimal("100.00"));
        dto.setCategory(Service_.ServiceCategory.SERVIÇO_1);
        dto.setProvider(10L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(providerUser));
        when(serviceRepository.save(any(Service_.class))).thenAnswer(i -> {
            Service_ s = i.getArgument(0);
            s.setId(102L);
            return s;
        });

        ResponseServiceDTO response = serviceService.createService(dto, adminPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(102L);
        verify(serviceRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao vincular serviço a um usuário que não é PROVIDER")
    void createService_FailsWhenUserIsNotProvider() {
        CreateServiceDTO dto = new CreateServiceDTO();
        dto.setTitle("Serviço");
        dto.setDescription("Desc");
        dto.setPrice(new BigDecimal("100.00"));
        dto.setCategory(Service_.ServiceCategory.SERVIÇO_1);
        dto.setProvider(20L); // clientUser

        when(userRepository.findById(20L)).thenReturn(Optional.of(clientUser));

        assertThatThrownBy(() -> serviceService.createService(dto, adminPrincipal))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("não é um prestador de serviços");

        verify(serviceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar serviços paginados")
    void findAll_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Service_> page = new PageImpl<>(List.of(sampleService), pageable, 1);
        when(serviceRepository.findAll(pageable)).thenReturn(page);

        Page<ResponseServiceDTO> result = serviceService.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Instalação Elétrica");
        verify(serviceRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar serviço por ID")
    void findById_Success() {
        when(serviceRepository.findById(100L)).thenReturn(Optional.of(sampleService));

        ResponseServiceDTO response = serviceService.findById(100L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para serviço inexistente")
    void findById_NotFound() {
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Proprietário deve conseguir atualizar seu serviço")
    void updateService_Success_ByOwner() {
        UpdateServiceDTO dto = new UpdateServiceDTO();
        dto.setTitle("Título Novo");
        dto.setDescription("Descrição Nova");
        dto.setPrice(new BigDecimal("300.00"));
        dto.setCategory(Service_.ServiceCategory.SERVIÇO_2);
        dto.setActive(true);

        when(serviceRepository.findById(100L)).thenReturn(Optional.of(sampleService));
        when(serviceRepository.save(any(Service_.class))).thenReturn(sampleService);

        ResponseServiceDTO response = serviceService.updateService(100L, dto, providerPrincipal);

        assertThat(response).isNotNull();
        assertThat(sampleService.getTitle()).isEqualTo("Título Novo");
        verify(serviceRepository, times(1)).save(sampleService);
    }

    @Test
    @DisplayName("Outro prestador não pode alterar serviço alheio (BOLA)")
    void updateService_ThrowsAccessDenied_WhenNotOwner() {
        UpdateServiceDTO dto = new UpdateServiceDTO();
        dto.setTitle("Invasão");
        dto.setDescription("Desc");
        dto.setPrice(new BigDecimal("1.00"));
        dto.setCategory(Service_.ServiceCategory.SERVIÇO_1);
        dto.setActive(true);

        when(serviceRepository.findById(100L)).thenReturn(Optional.of(sampleService));

        assertThatThrownBy(() -> serviceService.updateService(100L, dto, anotherProviderPrincipal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("só pode alterar serviços que você oferece");

        verify(serviceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Proprietário deve conseguir deletar seu serviço")
    void deleteService_Success_ByOwner() {
        when(serviceRepository.findById(100L)).thenReturn(Optional.of(sampleService));
        doNothing().when(serviceRepository).delete(sampleService);

        serviceService.deleteService(100L, providerPrincipal);

        verify(serviceRepository, times(1)).delete(sampleService);
    }

    @Test
    @DisplayName("Não proprietário não pode deletar serviço (BOLA)")
    void deleteService_ThrowsAccessDenied_WhenNotOwner() {
        when(serviceRepository.findById(100L)).thenReturn(Optional.of(sampleService));

        assertThatThrownBy(() -> serviceService.deleteService(100L, anotherProviderPrincipal))
                .isInstanceOf(AccessDeniedException.class);

        verify(serviceRepository, never()).delete(any());
    }
}
