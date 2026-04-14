package core.yaoquan.hanxu.util;

import core.yaoquan.hanxu.api.TimeHolder;

import java.util.concurrent.ThreadLocalRandom;

public class Converter {
    // Tool method.
    public static int convertToTicks(int durationTime, String timeUnit) {
        return switch (timeUnit) {
            case "second" -> durationTime * TimeHolder.TICKS_PER_SECOND;
            case "minute" -> durationTime * TimeHolder.TICKS_PER_MINUTE;
            case "hour" -> durationTime * TimeHolder.TICKS_PER_HOUR;
            default -> durationTime;
        };
    }

    public static int convertFromRangeToRandom(int firstRange, int secondRange) {
        // Compare.
        int lowerRange = Math.min(firstRange, secondRange);
        int upperRange = Math.max(firstRange, secondRange);

        // Take one random number between the range.
        return ThreadLocalRandom.current().nextInt(lowerRange, upperRange + 1);
    }
}
