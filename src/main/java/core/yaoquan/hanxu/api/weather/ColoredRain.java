package core.yaoquan.hanxu.api.weather;

import core.yaoquan.hanxu.api.WeatherHolder;
import net.minecraft.server.level.ServerLevel;

import java.util.Random;

/// @since 0.7.0 (Internal Development)
public class ColoredRain implements WeatherHolder.WeatherDefinition {
    // Define what behavior should override when player enter a specific climate (Biome set).
    public enum RainType {
        DEFAULT, RAIN, SNOW, DRY
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
        int duration = minimumDuration + random.nextInt(maximumDuration - minimumDuration);
        int stillness = minimumStillness + random.nextInt(maximumStillness - minimumStillness);
        return new WeatherHolder.WeatherInstance(
                id,
                WeatherHolder.WeatherType.COLORED_RAIN,
                duration,
                stillness,
                this
        );
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
}
