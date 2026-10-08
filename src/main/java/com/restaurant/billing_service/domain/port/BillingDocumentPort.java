package com.restaurant.billing_service.domain.port;

import com.restaurant.billing_service.domain.model.BillingDocument;
import com.restaurant.billing_service.domain.model.DocumentType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingDocumentPort {
    BillingDocument save(BillingDocument billingDocument);
    Optional<BillingDocument> findById(UUID billingId);
    Optional<BillingDocument> findByOrderIdAndDocumentType(UUID orderId, DocumentType documentType);
    List<BillingDocument> findByOrderId(UUID orderId);
}
