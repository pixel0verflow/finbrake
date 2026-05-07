package net.bartcloud.finbrake.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class TimeWindowsTest {

    @Test
    void dayWindowAlignsToUtcMidnight() {
        Instant t = Instant.parse("2018-01-01T15:30:45Z");
        assertThat(TimeWindows.startOfDay(t)).isEqualTo(Instant.parse("2018-01-01T00:00:00Z"));
        assertThat(TimeWindows.startOfNextDay(t)).isEqualTo(Instant.parse("2018-01-02T00:00:00Z"));
    }

    @Test
    void weekWindowStartsOnMondayUtc() {
        Instant wednesday = Instant.parse("2018-01-03T12:00:00Z");
        assertThat(TimeWindows.startOfWeek(wednesday)).isEqualTo(Instant.parse("2018-01-01T00:00:00Z"));
        assertThat(TimeWindows.startOfNextWeek(wednesday)).isEqualTo(Instant.parse("2018-01-08T00:00:00Z"));
    }

    @Test
    void sundayLastSecondIsInPriorWeek() {
        Instant sundayLast = Instant.parse("2018-01-07T23:59:59Z");
        assertThat(TimeWindows.startOfWeek(sundayLast)).isEqualTo(Instant.parse("2018-01-01T00:00:00Z"));
    }

    @Test
    void mondayMidnightStartsNewWeek() {
        Instant mondayStart = Instant.parse("2018-01-08T00:00:00Z");
        assertThat(TimeWindows.startOfWeek(mondayStart)).isEqualTo(Instant.parse("2018-01-08T00:00:00Z"));
    }
}
