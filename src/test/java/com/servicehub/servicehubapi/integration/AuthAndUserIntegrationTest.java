package com.servicehub.servicehubapi.integration;

import com.servicehub.servicehubapi.dto.auth.LoginRequestDTO;
import com.servicehub.servicehubapi.dto.auth.LoginResponseDTO;
import com.servicehub.servicehubapi.dto.user.CreateUserDTO;
import com.servicehub.servicehubapi.dto.user.ResponseUserDTO;
import com.servicehub.servicehubapi.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração ponta a ponta (E2E) com servidor HTTP real em RANDOM_PORT
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@DisplayName("Testes de Integração Ponta a Ponta - Auth e Users")
class AuthAndUserIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Fluxo completo: Cadastrar usuário via POST -> Fazer login com JWT -> Acessar recurso protegido")
    void fullAuthFlow() {
        // 1. Cadastra um novo usuário
        CreateUserDTO createDTO = new CreateUserDTO();
        createDTO.setFullName("Cliente Integracao");
        createDTO.setEmail("integracao@servicehub.com");
        createDTO.setPassword("senhaSegura123");
        createDTO.setPhone("11999991111");
        createDTO.setRole(Role.CLIENT);

        ResponseEntity<ResponseUserDTO> createResp = restTemplate.postForEntity(
                "/api/v1/users", createDTO, ResponseUserDTO.class);

        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResp.getBody()).isNotNull();
        Long createdUserId = createResp.getBody().getId();
        assertThat(createdUserId).isPositive();

        // 2. Realiza login e obtém token JWT
        LoginRequestDTO loginDTO = new LoginRequestDTO("integracao@servicehub.com", "senhaSegura123");
        ResponseEntity<LoginResponseDTO> loginResp = restTemplate.postForEntity(
                "/api/auth/login", loginDTO, LoginResponseDTO.class);

        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody()).isNotNull();
        String token = loginResp.getBody().accessToken();
        assertThat(token).isNotBlank();

        // 3. Tenta acessar rota protegida SEM token (deve retornar 401)
        ResponseEntity<String> unauthResp = restTemplate.getForEntity(
                "/api/v1/users/" + createdUserId, String.class);
        assertThat(unauthResp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // 4. Acessa a rota protegida COM o token JWT no cabeçalho Authorization
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ResponseUserDTO> authResp = restTemplate.exchange(
                "/api/v1/users/" + createdUserId, HttpMethod.GET, entity, ResponseUserDTO.class);

        assertThat(authResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(authResp.getBody()).isNotNull();
        assertThat(authResp.getBody().getEmail()).isEqualTo("integracao@servicehub.com");
    }
}
