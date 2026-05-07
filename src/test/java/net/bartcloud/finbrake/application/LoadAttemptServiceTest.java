package net.bartcloud.finbrake.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.bartcloud.finbrake.application.port.out.LoadAttemptStore;
import net.bartcloud.finbrake.domain.LoadAttempt;
import net.bartcloud.finbrake.domain.LoadDecision;
import org.junit.jupiter.api.Test;

class LoadAttemptServiceTest {

    private final InMemoryStore store = new InMemoryStore();
    private final LoadAttemptService service = new LoadAttemptService(store);

    @Test
    void firstAttemptIsAccepted() {
        Optional<LoadDecision> decision = service.process(attempt("a1", "c1", "100", "2018-01-01T10:00:00Z"));
        assertThat(decision)
                .isPresent()
                .get()
                .extracting(LoadDecision::accepted)
                .isEqualTo(true);
    }

    @Test
    void duplicateAttemptReturnsEmpty() {
        service.process(attempt("a1", "c1", "100", "2018-01-01T10:00:00Z"));
        Optional<LoadDecision> second = service.process(attempt("a1", "c1", "999", "2018-01-01T11:00:00Z"));
        assertThat(second).isEmpty();
    }

    @Test
    void dailyAmountLimitRejectsExcessLoad() {
        service.process(attempt("a1", "c1", "3000", "2018-01-01T10:00:00Z"));
        Optional<LoadDecision> second = service.process(attempt("a2", "c1", "3000", "2018-01-01T11:00:00Z"));
        assertThat(second).get().extracting(LoadDecision::accepted).isEqualTo(false);
    }

    @Test
    void dailyCountLimitRejectsFourthLoad() {
        service.process(attempt("a1", "c1", "100", "2018-01-01T10:00:00Z"));
        service.process(attempt("a2", "c1", "100", "2018-01-01T11:00:00Z"));
        service.process(attempt("a3", "c1", "100", "2018-01-01T12:00:00Z"));
        Optional<LoadDecision> fourth = service.process(attempt("a4", "c1", "100", "2018-01-01T13:00:00Z"));
        assertThat(fourth).get().extracting(LoadDecision::accepted).isEqualTo(false);
    }

    @Test
    void weeklyAmountLimitRejectsExcess() {
        service.process(attempt("a1", "c1", "5000", "2018-01-01T10:00:00Z"));
        service.process(attempt("a2", "c1", "5000", "2018-01-02T10:00:00Z"));
        service.process(attempt("a3", "c1", "5000", "2018-01-03T10:00:00Z"));
        service.process(attempt("a4", "c1", "5000", "2018-01-04T10:00:00Z"));
        Optional<LoadDecision> over = service.process(attempt("a5", "c1", "1", "2018-01-05T10:00:00Z"));
        assertThat(over).get().extracting(LoadDecision::accepted).isEqualTo(false);
    }

    @Test
    void weeklyCounterResetsOnMondayMidnightUtc() {
        service.process(attempt("a1", "c1", "5000", "2018-01-01T10:00:00Z"));
        service.process(attempt("a2", "c1", "5000", "2018-01-02T10:00:00Z"));
        service.process(attempt("a3", "c1", "5000", "2018-01-03T10:00:00Z"));
        service.process(attempt("a4", "c1", "5000", "2018-01-07T10:00:00Z"));
        Optional<LoadDecision> nextWeek = service.process(attempt("a5", "c1", "5000", "2018-01-08T00:00:00Z"));
        assertThat(nextWeek).get().extracting(LoadDecision::accepted).isEqualTo(true);
    }

    @Test
    void declinedAttemptDoesNotConsumeWindow() {
        service.process(attempt("a1", "c1", "4000", "2018-01-01T10:00:00Z"));
        Optional<LoadDecision> declined = service.process(attempt("a2", "c1", "4000", "2018-01-01T11:00:00Z"));
        assertThat(declined).get().extracting(LoadDecision::accepted).isEqualTo(false);
        Optional<LoadDecision> third = service.process(attempt("a3", "c1", "1000", "2018-01-01T12:00:00Z"));
        assertThat(third).get().extracting(LoadDecision::accepted).isEqualTo(true);
    }

    @Test
    void otherCustomersDoNotInterfere() {
        service.process(attempt("a1", "c1", "5000", "2018-01-01T10:00:00Z"));
        Optional<LoadDecision> otherCustomer = service.process(attempt("a2", "c2", "5000", "2018-01-01T11:00:00Z"));
        assertThat(otherCustomer).get().extracting(LoadDecision::accepted).isEqualTo(true);
    }

    private static LoadAttempt attempt(String id, String customerId, String amount, String time) {
        return new LoadAttempt(id, customerId, new BigDecimal(amount), Instant.parse(time));
    }

    private static final class InMemoryStore implements LoadAttemptStore {
        private record Row(String customerId, String attemptId, BigDecimal amount, Instant time, boolean accepted) {}

        private final List<Row> rows = new ArrayList<>();

        @Override
        public boolean exists(String customerId, String id) {
            return rows.stream().anyMatch(r -> r.customerId.equals(customerId) && r.attemptId.equals(id));
        }

        @Override
        public BigDecimal sumAcceptedAmountBetween(String customerId, Instant from, Instant to) {
            return rows.stream()
                    .filter(r -> r.customerId.equals(customerId) && r.accepted)
                    .filter(r -> !r.time.isBefore(from) && r.time.isBefore(to))
                    .map(Row::amount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        @Override
        public int countAcceptedBetween(String customerId, Instant from, Instant to) {
            return (int) rows.stream()
                    .filter(r -> r.customerId.equals(customerId) && r.accepted)
                    .filter(r -> !r.time.isBefore(from) && r.time.isBefore(to))
                    .count();
        }

        @Override
        public void save(LoadAttempt attempt, boolean accepted) {
            rows.add(new Row(attempt.customerId(), attempt.id(), attempt.amount(), attempt.time(), accepted));
        }
    }
}
