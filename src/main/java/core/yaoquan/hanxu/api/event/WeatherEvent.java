package core.yaoquan.hanxu.api.event;

import core.yaoquan.hanxu.api.WeatherHolder;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

public abstract class WeatherEvent extends Event {
    private final ServerLevel level;
    private final WeatherHolder.WeatherInstance weatherInstance;

    public WeatherEvent(ServerLevel level, WeatherHolder.WeatherInstance weatherInstance) {
        this.level = level;
        this.weatherInstance = weatherInstance;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public WeatherHolder.WeatherInstance getWeatherInstance() {
        return weatherInstance;
    }

    public String getId() {
        return weatherInstance.getId();
    }

    public WeatherHolder.WeatherType getType() {
        return weatherInstance.getType();
    }
}
