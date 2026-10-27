package com.servicehub.servicehubapi.service;

import com.servicehub.servicehubapi.dto.user.ResponseUserDTO;
import com.servicehub.servicehubapi.dto.user.UpdateUserDTO;
import com.servicehub.servicehubapi.exception.EmailAreadyExistsException;
import com.servicehub.servicehubapi.exception.ResourceNotFoundException;
import com.servicehub.servicehubapi.model.Role;
import com.servicehub.servicehubapi.model.User;
import com.servicehub.servicehubapi.dto.user.CreateUserDTO;
import com.servicehub.servicehubapi.repository.UserRepository;
import com.servicehub.servicehubapi.security.SecurityExpressions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static com.servicehub.servicehubapi.util.InputSanitizer.sanitize;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService (UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Cadastro público. Senha armazenada com BCrypt; textos livres sanitizados contra XSS. */
    public ResponseUserDTO createUser(CreateUserDTO dto){
        String email = normalizeEmail(dto.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)){
            throw new EmailAreadyExistsException("Email já cadastrado.");
        }

        User user = new User();
        user.setFullName(sanitize(dto.getFullName()));
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setBio(sanitize(dto.getBio()));
        user.setAvatarUrl(dto.getAvatarUrl());
        // ADMIN nunca é atribuído por aqui (bloqueado também no DTO com @AllowedRoles)
        user.setRole(dto.getRole() == Role.PROVIDER ? Role.PROVIDER : Role.CLIENT);
        
        User savedUser = userRepository.save(user);
        return new ResponseUserDTO(savedUser);
    }

    /** Listagem completa de usuários: somente ADMIN, sempre paginada. */
    @PreAuthorize(SecurityExpressions.ADMIN)
    @Transactional(readOnly = true)
    public Page<ResponseUserDTO> findAll(Pageable pageable){
        return userRepository.findAll(pageable).map(ResponseUserDTO::new);
    }

    /** O próprio usuário ou ADMIN (prevenção de BOLA). */
    @PreAuthorize(SecurityExpressions.ADMIN_OR_SELF)
    @Transactional(readOnly = true)
    public ResponseUserDTO findById(Long id){
        return new ResponseUserDTO(getUser(id));
    }

    @PreAuthorize(SecurityExpressions.ADMIN_OR_SELF)
    public ResponseUserDTO updateUser(Long id, UpdateUserDTO dto){
        User user = getUser(id);
        String email = normalizeEmail(dto.getEmail());

        if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmailIgnoreCase(email)){
            throw new EmailAreadyExistsException("Email já cadastrado.");
        }

        user.setFullName(sanitize(dto.getFullName()));
        user.setEmail(email);
        user.setPhone(dto.getPhone());
        user.setBio(sanitize(dto.getBio()));
        user.setAvatarUrl(dto.getAvatarUrl());

        User savedUser = userRepository.save(user);
        return new ResponseUserDTO(savedUser);
    }

    @PreAuthorize(SecurityExpressions.ADMIN_OR_SELF)
    public void deleteUser(Long id){
        userRepository.delete(getUser(id));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }
}
