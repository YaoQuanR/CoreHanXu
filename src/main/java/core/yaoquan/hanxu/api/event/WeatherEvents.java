package core.yaoquan.hanxu.api.event;

import core.yaoquan.hanxu.api.WeatherHolder;
import net.minecraft.server.level.ServerLevel;

public class WeatherEvents {
    public static class WeatherEndEvent extends WeatherEvent {
        public WeatherEndEvent(ServerLevel level, WeatherHolder.WeatherInstance weatherInstance) {
            super(level, weatherInstance);
        }
    }

    public static class WeatherPauseEvent extends WeatherEvent {
        public WeatherPauseEvent(ServerLevel level, WeatherHolder.WeatherInstance weatherInstance) {
            super(level, weatherInstance);
        }
    }

    public static class WeatherResumeEvent extends WeatherEvent {
        public WeatherResumeEvent(ServerLevel level, WeatherHolder.WeatherInstance weatherInstance) {
            super(level, weatherInstance);
        }
    }

    public static class WeatherStartEvent extends WeatherEvent {
        public WeatherStartEvent(ServerLevel level, WeatherHolder.WeatherInstance weatherInstance) {
            super(level, weatherInstance);
        }
    }

    public static class WeatherTickEvent extends WeatherEvent {
        public WeatherTickEvent(ServerLevel level, WeatherHolder.WeatherInstance weatherInstance) {
            super(level, weatherInstance);
        }
    }
}
