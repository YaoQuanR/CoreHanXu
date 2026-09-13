package core.yaoquan.hanxu.util.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import core.yaoquan.hanxu.api.TimeHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.concurrent.ThreadLocalRandom;

public class Converter {
    // Tool method.
    public static int convertToTicks(int durationTime, String timeUnit) {
        return switch (timeUnit) {
            case "s", "second" -> durationTime * TimeHolder.TICKS_PER_SECOND;
            case "m", "minute" -> durationTime * TimeHolder.TICKS_PER_MINUTE;
            case "h", "hour" -> durationTime * TimeHolder.TICKS_PER_HOUR;
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

    public static Component convertFromJsonToComponent(String json) {
        try {
            JsonElement jsonElement = JsonParser.parseString(json);
            return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, jsonElement).getOrThrow();
        }
        catch (Exception e) {
            return Component.literal(json);
        }
    }
}
