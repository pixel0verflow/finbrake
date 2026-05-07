package net.bartcloud.finbrake.domain;

import java.math.BigDecimal;

public final class VelocityPolicy {

    public static final BigDecimal MAX_DAILY_AMOUNT = new BigDecimal("5000");
    public static final BigDecimal MAX_WEEKLY_AMOUNT = new BigDecimal("20000");
    public static final int MAX_DAILY_COUNT = 3;

    private VelocityPolicy() {}

    public static boolean accepts(BigDecimal incoming, BigDecimal dailyTotal, int dailyCount, BigDecimal weeklyTotal) {
        if (dailyCount >= MAX_DAILY_COUNT) {
            return false;
        }
        if (dailyTotal.add(incoming).compareTo(MAX_DAILY_AMOUNT) > 0) {
            return false;
        }
        if (weeklyTotal.add(incoming).compareTo(MAX_WEEKLY_AMOUNT) > 0) {
            return false;
        }
        return true;
    }
}
