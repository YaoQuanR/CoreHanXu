package core.yaoquan.hanxu.api;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.define.Error;
import core.yaoquan.hanxu.api.define.FilePath;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.api.event.WeatherStartEvent;
import core.yaoquan.hanxu.util.Cast;
import core.yaoquan.hanxu.util.Creator;
import core.yaoquan.hanxu.util.YamlReader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
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
        NULL,
    }

    public enum WeatherPhase {
        ACTIVE, READY, STILLNESS, PAUSE_ACTIVE, IDLE,
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
        WeatherInstance createInstance(Random random);
    }

    public static class WeatherInstance {
        private final String id;
        private final WeatherType type;
        private final WeatherDefinition definition;
        private int remainingTicks;
        private int initialTicks;
        private int durationTicks;
        private int stillnessTicks;
        private WeatherPhase phase;

        public WeatherInstance(String id, WeatherType type, int durationTicks, int stillnessTicks, WeatherDefinition definition) {
            this.id = id;
            this.type = type;
            this.remainingTicks = durationTicks;
            this.durationTicks = durationTicks;
            this.stillnessTicks = stillnessTicks;
            this.definition = definition;

            this.phase = WeatherPhase.IDLE;
        }

        public void activate() {
            this.phase = WeatherPhase.ACTIVE;
            this.remainingTicks = this.durationTicks;
            this.initialTicks = this.durationTicks;
            this.durationTicks = definition.getMinimumDuration() + definition.getRandom().nextInt(definition.getMaximumDuration() - definition.getMinimumDuration() + 1);
        }

        public void ready() {
            this.phase = WeatherPhase.READY;
        }

        public void stillness() {
            this.phase = WeatherPhase.STILLNESS;
            this.remainingTicks = this.stillnessTicks;
            this.initialTicks = this.stillnessTicks;
            this.stillnessTicks = definition.getMinimumStillness() + definition.getRandom().nextInt(definition.getMaximumStillness() - definition.getMinimumStillness() + 1);
        }

        public void pause() {
            if (this.phase == WeatherPhase.ACTIVE) {
                this.phase = WeatherPhase.PAUSE_ACTIVE;
            }
        }

        public void resume() {
            this.phase = WeatherPhase.ACTIVE;
        }

        public void idle() {
            this.phase = WeatherPhase.IDLE;
        }

        public void reset() {
            this.remainingTicks = this.initialTicks;
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

        public int getInitialTicks() {
            return initialTicks;
        }

        public int getDurationTicks() {
            return durationTicks;
        }

        public int getStillnessTicks() {
            return stillnessTicks;
        }

        public boolean doesStillness() {
            return phase == WeatherPhase.STILLNESS;
        }

        public boolean doesActive() {
            return phase == WeatherPhase.ACTIVE;
        }

        public boolean doesReady() {
            return phase == WeatherPhase.READY;
        }

        public boolean doesPauseActive() {
            return phase == WeatherPhase.PAUSE_ACTIVE;
        }

        public boolean doesIdle() {
            return phase == WeatherPhase.IDLE;
        }
    }

    public static class WeatherState {
        private final Map<WeatherType, WeatherInstance> activeInstances = new ConcurrentHashMap<>();
        private final Map<WeatherType, WeatherInstance> readyInstances = new ConcurrentHashMap<>();
        private final Map<WeatherType, WeatherInstance> stillnessInstances = new ConcurrentHashMap<>();

        public void setNextActive(WeatherType type) {
            WeatherInstance instance = readyInstances.get(type);
            if (instance == null) {
                return;
            }

            WeatherInstance stillnessInstance = activeInstances.get(type);
            if (stillnessInstance != null) {
                stillnessInstance.stillness();
                stillnessInstances.put(type, stillnessInstance);
            }

            instance.activate();
            activeInstances.put(type, instance);
            readyInstances.remove(type);
        }

        public void setActive(WeatherInstance instance) {
            if (instance == null) {
                return;
            }

            stillnessInstances.remove(instance.getType());

            instance.activate();
            activeInstances.put(instance.getType(), instance);
        }

        public void setReady(WeatherInstance instance) {
            if (instance == null) {
                return;
            }

            stillnessInstances.remove(instance.getType());

            instance.ready();
            readyInstances.put(instance.getType(), instance);
        }

        public void pauseActive(WeatherType type) {
            WeatherInstance instance = activeInstances.get(type);
            if (instance != null) {
                instance.pause();
            }
        }

        public void resumeActive(WeatherType type) {
            WeatherInstance instance = activeInstances.get(type);
            if (instance != null) {
                instance.resume();
            }
        }

        public void pauseActive(String id) {
            for (WeatherInstance instance : activeInstances.values()) {
                if (instance.getId().equals(id)) {
                    instance.pause();
                }
            }
        }

        public void resumeActive(String id) {
            for (WeatherInstance instance : activeInstances.values()) {
                if (instance.getId().equals(id)) {
                    instance.resume();
                }
            }
        }

        public void setActiveToStillness(WeatherType type) {
            WeatherInstance instance = activeInstances.get(type);
            if (instance == null) {
                return;
            }
            instance.stillness();
            activeInstances.remove(type);
            stillnessInstances.put(type, instance);
        }

        public void setReadyToStillness(WeatherType type) {
            WeatherInstance instance = readyInstances.get(type);
            if (instance == null) {
                return;
            }
            instance.stillness();
            readyInstances.remove(type);
            stillnessInstances.put(type, instance);
        }

        public void setActiveToStillness(String id) {
            for (WeatherInstance instance : activeInstances.values()) {
                if (instance.getId().equals(id)) {
                    instance.stillness();
                    activeInstances.remove(instance.getType());
                    stillnessInstances.put(instance.getType(), instance);
                    break;
                }
            }
        }

        public void setReadyToStillness(String id) {
            for (WeatherInstance instance : readyInstances.values()) {
                if (instance.getId().equals(id)) {
                    instance.stillness();
                    readyInstances.remove(instance.getType());
                    stillnessInstances.put(instance.getType(), instance);
                    break;
                }
            }
        }

        public void setAllActiveInstancesToStillness() {
            for (WeatherInstance instance : activeInstances.values()) {
                instance.stillness();
                stillnessInstances.put(instance.getType(), instance);
            }
            activeInstances.clear();
        }

        public void clearAllReadyInstancesToStillness() {
            for (WeatherInstance instance : readyInstances.values()) {
                instance.stillness();
                stillnessInstances.put(instance.getType(), instance);
            }
            readyInstances.clear();
        }

        public void putActiveInstance(WeatherType type, WeatherInstance instance) {
            activeInstances.put(type, instance);
        }

        public void putReadyInstance(WeatherType type, WeatherInstance instance) {
            readyInstances.put(type, instance);
        }

        public void putStillnessInstance(WeatherType type, WeatherInstance instance) {
            stillnessInstances.put(type, instance);
        }

        public Collection<WeatherInstance> getActiveInstances() {
            return activeInstances.values();
        }

        public Collection<WeatherInstance> getReadyInstances() {
            return readyInstances.values();
        }

        public Collection<WeatherInstance> getStillnessInstances() {
            return stillnessInstances.values();
        }

        public WeatherInstance getActiveInstance(WeatherType type) {
            return activeInstances.get(type);
        }

        public WeatherInstance getReadyInstance(WeatherType type) {
            return readyInstances.get(type);
        }

        public WeatherInstance getStillnessInstance(WeatherType type) {
            return stillnessInstances.get(type);
        }

        public boolean doesActive(WeatherType type) {
            return activeInstances.get(type) != null;
        }

        public boolean doesReady(WeatherType type) {
            return readyInstances.get(type) != null;
        }

        public boolean doesStillness(WeatherType type) {
            return stillnessInstances.get(type) != null;
        }

        public boolean doesActive(String id) {
            for (WeatherInstance instance : activeInstances.values()) {
                if (instance.getId().equals(id)) {
                    return true;
                }
            }
            return false;
        }

        public boolean doesReady(String id) {
            for (WeatherInstance instance : readyInstances.values()) {
                if (instance.getId().equals(id)) {
                    return true;
                }
            }
            return false;
        }

        public boolean doesStillness(String id) {
            for (WeatherInstance instance : stillnessInstances.values()) {
                if (instance.getId().equals(id)) {
                    return true;
                }
            }
            return false;
        }

        public boolean doesAnyActive() {
            return !activeInstances.isEmpty();
        }

        public boolean doesAnyReady() {
            return !readyInstances.isEmpty();
        }

        public boolean doesAnyStillness() {
            return !stillnessInstances.isEmpty();
        }

        public boolean doesPrepareOrUsing(String id) {
            if (doesActive(id)) {
                return true;
            }
            return doesReady(id);
        }

        public boolean doesStillnessOrIdle(String id) {
            return !doesPrepareOrUsing(id);
        }
    }

    // All registered weather.
    private static final Map<String, WeatherDefinition> apiWeathers = new ConcurrentHashMap<>();
    private static final Map<String, WeatherDefinition> commandWeathers = new ConcurrentHashMap<>();
    // State of weather.
    private static final Map<ResourceKey<Level>, WeatherState> weatherStates = new ConcurrentHashMap<>();
    // Storage unclaimed states (If weather definition not registered).
    private static final Map<ResourceKey<Level>, Set<String>> unclaimedStates = new ConcurrentHashMap<>();

    public static void register(WeatherDefinition weatherDefinition) {
        apiWeathers.put(weatherDefinition.getId(), weatherDefinition);
        CoreHanXu.LOGGER.info("[HX] Registered weather definition: {}", weatherDefinition.getId());
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

    public static boolean unregister(WeatherDefinition definition) {
        for (WeatherState state : weatherStates.values()) {
            if (state.doesPrepareOrUsing(definition.getId())) {
                CoreHanXu.LOGGER.warn("[HX] Reject to unregister a weather that in use: {}", definition.getId());
                return false;
            }
        }

        if (commandWeathers.containsKey(definition.getId())) {
            commandWeathers.remove(definition.getId());
            CoreHanXu.LOGGER.info("[HX] Unregistered weather definition: {}", definition.getId());

            return true;
        }

        return false;
    }

    public static boolean unregisterAndDelete(String id, YamlReader.TargetPath targetPath) {
        WeatherDefinition definition = commandWeathers.get(id);

        boolean unregister = unregister(definition);

        if (unregister) {
            try {
                YamlReader.delete("weather", id, targetPath);
                CoreHanXu.LOGGER.info("[HX] Deleted weather YAML: {}", id);
                return true;
            }
            catch (IOException e) {
                CoreHanXu.LOGGER.warn("[HX] Failed to unregister weather and delete weather YAML: {}", id, e);
            }
        }

        return false;
    }

    public static WeatherDefinition getWeatherDefinition(String id) {
        if (apiWeathers.containsKey(id)) {
            return apiWeathers.get(id);
        }
        else {
            return commandWeathers.get(id);
        }
    }

    public static WeatherState getWeatherState(ServerLevel level) {
        ResourceKey<Level> key = level.dimension();
        return weatherStates.get(key);
    }

    public static WeatherState getWeatherStateOrNew(ServerLevel level) {
        ResourceKey<Level> key = level.dimension();
        return weatherStates.computeIfAbsent(key, k -> new WeatherState());
    }

    public static boolean startWeather(ServerLevel level, String id, int duration) {
        WeatherDefinition definition = getWeatherDefinition(id);

        // Check if definition completed.
        if (definition == null) {
            CoreHanXu.LOGGER.warn("[HX] Unknown weather definition for start: {}", id);
            return false;
        }

        WeatherState state = getWeatherState(level);

        // Check if the state completed.
        if (state == null) {
            CoreHanXu.LOGGER.warn("[HX] Unknown weather state for start: {}", id);
            return false;
        }

        // Then check if this weather activated.
        if (state.doesActive(definition.getWeatherType())) {
            CoreHanXu.LOGGER.warn("[HX] Rejected to start a activated weather type: {}", definition.getWeatherType());
            return false;
        }

        Random random = definition.getRandom();

        WeatherInstance instance;
        // Generate the stillness phase value (If specified duration: duration > 0).
        if (duration > 0) {
            int stillness = definition.getMinimumStillness() + random.nextInt(definition.getMaximumStillness() - definition.getMinimumStillness() + 1);
            instance = new WeatherInstance(id, definition.getWeatherType(), duration, stillness, definition);
        }
        // Or else use randomized duration and stillness.
        else {
            instance = definition.createInstance(random);
        }

        // Set activate.
        state.setActive(instance);

        // Post event.
        NeoForge.EVENT_BUS.post(new WeatherStartEvent(level, instance));

        CoreHanXu.LOGGER.info("[HX] Started weather: {} -> {}", id, level.dimension());
        return true;
    }

    public static boolean startWeather(ServerLevel level, String id) {
        return startWeather(level, id, -1);
    }

    public static boolean stopWeather(ServerLevel level, WeatherType type) {
        return stopOrKillWeather(level, type, "stop");
    }

    public static boolean stopWeather(ServerLevel level, String id) {
        return stopOrKillWeather(level, id, "stop");
    }

    public static boolean killWeather(ServerLevel level, WeatherType type) {
        return stopOrKillWeather(level, type, "kill");
    }

    public static boolean killWeather(ServerLevel level, String id) {
        return stopOrKillWeather(level, id, "kill");
    }

    public static boolean prepareWeather(ServerLevel level, String id) {
        WeatherDefinition definition = getWeatherDefinition(id);
        if (definition == null) {
            CoreHanXu.LOGGER.warn("[HX] Unknown weather definition for prepare: {}", id);
            return false;
        }

        WeatherState state = getWeatherStateOrNew(level);
        WeatherType type = definition.getWeatherType();

        if (state.doesActive(type)) {
            CoreHanXu.LOGGER.warn("[HX] Rejected to prepare a activated weather: {}", id);
            return false;
        }

        Random random = definition.getRandom();
        WeatherInstance instance = definition.createInstance(random);
        state.setReady(instance);

        CoreHanXu.LOGGER.info("[HX] Prepared weather: {} -> {}", id, definition.getWeatherType());
        return true;
    }

    public static void clearReady(ServerLevel level, WeatherType type) {
        WeatherState state = getWeatherState(level);
        if (state != null) {
            state.setReadyToStillness(type);
        }
    }

    public static void clearAllReady(ServerLevel level) {
        WeatherState state = getWeatherState(level);
        if (state != null) {
            state.clearAllReadyInstancesToStillness();
        }
    }

    public static boolean doesActiveWeatherExist(ServerLevel level, WeatherType type) {
        WeatherState state = getWeatherState(level);
        return state != null && state.doesActive(type);
    }

    public static boolean doesActiveWeatherExist(ServerLevel level, String id) {
        WeatherState state = getWeatherState(level);
        return state != null && state.doesActive(id);
    }

    public static boolean doesReadyWeatherExist(ServerLevel level, WeatherType type) {
        WeatherState state = getWeatherState(level);
        return state != null && state.doesReady(type);
    }

    public static boolean doesReadyWeatherExist(ServerLevel level, String id) {
        WeatherState state = getWeatherState(level);
        return state != null && state.doesReady(id);
    }

    public static boolean doesAnyActiveWeatherExist(ServerLevel level) {
        WeatherState state = getWeatherState(level);
        return state != null && state.doesAnyActive();
    }

    public static boolean doesAnyReadyWeatherExist(ServerLevel level) {
        WeatherState state = getWeatherState(level);
        return state != null && state.doesAnyReady();
    }

    public static boolean doesWeatherPaused(ServerLevel level, WeatherType type) {
        WeatherState state = getWeatherState(level);
        if (state == null) {
            return false;
        }

        WeatherInstance instance = state.getActiveInstance(type);
        return instance != null && instance.doesPauseActive();
    }

    public static boolean doesWeatherPaused(ServerLevel level, String id) {
        WeatherState state = getWeatherState(level);
        if (state == null) {
            return false;
        }

        for (WeatherInstance instance : state.getActiveInstances()) {
            if (instance.getId().equals(id) && instance.doesPauseActive()) {
                return true;
            }
        }

        return false;
    }

    public static boolean doesYamlWeatherExist(String fileName) {
        try {
            YamlReader.read("weather", fileName);
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    public static boolean doesYamlWeatherExist(String fileName, YamlReader.TargetPath targetPath) {
        return YamlReader.doesFileExist(targetPath, "weather", fileName);
    }

    public static boolean doesWeatherExist(String id) {
        return commandWeathers.containsKey(id) || apiWeathers.containsKey(id);
    }

    public static boolean doesWeatherExist(String id, String category) {
        return switch (category) {
            case "api" -> apiWeathers.containsKey(id);
            case "command", "yaml" -> commandWeathers.containsKey(id);
            default -> false;
        };
    }

    public static WeatherDefinition loadYamlWeather(String fileName) throws IOException {
        Map<String, Object> rawData = YamlReader.read("weather", fileName);

        String id = Cast.toStringOrThrow(rawData, "id", Error.returnCodeError(Error.CodeError.missingNecessaryField) + "'id' for " + fileName);
        if (!id.equals(fileName)) {
            throw new IOException(Error.returnCodeError(Error.CodeError.mismatchFileElement) + fileName + "≠" + id);
        }

        String typeString = Cast.toStringOrThrow(rawData, "type", Error.returnCodeError(Error.CodeError.missingNecessaryField) + "'type' for " + fileName);
        WeatherType type = parseType(typeString);
        if (type == WeatherType.NULL) {
            throw new IOException("[HX] Unknown weather type: " + typeString);
        }

        long seed = Cast.toLong(rawData, "seed", new Random().nextLong());
        Random random = new Random(seed);

        Map<String, Object> remainingParameters = new LinkedHashMap<>(rawData);
        remainingParameters.remove("id");
        remainingParameters.remove("type");
        remainingParameters.remove("seed");

        WeatherDefinition definition = Creator.createWeatherDefinition(
                id,
                type,
                random,
                remainingParameters
        );

        if (definition == null) {
            throw new IOException("[HX] Weather type not supported: " + typeString);
        }

        return definition;
    }

    public static void registerAllYamlWeathers() {
        List<Path> files = YamlReader.listOut("weather");
        for (Path file : files) {
            String fileName = file.getFileName().toString().replace(".yaml", "");
            try {
                WeatherDefinition definition = loadYamlWeather(fileName);
                registerYamlWeather(definition);
            }
            catch (IOException e) {
                CoreHanXu.LOGGER.warn("[HX] Failed to load YAML weather: {}", fileName, e);
            }
        }
    }

    public static void saveAllWeatherStates(ServerLevel level) {
        String headKey = "core.yaoquan.hanxu.weathers";
        CompoundTag root = new CompoundTag();
        CompoundTag weatherTag = new CompoundTag();

        WeatherState state = weatherStates.get(level.dimension());
        if (state == null) {
            return;
        }

        for (WeatherInstance instance : state.getActiveInstances()) {
            CompoundTag instanceTag = new CompoundTag();
            instanceTag.putString("type", instance.getType().name().toLowerCase());
            instanceTag.putInt("remaining", instance.getRemainingTicks());
            instanceTag.putInt("duration", instance.getDurationTicks());
            instanceTag.putInt("stillness", instance.getStillnessTicks());
            instanceTag.putString("phase", instance.phase.name().toLowerCase());

            weatherTag.put(instance.getId(), instanceTag);
        }

        for (WeatherInstance instance : state.getReadyInstances()) {
            CompoundTag instanceTag = new CompoundTag();
            instanceTag.putString("type", instance.getType().name().toLowerCase());
            instanceTag.putInt("remaining", instance.getRemainingTicks());
            instanceTag.putInt("duration", instance.getDurationTicks());
            instanceTag.putInt("stillness", instance.getStillnessTicks());
            instanceTag.putString("phase", instance.phase.name().toLowerCase());

            weatherTag.put(instance.getId(), instanceTag);
        }

        root.put(headKey, weatherTag);

        Path file = FilePath.getModDataPath(level);
        try {
            NbtIo.writeCompressed(root, file.toFile().toPath());
        }
        catch (IOException e) {
            CoreHanXu.LOGGER.warn("[HX] Failed to save weather states", e);
        }
    }

    public static void loadAllWeatherStates(ServerLevel level, Set<String> specificIds) {
        String headKey = "core.yaoquan.hanxu.weathers";
        Path file = FilePath.getModDataPath(level);

        if (!file.toFile().exists()) {
            return;
        }

        CompoundTag root;
        try {
            NbtAccounter accounter = General.Standard.newNbtAccounter();
            root = NbtIo.readCompressed(file, accounter);
        }
        catch (IOException e) {
            CoreHanXu.LOGGER.warn("[HX] Failed to load weather states", e);
            return;
        }

        CompoundTag weatherTag = root.getCompound(headKey).orElse(new CompoundTag());

        WeatherState state = getWeatherStateOrNew(level);

        Set<String> recoveredDefinitionIds = new LinkedHashSet<>();

        for (String id : (specificIds == null? weatherTag.keySet() : specificIds)) {
            CompoundTag instanceTag = weatherTag.getCompound(id).orElse(new CompoundTag());
            String typeString = instanceTag.getString("type").orElse(null);

            if (typeString == null) {
                continue;
            }

            WeatherType type = parseType(typeString);
            if (type == WeatherType.NULL) {
                CoreHanXu.LOGGER.warn("[HX] Weather type not supported: {}", typeString);
                continue;
            }

            int remaining = instanceTag.getInt("remaining").orElse(0);
            int duration = instanceTag.getInt("duration").orElse(0);
            int stillness = instanceTag.getInt("stillness").orElse(0);
            String phaseString = instanceTag.getString("phase").orElse("idle");

            WeatherPhase phase = parsePhase(phaseString);

            WeatherDefinition definition = getWeatherDefinition(id);

            // Storage index into unclaimed if definition not found.
            if (definition == null) {
                unclaimedStates.computeIfAbsent(
                        level.dimension(),
                        k -> ConcurrentHashMap.newKeySet()
                ).add(id);
                CoreHanXu.LOGGER.warn("[HX] Unclaimed weather state for definition: {}", id);
                continue;
            }
            else {
                recoveredDefinitionIds.add(id);
            }

            WeatherInstance instance = new WeatherInstance(id, type, duration, stillness, definition);
            instance.setRemainingTicks(remaining);
            instance.phase = phase;

            switch (phase) {
                case ACTIVE, PAUSE_ACTIVE -> state.putActiveInstance(type, instance);
                case READY -> state.putReadyInstance(type, instance);
                case STILLNESS -> state.putStillnessInstance(type, instance);
                default -> {}
            }
        }

        unclaimedStates.remove(level.dimension(), recoveredDefinitionIds);
    }

    public static void loadAllWeatherStates(ServerLevel level) {
        loadAllWeatherStates(level, null);
    }

    public static void pickupUnclaimedStates() {
        if (unclaimedStates.isEmpty()) {
            return;
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        for (Map.Entry<ResourceKey<Level>, Set<String>> entry : unclaimedStates.entrySet()) {
            ResourceKey<Level> dimension = entry.getKey();
            Set<String> ids = entry.getValue();

            if (ids.isEmpty()) {
                continue;
            }

            ServerLevel level = server.getLevel(dimension);
            if (level == null) {
                CoreHanXu.LOGGER.warn("[HX] Cannot pickup unclaimed weather states for level: {}", dimension);
                continue;
            }

            loadAllWeatherStates(level, ids);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        WeatherState state = getWeatherState(level);
        if (state == null) {
            return;
        }

        for (WeatherInstance instance : state.getActiveInstances()) {
            if (!instance.doesActive()) {
                continue;
            }

            instance.tickCount();

            if (instance.doesStageChange()) {
                WeatherType type = instance.getType();
                state.setNextActive(type);
            }
        }

        for (WeatherInstance instance : state.getStillnessInstances()) {
            instance.tickCount();

            if (instance.doesStageChange()) {
                state.setReady(instance);
            }
        }
    }

    private static void registerYamlWeather(WeatherDefinition weatherDefinition) {
        if (apiWeathers.containsKey(weatherDefinition.getId()) || commandWeathers.containsKey(weatherDefinition.getId())) {
            CoreHanXu.LOGGER.warn("[HX] Rejected duplicate weather: {}", weatherDefinition.getId());
            return;
        }

        commandWeathers.put(weatherDefinition.getId(), weatherDefinition);
    }

    private static boolean stopOrKillWeather(ServerLevel level, Object key, String category) {
        WeatherState state = getWeatherState(level);

        if (state == null) {
            CoreHanXu.LOGGER.warn("[HX] Unknown weather state to stop: {}", key);
            return false;
        }

        WeatherType type = key instanceof WeatherType? (WeatherType) key : null;
        String id = key instanceof String? (String) key : null;

        boolean doesActive = type == null? state.doesActive(id) : state.doesActive(type);

        if (!doesActive) {
            CoreHanXu.LOGGER.warn("[HX] Reject to stop a stillness/idle weather: {}", key);
            return false;
        }

        switch (category) {
            case "stop" -> {
                if (type == null) {
                    state.pauseActive(id);
                }
                else {
                    state.pauseActive(type);
                }

                CoreHanXu.LOGGER.info("[HX] Stopped weather: {} -> {}", id, level.dimension());
                return true;
            }
            case "kill" -> {
                if (type == null) {
                    state.setActiveToStillness(id);
                }
                else {
                    state.setActiveToStillness(type);
                }

                CoreHanXu.LOGGER.info("[HX] Killed weather: {} -> {}", id, level.dimension());
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private static void tryNextActive(ServerLevel level, WeatherType type) {
        WeatherState state = getWeatherState(level);
        if (state == null) {
            return;
        }

        WeatherInstance instance = state.getReadyInstance(type);
        if (instance == null) {
            return;
        }

        WeatherDefinition definition = instance.getDefinition();
        if (definition == null) {
            return;
        }

        if (definition.isAble(level)) {
            state.setNextActive(type);
            NeoForge.EVENT_BUS.post(new WeatherStartEvent(level, instance));
            CoreHanXu.LOGGER.info("[HX] Started weather from ready state to active: {} -> {}", type, level.dimension());
        }
    }

    private static void tryAllNextActive(ServerLevel level) {
        WeatherState state = getWeatherState(level);
        if (state == null) {
            return;
        }

        for (WeatherInstance instance : state.getReadyInstances()) {
            state.setNextActive(instance.getType());
        }
    }

    private static WeatherType parseType(String type) {
        return switch (type) {
            case "fog" -> WeatherType.FOG;
            case "colored_rain" -> WeatherType.COLORED_RAIN;
            case "wind" -> WeatherType.WIND;
            case "colored_moon" -> WeatherType.COLORED_MOON;
            case "particle_storm" -> WeatherType.PARTICLE_STORM;
            case "aurora" -> WeatherType.AURORA;
            case "void_fog" -> WeatherType.VOID_FOG;
            case null, default -> WeatherType.NULL;
        };
    }

    private static WeatherPhase parsePhase(String phase) {
        return switch (phase) {
            case "active" -> WeatherPhase.ACTIVE;
            case "ready" -> WeatherPhase.READY;
            case "stillness" -> WeatherPhase.STILLNESS;
            case "pause_active" -> WeatherPhase.PAUSE_ACTIVE;
            case null, default -> WeatherPhase.IDLE;
        };
    }
}
