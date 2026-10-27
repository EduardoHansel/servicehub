package com.servicehub.servicehubapi.repository;

import com.servicehub.servicehubapi.model.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
}
