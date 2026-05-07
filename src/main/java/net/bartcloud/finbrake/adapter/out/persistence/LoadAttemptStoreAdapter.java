package net.bartcloud.finbrake.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import net.bartcloud.finbrake.application.port.out.LoadAttemptStore;
import net.bartcloud.finbrake.domain.LoadAttempt;
import org.springframework.stereotype.Component;

@Component
public class LoadAttemptStoreAdapter implements LoadAttemptStore {

    private final LoadAttemptJpaRepository repo;

    public LoadAttemptStoreAdapter(LoadAttemptJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean exists(String customerId, String id) {
        return repo.existsByCustomerIdAndAttemptId(customerId, id);
    }

    @Override
    public BigDecimal sumAcceptedAmountBetween(String customerId, Instant fromInclusive, Instant toExclusive) {
        return repo.sumAcceptedAmount(customerId, fromInclusive, toExclusive);
    }

    @Override
    public int countAcceptedBetween(String customerId, Instant fromInclusive, Instant toExclusive) {
        return Math.toIntExact(repo.countAccepted(customerId, fromInclusive, toExclusive));
    }

    @Override
    public void save(LoadAttempt attempt, boolean accepted) {
        repo.save(
                new LoadAttemptEntity(attempt.id(), attempt.customerId(), attempt.amount(), attempt.time(), accepted));
    }
}
