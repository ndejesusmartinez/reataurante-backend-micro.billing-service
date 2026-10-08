package com.restaurant.billing_service.presentation.controller;

import com.restaurant.billing_service.application.service.BillingService;
import com.restaurant.billing_service.domain.model.BillingDocument;
import com.restaurant.billing_service.domain.model.BillingDocumentStatus;
import com.restaurant.billing_service.infrastructure.report.JasperReportService;
import com.restaurant.billing_service.presentation.dto.BillingDocumentResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final JasperReportService jasperReportService;
    private final BillingService billingService;

    public BillingController(
            JasperReportService jasperReportService,
            BillingService billingService
    ) {
        this.jasperReportService = jasperReportService;
        this.billingService = billingService;
    }

    @GetMapping("/test-pdf")
    public ResponseEntity<byte[]> generateTestPdf() {
        byte[] pdf = jasperReportService.generateTestReceipt();
        return buildPdfResponse("comprobante-prueba.pdf", pdf);
    }

    @PostMapping("/orders/{orderId}/receipt")
    public ResponseEntity<byte[]> generateReceipt(@PathVariable UUID orderId) {
        BillingDocument document = billingService.generateReceipt(orderId);
        byte[] pdf = billingService.downloadPdf(document.getId());
        return buildPdfResponse(document.getFileName(), pdf);
    }

    @GetMapping("/{billingId}")
    public ResponseEntity<BillingDocumentResponse> getById(@PathVariable UUID billingId) {
        BillingDocument document = billingService.findById(billingId);
        return ResponseEntity.ok(toResponse(document));
    }

    @GetMapping("/orders/{orderId}/receipts")
    public ResponseEntity<List<BillingDocumentResponse>> getByOrder(@PathVariable UUID orderId) {
        List<BillingDocumentResponse> documents = billingService.findByOrderId(orderId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{billingId}/status")
    public ResponseEntity<BillingDocumentStatus> getStatus(@PathVariable UUID billingId) {
        return ResponseEntity.ok(billingService.getStatus(billingId));
    }

    @GetMapping("/{billingId}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID billingId) {
        BillingDocument document = billingService.findById(billingId);
        byte[] pdf = billingService.downloadPdf(billingId);
        return buildPdfResponse(document.getFileName(), pdf);
    }

    private ResponseEntity<byte[]> buildPdfResponse(String fileName, byte[] pdf) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename(fileName == null ? "comprobante.pdf" : fileName).build());
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    private BillingDocumentResponse toResponse(BillingDocument document) {
        return new BillingDocumentResponse(
                document.getId(),
                document.getOrderId(),
                document.getRestaurantId(),
                document.getGeneratedAt(),
                document.getStatus(),
                document.getFileName(),
                document.getDocumentType(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}