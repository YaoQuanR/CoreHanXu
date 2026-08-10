package core.yaoquan.hanxu.api.weather;

import core.yaoquan.hanxu.api.WeatherHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/// @since 0.7.0 (Internal Development)
public class Wind implements WeatherHolder.WeatherDefinition {
    // Determine how the direction change.
    public enum WindType {
        STATIC, STATIC_RANGE, DYNAMIC
    }

    private final String id;
    private final Random random;
    private Vec3 staticVector = Vec3.ZERO;
    private Vec3 minimumVector = Vec3.ZERO;
    private Vec3 maximumVector = Vec3.ZERO;
    private WindType type = WindType.STATIC;
    private float minimumSpeedReduction = 0f;
    private float maximumSpeedReduction = 0f;
    private float minimumDriftDistance = 0f;
    private float maximumDriftDistance = 0f;
    private boolean affectRain = true;
    private float dynamicSpeedChange = 0.02f;
    private double dynamicPhaseChangeX, dynamicPhaseChangeY, dynamicPhaseChangeZ;
    private int minimumDuration = 1200;
    private int maximumDuration = 6000;
    private int minimumStillness = 1200;
    private int maximumStillness = 6000;

    public Wind(String id, Random random) {
        this.id = id;
        this.random = random;

        this.dynamicPhaseChangeX = random.nextDouble() * 2 * Math.PI;
        this.dynamicPhaseChangeY = random.nextDouble() * 2 * Math.PI;
        this.dynamicPhaseChangeZ = random.nextDouble() * 2 * Math.PI;
    }

    public Wind staticVector(double x, double y, double z) {
        this.type = WindType.STATIC;
        this.staticVector = new Vec3(x, y, z);
        return this;
    }

    public Wind staticVector(Vec3 vector) {
        this.type = WindType.STATIC;
        this.staticVector = vector;
        return this;
    }

    public Wind staticVector(double minimumX, double maximumX, double minimumY, double maximumY, double minimumZ, double maximumZ) {
        this.type = WindType.STATIC_RANGE;
        this.minimumVector = new Vec3(minimumX, minimumY, minimumZ);
        this.maximumVector = new Vec3(maximumX, maximumY, maximumZ);
        return this;
    }

    public Wind staticVector(Vec3 minimumVector, Vec3 maximumVector) {
        this.type = WindType.STATIC_RANGE;
        this.minimumVector = minimumVector;
        this.maximumVector = maximumVector;
        return this;
    }

    public Wind dynamicVector(double minimumX, double maximumX, double minimumY, double maximumY, double minimumZ, double maximumZ) {
        this.type = WindType.DYNAMIC;
        this.minimumVector = new Vec3(minimumX, minimumY, minimumZ);
        this.maximumVector = new Vec3(maximumX, maximumY, maximumZ);
        return this;
    }

    public Wind dynamicVector(Vec3 minimumVector, Vec3 maximumVector) {
        this.type = WindType.DYNAMIC;
        this.minimumVector = minimumVector;
        this.maximumVector = maximumVector;
        return this;
    }

    public Wind speedReduction(float minimum, float maximum) {
        this.minimumSpeedReduction = Math.min(minimum, maximum);
        this.maximumSpeedReduction = Math.max(minimum, maximum);
        return this;
    }

    public Wind driftDistance(float minimum, float maximum) {
        this.minimumDriftDistance = Math.min(minimum, maximum);
        this.maximumDriftDistance = Math.max(minimum, maximum);
        return this;
    }

    public Wind affectRain(boolean effect) {
        this.affectRain = effect;
        return this;
    }

    public Wind dynamicSpeedChange(float value) {
        this.dynamicSpeedChange = value;
        return this;
    }

    public Wind duration(int minimum, int maximum) {
        this.minimumDuration = Math.min(minimum, maximum);
        this.maximumDuration = Math.max(minimum, maximum);
        return this;
    }

    public Wind stillness(int minimum, int maximum) {
        this.minimumStillness = Math.min(minimum, maximum);
        this.maximumStillness = Math.max(minimum, maximum);
        return this;
    }

    public void tickDynamic(Random random) {
        if (type != WindType.DYNAMIC) {
            return;
        }

        dynamicPhaseChangeX = dynamicSpeedChange * (0.5 + 0.5 * random.nextDouble());
        dynamicPhaseChangeY = dynamicSpeedChange * (0.5 + 0.5 * random.nextDouble());
        dynamicPhaseChangeZ = dynamicSpeedChange * (0.5 + 0.5 * random.nextDouble());
    }

    public Vec3 getVector() {
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

        return minimumDriftDistance + random.nextFloat() * (maximumDriftDistance - minimumDriftDistance);
    }

    @Override
    public WeatherHolder.WeatherInstance createInstance(Random random) {
        int duration = minimumDuration + random.nextInt(maximumDuration - minimumDuration + 1);
        int stillness = minimumStillness + random.nextInt(maximumStillness - minimumStillness + 1);
        return new WeatherHolder.WeatherInstance(
                id,
                WeatherHolder.WeatherType.WIND,
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

    // Always able to use (No conditions).
    @Override
    public boolean isAble(ServerLevel level) {
        return true;
    }

    @Override
    public WeatherHolder.WeatherType getWeatherType() {
        return WeatherHolder.WeatherType.WIND;
    }

    public Vec3 getStaticVector() {
        return staticVector;
    }

    public Vec3 getMinimumVector() {
        return minimumVector;
    }

    public Vec3 getMaximumVector() {
        return maximumVector;
    }

    public WindType getWindType() {
        return type;
    }

    public float getMinimumSpeedReduction() {
        return minimumSpeedReduction;
    }

    public float getMaximumSpeedReduction() {
        return maximumSpeedReduction;
    }

    public float getMinimumDriftDistance() {
        return minimumDriftDistance;
    }

    public float getMaximumDriftDistance() {
        return maximumDriftDistance;
    }

    public boolean doesAffectRain() {
        return affectRain;
    }

    public float getDynamicSpeedChange() {
        return dynamicSpeedChange;
    }

    public double getDynamicPhaseChangeX() {
        return dynamicPhaseChangeX;
    }

    public double getDynamicPhaseChangeY() {
        return dynamicPhaseChangeY;
    }

    public double getDynamicPhaseChangeZ() {
        return dynamicPhaseChangeZ;
    }

    @Override
    public String toString() {
        return "HanXu(Core):WeatherHolder-Definitions:{id=" + id +
                ", staticVector=" + staticVector +
                ", minimumVector=" + minimumVector +
                ", maximumVector=" + maximumVector +
                ", windType=" + type +
                ", speedReduction=" + minimumSpeedReduction +
                "~" + maximumSpeedReduction +
                ", driftDistance=" + minimumDriftDistance +
                "~" + maximumDriftDistance +
                ", affectRain=" + affectRain +
                ", dynamicSpeedChange=" + dynamicSpeedChange +
                ", dynamicPhaseChange=[" + dynamicPhaseChangeX +
                "," + dynamicPhaseChangeY +
                "," + dynamicPhaseChangeZ +
                "], duration=" + minimumDuration +
                "~" + maximumDuration +
                ", stillness=" + minimumStillness +
                "~" + maximumStillness +
                "}";
    }
}
