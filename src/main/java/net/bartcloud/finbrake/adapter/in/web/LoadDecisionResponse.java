package net.bartcloud.finbrake.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.bartcloud.finbrake.domain.LoadDecision;

@JsonPropertyOrder({"id", "customer_id", "accepted"})
public record LoadDecisionResponse(
        @JsonProperty("id") String id,
        @JsonProperty("customer_id") String customerId,
        @JsonProperty("accepted") boolean accepted) {

    public static LoadDecisionResponse from(LoadDecision decision) {
        return new LoadDecisionResponse(decision.id(), decision.customerId(), decision.accepted());
    }
}
