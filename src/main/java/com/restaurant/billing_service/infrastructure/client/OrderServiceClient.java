package com.restaurant.billing_service.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.restaurant.billing_service.application.service.OrderReceiptData;
import com.restaurant.billing_service.domain.port.OrderLookupPort;
import com.restaurant.billing_service.presentation.exception.AuthorizationDeniedException;
import com.restaurant.billing_service.presentation.exception.AuthenticationRequiredException;
import com.restaurant.billing_service.presentation.exception.InvalidRequestException;
import com.restaurant.billing_service.presentation.exception.OrderServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class OrderServiceClient implements OrderLookupPort {

    private final RestClient restClient;

    public OrderServiceClient(
            @Value("${order.service.base-url:http://localhost:8084}") String baseUrl
    ) {
        this.restClient = RestClient.create(baseUrl);
    }

    public OrderReceiptData fetchOrder(UUID orderId) {
        if (orderId == null) {
            throw new InvalidRequestException("El identificador del pedido es obligatorio.");
        }

        String jwt = CurrentJwtToken.get()
                .orElseThrow(() -> new AuthenticationRequiredException(
                        "Debe enviar un JWT válido para consultar los pedidos."
                ));

        RestClientException lastFailure = null;

        for (String uri : List.of(
                "/api/orders/{orderId}",
                "/api/order/{orderId}",
                "/orders/{orderId}",
                "/order/{orderId}"
        )) {
            try {
                OrderResponse response = restClient
                        .get()
                        .uri(uri, orderId)
                        .headers(headers -> headers.setBearerAuth(jwt))
                        .retrieve()
                        .body(OrderResponse.class);

                if (response == null) {
                    continue;
                }

                return response.toReceiptData();
            } catch (HttpClientErrorException.NotFound ex) {
                throw new InvalidRequestException("No existe un pedido con el identificador indicado.");
            } catch (HttpClientErrorException.BadRequest ex) {
                throw new InvalidRequestException("El pedido solicitado no es válido.");
            } catch (HttpClientErrorException.Unauthorized ex) {
                throw new AuthenticationRequiredException(
                        "order-service rechazó el JWT. Verifique que sea válido y no haya expirado."
                );
            } catch (HttpClientErrorException.Forbidden ex) {
                throw new AuthorizationDeniedException(
                        "No tiene permisos para consultar este pedido."
                );
            } catch (HttpClientErrorException ex) {
                lastFailure = ex;
            } catch (RestClientException ex) {
                lastFailure = ex;
            }
        }

        throw new OrderServiceUnavailableException(
                "No fue posible consultar la información del pedido en order-service.",
                lastFailure == null ? new IllegalStateException("Pedido no disponible") : lastFailure
        );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderResponse {
        @JsonAlias({"id", "orderId"})
        private UUID id;

        @JsonAlias({"restaurantId", "restaurant_id", "restaurant"})
        private UUID restaurantId;

        @JsonAlias({"customerId", "customer_id", "clientId", "client_id"})
        private UUID customerId;

        @JsonAlias({"orderDate", "createdAt", "date", "created_at"})
        private LocalDateTime orderDate;

        @JsonAlias({"orderType", "type", "order_type"})
        private String orderType;

        @JsonAlias({"total", "totalAmount", "amount"})
        private BigDecimal total;

        @JsonAlias({"items", "products", "orderItems"})
        private List<OrderItemResponse> items = new ArrayList<>();

        public OrderReceiptData toReceiptData() {
            var itemData = items == null
                    ? new ArrayList<OrderReceiptData.OrderItemData>()
                    : items.stream().map(OrderItemResponse::toItemData).toList();

            return new OrderReceiptData(
                    id,
                    null,
                    restaurantId,
                    customerId,
                    orderDate,
                    orderType,
                    total == null ? BigDecimal.ZERO : total,
                    itemData
            );
        }

        public UUID getId() { return id; }
        public UUID getRestaurantId() { return restaurantId; }
        public UUID getCustomerId() { return customerId; }
        public LocalDateTime getOrderDate() { return orderDate; }
        public String getOrderType() { return orderType; }
        public BigDecimal getTotal() { return total; }
        public List<OrderItemResponse> getItems() { return items; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderItemResponse {
        @JsonAlias({"productName", "name", "description", "product_name"})
        private String productName;

        @JsonAlias({"quantity", "qty", "count"})
        private Integer quantity;

        @JsonAlias({"unitPrice", "unit_price", "price", "unitValue"})
        private BigDecimal unitPrice;

        @JsonAlias({"subtotal", "lineTotal", "total", "amount"})
        private BigDecimal subtotal;

        public OrderReceiptData.OrderItemData toItemData() {
            return new OrderReceiptData.OrderItemData(
                    productName == null ? "Producto" : productName,
                    quantity == null ? 0 : quantity,
                    unitPrice == null ? BigDecimal.ZERO : unitPrice,
                    subtotal == null ? BigDecimal.ZERO : subtotal
            );
        }

        public String getProductName() { return productName; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getSubtotal() { return subtotal; }
    }
}
