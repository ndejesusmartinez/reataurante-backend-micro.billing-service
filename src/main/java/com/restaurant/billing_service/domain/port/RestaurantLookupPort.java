package com.restaurant.billing_service.domain.port;

import java.util.Optional;
import java.util.UUID;

public interface RestaurantLookupPort {
    Optional<String> findRestaurantName(UUID restaurantId);
}
