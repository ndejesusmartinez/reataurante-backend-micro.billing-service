package com.restaurant.billing_service.infrastructure.persistence.entity;

import com.restaurant.billing_service.domain.model.BillingDocument;
import com.restaurant.billing_service.domain.model.BillingDocumentStatus;
import com.restaurant.billing_service.domain.model.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "billing_documents",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_billing_order_type", columnNames = {"order_id", "document_type"})
        }
)
@Getter
@Setter
public class BillingDocumentEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "restaurant_id")
    private UUID restaurantId;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingDocumentStatus status;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static BillingDocumentEntity fromDomain(BillingDocument document) {
        BillingDocumentEntity entity = new BillingDocumentEntity();
        entity.setId(document.getId());
        entity.setOrderId(document.getOrderId());
        entity.setRestaurantId(document.getRestaurantId());
        entity.setGeneratedAt(document.getGeneratedAt());
        entity.setStatus(document.getStatus());
        entity.setFileName(document.getFileName());
        entity.setFilePath(document.getFilePath());
        entity.setDocumentType(document.getDocumentType());
        entity.setCreatedAt(document.getCreatedAt());
        entity.setUpdatedAt(document.getUpdatedAt());
        return entity;
    }

    public BillingDocument toDomain() {
        return BillingDocument.builder()
                .id(this.id)
                .orderId(this.orderId)
                .restaurantId(this.restaurantId)
                .generatedAt(this.generatedAt)
                .status(this.status)
                .fileName(this.fileName)
                .filePath(this.filePath)
                .documentType(this.documentType)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
