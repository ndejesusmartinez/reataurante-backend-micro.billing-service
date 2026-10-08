package com.restaurant.billing_service.infrastructure.persistence.adapter;

import com.restaurant.billing_service.domain.model.BillingDocument;
import com.restaurant.billing_service.domain.model.DocumentType;
import com.restaurant.billing_service.domain.port.BillingDocumentPort;
import com.restaurant.billing_service.infrastructure.persistence.entity.BillingDocumentEntity;
import com.restaurant.billing_service.infrastructure.persistence.repository.BillingDocumentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BillingDocumentPersistenceAdapter implements BillingDocumentPort {

    private final BillingDocumentRepository repository;

    public BillingDocumentPersistenceAdapter(BillingDocumentRepository repository) {
        this.repository = repository;
    }

    @Override
    public BillingDocument save(BillingDocument billingDocument) {
        BillingDocumentEntity entity = BillingDocumentEntity.fromDomain(billingDocument);
        return repository.save(entity).toDomain();
    }

    @Override
    public Optional<BillingDocument> findById(UUID billingId) {
        return repository.findById(billingId).map(BillingDocumentEntity::toDomain);
    }

    @Override
    public Optional<BillingDocument> findByOrderIdAndDocumentType(UUID orderId, DocumentType documentType) {
        return repository.findByOrderIdAndDocumentType(orderId, documentType).map(BillingDocumentEntity::toDomain);
    }

    @Override
    public List<BillingDocument> findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId).stream().map(BillingDocumentEntity::toDomain).toList();
    }
}
