package core.yaoquan.hanxu.api.weather;

import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.registry.config.GeneralConfig;
import core.yaoquan.hanxu.registry.event.payload.WeatherPayload;
import core.yaoquan.hanxu.util.tool.Cast;
import core.yaoquan.hanxu.util.tool.ColorHSV;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * <p><h3>
 *     Colored Rain
 * </h3></p>
 * <p>
 *     Classic type that implements from {@link WeatherHolder.WeatherDefinition} and {@link WeatherHolder.WeatherType}.
 * </p>
 *
 * @since 0.7.1 (Internal Development)
 */
public class ColoredRain implements WeatherHolder.WeatherDefinition {
    /// Define what behavior should override when player enter a specific climate (Biome set).
    public enum RainType {
        DEFAULT, RAIN, SNOW, DRY
    }

    /// Data set for networking.
    public static class RainTypeData {
        private final String rainBiomes;
        private final String snowBiomes;
        private final String dryBiomes;

        public RainTypeData(@NotNull RainType rainBiomes, @NotNull RainType snowBiomes, @NotNull RainType dryBiomes) {
            this.rainBiomes = rainBiomes.name();
            this.snowBiomes = snowBiomes.name();
            this.dryBiomes = dryBiomes.name();
        }

        public @NotNull RainType rainBiomes() {
            return RainType.valueOf(rainBiomes);
        }

        public @NotNull RainType snowBiomes() {
            return RainType.valueOf(snowBiomes);
        }

        public @NotNull RainType dryBiomes() {
            return RainType.valueOf(dryBiomes);
        }

        public String encode() {
            return rainBiomes + ":" + snowBiomes + ":" + dryBiomes;
        }

        public static @NotNull RainTypeData decode(@NotNull String encoded) {
            String[] types = encoded.split(":", 3);
            return new RainTypeData(
                    castToType(types[0]),
                    castToType(types[1]),
                    castToType(types[2])
            );
        }

        public static @NotNull RainType castToType(@NotNull String type) {
            try {
                return RainType.valueOf(type);
            }
            catch (IllegalArgumentException e) {
                return RainType.DEFAULT;
            }
        }
    }

    private final String id;
    private final Random random;
    private int skyColor = WeatherHolder.DefaultColor.RAINY_SKY;
    private int rainColor = WeatherHolder.DefaultColor.RAIN;
    private int snowColor = WeatherHolder.DefaultColor.SNOW;
    private RainType rainBiomes = RainType.DEFAULT;
    private RainType snowBiomes = RainType.DEFAULT;
    private RainType dryBiomes = RainType.DEFAULT;
    private int minimumDuration = 1200;
    private int maximumDuration = 6000;
    private int minimumStillness = 1200;
    private int maximumStillness = 6000;

    public ColoredRain(String id, Random random) {
        this.id = id;
        this.random = random;
    }

    public ColoredRain skyColor(int color) {
        this.skyColor = color;
        return this;
    }

    public ColoredRain rainColor(int color) {
        this.rainColor = color;
        return this;
    }

    public ColoredRain snowColor(int color) {
        this.snowColor = color;
        return this;
    }

    public ColoredRain rainBiomes(RainType overrideType) {
        this.rainBiomes = overrideType;
        return this;
    }

    public ColoredRain snowBiomes(RainType overrideType) {
        this.snowBiomes = overrideType;
        return this;
    }

    public ColoredRain dryBiomes(RainType overrideType) {
        this.dryBiomes = overrideType;
        return this;
    }

    public ColoredRain duration(int minimum, int maximum) {
        this.minimumDuration = Math.min(minimum, maximum);
        this.maximumDuration = Math.max(minimum, maximum);
        return this;
    }

    public ColoredRain stillness(int minimum, int maximum) {
        this.minimumStillness = Math.min(minimum, maximum);
        this.maximumStillness = Math.max(minimum, maximum);
        return this;
    }

    @Override
    public WeatherHolder.WeatherInstance createInstance(Random random) {
        int duration = minimumDuration + random.nextInt(maximumDuration - minimumDuration + 1);
        int stillness = minimumStillness + random.nextInt(maximumStillness - minimumStillness + 1);
        return new WeatherHolder.WeatherInstance(
                id,
                WeatherHolder.WeatherType.COLORED_RAIN,
                duration,
                stillness,
                this
        );
    }

    @Override
    public void sendToPlayer(ServerPlayer player, WeatherHolder.WeatherInstance instance, String dimension) {
        if (player == null || instance == null) {
            return;
        }

        switch (instance.getPhase()) {
            case ACTIVE, STILLNESS -> {
                float fogBrightness = ((Number) GeneralConfig.coloredRainEnvironmentFogBrightness.getAsDouble()).floatValue();
                int fogColor = ColorHSV.adjustBrightness(skyColor, fogBrightness);

                // Calculate the transition for color.
                float colorTransition = transitionProgress(instance);

                // Calculate the intensity for biome transition.
                float intensity = transitionIntensity(instance);

                // Set plains default color as the starting point.
                int transitionSky = ColorHSV.interpolate(WeatherHolder.DefaultColor.RAINY_SKY, skyColor, colorTransition);
                int transitionRain = ColorHSV.interpolate(WeatherHolder.DefaultColor.RAIN, rainColor, colorTransition);
                int transitionSnow = ColorHSV.interpolate(WeatherHolder.DefaultColor.SNOW, snowColor, colorTransition);
                int transitionFog = ColorHSV.interpolate(WeatherHolder.DefaultColor.RAINY_FOG, fogColor, colorTransition);

                WeatherPayload.ColoredRainPacket packet = new WeatherPayload.ColoredRainPacket(
                        instance.getPhase(),
                        dimension,
                        transitionSky,
                        transitionRain,
                        transitionSnow,
                        transitionFog,
                        intensity,
                        new ColoredRain.RainTypeData(
                                rainBiomes,
                                snowBiomes,
                                dryBiomes
                        )
                );

                PacketDistributor.sendToPlayer(player, packet);
            }
            case null, default -> {}
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Random getRandom() {
        return random;
    }

    @Override
    public int getMinimumDuration() {
        return minimumDuration;
    }

    @Override
    public int getMaximumDuration() {
        return maximumDuration;
    }

    @Override
    public int getMinimumStillness() {
        return minimumStillness;
    }

    @Override
    public int getMaximumStillness() {
        return maximumStillness;
    }

    // Only raining or thundering is able to trigger colored rain.
    @Override
    public boolean isAble(ServerLevel level) {
        return level.isRaining() || level.isThundering();
    }

    @Override
    public WeatherHolder.WeatherType getWeatherType() {
        return WeatherHolder.WeatherType.COLORED_RAIN;
    }

    public int getSkyColor() {
        return skyColor;
    }

    public int getRainColor() {
        return rainColor;
    }

    public int getSnowColor() {
        return snowColor;
    }

    public RainType getRainBiomes() {
        return rainBiomes;
    }

    public RainType getSnowBiomes() {
        return snowBiomes;
    }

    public RainType getDryBiomes() {
        return dryBiomes;
    }

    @Override
    public String toString() {
        return "HanXu(Core):WeatherHolder-Definitions:{id=" + id +
                ", skyColor=" + skyColor +
                ", rainColor=" + rainColor +
                ", snowColor=" + snowColor +
                ", rainBiomeOverride=" + rainBiomes +
                ", snowBiomeOverride=" + snowBiomes +
                ", dryBiomeOverride=" + dryBiomes +
                ", duration=" + minimumDuration +
                "~" + maximumDuration +
                ", stillness=" + minimumStillness +
                "~" + maximumStillness +
                "}";
    }

    private float transitionProgress(WeatherHolder.WeatherInstance instance) {
        float transition = Cast.toFloat(GeneralConfig.coloredRainTransitionRatio.getAsDouble(), 0.1f);

        if (instance.getInitialTicks() <= 0 || transition <= 0.0f) {
            return 1.0f;
        }

        float progress = (float) instance.getRemainingTicks() / instance.getInitialTicks();
        float activeProgress = 1.0f - progress;

        if (instance.getPhase() == WeatherHolder.WeatherPhase.ACTIVE) {
            if (activeProgress < transition) {
                return activeProgress / transition;
            }
            else if (activeProgress < (1.0f - transition)) {
                return 1.0f;
            }
            else {
                return (1.0f - activeProgress) / transition;
            }
        }

        return 0.0f;
    }

    private float transitionIntensity(WeatherHolder.WeatherInstance instance) {
        int initialTicks = instance.getInitialTicks();
        int remainingTicks = instance.getRemainingTicks();

        if (initialTicks <= 0 || remainingTicks <= 0) {
            return 1.0f;
        }

        int progressTicks = initialTicks - remainingTicks;

        float raiseIntensity = Math.min(1.0f, progressTicks * 0.01f);
        float fallIntensity = Math.min(1.0f, remainingTicks * 0.01f);

        return Math.min(raiseIntensity, fallIntensity);
    }
}
