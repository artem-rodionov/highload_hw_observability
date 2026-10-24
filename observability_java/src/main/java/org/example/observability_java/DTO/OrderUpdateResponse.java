package org.example.observability_java.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record OrderUpdateResponse(
        @JsonProperty("order_id")
        Long orderID,

        @JsonProperty("user_id")
        Long userID,

        Long price,

        @JsonProperty("updated_at")
        Instant updatedAt
) {
}
