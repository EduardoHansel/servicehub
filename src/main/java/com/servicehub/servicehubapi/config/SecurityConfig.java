package com.servicehub.servicehubapi.config;

import com.servicehub.servicehubapi.security.JsonErrorResponseWriter;
import com.servicehub.servicehubapi.security.JwtAuthFilter;
import com.servicehub.servicehubapi.security.JwtService;
import com.servicehub.servicehubapi.security.LegacySha256PasswordEncoder;
import com.servicehub.servicehubapi.security.RateLimitFilter;
import com.servicehub.servicehubapi.security.RateLimitProperties;
import com.servicehub.servicehubapi.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

/**
 * Configuração central de segurança (Semanas 08 e 09).
 * <ul>
 *   <li>API 100% stateless: sem sessão HTTP, autenticação via JWT ({@link JwtAuthFilter});</li>
 *   <li>Senhas com BCrypt (hashes SHA-256 legados são aceitos e migrados no login);</li>
 *   <li>Autorização por rota aqui e granular por método com {@code @PreAuthorize};</li>
 *   <li>Rate limiting ({@link RateLimitFilter}) e CORS restrito por configuração.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(RateLimitProperties.class)
public class SecurityConfig {

    private static final String[] DOCS_PATHS = {"/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   RateLimitProperties rateLimitProperties,
                                                   JsonErrorResponseWriter errorWriter) throws Exception {

        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtService);
        RateLimitFilter rateLimitFilter = new RateLimitFilter(rateLimitProperties, errorWriter);

        http
                // Token trafega no header Authorization (não em cookie) -> CSRF não se aplica
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> errorWriter.write(request, response,
                                HttpStatus.UNAUTHORIZED, "Não autenticado",
                                "Autenticação necessária. Envie o header 'Authorization: Bearer <token>'."))
                        .accessDeniedHandler((request, response, e) -> errorWriter.write(request, response,
                                HttpStatus.FORBIDDEN, "Acesso negado",
                                "Você não tem permissão para acessar este recurso.")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                        .requestMatchers(DOCS_PATHS).permitAll()
                        // GraphQL: a autorização é feita nos resolvers/services via @PreAuthorize
                        .requestMatchers("/graphql", "/graphiql", "/graphiql/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, JwtAuthFilter.class);

        return http.build();
    }

    /**
     * BCrypt como algoritmo padrão para novas senhas ({@code {bcrypt}$2a$...}).
     * Hashes sem prefixo (SHA-256 hex da versão anterior) ainda são validados e
     * {@code upgradeEncoding} sinaliza a migração automática para BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        LegacySha256PasswordEncoder legacy = new LegacySha256PasswordEncoder();
        DelegatingPasswordEncoder encoder = new DelegatingPasswordEncoder("bcrypt",
                Map.of("bcrypt", new BCryptPasswordEncoder(), "sha256", legacy));
        encoder.setDefaultPasswordEncoderForMatches(legacy);
        return encoder;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsServiceImpl userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsPasswordService(userDetailsService);
        return new ProviderManager(provider);
    }

    /**
     * CORS explícito: apenas as origens configuradas em {@code app.cors.allowed-origins},
     * sem wildcard e sem credenciais (o JWT vai no header, não em cookies).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("X-Rate-Limit-Remaining", "Retry-After"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/graphql", config);
        return source;
    }
}
