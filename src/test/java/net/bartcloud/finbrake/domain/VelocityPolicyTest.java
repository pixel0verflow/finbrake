package net.bartcloud.finbrake.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class VelocityPolicyTest {

    @Test
    void acceptsWhenAllLimitsObserved() {
        assertThat(VelocityPolicy.accepts(amt("100"), amt("0"), 0, amt("0"))).isTrue();
    }

    @Test
    void acceptsExactlyAtDailyAmountLimit() {
        assertThat(VelocityPolicy.accepts(amt("2000"), amt("3000"), 1, amt("3000")))
                .isTrue();
    }

    @Test
    void rejectsWhenDailyAmountExceeded() {
        assertThat(VelocityPolicy.accepts(amt("2000.01"), amt("3000"), 1, amt("3000")))
                .isFalse();
    }

    @Test
    void rejectsWhenDailyCountExceeded() {
        assertThat(VelocityPolicy.accepts(amt("1"), amt("0"), 3, amt("0"))).isFalse();
    }

    @Test
    void rejectsWhenWeeklyAmountExceeded() {
        assertThat(VelocityPolicy.accepts(amt("1000"), amt("0"), 0, amt("19500")))
                .isFalse();
    }

    @Test
    void acceptsExactlyAtWeeklyAmountLimit() {
        assertThat(VelocityPolicy.accepts(amt("500"), amt("0"), 0, amt("19500")))
                .isTrue();
    }

    private static BigDecimal amt(String value) {
        return new BigDecimal(value);
    }
}
