package net.bartcloud.finbrake.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record LoadAttempt(String id, String customerId, BigDecimal amount, Instant time) {}
