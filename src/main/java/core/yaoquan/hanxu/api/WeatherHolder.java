package core.yaoquan.hanxu.api;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.util.Creator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Weather System API
 * @since 0.7.0 (Internal Development)
 */
@EventBusSubscriber(modid = CoreHanXu.MOD_ID)
public class WeatherHolder {
    public static class DefaultColor {
        public static final int RAIN = 0xCFEBFF;
        public static final int RAINY_SKY = 0x4D82A8;
        public static final int SNOW = 0xEDF8FF;
        public static final int FOG = 0xCCDDEE;
    }

    public enum WeatherType {
        FOG, COLORED_RAIN, WIND,
        COLORED_MOON, PARTICLE_STORM,
        AURORA, VOID_FOG,
    }

    public interface WeatherDefinition {
        String getId();
        Random getRandom();
        int getMinimumDuration();
        int getMaximumDuration();
        int getMinimumStillness();
        int getMaximumStillness();
        WeatherType getWeatherType();
        boolean isAble(ServerLevel level);
        WeatherInstance create(Random random);
    }

    public static class WeatherInstance {
        private final String id;
        private final WeatherType type;
        private final WeatherDefinition definition;
        private int remainingTicks;
        private int durationTicks;
        private int stillnessTicks;
        private boolean isStillness;
        private boolean isActive;
        private boolean isReady;

        public WeatherInstance(String id, WeatherType type, int durationTicks, int stillnessTicks, WeatherDefinition definition) {
            this.id = id;
            this.type = type;
            this.remainingTicks = durationTicks;
            this.durationTicks = durationTicks;
            this.stillnessTicks = stillnessTicks;
            this.definition = definition;

            this.isStillness = false;
            this.isActive = false;
            this.isReady = false;
        }

        public void activate() {
            this.isActive = true;
            this.isReady = false;
            this.remainingTicks = this.durationTicks;
        }

        public void ready() {
            this.isReady = true;
            this.isActive = false;
            this.isStillness = false;
        }

        public void stillness() {
            this.isStillness = true;
            this.isActive = false;
            this.isReady = false;
            this.remainingTicks = this.stillnessTicks;
        }

        public void tickCount() {
            if (remainingTicks <= 0) {
                return;
            }

            remainingTicks--;
        }

        public void setRemainingTicks(int ticks) {
            this.remainingTicks = ticks;
        }

        public void setDurationTicks(int ticks) {
            this.durationTicks = ticks;
        }

        public void setStillnessTicks(int ticks) {
            this.stillnessTicks = ticks;
        }

        public boolean doesStageChange() {
            return remainingTicks <= 0;
        }

        public String getId() {
            return id;
        }

        public WeatherType getType() {
            return type;
        }

        public WeatherDefinition getDefinition() {
            return definition;
        }

        public int getRemainingTicks() {
            return remainingTicks;
        }

        public int getDurationTicks() {
            return durationTicks;
        }

        public int getStillnessTicks() {
            return stillnessTicks;
        }

        public boolean doesStillness() {
            return isStillness;
        }

        public boolean doesActive() {
            return isActive;
        }

        public boolean doesReady() {
            return isReady;
        }
    }

    public static class WeatherState {
        private WeatherInstance activeInstance;
        private final Map<WeatherType, WeatherInstance> readyInstances = new ConcurrentHashMap<>();

        public void activeNextInstance(WeatherType type) {
            WeatherInstance instance = readyInstances.get(type);
            if (instance == null) {
                return;
            }

            instance.activate();
            this.activeInstance = instance;

            readyInstances.remove(type);
        }

        public WeatherInstance getActiveInstance() {
            return activeInstance;
        }

        public WeatherInstance getReadyInstance(WeatherType type) {
            return readyInstances.get(type);
        }

        public void setActiveInstance(WeatherInstance activeInstance) {
            this.activeInstance = activeInstance;
        }

        public void setReadyInstance(WeatherInstance readyInstance) {
            if (readyInstance == null) {
                return;
            }

            readyInstances.put(readyInstance.getType(), readyInstance);
        }

        public void clearReadyInstance(WeatherType type) {
            readyInstances.remove(type);
        }

        public void clearAllReadyInstance() {
            readyInstances.clear();
        }

        public boolean doesReady(WeatherType type) {
            WeatherInstance instance = readyInstances.get(type);
            return instance != null && instance.doesReady();
        }

        public boolean doesReady(String id) {
            for (WeatherInstance instance : readyInstances.values()) {
                if (instance.getId().equals(id) && instance.doesReady()) {
                    return true;
                }
            }

            return false;
        }

        public boolean doesAnyReady() {
            return !readyInstances.isEmpty();
        }

        public Collection<WeatherInstance> getAllReadyInstances() {
            return readyInstances.values();
        }

        public boolean doesActive(String id) {
            return activeInstance != null && activeInstance.getId().equals(id);
        }

        public boolean doesPrepareOrUsing(String id) {
            if (activeInstance != null && activeInstance.getId().equals(id)) {
                return true;
            }

            for (WeatherInstance instance : readyInstances.values()) {
                if (instance.getId().equals(id)) {
                    return true;
                }
            }

            return false;
        }
    }

    // All registered weather.
    private static final Map<String, WeatherDefinition> apiWeathers = new ConcurrentHashMap<>();
    private static final Map<String, WeatherDefinition> commandWeathers = new ConcurrentHashMap<>();
    // State of weather.
    private static final Map<ResourceKey<Level>, WeatherState> weatherStates = new ConcurrentHashMap<>();

    public static void register(WeatherDefinition weatherDefinition) {
        apiWeathers.put(weatherDefinition.getId(), weatherDefinition);
        CoreHanXu.LOGGER.info("[HX] Registered weather definition: {}", weatherDefinition.getId());
    }

    public static void unregister(WeatherDefinition weatherDefinition) {
        apiWeathers.remove(weatherDefinition.getId());
        CoreHanXu.LOGGER.info("[HX] Unregistered weather definition: {}", weatherDefinition.getId());
    }

    public static boolean register(String id, WeatherType type, Random random, Map<String, Object> parameters) {
        if (apiWeathers.containsKey(id) || commandWeathers.containsKey(id)) {
            CoreHanXu.LOGGER.warn("[HX] Reject duplicated weather definition: {}", id);
            return false;
        }
        WeatherDefinition definition = Creator.createWeatherDefinition(id, type, random, parameters);
        if (definition == null) {
            return false;
        }

        commandWeathers.put(id, definition);

        CoreHanXu.LOGGER.info("[HX] Registered weather definition by command: {}", id);
        return true;
    }
}
