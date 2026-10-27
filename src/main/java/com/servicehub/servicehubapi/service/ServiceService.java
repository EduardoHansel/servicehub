package com.servicehub.servicehubapi.service;

import com.servicehub.servicehubapi.dto.service.CreateServiceDTO;
import com.servicehub.servicehubapi.dto.service.ResponseServiceDTO;
import com.servicehub.servicehubapi.dto.service.UpdateServiceDTO;
import com.servicehub.servicehubapi.exception.BusinessRuleException;
import com.servicehub.servicehubapi.exception.ResourceNotFoundException;
import com.servicehub.servicehubapi.model.Role;
import com.servicehub.servicehubapi.model.User;
import com.servicehub.servicehubapi.repository.ServiceRepository;
import com.servicehub.servicehubapi.model.Service_;
import com.servicehub.servicehubapi.repository.UserRepository;
import com.servicehub.servicehubapi.security.SecurityExpressions;
import com.servicehub.servicehubapi.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.servicehub.servicehubapi.util.InputSanitizer.sanitize;

@Service
@Transactional
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    public ServiceService(ServiceRepository serviceRepository, UserRepository userRepository) {
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
    }

    /**
     * PROVIDER cadastra serviços apenas em seu próprio nome; ADMIN pode informar qualquer provedor.
     */
    @PreAuthorize(SecurityExpressions.ADMIN_OR_PROVIDER)
    public ResponseServiceDTO createService(CreateServiceDTO dto, UserPrincipal actor) {
        Long providerId = dto.getProvider() != null ? dto.getProvider() : actor.id();
        if (!actor.isAdmin() && !Objects.equals(providerId, actor.id())) {
            throw new AccessDeniedException("Prestadores só podem cadastrar serviços em seu próprio nome.");
        }

        Service_ service = new Service_();
        service.setTitle(sanitize(dto.getTitle()));
        service.setDescription(sanitize(dto.getDescription()));
        service.setPrice(dto.getPrice());
        service.setCategory(dto.getCategory());
        service.setProvider(getProvider(providerId));

        Service_ createdService = serviceRepository.save(service);
        return new ResponseServiceDTO(createdService);
    }

    @PreAuthorize(SecurityExpressions.AUTHENTICATED)
    @Transactional(readOnly = true)
    public Page<ResponseServiceDTO> findAll(Pageable pageable) {
        return serviceRepository.findAll(pageable).map(ResponseServiceDTO::new);
    }

    @PreAuthorize(SecurityExpressions.AUTHENTICATED)
    @Transactional(readOnly = true)
    public ResponseServiceDTO findById(long id) {
        return new ResponseServiceDTO(getService(id));
    }

    /** Somente o provedor dono do serviço ou ADMIN (prevenção de BOLA). */
    @PreAuthorize(SecurityExpressions.ADMIN_OR_PROVIDER)
    public ResponseServiceDTO updateService(long id, UpdateServiceDTO dto, UserPrincipal actor) {
        Service_ service = getService(id);
        assertCanModify(service, actor);

        if (dto.getProvider() != null && !Objects.equals(dto.getProvider(), service.getProvider().getId())) {
            if (!actor.isAdmin()) {
                throw new AccessDeniedException("Somente administradores podem transferir um serviço.");
            }
            service.setProvider(getProvider(dto.getProvider()));
        }

        service.setTitle(sanitize(dto.getTitle()));
        service.setDescription(sanitize(dto.getDescription()));
        service.setPrice(dto.getPrice());
        service.setCategory(dto.getCategory());
        service.setActive(dto.getActive());

        Service_ savedService = serviceRepository.save(service);
        return new ResponseServiceDTO(savedService);
    }

    @PreAuthorize(SecurityExpressions.ADMIN_OR_PROVIDER)
    public void deleteService(long id, UserPrincipal actor) {
        Service_ service = getService(id);
        assertCanModify(service, actor);

        serviceRepository.delete(service);
    }

    private void assertCanModify(Service_ service, UserPrincipal actor) {
        if (!actor.isAdmin() && !Objects.equals(service.getProvider().getId(), actor.id())) {
            throw new AccessDeniedException("Você só pode alterar serviços que você oferece.");
        }
    }

    private Service_ getService(long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado: " + id));
    }

    private User getProvider(Long providerId) {
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + providerId));
        if (provider.getRole() != Role.PROVIDER) {
            throw new BusinessRuleException("O usuário informado não é um prestador de serviços (PROVIDER).");
        }
        return provider;
    }
}
