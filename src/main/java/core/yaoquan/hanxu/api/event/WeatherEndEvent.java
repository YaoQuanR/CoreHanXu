package core.yaoquan.hanxu.api.event;

import core.yaoquan.hanxu.api.WeatherHolder;
import net.minecraft.server.level.ServerLevel;

public class WeatherEndEvent extends WeatherEvent {
    public WeatherEndEvent(ServerLevel level, WeatherHolder.WeatherInstance weatherInstance) {
        super(level, weatherInstance);
    }
}
