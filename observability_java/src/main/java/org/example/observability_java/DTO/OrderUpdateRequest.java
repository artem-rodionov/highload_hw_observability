package org.example.observability_java.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderUpdateRequest(
        @JsonProperty("user_id")
        Long userID,

        Long price
) {
}
