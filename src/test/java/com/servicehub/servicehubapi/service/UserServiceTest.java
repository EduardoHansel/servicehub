package com.servicehub.servicehubapi.service;

import com.servicehub.servicehubapi.dto.user.CreateUserDTO;
import com.servicehub.servicehubapi.dto.user.ResponseUserDTO;
import com.servicehub.servicehubapi.dto.user.UpdateUserDTO;
import com.servicehub.servicehubapi.exception.EmailAreadyExistsException;
import com.servicehub.servicehubapi.exception.ResourceNotFoundException;
import com.servicehub.servicehubapi.model.Role;
import com.servicehub.servicehubapi.model.User;
import com.servicehub.servicehubapi.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setFullName("João da Silva");
        sampleUser.setEmail("joao@example.com");
        sampleUser.setPasswordHash("$2a$10$hashedPassword");
        sampleUser.setPhone("11999998888");
        sampleUser.setBio("Bio de teste");
        sampleUser.setAvatarUrl("https://example.com/avatar.jpg");
        sampleUser.setRole(Role.CLIENT);
        sampleUser.setActive(true);
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso e criptografar a senha")
    void createUser_Success() {
        // Arrange
        CreateUserDTO dto = new CreateUserDTO();
        dto.setFullName("Maria Souza");
        dto.setEmail("maria@example.com");
        dto.setPassword("senha123");
        dto.setPhone("11988887777");
        dto.setBio("Bio");
        dto.setRole(Role.PROVIDER);

        when(userRepository.existsByEmailIgnoreCase("maria@example.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });

        // Act
        ResponseUserDTO response = userService.createUser(dto);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getEmail()).isEqualTo("maria@example.com");
        assertThat(response.getRole()).isEqualTo(Role.PROVIDER);

        verify(userRepository, times(1)).existsByEmailIgnoreCase("maria@example.com");
        verify(passwordEncoder, times(1)).encode("senha123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar EmailAreadyExistsException ao tentar cadastrar email já existente")
    void createUser_EmailAlreadyExists_ThrowsException() {
        CreateUserDTO dto = new CreateUserDTO();
        dto.setEmail("joao@example.com");
        dto.setPassword("senha123");

        when(userRepository.existsByEmailIgnoreCase("joao@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(EmailAreadyExistsException.class)
                .hasMessageContaining("Email já cadastrado.");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar usuários de forma paginada")
    void findAll_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(sampleUser), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<ResponseUserDTO> result = userService.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("joao@example.com");
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void findById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        ResponseUserDTO response = userService.findById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFullName()).isEqualTo("João da Silva");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando ID não existir")
    void findById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado: 99");

        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso")
    void updateUser_Success() {
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setFullName("João Atualizado");
        dto.setEmail("joao.novo@example.com");
        dto.setPhone("11977776666");
        dto.setBio("Nova bio");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.existsByEmailIgnoreCase("joao.novo@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        ResponseUserDTO response = userService.updateUser(1L, dto);

        assertThat(response).isNotNull();
        assertThat(sampleUser.getFullName()).isEqualTo("João Atualizado");
        assertThat(sampleUser.getEmail()).isEqualTo("joao.novo@example.com");
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    @DisplayName("Deve falhar ao atualizar se novo email já pertencer a outro usuário")
    void updateUser_EmailConflict_ThrowsException() {
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setEmail("outro@example.com");
        dto.setFullName("João");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.existsByEmailIgnoreCase("outro@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, dto))
                .isInstanceOf(EmailAreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar usuário com sucesso")
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        doNothing().when(userRepository).delete(sampleUser);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).delete(sampleUser);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar deletar usuário inexistente")
    void deleteUser_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).delete(any());
    }
}
