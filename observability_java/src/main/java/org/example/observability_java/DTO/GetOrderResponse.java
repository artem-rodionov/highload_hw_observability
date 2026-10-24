package org.example.observability_java.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record GetOrderResponse(
        @JsonProperty("order_id")
        Long orderID,

        @JsonProperty("user_id")
        Long UserID,

        Long price,

        @JsonProperty("updated_at")
        Instant updateAt) {
}
