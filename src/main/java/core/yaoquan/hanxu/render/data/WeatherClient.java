package core.yaoquan.hanxu.render.data;

import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.registry.event.payload.WeatherPayload;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WeatherClient {
    private static final Map<String, FogInfo> fogCache = new ConcurrentHashMap<>();
    private static final Map<String, ColoredRainInfo> coloredRainCache = new ConcurrentHashMap<>();
    private static final Map<String, WindInfo> windCache = new ConcurrentHashMap<>();

    public static void updateFog(WeatherPayload.FogPacket packet) {
        if (packet.phase() == WeatherHolder.WeatherPhase.IDLE) {
            fogCache.remove(packet.dimension());
        }
        else {
            fogCache.put(
                    packet.dimension(),
                    new FogInfo(
                            packet.phase(),
                            packet.color(),
                            packet.currentDistance()
                    )
            );
        }
    }

    public static void updateColoredRain(WeatherPayload.ColoredRainPacket packet) {
        if (packet.phase() == WeatherHolder.WeatherPhase.IDLE) {
            coloredRainCache.remove(packet.dimension());
        }
        else {
            coloredRainCache.put(
                    packet.dimension(),
                    new ColoredRainInfo(
                            packet.phase(),
                            packet.skyColor(),
                            packet.rainColor(),
                            packet.snowColor()
                    )
            );
        }
    }

    public static void updateWind(WeatherPayload.WindPacket packet) {
        if (packet.phase() == WeatherHolder.WeatherPhase.IDLE) {
            windCache.remove(packet.dimension());
        }
        else {
            windCache.put(
                    packet.dimension(),
                    new WindInfo(
                            packet.phase(),
                            packet.vectorX(),
                            packet.vectorY(),
                            packet.vectorZ(),
                            packet.intensity(),
                            packet.affectRain()
                    )
            );
        }
    }

    public static FogInfo getFog(String dimension) {
        return fogCache.get(dimension);
    }

    public static ColoredRainInfo getColoredRain(String dimension) {
        return coloredRainCache.get(dimension);
    }

    public static WindInfo getWind(String dimension) {
        return windCache.get(dimension);
    }

    public record FogInfo(
            WeatherHolder.WeatherPhase phase,
            int color,
            float currentDistance
    ) {}

    public record ColoredRainInfo(
            WeatherHolder.WeatherPhase phase,
            int skyColor,
            int rainColor,
            int snowColor
    ) {}

    public record WindInfo(
            WeatherHolder.WeatherPhase phase,
            double vectorX,
            double vectorY,
            double vectorZ,
            float intensity,
            boolean affectRain
    ) {}
}
