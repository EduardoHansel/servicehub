package com.servicehub.servicehubapi.security;

import com.servicehub.servicehubapi.model.Role;
import com.servicehub.servicehubapi.model.User;
import com.servicehub.servicehubapi.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - UserDetailsServiceImpl")
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Deve carregar UserDetails com sucesso pelo email")
    void loadUserByUsername_Success() {
        User u = new User();
        u.setId(1L);
        u.setEmail("test@servicehub.com");
        u.setPasswordHash("$2a$10$xyz");
        u.setRole(Role.ADMIN);
        u.setActive(true);

        when(userRepository.findByEmailIgnoreCase("test@servicehub.com")).thenReturn(Optional.of(u));

        UserDetails details = userDetailsService.loadUserByUsername("test@servicehub.com");

        assertThat(details).isNotNull();
        assertThat(details.getUsername()).isEqualTo("test@servicehub.com");
        assertThat(details.getAuthorities()).hasSize(1);
        assertThat(details.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando usuário não for encontrado")
    void loadUserByUsername_NotFound() {
        when(userRepository.findByEmailIgnoreCase("none@servicehub.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("none@servicehub.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("Deve atualizar senha com sucesso")
    void updatePassword_Success() {
        User u = new User();
        u.setId(1L);
        u.setEmail("user@servicehub.com");
        u.setPasswordHash("oldHash");
        u.setRole(Role.CLIENT);
        u.setActive(true);

        UserPrincipal principal = UserPrincipal.from(u);

        when(userRepository.findByEmailIgnoreCase("user@servicehub.com")).thenReturn(Optional.of(u));
        when(userRepository.save(any(User.class))).thenReturn(u);

        UserDetails updated = userDetailsService.updatePassword(principal, "$2a$10$newBcryptHash");

        assertThat(updated).isNotNull();
        assertThat(u.getPasswordHash()).isEqualTo("$2a$10$newBcryptHash");
        verify(userRepository, times(1)).save(u);
    }
}
