package com.restaurant.billing_service.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class BillingDocument {
    private UUID id;
    private UUID orderId;
    private UUID restaurantId;
    private LocalDateTime generatedAt;
    private BillingDocumentStatus status;
    private String fileName;
    private String filePath;
    private DocumentType documentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
