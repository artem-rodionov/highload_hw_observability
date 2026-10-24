package org.example.observability_java.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderCreateRequest(
        @JsonProperty("user_id")
        Long userID,
        Long price
) {
}
