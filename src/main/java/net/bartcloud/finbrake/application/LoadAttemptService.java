package net.bartcloud.finbrake.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import net.bartcloud.finbrake.application.port.in.ProcessLoadAttempt;
import net.bartcloud.finbrake.application.port.out.LoadAttemptStore;
import net.bartcloud.finbrake.domain.LoadAttempt;
import net.bartcloud.finbrake.domain.LoadDecision;
import net.bartcloud.finbrake.domain.TimeWindows;
import net.bartcloud.finbrake.domain.VelocityPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoadAttemptService implements ProcessLoadAttempt {

    private static final Logger log = LoggerFactory.getLogger(LoadAttemptService.class);

    private final LoadAttemptStore store;

    public LoadAttemptService(LoadAttemptStore store) {
        this.store = store;
    }

    @Override
    @Transactional
    public Optional<LoadDecision> process(LoadAttempt attempt) {
        if (store.exists(attempt.customerId(), attempt.id())) {
            log.debug("Ignoring duplicate load attempt id={} customer={}", attempt.id(), attempt.customerId());
            return Optional.empty();
        }

        Instant dayStart = TimeWindows.startOfDay(attempt.time());
        Instant dayEnd = TimeWindows.startOfNextDay(attempt.time());
        Instant weekStart = TimeWindows.startOfWeek(attempt.time());
        Instant weekEnd = TimeWindows.startOfNextWeek(attempt.time());

        BigDecimal dailyTotal = store.sumAcceptedAmountBetween(attempt.customerId(), dayStart, dayEnd);
        int dailyCount = store.countAcceptedBetween(attempt.customerId(), dayStart, dayEnd);
        BigDecimal weeklyTotal = store.sumAcceptedAmountBetween(attempt.customerId(), weekStart, weekEnd);

        boolean accepted = VelocityPolicy.accepts(attempt.amount(), dailyTotal, dailyCount, weeklyTotal);
        store.save(attempt, accepted);
        return Optional.of(new LoadDecision(attempt.id(), attempt.customerId(), accepted));
    }
}
