package net.bartcloud.finbrake.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import net.bartcloud.finbrake.domain.LoadAttempt;

public record LoadAttemptRequest(
        @NotBlank @JsonProperty("id") String id,
        @NotBlank @JsonProperty("customer_id") String customerId,
        @NotBlank @JsonProperty("load_amount") String loadAmount,
        @NotNull @JsonProperty("time") Instant time) {

    public LoadAttempt toDomain() {
        return new LoadAttempt(id, customerId, parseAmount(loadAmount), time);
    }

    private static BigDecimal parseAmount(String raw) {
        String stripped = raw.startsWith("$") ? raw.substring(1) : raw;
        return new BigDecimal(stripped);
    }
}
