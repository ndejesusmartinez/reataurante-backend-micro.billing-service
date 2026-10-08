package com.restaurant.billing_service.infrastructure.persistence.repository;

import com.restaurant.billing_service.domain.model.DocumentType;
import com.restaurant.billing_service.infrastructure.persistence.entity.BillingDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingDocumentRepository extends JpaRepository<BillingDocumentEntity, UUID> {
    Optional<BillingDocumentEntity> findByOrderIdAndDocumentType(UUID orderId, DocumentType documentType);
    List<BillingDocumentEntity> findByOrderId(UUID orderId);
}
