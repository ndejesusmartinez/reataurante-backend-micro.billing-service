package com.restaurant.billing_service.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderReceiptData(
        UUID orderId,
        String restaurantName,
        UUID restaurantId,
        UUID customerId,
        LocalDateTime orderDate,
        String orderType,
        BigDecimal total,
        List<OrderItemData> items
) {
    public static record OrderItemData(
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {}
}
