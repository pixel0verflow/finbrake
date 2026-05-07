package net.bartcloud.finbrake.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import net.bartcloud.finbrake.domain.LoadAttempt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(LoadAttemptStoreAdapter.class)
class LoadAttemptStoreAdapterTest {

    @Autowired
    private LoadAttemptStoreAdapter adapter;

    private final Instant t0 = Instant.parse("2018-01-01T10:00:00Z");
    private final Instant t1 = Instant.parse("2018-01-01T12:00:00Z");
    private final Instant t2 = Instant.parse("2018-01-02T01:00:00Z");

    @BeforeEach
    void seed() {
        adapter.save(new LoadAttempt("a1", "c1", new BigDecimal("100.00"), t0), true);
        adapter.save(new LoadAttempt("a2", "c1", new BigDecimal("250.00"), t1), true);
        adapter.save(new LoadAttempt("a3", "c1", new BigDecimal("999.00"), t1), false);
        adapter.save(new LoadAttempt("a4", "c1", new BigDecimal("50.00"), t2), true);
    }

    @Test
    void existsDetectsByCompositeNaturalKey() {
        assertThat(adapter.exists("c1", "a1")).isTrue();
        assertThat(adapter.exists("c1", "missing")).isFalse();
        assertThat(adapter.exists("other", "a1")).isFalse();
    }

    @Test
    void sumIncludesOnlyAcceptedInWindow() {
        Instant from = Instant.parse("2018-01-01T00:00:00Z");
        Instant to = Instant.parse("2018-01-02T00:00:00Z");
        assertThat(adapter.sumAcceptedAmountBetween("c1", from, to)).isEqualByComparingTo("350.00");
    }

    @Test
    void countIncludesOnlyAcceptedInWindow() {
        Instant from = Instant.parse("2018-01-01T00:00:00Z");
        Instant to = Instant.parse("2018-01-02T00:00:00Z");
        assertThat(adapter.countAcceptedBetween("c1", from, to)).isEqualTo(2);
    }
}
