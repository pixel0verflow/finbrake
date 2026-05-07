package net.bartcloud.finbrake.domain;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

public final class TimeWindows {

    private TimeWindows() {}

    public static Instant startOfDay(Instant t) {
        return t.atZone(ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
    }

    public static Instant startOfNextDay(Instant t) {
        return startOfDay(t).plus(1, ChronoUnit.DAYS);
    }

    public static Instant startOfWeek(Instant t) {
        LocalDate monday =
                t.atZone(ZoneOffset.UTC).toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return monday.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    public static Instant startOfNextWeek(Instant t) {
        return startOfWeek(t).plus(7, ChronoUnit.DAYS);
    }
}
