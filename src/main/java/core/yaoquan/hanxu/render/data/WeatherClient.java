package core.yaoquan.hanxu.render.data;

import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.api.weather.ColoredRain;
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
            ColoredRain.RainTypeData type = packet.rainTypeData();
            coloredRainCache.put(
                    packet.dimension(),
                    new ColoredRainInfo(
                            packet.phase(),
                            packet.skyColor(),
                            packet.rainColor(),
                            packet.snowColor(),
                            packet.fogColor(),
                            packet.intensity(),
                            type.rainBiomes(),
                            type.snowBiomes(),
                            type.dryBiomes(),
                            packet.progress()
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
                            packet.velocity(),
                            packet.intensity(),
                            packet.driftIntensityRatio(),
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
            int snowColor,
            int fogColor,
            float intensity,
            ColoredRain.RainType rainBiomes,
            ColoredRain.RainType snowBiomes,
            ColoredRain.RainType dryBiomes,
            float progress
    ) {}

    public record WindInfo(
            WeatherHolder.WeatherPhase phase,
            double vectorX,
            double vectorY,
            double vectorZ,
            float velocity,
            float intensity,
            float driftIntensityRatio,
            boolean affectRain
    ) {}
}
