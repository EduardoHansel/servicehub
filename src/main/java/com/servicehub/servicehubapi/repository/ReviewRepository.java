package com.servicehub.servicehubapi.repository;

import com.servicehub.servicehubapi.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
