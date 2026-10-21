package core.yaoquan.hanxu.api.weather;

import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.registry.config.GeneralConfig;
import core.yaoquan.hanxu.registry.event.payload.WeatherPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Random;

/**
 * <p><h3>
 *     Wind
 * </h3></p>
 * <p>
 *     Classic type that implements from {@link WeatherHolder.WeatherDefinition} and {@link WeatherHolder.WeatherType}.
 * </p>
 *
 * @since 0.7.2 (Internal Development)
 */
public class Wind implements WeatherHolder.WeatherDefinition {
    // Determine how the direction change.
    public enum WindType {
        STATIC, DYNAMIC
    }

    private final String id;
    private final Random random;
    private Vec3 unitVector = Vec3.ZERO;
    private float velocity = 0f;
    private Vec3 minimumDeltaDirection = Vec3.ZERO;
    private Vec3 maximumDeltaDirection = Vec3.ZERO;
    private float minimumDeltaVelocity = 0f;
    private float maximumDeltaVelocity = 0f;
    private double dynamicSpeedChange = 0.02f;
    private boolean affectRain = true;
    private float driftIntensityRatio = 0f;
    private final double initialPhaseX, initialPhaseY, initialPhaseZ, initialPhaseV;
    private final WindType type;

    private int minimumDuration = 1200;
    private int maximumDuration = 6000;
    private int minimumStillness = 1200;
    private int maximumStillness = 6000;

    public Wind(String id, Random random, WindType type) {
        this.id = id;
        this.random = random;
        this.type = type;

        this.initialPhaseX = random.nextDouble() * 2 * Math.PI;
        this.initialPhaseY = random.nextDouble() * 2 * Math.PI;
        this.initialPhaseZ = random.nextDouble() * 2 * Math.PI;
        this.initialPhaseV = random.nextDouble() * 2 * Math.PI;
    }

    public Wind staticVector(Vec3 unitVector) {
        if (!this.type.equals(WindType.STATIC)) {
            return this;
        }

        this.unitVector = unitVector.normalize();
        return this;
    }

    public Wind staticVector(double vectorX, double vectorY, double vectorZ) {
        if (!this.type.equals(WindType.STATIC)) {
            return this;
        }

        this.unitVector = new Vec3(vectorX, vectorY, vectorZ).normalize();
        return this;
    }

    public Wind dynamicVector(Vec3 minVector, Vec3 maxVector) {
        if (!this.type.equals(WindType.DYNAMIC)) {
            return this;
        }

        this.unitVector = minVector.normalize();
        this.minimumDeltaDirection = minVector.normalize();
        this.maximumDeltaDirection = maxVector.normalize();
        return this;
    }

    public Wind dynamicVector(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (!this.type.equals(WindType.DYNAMIC)) {
            return this;
        }

        this.unitVector = new Vec3(minX, minY, minZ).normalize();
        this.minimumDeltaDirection = new Vec3(minX, minY, minZ).normalize();
        this.maximumDeltaDirection = new Vec3(maxX, maxY, maxZ).normalize();
        return this;
    }

    public Wind velocity(float velocity) {
        this.velocity = velocity;
        this.minimumDeltaVelocity = velocity;
        this.maximumDeltaVelocity = velocity;
        return this;
    }

    public Wind velocityRange(float minVelocity, float maxVelocity) {
        if (!this.type.equals(WindType.DYNAMIC)) {
            return this;
        }

        this.minimumDeltaVelocity = minVelocity;
        this.maximumDeltaVelocity = maxVelocity;
        return this;
    }

    public Wind dynamicSpeedChange(float change) {
        this.dynamicSpeedChange = change;
        return this;
    }

    public Wind driftIntensityRatio(float ratio) {
        this.driftIntensityRatio = ratio;
        return this;
    }

    public Wind affectRain(boolean effect) {
        this.affectRain = effect;
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

    public Vec3 getUnitVector(WeatherHolder.WeatherInstance instance) {
        return switch (type) {
            case STATIC -> unitVector;
            case DYNAMIC -> new Vec3(
                    minimumDeltaDirection.x + (maximumDeltaDirection.x - minimumDeltaDirection.x) * (0.5 + 0.5 * Math.sin(currentPhaseX(instance))),
                    minimumDeltaDirection.y + (maximumDeltaDirection.y - minimumDeltaDirection.y) * (0.5 + 0.5 * Math.sin(currentPhaseY(instance))),
                    minimumDeltaDirection.z + (maximumDeltaDirection.z - minimumDeltaDirection.z) * (0.5 + 0.5 * Math.sin(currentPhaseZ(instance)))
            ).normalize();
        };
    }

    public float getVelocity(WeatherHolder.WeatherInstance instance) {
        return switch (type) {
            case STATIC -> velocity;
            case DYNAMIC -> (float) (minimumDeltaVelocity + (maximumDeltaVelocity - minimumDeltaVelocity) * (0.5 + 0.5 * Math.sin(currentPhaseV(instance))));
        };
    }

    public float getIntensity(WeatherHolder.WeatherInstance instance) {
        float transition = transitionSpeed(instance);

        return switch (type) {
            case STATIC -> 1.0f;
            case DYNAMIC -> {
                if (maximumDeltaVelocity <= 0f) {
                    yield 0f;
                }
                yield Math.min(1.0f, getVelocity(instance) / maximumDeltaVelocity) * transition;
            }
        };
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
    public void sendToPlayer(ServerPlayer player, WeatherHolder.WeatherInstance instance, String dimension) {
        if (player == null || instance == null) {
            return;
        }

        switch (instance.getPhase()) {
            case ACTIVE, STILLNESS -> {
                Vec3 unitVector = getUnitVector(instance);
                float velocity = getVelocity(instance);
                float intensity = getIntensity(instance);

                WeatherPayload.WindPacket packet = new WeatherPayload.WindPacket(
                        instance.getPhase(),
                        dimension,
                        unitVector.x(),
                        unitVector.y(),
                        unitVector.z(),
                        velocity,
                        intensity,
                        driftIntensityRatio,
                        affectRain
                );

                PacketDistributor.sendToPlayer(player, packet);
            }
            default -> {}
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

    // Always able to use (No conditions).
    @Override
    public boolean isAble(ServerLevel level) {
        return true;
    }

    @Override
    public WeatherHolder.WeatherType getWeatherType() {
        return WeatherHolder.WeatherType.WIND;
    }

    public WindType getWindType() {
        return type;
    }

    public Vec3 getUnitVector() {
        return unitVector;
    }

    public float getVelocity() {
        return velocity;
    }

    public Vec3 getMinimumDeltaDirection() {
        return minimumDeltaDirection;
    }

    public Vec3 getMaximumDeltaDirection() {
        return maximumDeltaDirection;
    }

    public float getMinimumDeltaVelocity() {
        return minimumDeltaVelocity;
    }

    public float getMaximumDeltaVelocity() {
        return maximumDeltaVelocity;
    }

    public double getDynamicSpeedChange() {
        return dynamicSpeedChange;
    }

    public float getDriftIntensityRatio() {
        return driftIntensityRatio;
    }

    @Override
    public String toString() {
        return "HanXu(Core):WeatherHolder-Definitions:{id=" + id +
                ", unitVector=" + unitVector +
                ", velocity=" + velocity +
                ", deltaDirection=" + minimumDeltaDirection +
                "~" + maximumDeltaDirection +
                ", deltaVelocity=" + minimumDeltaVelocity +
                "~" + maximumDeltaVelocity +
                ", dynamicSpeedChange=" + dynamicSpeedChange +
                ", driftIntensityRatio=" + driftIntensityRatio +
                ", initialPhase={" + initialPhaseX +
                ", " + initialPhaseY +
                ", " + initialPhaseZ +
                "}, affectedRain=" + affectRain +
                ", duration=" + minimumDuration +
                "~" + maximumDuration +
                ", stillness=" + minimumStillness +
                "~" + maximumStillness +
                "}";
    }

    private int elapsedTicks(WeatherHolder.WeatherInstance instance) {
        return instance.getInitialTicks() - instance.getRemainingTicks();
    }

    private double currentPhaseX(WeatherHolder.WeatherInstance instance) {
        return initialPhaseX + elapsedTicks(instance) * dynamicSpeedChange;
    }

    private double currentPhaseY(WeatherHolder.WeatherInstance instance) {
        return initialPhaseY + elapsedTicks(instance) * dynamicSpeedChange;
    }

    private double currentPhaseZ(WeatherHolder.WeatherInstance instance) {
        return initialPhaseZ + elapsedTicks(instance) * dynamicSpeedChange;
    }

    private double currentPhaseV(WeatherHolder.WeatherInstance instance) {
        return initialPhaseV + elapsedTicks(instance) * dynamicSpeedChange;
    }

    private float transitionSpeed(WeatherHolder.WeatherInstance instance) {
        int transitionSecond = GeneralConfig.windTransitionSecond.getAsInt();

        if (transitionSecond <= 0) {
            return 1.0f;
        }

        if (instance.getInitialTicks() <= 0) {
            return 1.0f;
        }

        float transitionTicks = transitionSecond * 20;
        int elapsedTicks = instance.getInitialTicks() - instance.getRemainingTicks();
        int remainingTicks = instance.getRemainingTicks();

        float raise = Math.min(1.0f, elapsedTicks / transitionTicks);
        float fall = Math.min(1.0f, remainingTicks / transitionTicks);

        return Math.min(raise, fall);
    }
}
