package com.sujit.pdf_service.repository;

import com.sujit.pdf_service.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {

    Optional<DocumentEntity> findFirstByOrderIdOrderByCreatedAtDesc(UUID orderId);

}
