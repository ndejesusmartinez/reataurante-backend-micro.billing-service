package com.restaurant.billing_service.infrastructure.report;

import com.restaurant.billing_service.application.service.OrderReceiptData;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JasperReportServiceTest {

    @Test
    void generateReceipt_shouldCreateValidPdf() {
        JasperReportService service = new JasperReportService();

        OrderReceiptData receiptData = new OrderReceiptData(
                UUID.randomUUID(),
                "Restaurante Prueba",
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now(),
                "Domicilio",
                new BigDecimal("56000"),
                List.of(
                        new OrderReceiptData.OrderItemData("Hamburguesa", 2, new BigDecimal("18000"), new BigDecimal("36000")),
                        new OrderReceiptData.OrderItemData("Gaseosa", 1, new BigDecimal("20000"), new BigDecimal("20000"))
                )
        );

        byte[] pdf = service.generateReceipt(receiptData);

        assertNotNull(pdf);
        assertTrue(pdf.length > 4);
        assertTrue(new String(pdf, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF"));
    }
}
