package com.restaurant.billing_service.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.restaurant.billing_service.domain.port.RestaurantLookupPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RestaurantServiceClient implements RestaurantLookupPort {

    private final RestClient restClient;

    public RestaurantServiceClient(
            @Value("${restaurant.service.base-url:http://localhost:8083}") String baseUrl
    ) {
        this.restClient = RestClient.create(baseUrl);
    }

    public Optional<String> findRestaurantName(UUID restaurantId) {
        if (restaurantId == null) {
            return Optional.empty();
        }

        for (String uri : List.of(
                "/api/restaurants/{restaurantId}",
                "/api/restaurant/{restaurantId}",
                "/restaurants/{restaurantId}",
                "/restaurant/{restaurantId}"
        )) {
            try {
                RestaurantResponse response = restClient
                        .get()
                        .uri(uri, restaurantId)
                        .headers(headers -> CurrentJwtToken.get().ifPresent(headers::setBearerAuth))
                        .retrieve()
                        .body(RestaurantResponse.class);

                if (response == null || response.getName() == null || response.getName().isBlank()) {
                    return Optional.empty();
                }

                return Optional.of(response.getName());
            } catch (HttpClientErrorException.NotFound ex) {
                return Optional.empty();
            } catch (RestClientException ex) {
                // Try next URI pattern when the service is not available.
            }
        }

        return Optional.empty();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RestaurantResponse {
        @JsonAlias({"id", "restaurantId"})
        private UUID id;

        @JsonAlias({"name", "restaurantName", "restaurant_name"})
        private String name;

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
