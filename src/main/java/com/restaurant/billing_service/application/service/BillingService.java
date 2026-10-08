package com.restaurant.billing_service.application.service;

import com.restaurant.billing_service.domain.model.BillingDocument;
import com.restaurant.billing_service.domain.model.BillingDocumentStatus;
import com.restaurant.billing_service.domain.model.DocumentType;
import com.restaurant.billing_service.infrastructure.client.OrderServiceClient;
import com.restaurant.billing_service.infrastructure.client.RestaurantServiceClient;
import com.restaurant.billing_service.infrastructure.persistence.entity.BillingDocumentEntity;
import com.restaurant.billing_service.infrastructure.persistence.repository.BillingDocumentRepository;
import com.restaurant.billing_service.infrastructure.report.JasperReportService;
import com.restaurant.billing_service.infrastructure.storage.BillingStorageService;
import com.restaurant.billing_service.presentation.exception.InvalidRequestException;
import com.restaurant.billing_service.presentation.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BillingService {

    private final OrderServiceClient orderServiceClient;
    private final RestaurantServiceClient restaurantServiceClient;
    private final JasperReportService jasperReportService;
    private final BillingDocumentRepository billingDocumentRepository;
    private final BillingStorageService billingStorageService;

    public BillingService(
            OrderServiceClient orderServiceClient,
            RestaurantServiceClient restaurantServiceClient,
            JasperReportService jasperReportService,
            BillingDocumentRepository billingDocumentRepository,
            BillingStorageService billingStorageService
    ) {
        this.orderServiceClient = orderServiceClient;
        this.restaurantServiceClient = restaurantServiceClient;
        this.jasperReportService = jasperReportService;
        this.billingDocumentRepository = billingDocumentRepository;
        this.billingStorageService = billingStorageService;
    }

    @Transactional
    public BillingDocument generateReceipt(UUID orderId) {
        if (orderId == null) {
            throw new InvalidRequestException("El identificador del pedido es obligatorio.");
        }

        var existing = billingDocumentRepository.findByOrderIdAndDocumentType(orderId, DocumentType.ORDER_RECEIPT);
        if (existing.isPresent() && existing.get().getStatus() == BillingDocumentStatus.COMPLETED) {
            return existing.get().toDomain();
        }

        OrderReceiptData orderReceiptData = orderServiceClient.fetchOrder(orderId);
        String restaurantName = restaurantServiceClient.findRestaurantName(orderReceiptData.restaurantId())
                .orElse("Restaurante no informado");

        OrderReceiptData merged = new OrderReceiptData(
                orderReceiptData.orderId(),
                restaurantName,
                orderReceiptData.restaurantId(),
                orderReceiptData.customerId(),
                orderReceiptData.orderDate(),
                normalizeOrderType(orderReceiptData.orderType()),
                orderReceiptData.total(),
                orderReceiptData.items()
        );

        byte[] pdf = jasperReportService.generateReceipt(merged);
        String billingId = UUID.randomUUID().toString();
        BillingStorageService.StoredBillingDocument stored = billingStorageService.store(UUID.fromString(billingId), pdf);

        LocalDateTime now = LocalDateTime.now();
        BillingDocumentEntity entity = new BillingDocumentEntity();
        entity.setId(UUID.fromString(billingId));
        entity.setOrderId(orderId);
        entity.setRestaurantId(orderReceiptData.restaurantId());
        entity.setGeneratedAt(now);
        entity.setStatus(BillingDocumentStatus.COMPLETED);
        entity.setFileName(stored.fileName());
        entity.setFilePath(stored.filePath());
        entity.setDocumentType(DocumentType.ORDER_RECEIPT);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        BillingDocumentEntity saved = billingDocumentRepository.save(entity);
        return saved.toDomain();
    }

    public BillingDocument findById(UUID billingId) {
        return billingDocumentRepository.findById(billingId)
                .map(BillingDocumentEntity::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un comprobante con ese identificador."));
    }

    public List<BillingDocument> findByOrderId(UUID orderId) {
        return billingDocumentRepository.findByOrderId(orderId)
                .stream()
                .map(BillingDocumentEntity::toDomain)
                .toList();
    }

    public byte[] downloadPdf(UUID billingId) {
        BillingDocument document = findById(billingId);
        if (document.getFilePath() == null || document.getFilePath().isBlank()) {
            throw new ResourceNotFoundException("El comprobante no tiene archivo PDF asociado.");
        }
        return billingStorageService.load(document.getFilePath());
    }

    public BillingDocumentStatus getStatus(UUID billingId) {
        return findById(billingId).getStatus();
    }

    private String normalizeOrderType(String orderType) {
        if (orderType == null || orderType.isBlank()) {
            return "Delivery";
        }

        return switch (orderType.trim().toUpperCase()) {
            case "TABLE", "MESA", "CONSUMO_EN_MESA" -> "Consumo en mesa";
            case "TAKEAWAY", "PARA_LLEVAR", "TAKE_AWAY" -> "Para llevar";
            case "DELIVERY", "DOMICILIO" -> "Domicilio";
            default -> orderType;
        };
    }
}
