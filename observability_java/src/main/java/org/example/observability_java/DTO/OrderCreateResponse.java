package org.example.observability_java.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderCreateResponse(
        @JsonProperty("order_id")
        Long orderID
) {
}
