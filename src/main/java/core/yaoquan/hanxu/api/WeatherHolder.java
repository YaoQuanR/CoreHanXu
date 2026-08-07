package core.yaoquan.hanxu.api;

import core.yaoquan.hanxu.CoreHanXu;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.*;

/**
 * Weather System API
 * @since 0.7.0 (Internal Development)
 */
@EventBusSubscriber(modid = CoreHanXu.MOD_ID)
public class WeatherHolder {
    private static class DefaultColor {
        private static final int RAIN = 0xCFEBFF;
        private static final int SKY = 0xCCF0FF;
        private static final int RAINY_SKY = 0x4D82A8;
        private static final int SNOW = 0xEDF8FF;
        private static final int FOG = 0xCCDDEE;
    }

    // Structure of a custom weather.
    public static class CustomWeather {
        private final String id;
        private final Random random;
        private WeatherTrigger trigger;
        private final List<Fog> fogs = new ArrayList<>();
        private final List<ColoredRain> coloredRains = new ArrayList<>();
        private final List<Wind> winds = new ArrayList<>();

        public CustomWeather(String id, Random random) {
            this.id = id;
            this.random = random;
        }

        public CustomWeather trigger(WeatherTrigger trigger) {
            this.trigger = trigger;
            return this;
        }

        public CustomWeather fog(Fog fog) {
            this.fogs.add(fog);
            return this;
        }

        public CustomWeather coloredRain(ColoredRain coloredRain) {
            this.coloredRains.add(coloredRain);
            return this;
        }

        public CustomWeather wind(Wind wind) {
            this.winds.add(wind);
            return this;
        }

        public String getId() {
            return id;
        }

        public Random getRandom() {
            return random;
        }

        public WeatherTrigger getTrigger() {
            return trigger;
        }

        public List<Fog> getFogs() {
            return fogs;
        }

        public List<ColoredRain> getColoredRains() {
            return coloredRains;
        }

        public List<Wind> getWinds() {
            return winds;
        }
    }

    // Inner weather components:
    public static class WeatherTrigger {
        private int minimumDuration = 1200;
        private int maximumDuration = 3600;
        private int minimumStillness = 1200;
        private int maximumStillness = 3600;
        private boolean ensureSatisfied = false;

        public WeatherTrigger duration(int minimumDuration, int maximumDuration) {
            this.minimumDuration = Math.min(minimumDuration, maximumDuration);
            this.maximumDuration = Math.max(minimumDuration, maximumDuration);
            return this;
        }

        public WeatherTrigger stillness(int minimumStillness, int maximumStillness) {
            this.minimumStillness = Math.min(minimumStillness, maximumStillness);
            this.maximumStillness = Math.max(minimumStillness, maximumStillness);
            return this;
        }

        public WeatherTrigger ensureSatisfied(boolean ensureSatisfied) {
            this.ensureSatisfied = ensureSatisfied;
            return this;
        }

        public int getDuration(Random random) {
            if (minimumDuration == maximumDuration) {
                return minimumDuration;
            }

            return minimumDuration + random.nextInt(maximumDuration - minimumDuration + 1);
        }

        public int getStillness(Random random) {
            if (minimumStillness == maximumStillness) {
                return minimumStillness;
            }

            return minimumStillness + random.nextInt(maximumStillness - minimumStillness + 1);
        }

        public int getDuration() {
            if (minimumDuration == maximumDuration) {
                return minimumDuration;
            }

            return minimumDuration + new Random().nextInt(maximumDuration - minimumDuration + 1);
        }

        public int getStillness() {
            if (minimumStillness == maximumStillness) {
                return minimumStillness;
            }

            return minimumStillness + new Random().nextInt(maximumStillness - minimumStillness + 1);
        }

        public boolean doesEnsureSatisfied() {
            return ensureSatisfied;
        }
    }

    public static class Fog {
        private int color = DefaultColor.FOG;
        private float minimumDistance = 4f;
        private float maximumDistance = 64f;
        private final SortedMap<Float, Float> heightOffsets = new TreeMap<>();

        public Fog color(int color) {
            this.color = color;
            return this;
        }

        public Fog distance(float minimumDistance, float maximumDistance) {
            this.minimumDistance = Math.min(minimumDistance, maximumDistance);
            this.maximumDistance = Math.max(minimumDistance, maximumDistance);
            return this;
        }

        public Fog heightOffset(float height, float offsetDistance) {
            this.heightOffsets.put(height, offsetDistance);
            return this;
        }

        public float getDistance(float height) {
            float offset = 0;

            if (!heightOffsets.isEmpty()) {
                offset = offsetInterpolation(height);
            }

            return Math.min(Math.max(minimumDistance + offset, 0), maximumDistance);
        }

        public int getColor() {
            return color;
        }

        // Always able to use (No conditions):
        public boolean isAble() {
            return true;
        }

        public boolean isAble(ServerLevel level) {
            return true;
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

            return nearestLowerOffset + delta * (nearestUpperHeight - nearestLowerHeight);
        }
    }

    // Define what behavior should override when player enter a specific climate (Biome set).
    public enum RainType {
        DEFAULT, RAIN, SNOW, DRY
    }

    public static class ColoredRain {
        private int rainColor = DefaultColor.RAIN;
        private int snowColor = DefaultColor.SNOW;
        private int skyColor = DefaultColor.RAINY_SKY;
        private RainType rainBiomesOverride = RainType.DEFAULT;
        private RainType snowBiomesOverride = RainType.DEFAULT;
        private RainType dryBiomesOverride = RainType.DEFAULT;

        public ColoredRain rainColor(int rainColor) {
            this.rainColor = rainColor;
            return this;
        }

        public ColoredRain snowColor(int snowColor) {
            this.snowColor = snowColor;
            return this;
        }

        public ColoredRain skyColor(int skyColor) {
            this.skyColor = skyColor;
            return this;
        }

        public ColoredRain rainBiomes(RainType rainBiomesOverride) {
            this.rainBiomesOverride = rainBiomesOverride;
            return this;
        }

        public ColoredRain snowBiomes(RainType snowBiomesOverride) {
            this.snowBiomesOverride = snowBiomesOverride;
            return this;
        }

        public ColoredRain dryBiomes(RainType dryBiomesOverride) {
            this.dryBiomesOverride = dryBiomesOverride;
            return this;
        }

        public int getRainColor() {
            return rainColor;
        }

        public int getSnowColor() {
            return snowColor;
        }

        public int getSkyColor() {
            return skyColor;
        }

        public RainType getRainBiomesOverride() {
            return rainBiomesOverride;
        }

        public RainType getSnowBiomesOverride() {
            return snowBiomesOverride;
        }

        public RainType getDryBiomesOverride() {
            return dryBiomesOverride;
        }

        // Only able when raining or thundering.
        public boolean isAble(ServerLevel level) {
            return level.isRaining() || level.isThundering();
        }
    }

    // Determine how the direction change.
    public enum WindType {
        STATIC, STATIC_RANGE, DYNAMIC
    }

    public static class Wind {
        private Vec3 staticVector = Vec3.ZERO;
        private Vec3 minimumVector = Vec3.ZERO;
        private Vec3 maximumVector = Vec3.ZERO;
        private WindType type = WindType.STATIC;
        private float minimumSpeedReduction = 0f;
        private float maximumSpeedReduction = 0f;
        private float minimumDriftDistance = 0f;
        private float maximumDriftDistance = 0f;
        private boolean affectRainDirection = true;
        private float dynamicSpeedChange = 0.02f;
        private double dynamicPhaseChangeX, dynamicPhaseChangeY, dynamicPhaseChangeZ;

        public Wind staticVector(double vectorX, double vectorY, double vectorZ) {
            this.type = WindType.STATIC;
            this.staticVector = new Vec3(vectorX, vectorY, vectorZ);
            return this;
        }

        public Wind staticVector(double minimumX, double maximumX, double minimumY, double maximumY, double minimumZ, double maximumZ) {
            this.type = WindType.STATIC_RANGE;
            this.minimumVector = new Vec3(minimumX, minimumY, minimumZ);
            this.maximumVector = new Vec3(maximumX, maximumY, maximumZ);
            return this;
        }

        public Wind dynamicVector(Random random, double minimumX, double maximumX, double minimumY, double maximumY, double minimumZ, double maximumZ) {
            this.type = WindType.DYNAMIC;
            this.minimumVector = new Vec3(minimumX, minimumY, minimumZ);
            this.maximumVector = new Vec3(maximumX, maximumY, maximumZ);

            this.dynamicPhaseChangeX = random.nextDouble() * 2 * Math.PI;
            this.dynamicPhaseChangeY = random.nextDouble() * 2 * Math.PI;
            this.dynamicPhaseChangeZ = random.nextDouble() * 2 * Math.PI;
            return this;
        }

        public Wind staticVector(Vec3 staticVector) {
            this.type = WindType.STATIC;
            this.staticVector = staticVector;
            return this;
        }

        public Wind staticVector(Vec3 minimumVector, Vec3 maximumVector) {
            this.type = WindType.STATIC_RANGE;
            this.minimumVector = minimumVector;
            this.maximumVector = maximumVector;
            return this;
        }

        public Wind dynamicVector(Random random, Vec3 minimumVector, Vec3 maximumVector) {
            this.type = WindType.DYNAMIC;
            this.minimumVector = minimumVector;
            this.maximumVector = maximumVector;

            this.dynamicPhaseChangeX = random.nextDouble() * 2 * Math.PI;
            this.dynamicPhaseChangeY = random.nextDouble() * 2 * Math.PI;
            this.dynamicPhaseChangeZ = random.nextDouble() * 2 * Math.PI;
            return this;
        }

        public Wind speedReduction(float minimumSpeedReduction, float maximumSpeedReduction) {
            this.minimumSpeedReduction = Math.min(minimumSpeedReduction, maximumSpeedReduction);
            this.maximumSpeedReduction = Math.max(minimumSpeedReduction, maximumSpeedReduction);
            return this;
        }

        public Wind driftDistance(float minimumDriftDistance, float maximumDriftDistance) {
            this.minimumDriftDistance = Math.min(minimumDriftDistance, maximumDriftDistance);
            this.maximumDriftDistance = Math.max(minimumDriftDistance, maximumDriftDistance);
            return this;
        }

        public Wind affectWindDirection(boolean affectRainDirection) {
            this.affectRainDirection = affectRainDirection;
            return this;
        }

        public Wind dynamicSpeed(float dynamicSpeedChange) {
            this.dynamicSpeedChange = dynamicSpeedChange;
            return this;
        }

        public Vec3 getStaticVector() {
            return staticVector;
        }

        public Vec3 getVector(Random random) {
            return switch (type) {
                case STATIC -> staticVector;
                case STATIC_RANGE -> new Vec3(
                        minimumVector.x + random.nextDouble() * (maximumVector.x - minimumVector.x),
                        minimumVector.y + random.nextDouble() * (maximumVector.y - minimumVector.y),
                        minimumVector.z + random.nextDouble() * (maximumVector.z - minimumVector.z)
                );
                case DYNAMIC -> new Vec3(
                        minimumVector.x + (maximumVector.x - minimumVector.x) * (0.5 + 0.5 * Math.sin(dynamicPhaseChangeX)),
                        minimumVector.y + (maximumVector.y - minimumVector.y) * (0.5 + 0.5 * Math.sin(dynamicPhaseChangeY)),
                        minimumVector.z + (maximumVector.z - minimumVector.z) * (0.5 + 0.5 * Math.sin(dynamicPhaseChangeZ))
                );
            };
        }

        public void tickDynamic(Random random) {
            if (type == WindType.DYNAMIC) {
                dynamicPhaseChangeX += dynamicSpeedChange * (0.5 + 0.5 * random.nextDouble());
                dynamicPhaseChangeY += dynamicSpeedChange * (0.5 + 0.5 * random.nextDouble());
                dynamicPhaseChangeZ += dynamicSpeedChange * (0.5 + 0.5 * random.nextDouble());
            }
        }

        public float getSpeedReduction(Random random) {
            if (minimumSpeedReduction == maximumSpeedReduction) {
                return minimumSpeedReduction;
            }

            return minimumSpeedReduction + random.nextFloat() * (maximumSpeedReduction - minimumSpeedReduction);
        }

        public float getDriftDistance(Random random) {
            if (minimumDriftDistance == maximumDriftDistance) {
                return minimumDriftDistance;
            }

            return minimumDriftDistance + random.nextFloat() * maximumDriftDistance;
        }

        public boolean doesAffectRainDirection() {
            return affectRainDirection;
        }

        public WindType getWindType() {
            return type;
        }

        // Always able to use (No conditions):
        public boolean isAble() {
            return true;
        }

        public boolean isAble(ServerLevel level) {
            return true;
        }
    }
}
