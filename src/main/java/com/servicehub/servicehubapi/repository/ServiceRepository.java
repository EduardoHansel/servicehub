package com.servicehub.servicehubapi.repository;

import com.servicehub.servicehubapi.model.Service_;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service_, Long> {

    /** Carrega o provedor junto (evita N+1 ao montar o DTO de resposta). */
    @Override
    @EntityGraph(attributePaths = "provider")
    Page<Service_> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "provider")
    Optional<Service_> findById(Long id);
}
