package com.restaurant.billing_service.infrastructure.report;

import com.restaurant.billing_service.application.service.OrderReceiptData;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class JasperReportService {

    private static final String REPORT_PATH = "reports/order-receipt.jrxml";

    public byte[] generateTestReceipt() {
        var items = List.of(
                new OrderItemReport("Hamburguesa Especial", 2, new BigDecimal("15000"), new BigDecimal("30000")),
                new OrderItemReport("Gaseosa", 3, new BigDecimal("5000"), new BigDecimal("15000"))
        );

        var order = new OrderReceiptData(
                java.util.UUID.randomUUID(),
                "Restaurante Demo",
                java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID(),
                LocalDateTime.now(),
                "Domicilio",
                new BigDecimal("45000"),
                items.stream().map(item -> new OrderReceiptData.OrderItemData(
                        item.getProductName(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal())).toList()
        );

        return generateReceipt(order);
    }

    public byte[] generateReceipt(OrderReceiptData receiptData) {
        try {
            ClassPathResource resource = new ClassPathResource(REPORT_PATH);
            if (!resource.exists()) {
                throw new IllegalStateException("No se encontró la plantilla Jasper: " + REPORT_PATH);
            }

            try (InputStream inputStream = resource.getInputStream()) {
                JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
                Map<String, Object> parameters = buildParameters(receiptData);
                List<OrderItemReport> items = toItemReports(receiptData.items());
                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(items);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
                return outputStream.toByteArray();
            }
        } catch (JRException ex) {
            throw new IllegalStateException("Error de JasperReports al generar el PDF: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Error al generar el comprobante PDF: " + ex.getMessage(), ex);
        }
    }

    private Map<String, Object> buildParameters(OrderReceiptData receiptData) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ORDER_ID", receiptData.orderId() == null ? "N/A" : receiptData.orderId().toString());
        parameters.put("ORDER_DATE", formatDate(receiptData.orderDate()));
        parameters.put("RESTAURANT_NAME", receiptData.restaurantName() == null || receiptData.restaurantName().isBlank()
                ? "Restaurante"
                : receiptData.restaurantName());
        parameters.put("CUSTOMER_ID", receiptData.customerId() == null ? "No informado" : receiptData.customerId().toString());
        parameters.put("ORDER_TYPE", receiptData.orderType() == null || receiptData.orderType().isBlank() ? "Domicilio" : receiptData.orderType());
        parameters.put("TOTAL", receiptData.total() == null ? BigDecimal.ZERO : receiptData.total().setScale(2, RoundingMode.HALF_UP));
        parameters.put("TOTAL_TEXT", formatCurrency(receiptData.total() == null ? BigDecimal.ZERO : receiptData.total()));
        return parameters;
    }

    private String formatDate(LocalDateTime orderDate) {
        return orderDate == null
                ? LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : orderDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String formatCurrency(BigDecimal total) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        return formatter.format(total == null ? BigDecimal.ZERO : total);
    }

    private List<OrderItemReport> toItemReports(List<OrderReceiptData.OrderItemData> items) {
        if (items == null || items.isEmpty()) {
            return List.of(new OrderItemReport("Sin productos", 0, BigDecimal.ZERO, BigDecimal.ZERO));
        }

        List<OrderItemReport> reports = new ArrayList<>();
        for (var item : items) {
            reports.add(new OrderItemReport(
                    item.productName() == null ? "Producto" : item.productName(),
                    item.quantity() == null ? 0 : item.quantity(),
                    item.unitPrice() == null ? BigDecimal.ZERO : item.unitPrice(),
                    item.subtotal() == null ? BigDecimal.ZERO : item.subtotal()
            ));
        }
        return reports;
    }

    public static class OrderItemReport {
        private final String productName;
        private final Integer quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal subtotal;

        public OrderItemReport(String productName, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.subtotal = subtotal;
        }

        public String getProductName() {
            return productName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }
    }
}