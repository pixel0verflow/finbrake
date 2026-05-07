package net.bartcloud.finbrake.application.port.out;

import java.math.BigDecimal;
import java.time.Instant;
import net.bartcloud.finbrake.domain.LoadAttempt;

public interface LoadAttemptStore {

    boolean exists(String customerId, String id);

    BigDecimal sumAcceptedAmountBetween(String customerId, Instant fromInclusive, Instant toExclusive);

    int countAcceptedBetween(String customerId, Instant fromInclusive, Instant toExclusive);

    void save(LoadAttempt attempt, boolean accepted);
}
