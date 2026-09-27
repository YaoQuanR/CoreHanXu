package core.yaoquan.hanxu.api.weather;

import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.registry.config.GeneralConfig;
import core.yaoquan.hanxu.registry.event.payload.WeatherPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * <p><h3>
 *     Fog
 * </h3></p>
 * <p>
 *     Classic type that implements from {@link WeatherHolder.WeatherDefinition} and {@link WeatherHolder.WeatherType}.
 * </p>
 *
 * @since 0.7.0 (Internal Development)
 */
public class Fog implements WeatherHolder.WeatherDefinition {
    private final String id;
    private final Random random;
    private int color = WeatherHolder.DefaultColor.FOG;
    private float minimumDistance = 8f;
    private float maximumDistance = 64f;
    private int minimumDuration = 1200;
    private int maximumDuration = 6000;
    private int minimumStillness = 1200;
    private int maximumStillness = 6000;
    private final SortedMap<Float, Float> heightOffsets = new TreeMap<>();

    public Fog(String id, Random random) {
        this.id = id;
        this.random = random;
    }

    public Fog color(int color) {
        this.color = color;
        return this;
    }

    public Fog distance(float minimum, float maximum) {
        this.minimumDistance = Math.min(minimum, maximum);
        this.maximumDistance = Math.max(minimum, maximum);
        return this;
    }

    public Fog duration(int minimum, int maximum) {
        this.minimumDuration = Math.min(minimum, maximum);
        this.maximumDuration = Math.max(minimum, maximum);
        return this;
    }

    public Fog stillness(int minimum, int maximum) {
        this.minimumStillness = Math.min(minimum, maximum);
        this.maximumStillness = Math.max(minimum, maximum);
        return this;
    }

    public Fog heightOffset(float height, float offset) {
        this.heightOffsets.put(height, offset);
        return this;
    }

    public float getDistance(float height) {
        float offset = 0;

        if (!heightOffsets.isEmpty()) {
            offset = offsetInterpolation(height);
        }

        return Math.min(Math.max(minimumDistance + offset, 0f), maximumDistance);
    }

    @Override
    public WeatherHolder.WeatherInstance createInstance(Random random) {
        int duration = minimumDuration + random.nextInt(maximumDuration - minimumDuration + 1);
        int stillness = minimumStillness + random.nextInt(maximumStillness - minimumStillness + 1);

        return new WeatherHolder.WeatherInstance(
                id,
                WeatherHolder.WeatherType.FOG,
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

        float transition = ((Number) GeneralConfig.fogTransitionRatio.getAsDouble()).floatValue();

        float progress = (float) instance.getRemainingTicks() / instance.getInitialTicks();
        float minimumDistance = getMinimumDistance();
        float maximumDistance = getMaximumDistance();

        float playerY = ((Number) player.getY()).floatValue();
        float baseDistance = getDistance(playerY);

        baseDistance = Math.max(minimumDistance, Math.min(maximumDistance, baseDistance));

        float currentDistance;

        switch (instance.getPhase()) {
            case ACTIVE -> {
                float activeProgress = 1.0f - progress;
                if (activeProgress < transition) {
                    float ratio = activeProgress / transition;
                    currentDistance = maximumDistance - (maximumDistance - baseDistance) * ratio;
                }
                else if (activeProgress < (1.0f - transition)) {
                    currentDistance = baseDistance;
                }
                else {
                    float ratio = (activeProgress - (1.0f - transition)) / transition;
                    currentDistance = baseDistance + (maximumDistance - baseDistance) * ratio;
                }
            }
            case STILLNESS -> currentDistance = -1;
            case null, default -> {
                return;
            }
        }

        currentDistance = currentDistance != -1? Math.max(currentDistance, 2.0f) : -1;

        WeatherPayload.FogPacket packet = new WeatherPayload.FogPacket(
                instance.getPhase(),
                dimension,
                getColor(),
                currentDistance
        );

        PacketDistributor.sendToPlayer(player, packet);
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

    // Always able to use (No conditions).
    @Override
    public boolean isAble(ServerLevel level) {
        return true;
    }

    @Override
    public WeatherHolder.WeatherType getWeatherType() {
        return WeatherHolder.WeatherType.FOG;
    }

    public int getColor() {
        return color;
    }

    public float getMinimumDistance() {
        return minimumDistance;
    }

    public float getMaximumDistance() {
        return maximumDistance;
    }

    public SortedMap<Float, Float> getHeightOffsets() {
        return heightOffsets;
    }

    @Override
    public String toString() {
        return "HanXu(Core):WeatherHolder-Definitions:{id=" + id +
                ", color=" + color +
                ", distance=" + minimumDistance +
                "~" + maximumDistance +
                ", duration=" + minimumDuration +
                "~" + maximumDuration +
                ", stillness=" + minimumStillness +
                "~" + maximumStillness +
                ", heightOffsets=" + heightOffsets
                + "}";
    }

    private float offsetInterpolation(float height) {
        float nearestLowerHeight = Float.NEGATIVE_INFINITY;
        float nearestUpperHeight = Float.POSITIVE_INFINITY;
        float nearestLowerOffset = 0f;
        float nearestUpperOffset = 0f;

        for (Map.Entry<Float, Float> entry : heightOffsets.entrySet()) {
            // Nearest lower variables.
            if (entry.getKey() <= height) {
                nearestLowerHeight = entry.getKey();
                nearestLowerOffset = entry.getValue();
            }
            // First nearest upper variables.
            if (entry.getKey() > height && nearestUpperHeight == Float.POSITIVE_INFINITY) {
                nearestUpperHeight = entry.getKey();
                nearestUpperOffset = entry.getValue();
            }
        }

        // If not in an interval:
        // If this height lower than every node, use the first offset value.
        if (nearestLowerHeight == Float.NEGATIVE_INFINITY) {
            return nearestUpperOffset;
        }
        // If this height is the upperest value than every node, use the last offset value.
        if (nearestUpperHeight == Float.POSITIVE_INFINITY) {
            return nearestLowerOffset;
        }
        // If exactly into a node, use this node's offset.
        if (nearestLowerHeight == nearestUpperHeight) {
            return nearestLowerOffset;
        }

        float delta = (height - nearestLowerHeight) / (nearestUpperHeight - nearestLowerHeight);
        return nearestLowerOffset + delta * (nearestUpperOffset - nearestLowerOffset);
    }
}
