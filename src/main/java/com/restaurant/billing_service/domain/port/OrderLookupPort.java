package com.restaurant.billing_service.domain.port;

import com.restaurant.billing_service.application.service.OrderReceiptData;

import java.util.UUID;

public interface OrderLookupPort {
    OrderReceiptData fetchOrder(UUID orderId);
}
