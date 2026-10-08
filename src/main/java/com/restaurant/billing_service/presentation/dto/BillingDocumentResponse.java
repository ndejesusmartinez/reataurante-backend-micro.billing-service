package com.restaurant.billing_service.presentation.dto;

import com.restaurant.billing_service.domain.model.BillingDocumentStatus;
import com.restaurant.billing_service.domain.model.DocumentType;

import java.time.LocalDateTime;
import java.util.UUID;

public record BillingDocumentResponse(
        UUID billingId,
        UUID orderId,
        UUID restaurantId,
        LocalDateTime generatedAt,
        BillingDocumentStatus status,
        String fileName,
        DocumentType documentType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
