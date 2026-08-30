package core.yaoquan.hanxu.api;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.define.Error;
import core.yaoquan.hanxu.api.define.FilePath;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.api.define.SaveDat;
import core.yaoquan.hanxu.api.event.*;
import core.yaoquan.hanxu.api.weather.Fog;
import core.yaoquan.hanxu.registry.QuickSendPacket;
import core.yaoquan.hanxu.util.NullableValue;
import core.yaoquan.hanxu.registry.event.payload.GeneralPayload;
import core.yaoquan.hanxu.util.Cast;
import core.yaoquan.hanxu.util.Creator;
import core.yaoquan.hanxu.util.YamlReader;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        ACTIVE, READY, STILLNESS, IDLE,
    }

    // All registered weather.
    private static final Map<String, WeatherDefinition> apiWeathers = new ConcurrentHashMap<>();
    private static final Map<String, WeatherDefinition> commandWeathers = new ConcurrentHashMap<>();
    // State of weather.
    private static final Map<ResourceKey<Level>, WeatherState> weatherStates = new ConcurrentHashMap<>();
    // Storage unclaimed states (If weather definition not registered).
    private static final Map<ResourceKey<Level>, Set<String>> unclaimedStates = new ConcurrentHashMap<>();
    // Storage debug display list.
    private static final Map<ServerPlayer, Set<String>> refreshDisplayList = new ConcurrentHashMap<>();

    private static int tickCounter = 0;

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

    public static @NotNull NullableValue<WeatherDefinition> getWeatherDefinition(String id) {
        if (apiWeathers.containsKey(id)) {
            return NullableValue.ofNotNull(apiWeathers.get(id));
        }
        if (commandWeathers.containsKey(id)) {
            return NullableValue.ofNotNull(commandWeathers.get(id));
        }
        return NullableValue.none();
    }

    public static @NotNull NullableValue<WeatherState> getWeatherState(ServerLevel level) {
        ResourceKey<Level> key = level.dimension();
        return NullableValue.ofNullable(weatherStates.get(key));
    }

    public static WeatherState getWeatherStateOrNew(ServerLevel level) {
        ResourceKey<Level> key = level.dimension();
        return weatherStates.computeIfAbsent(key, k -> new WeatherState());
    }

    public static @NotNull NullableValue<WeatherState> getWeatherState(ResourceKey<Level> dimension) {
        return NullableValue.ofNullable(weatherStates.get(dimension));
    }

    public static boolean restartWeather(ServerLevel level, String id, int duration) {
        NullableValue<WeatherDefinition> nullableDefinition = getWeatherDefinition(id);
        // Check if definition completed.
        if (nullableDefinition.isNull()) {
            CoreHanXu.LOGGER.warn("[HX] Unknown weather definition for activate: {}", id);
            return false;
        }
        
        WeatherDefinition definition = nullableDefinition.get();

        WeatherState state = getWeatherStateOrNew(level);

        // Then check if this weather activated.
        if (state.doesActive(definition.getWeatherType())) {
            CoreHanXu.LOGGER.warn("[HX] Rejected to activate a activated weather type: {}", definition.getWeatherType());
            return false;
        }

        // If it is not existed, build new weather.
        return buildNewWeather(level, state, definition, id, duration);
    }

    public static boolean restartWeather(ServerLevel level, String id) {
        return restartWeather(level, id, -1);
    }

    public static boolean startWeather(ServerLevel level, String id, int duration) {
        NullableValue<WeatherDefinition> nullableDefinition = getWeatherDefinition(id);
        if (nullableDefinition.isNull()) {
            CoreHanXu.LOGGER.warn("[HX] Unknown weather definition for start: {}", id);
            return false;
        }

        WeatherDefinition definition = nullableDefinition.get();
        WeatherType type = definition.getWeatherType();
        WeatherState state = getWeatherStateOrNew(level);

        // Kill the weather if this type of weather is active.
        if (state.doesActive(type)) {
            state.getActiveInstance(type).ifPresent(instance -> {
                if (!instance.getId().equals(id)) {
                    CoreHanXu.LOGGER.info("[HX] Current weather has been silenced for next started weather: {} -> {}", instance.getId(), id);
                    state.onActiveEnd(type, level);
                }
            });
        }

        // Started the weather if existed.
        if (state.doesExist(id)) {
            findInstance(level, id).ifPresent(instance -> {
                if (instance.doesStillness()) {
                    state.setActive(instance);
                }
            });
            CoreHanXu.LOGGER.info("[HX] Started weather type: {}", type);

            // Post event:
            WeatherInstance instance = findInstance(level, id).get();

            NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherStartEvent(level, instance));
            return true;
        }

        return buildNewWeather(level, state, definition, id, duration);
    }

    public static boolean startWeather(ServerLevel level, String id) {
        return startWeather(level, id, -1);
    }

    public static boolean resumeWeather(ServerLevel level, WeatherType type) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);
        if (nullableState.isNull()) {
            CoreHanXu.LOGGER.warn("[HX] Unknown weather state for resume: {}", type);
            return false;
        }

        WeatherState state = nullableState.get();

        if (!state.doesExist(type)) {
            return false;
        }

        state.resume(type);

        Collection<WeatherInstance> instances = new ArrayList<>();
        instances.addAll(state.getActiveInstances());
        instances.addAll(state.getStillnessInstances());

        instances.forEach(instance -> {
            if (instance.getType().equals(type)) {
                NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherResumeEvent(level, instance));
            }
        });

        return true;
    }

    public static boolean resumeWeather(ServerLevel level, String id) {
        NullableValue<WeatherDefinition> nullableDefinition = getWeatherDefinition(id);
        if (nullableDefinition.isNull()) {
            CoreHanXu.LOGGER.warn("[HX] Unknown weather definition for resume: {}", id);
            return false;
        }

        WeatherState state = getWeatherStateOrNew(level);

        if (!state.doesExist(id)) {
            return false;
        }

        state.resume(id);

        WeatherInstance instance = findInstance(level, id).get();

        NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherResumeEvent(level, instance));
        return true;
    }

    public static boolean pauseWeather(ServerLevel level, WeatherType type) {
        return pauseOrKillWeather(level, type, "pause");
    }

    public static boolean pauseWeather(ServerLevel level, String id) {
        return pauseOrKillWeather(level, id, "pause");
    }

    public static boolean killWeather(ServerLevel level, WeatherType type) {
        return pauseOrKillWeather(level, type, "kill");
    }

    public static boolean killWeather(ServerLevel level, String id) {
        return pauseOrKillWeather(level, id, "kill");
    }

    public static boolean prepareWeather(ServerLevel level, String id) {
        NullableValue<WeatherDefinition> nullableDefinition = getWeatherDefinition(id);
        if (nullableDefinition.isNull()) {
            CoreHanXu.LOGGER.warn("[HX] Unknown weather definition for prepare: {}", id);
            return false;
        }

        WeatherDefinition definition = nullableDefinition.get();

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
        NullableValue<WeatherState> nullableState = getWeatherState(level);

        nullableState.ifPresent(
            state -> state.fallReadyToStillness(type)
        );
    }

    public static void clearAllReady(ServerLevel level, WeatherType type) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);

        nullableState.ifPresent(
            state -> state.fallAllReadyToStillness(type)
        );
    }

    public static boolean doesActiveWeatherExist(ServerLevel level, WeatherType type) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);
        return nullableState.isPresent() && nullableState.get().doesActive(type);
    }

    public static boolean doesActiveWeatherExist(ServerLevel level, String id) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);
        return nullableState.isPresent() && nullableState.get().doesActive(id);
    }

    public static boolean doesReadyWeatherExist(ServerLevel level, WeatherType type) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);
        return nullableState.isPresent() && nullableState.get().doesReady(type);
    }

    public static boolean doesReadyWeatherExist(ServerLevel level, String id) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);
        return nullableState.isPresent() && nullableState.get().doesReady(id);
    }

    public static boolean doesStillnessWeatherExist(ServerLevel level, WeatherType type) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);
        return nullableState.isPresent() && nullableState.get().doesStillness(type);
    }

    public static boolean doesStillnessWeatherExist(ServerLevel level, String id) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);
        return nullableState.isPresent() && nullableState.get().doesStillness(id);
    }

    public static boolean doesAnyActiveWeatherExist(ServerLevel level) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);
        return nullableState.isPresent() && nullableState.get().doesAnyActive();
    }

    public static boolean doesAnyReadyWeatherExist(ServerLevel level) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);
        return nullableState.isPresent() && nullableState.get().doesAnyReady();
    }

    public static boolean doesAnyStillnessWeatherExist(ServerLevel level) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);
        return nullableState.isPresent() && nullableState.get().doesAnyStillness();
    }

    public static boolean doesWeatherPaused(ServerLevel level, WeatherType type) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);

        return switch (nullableState.situation()) {
            case NULL -> false;
            case SOME -> nullableState.get().getActiveInstance(type).matching(
                    instance -> nullableState.get().doesAnyPaused(instance.getType()),
                    () -> false
            );
        };
    }

    public static boolean doesWeatherPaused(ServerLevel level, String id) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);

        return switch (nullableState.situation()) {
            case NULL -> false;
            case SOME -> {
                WeatherState state = nullableState.get();

                for (WeatherInstance instance : state.getActiveInstances()) {
                    if (instance.getId().equals(id) && nullableState.get().doesPaused(instance.getId())) {
                        yield true;
                    }
                }

                yield false;
            }
        };
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

    public static boolean doesWeatherStateExist(ServerLevel level, String id) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);

        return switch (nullableState.situation()) {
            case NULL -> false;
            case SOME -> {
                WeatherState state = nullableState.get();
                yield state.doesActive(id) || state.doesReady(id) || state.doesStillness(id);
            }
        };
    }

    public static boolean doesWeatherExist(String id, String category) {
        return switch (category) {
            case "api" -> apiWeathers.containsKey(id);
            case "command", "yaml" -> commandWeathers.containsKey(id);
            default -> false;
        };
    }

    public static boolean doesWeatherDefinitionExist(String id) {
        NullableValue<WeatherDefinition> nullableDefinition = WeatherHolder.getWeatherDefinition(id);
        return switch (nullableDefinition.situation()) {
            case NULL -> false;
            case SOME -> true;
        };
    }

    public static NullableValue<WeatherDefinition> loadYamlWeather(String fileName) {
        try {
            Map<String, Object> rawData = YamlReader.read("weather", fileName);

            String id = Cast.toStringOrThrow(rawData, "id", Error.errorString(Error.CodeError.missingNecessaryField) + "'id' for " + fileName);
            if (!id.equals(fileName)) {
                CoreHanXu.LOGGER.warn("{}{} ≠ {}", Error.errorString(Error.CodeError.mismatchFileElement), fileName, id);
                return NullableValue.none();
            }

            String typeString = Cast.toStringOrThrow(rawData, "type", Error.errorString(Error.CodeError.missingNecessaryField) + "'type' for " + fileName);
            WeatherType type = parseType(typeString);
            if (type == WeatherType.NULL) {
                CoreHanXu.LOGGER.warn("[HX] Unknown weather type: {}", typeString);
                return NullableValue.none();
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
                CoreHanXu.LOGGER.warn("[HX] Weather type not supported in YAML: {}", typeString);
            }

            return NullableValue.ofNotNull(definition);
        }
        catch (IOException e) {
            CoreHanXu.LOGGER.warn("[HX] Failed to load YAML weather: {}", fileName, e);
            return NullableValue.none();
        }
    }

    public static void registerAllYamlWeathers() {
        List<Path> files = YamlReader.listOut("weather");
        for (Path file : files) {
            String fileName = file.getFileName().toString().replace(".yaml", "");

            registerYamlWeather(fileName);
        }
    }

    public static void registerYamlWeather(String fileName) {
        NullableValue<WeatherDefinition> nullableDefinition = loadYamlWeather(fileName);
        nullableDefinition.ifPresent(WeatherHolder::registerYamlWeather);
    }

    public static @NotNull NullableValue<CompoundTag> packSingleLevelStates(ServerLevel level) {
        if (level == null) {
            return NullableValue.none();
        }

        CompoundTag weatherTag = new CompoundTag();
        NullableValue<WeatherState> nullableState = getWeatherState(level);
        if (nullableState.isNull()) {
            return NullableValue.none();
        }

        WeatherState state = nullableState.get();

        for (WeatherInstance instance : state.getActiveInstances()) {
            CompoundTag instanceTag = buildInstanceTag(instance, state);
            weatherTag.put(instance.getId(), instanceTag);
        }

        for (WeatherInstance instance : state.getReadyInstances()) {
            CompoundTag instanceTag = buildInstanceTag(instance, state);
            weatherTag.put(instance.getId(), instanceTag);
        }

        for (WeatherInstance instance : state.getStillnessInstances()) {
            CompoundTag instanceTag = buildInstanceTag(instance, state);
            weatherTag.put(instance.getId(), instanceTag);
        }

        if (weatherTag.keySet().isEmpty()) {
            return NullableValue.none();
        }

        CompoundTag root = new CompoundTag();
        root.put(level.dimension().location().toString(), weatherTag);
        return NullableValue.ofNotNull(root);
    }

    public static @NotNull NullableValue<CompoundTag> packAllLevelStates() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return NullableValue.none();
        }

        CompoundTag root = new CompoundTag();

        for (ServerLevel level : server.getAllLevels()) {
            NullableValue<CompoundTag> nullableTag = packSingleLevelStates(level);
            if (nullableTag.isNull()) {
                continue;
            }

            CompoundTag levelTag = nullableTag.get();

            for (String dimensionKey : levelTag.keySet()) {
                root.put(dimensionKey, levelTag.getCompound(dimensionKey).orElse(new CompoundTag()));
            }
        }

        if (root.keySet().isEmpty()) {
            return NullableValue.none();
        }

        return NullableValue.ofNotNull(root);
    }

    public static void loadSingleLevelStates(ServerLevel level, CompoundTag weatherTag) {
        if (level == null || weatherTag == null) {
            return;
        }

        WeatherState state = getWeatherStateOrNew(level);

        Set<String> recoveredUnclaims = new HashSet<>();

        for (String id : weatherTag.keySet()) {
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
            int initial = instanceTag.getInt("initial").orElse(remaining);
            int duration = instanceTag.getInt("duration").orElse(200);
            int stillness = instanceTag.getInt("stillness").orElse(200);
            String phaseString = instanceTag.getString("phase").orElse("idle");
            WeatherPhase phase = parsePhase(phaseString);

            boolean paused = instanceTag.getBoolean("paused").orElse(false);

            NullableValue<WeatherDefinition> nullableDefinition = getWeatherDefinition(id);
            if (nullableDefinition.isNull()) {
                // Register as unclaimed.
                unclaimedStates.computeIfAbsent(
                        level.dimension(),
                        k -> ConcurrentHashMap.newKeySet()
                ).add(id);
                CoreHanXu.LOGGER.warn("[HX] Unclaimed weather state for definition: {}", id);
                continue;
            }

            // Else add into recovered list.
            recoveredUnclaims.add(id);

            // Then rebuild states:
            WeatherDefinition definition = nullableDefinition.get();

            WeatherInstance instance = new WeatherInstance(id, type, duration, stillness, definition);

            instance.rebuild(phase, remaining, initial);

            // Pause if player paused weather.
            if (paused) {
                state.pause(id);
            }

            switch (phase) {
                case ACTIVE -> state.putActiveInstance(type, instance);
                case READY -> state.putReadyInstance(type, instance);
                case STILLNESS -> state.putStillnessInstance(id, instance);
                default -> {}
            }
        }

        // Then try to remove recovered states from map.
        if (!recoveredUnclaims.isEmpty()) {
            unclaimedStates.computeIfPresent(level.dimension(), (key, set) -> {
                set.removeAll(recoveredUnclaims);
                return set.isEmpty()? null : set;
            });
        }
    }

    public static void loadAllLevelStates(@Nullable Set<String> pickupIds) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        Path file = FilePath.getModDataPath(server.overworld());
        if (!file.toFile().exists()) {
            return;
        }

        // Try to read tags.
        CompoundTag root;
        try {
            NbtAccounter accounter = General.Standard.newNbtAccounter();
            root = NbtIo.readCompressed(file, accounter);
        }
        catch (IOException e) {
            CoreHanXu.LOGGER.warn("[HX] Failed to load weather level states", e);
            return;
        }

        String headKey = SaveDat.HeadKey.weathers.get();
        CompoundTag weathersTag = root.getCompound(headKey).orElse(new CompoundTag());

        for (String dimensionKey : weathersTag.keySet()) {
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimensionKey));
            ServerLevel level = server.getLevel(dimension);
            if (level == null) {
                continue;
            }

            CompoundTag weatherTag = weathersTag.getCompound(dimensionKey).orElse(new CompoundTag());

            if (pickupIds != null) {
                CompoundTag pickupTag = new CompoundTag();
                for (String id : pickupIds) {
                    weatherTag.getCompound(id).ifPresent(tag -> pickupTag.put(id, tag));
                }

                if (!pickupTag.keySet().isEmpty()) {
                    loadSingleLevelStates(level, pickupTag);
                }
            }
            else {
                loadSingleLevelStates(level, weatherTag);
            }
        }
    }

    public static void loadAllLevelStates() {
        loadAllLevelStates(null);
    }

    public static boolean pickupUnclaimedStates() {
        if (unclaimedStates.isEmpty()) {
            return false;
        }

        Set<String> unclaimedIds = new HashSet<>();
        for (Set<String> ids : unclaimedStates.values()) {
            unclaimedIds.addAll(ids);
        }
        if (unclaimedIds.isEmpty()) {
            return false;
        }

        loadAllLevelStates(unclaimedIds);
        return true;
    }

    // Display out to F4 page (info page).
    public static void displayToInfoPage(ServerPlayer player, ServerLevel level, String weatherId, boolean state) {
        if (weatherId == null || level == null) {
            return;
        }

        String key = level.dimension().location() + ":" + weatherId;

        if (state) {
            refreshDisplayList.computeIfAbsent(player, k -> ConcurrentHashMap.newKeySet()).add(key);
        }
        else {
            Set<String> weatherSet = refreshDisplayList.get(player);
            if (weatherSet != null) {
                weatherSet.remove(key);
                if (weatherSet.isEmpty()) {
                    refreshDisplayList.remove(player);
                }
            }

            syncLostPacketToClient(player, weatherId, level.dimension().location().toString(), false);
        }

        findInstance(level, weatherId)
                .matching(
                        instance -> {
                            syncPacketToClient(player, instance, level.dimension().location().toString(), state);
                            return null;
                        },
                        () -> {
                            CoreHanXu.LOGGER.warn("[HX] Weather instance not found for display: {} -> {}", weatherId, level.dimension().location());
                            return null;
                        }
                );
    }

    // Submit packet into F4 display.
    public static void syncPacketToClient(ServerPlayer player, WeatherInstance instance, String dimension, boolean state) {
        PacketDistributor.sendToPlayer(
                player,
                new GeneralPayload.WeatherF4Packet(
                        instance.getId(),
                        instance.getType(),
                        instance.getPhase(),
                        instance.getRemainingTicks(),
                        instance.getInitialTicks(),
                        instance.getDurationTicks(),
                        instance.getStillnessTicks(),
                        state,
                        dimension
                )
        );

        QuickSendPacket.sendRegisteredTermPacket(player);
    }

    // Submit packet to present lost.
    public static void syncLostPacketToClient(ServerPlayer player, String weatherId, String dimension, boolean state) {
        PacketDistributor.sendToPlayer(player, new GeneralPayload.WeatherF4Packet(
                weatherId,
                WeatherType.NULL,
                WeatherPhase.IDLE,
                0,
                0,
                0,
                0,
                state,
                dimension
        ));
    }

    public static @NotNull NullableValue<WeatherInstance> findInstance(ServerLevel level, String id) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);

        return switch (nullableState.situation()) {
            case NULL -> NullableValue.none();
            case SOME -> findInstance(nullableState.get(), id);
        };
    }

    public static @NotNull NullableValue<WeatherInstance> findInstance(String dimension, String id) {
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension));
        NullableValue<WeatherState> nullableState = getWeatherState(dimensionKey);

        return switch (nullableState.situation()) {
            case NULL -> NullableValue.none();
            case SOME -> findInstance(nullableState.get(), id);
        };
    }

    // Update display every 5 ticks.
    public static void tickSync() {
        if (tickCounter % 2 != 0) {
            return;
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        // F4 display.
        for (Map.Entry<ServerPlayer, Set<String>> entry : refreshDisplayList.entrySet()) {
            ServerPlayer player = entry.getKey();
            Set<String> keys = entry.getValue();

            if (player == null || keys.isEmpty()) {
                continue;
            }

            for (String key : keys) {
                String[] parts = key.split(":", 3);
                String dimension = parts[0] + ":" + parts[1];
                String weatherId = parts[2];

                findInstance(dimension, weatherId)
                        .ifPresent(instance -> syncPacketToClient(player, instance, dimension, true))
                        .ifNull(
                                () -> syncLostPacketToClient(player, weatherId, dimension, true)
                        );
            }
        }

        // Render.
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerLevel level = player.level();
            NullableValue<WeatherState> nullableState = getWeatherState(level);
            if (nullableState.isNull()) {
                continue;
            }

            String dimension = level.dimension().location().toString();
            WeatherState state = nullableState.get();

            Set<WeatherType> activeTypes = new HashSet<>();

            for (WeatherInstance instance : state.getActiveInstances()) {
                activeTypes.add(instance.getType());

                WeatherDefinition definition = instance.getDefinition();
                definition.sendToPlayer(player, instance, dimension);
            }

            for (WeatherInstance instance : state.getStillnessInstances()) {
                if (!activeTypes.contains(instance.getType())) {
                    WeatherDefinition definition = instance.getDefinition();
                    definition.sendToPlayer(player, instance, dimension);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        tickCounter++;

        if (tickCounter > 99) {
            tickCounter = 0;
        }

        tickSync();

        NullableValue<WeatherState> nullableState = getWeatherState(level);
        if (nullableState.isNull()) {
            return;
        }

        WeatherState state = nullableState.get();

        for (WeatherInstance instance : state.getActiveInstances()) {
            if (!instance.doesActive()) {
                continue;
            }

            if (state.doesPaused(instance.getId())) {
                continue;
            }

            instance.tickCount();

            if (tickCounter % 5 == 0) {
                NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherTickEvent(level, instance));
            }

            if (instance.doesStageChange()) {
                String id = instance.getId();
                state.onActiveEnd(id, level);
            }
        }

        for (WeatherInstance instance : state.getStillnessInstances()) {
            if (state.doesPaused(instance.getId())) {
                continue;
            }

            instance.tickCount();

            if (tickCounter % 5 == 0) {
                NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherTickEvent(level, instance));
            }

            if (instance.doesStageChange()) {
                String id = instance.getId();
                state.onStillnessEnd(id, level);
            }
        }
    }

    public static void initialize(ServerLevel level) {
        ResourceKey<Level> key = level.dimension();
        if (!weatherStates.containsKey(key)) {
            weatherStates.put(key, new WeatherState());
        }
    }

    public static void initializeAll() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        for (ServerLevel level : server.getAllLevels()) {
            initialize(level);
            CoreHanXu.LOGGER.info("[HX] Initialized weather state: {}", level.dimension().location());
        }
    }

    public static Map<String, WeatherDefinition> getApiWeatherDefinitions() {
        return apiWeathers;
    }

    public static Map<String, WeatherDefinition> getCommandWeatherDefinitions() {
        return commandWeathers;
    }

    public static @NotNull List<Component> readWeatherInstance(ServerLevel level, String weatherId) {
        List<Component> lines = new ArrayList<>();

        NullableValue<WeatherDefinition> nullableDefinition = getWeatherDefinition(weatherId);
        if (nullableDefinition.isNull()) {
            lines.add(Component.translatable("api.core_hanxu.weather.non_exist")
                    .append(Component.literal(" " + weatherId))
                    .withColor(General.Color.FAILURE)
            );
            return lines;
        }

        WeatherDefinition definition = nullableDefinition.get();

        // Write basic information.
        lines.add(Component.translatable("api.core_hanxu.weather.title")
                .append(Component.literal(" " + weatherId))
                .withColor(General.Color.TITLE)
        );

        lines.add(Component.translatable("api.core_hanxu.weather.type")
                .append(Component.literal(" " + definition.getWeatherType().name().toLowerCase()))
                .withColor(General.Color.CONTENT)
        );

        lines.add(Component.translatable("api.core_hanxu.weather.time_title")
                .withColor(General.Color.CONTENT)
        );

        lines.add(Component.translatable("api.core_hanxu.weather.duration")
                .append(Component.literal(" " + definition.getMinimumDuration() + " ~ " + definition.getMaximumDuration() + " (t)"))
                .withColor(General.Color.CONTENT)
        );

        lines.add(Component.translatable("api.core_hanxu.weather.stillness")
                .append(Component.literal(" " + definition.getMinimumStillness() + " ~ " + definition.getMaximumStillness() + " (t)"))
                .withColor(General.Color.CONTENT)
        );

        NullableValue<WeatherInstance> nullableInstance = findInstance(level, weatherId);

        nullableInstance
                .ifPresent(
                        instance -> {
                            NullableValue<WeatherState> nullableState = getWeatherState(level);
                            boolean paused = nullableState
                                    .modify(state -> state.doesPaused(instance.getId()))
                                    .getOrElse(false);

                            lines.add(Component.translatable("api.core_hanxu.weather.instance_title")
                                    .withColor(General.Color.TITLE)
                            );

                            lines.add(Component.translatable("api.core_hanxu.weather.phase")
                                    .append(Component.literal(" " + instance.getPhase().name().toLowerCase()))
                                    .withColor(General.Color.CONTENT)
                            );

                            lines.add(Component.translatable("api.core_hanxu.weather.time")
                                    .append(Component.literal(" " + instance.getRemainingTicks() + " / " + instance.getInitialTicks() + " (t)"))
                                    .withColor(General.Color.CONTENT)
                            );

                            lines.add(Component.translatable("api.core_hanxu.weather.duration")
                                    .append(Component.literal(" " + instance.getDurationTicks()))
                                    .withColor(General.Color.CONTENT)
                            );

                            lines.add(Component.translatable("api.core_hanxu.weather.stillness")
                                    .append(Component.literal(" " + instance.getStillnessTicks()))
                                    .withColor(General.Color.CONTENT)
                            );

                            lines.add(Component.translatable("api.core_hanxu.weather.paused")
                                    .append(Component.literal(" " + paused))
                                    .withColor(General.Color.CONTENT)
                            );

                            // Specific details.
                            if (definition instanceof Fog fog) {
                                lines.add(Component.translatable("api.core_hanxu.weather.fog.title")
                                        .withColor(General.Color.TITLE)
                                );

                                lines.add(Component.translatable("api.core_hanxu.weather.fog.color")
                                        .append(Component.literal(" " + String.format("%06X", fog.getColor())))
                                        .withColor(General.Color.CONTENT)
                                );

                                lines.add(Component.translatable("api.core_hanxu.weather.fog.distance")
                                        .append(Component.literal(" " + fog.getMinimumDistance() + " ~ " + fog.getMaximumDistance()))
                                        .withColor(General.Color.CONTENT)
                                );
                            }
                        }
                )
                .ifNull(
                        () -> {
                            lines.add(Component.translatable("api.core_hanxu.weather.instance_title")
                                    .withColor(General.Color.TITLE)
                            );

                            lines.add(Component.translatable("api.core_hanxu.weather.instance_none")
                                    .withColor(General.Color.CONTENT)
                            );
                        }
                );

        return lines;
    }

    public static @NotNull String readWeatherInstanceAsTranslatedString(ServerLevel level, String weatherId) {
        List<Component> lines = readWeatherInstance(level, weatherId);
        StringBuilder stringPackage = new StringBuilder();

        for (Component line : lines) {
            String thisLine = (line.getString() + "\n");
            stringPackage.append(thisLine);
        }

        return stringPackage.toString();
    }

    public static @NotNull String readWeatherInstanceAsString(ServerLevel level, String weatherId) {
        StringBuilder stringPackage = new StringBuilder();

        NullableValue<WeatherDefinition> nullableDefinition = getWeatherDefinition(weatherId);
        if (nullableDefinition.isNull()) {
            stringPackage.append("[HX] Non-exist weather: ").append(weatherId).append("\n");
            return stringPackage.toString();
        }

        WeatherDefinition definition = nullableDefinition.get();

        // Write basic information.
        stringPackage.append("[HX] Weather definition found: ").append(weatherId).append("\n");

        stringPackage.append("- Type: ").append(definition.getWeatherType().name().toLowerCase()).append("\n");

        stringPackage.append("- Time: ").append("\n");

        stringPackage.append("    Duration: ").append(definition.getMinimumDuration()).append(" ~ ").append(definition.getMaximumDuration()).append(" (t)\n");

        stringPackage.append("    Stillness: ").append(definition.getMinimumStillness()).append(" ~ ").append(definition.getMaximumStillness()).append(" (t)\n");

        NullableValue<WeatherInstance> nullableInstance = findInstance(level, weatherId);

        nullableInstance
            .ifPresent(
                instance -> {
                    NullableValue<WeatherState> nullableState = getWeatherState(level);
                    boolean paused = nullableState
                        .modify(state -> state.doesPaused(instance.getId()))
                        .getOrElse(false);

                    stringPackage.append("-> Instance: ").append("\n");

                    stringPackage.append("- Phase: ").append(instance.getPhase().name().toLowerCase()).append("\n");

                    stringPackage.append("- Time: ").append(instance.getRemainingTicks()).append(" / ").append(instance.getInitialTicks()).append(" (t)\n");

                    stringPackage.append("    Duration: ").append(instance.getDurationTicks()).append("\n");

                    stringPackage.append("    Stillness: ").append(instance.getStillnessTicks()).append("\n");

                    stringPackage.append("- Paused: ").append(paused).append("\n");

                    // Specific details.
                    if (definition instanceof Fog fog) {
                        stringPackage.append("-> Fog: ").append("\n");

                        stringPackage.append("- Color: ").append(String.format("%06X", fog.getColor())).append("\n");

                        stringPackage.append("- Distance: ").append(fog.getMinimumDistance()).append(" ~ ").append(fog.getMaximumDistance()).append("\n");
                    }
                }
            )
            .ifNull(
                () -> {
                    stringPackage.append("-> Instance: ").append("\n");

                    stringPackage.append("- Nothing...").append("\n");
                }
            );

        return stringPackage.toString();
    }

    public static @NotNull WeatherType parseStringToType(String typeString) {
        return switch (typeString) {
            case "fog" -> WeatherType.FOG;
            case "colored_rain" -> WeatherType.COLORED_RAIN;
            case "wind" -> WeatherType.WIND;
            case "colored_moon" -> WeatherType.COLORED_MOON;
            case "particle_storm" -> WeatherType.PARTICLE_STORM;
            case "aurora" -> WeatherType.AURORA;
            case "void_fog" -> WeatherType.VOID_FOG;
            default -> WeatherType.NULL;
        };
    }

    private static void registerYamlWeather(WeatherDefinition weatherDefinition) {
        if (apiWeathers.containsKey(weatherDefinition.getId()) || commandWeathers.containsKey(weatherDefinition.getId())) {
            CoreHanXu.LOGGER.warn("[HX] Rejected duplicate weather: {}", weatherDefinition.getId());
            return;
        }

        commandWeathers.put(weatherDefinition.getId(), weatherDefinition);
    }

    private static NullableValue<WeatherInstance> findInstance(WeatherState state, String id) {
        for (WeatherInstance thisInstance : state.getActiveInstances()) {
            if (thisInstance.getId().equals(id)) {
                return NullableValue.ofNotNull(thisInstance);
            }
        }

        for (WeatherInstance thisInstance : state.getReadyInstances()) {
            if (thisInstance.getId().equals(id)) {
                return NullableValue.ofNotNull(thisInstance);
            }
        }

        for (WeatherInstance thisInstance : state.getStillnessInstances()) {
            if (thisInstance.getId().equals(id)) {
                return NullableValue.ofNotNull(thisInstance);
            }
        }

        return NullableValue.none();
    }

    private static boolean pauseOrKillWeather(ServerLevel level, Object key, String category) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);
        if (nullableState.isNull()) {
            CoreHanXu.LOGGER.warn("[HX] Unknown weather state to stop: {}", key);
            return false;
        }

        WeatherState state = nullableState.get();

        WeatherType type = key instanceof WeatherType? (WeatherType) key : null;
        String id = key instanceof String? (String) key : null;

        switch (category) {
            case "pause" -> {
                if (type == null) {
                    WeatherInstance instance = null;
                    for (WeatherInstance thisInstance : state.getActiveInstances()) {
                        if (thisInstance.getId().equals(id)) {
                            state.pause(thisInstance.getId());
                            instance = thisInstance;
                            break;
                        }
                    }

                    if (instance == null) {
                        for (WeatherInstance thisInstance : state.getStillnessInstances()) {
                            if (thisInstance.getId().equals(id)) {
                                state.pause(thisInstance.getId());
                                instance = thisInstance;
                                break;
                            }
                        }
                    }

                    if (instance == null) {
                        CoreHanXu.LOGGER.warn("[HX] Weather instance not found for stop: {}", id);
                        return false;
                    }

                    NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherPauseEvent(level, instance));
                }
                else {
                    boolean notFound = state.getActiveInstance(type).isNull() && state.getStillnessInstance(type).isNull();
                    if (notFound) {
                        return false;
                    }

                    // Pause this type of weather.
                    state.pause(type);
                    // Post this type of active instance.
                    state.getActiveInstance(type).ifPresent(instance ->
                        NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherPauseEvent(level, instance))
                    );
                    // Post all this type of stillness instance.
                    state.getStillnessInstance(type).ifPresent(set ->
                        set.forEach(instance ->
                            NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherPauseEvent(level, instance))
                        )
                    );
                }

                CoreHanXu.LOGGER.info("[HX] Paused weather: {} -> {}", id, level.dimension().location());
                return true;
            }
            case "kill" -> {
                if (type == null) {
                    WeatherInstance instance = null;
                    for (WeatherInstance thisInstance : state.getActiveInstances()) {
                        if (thisInstance.getId().equals(id)) {
                            state.onActiveEnd(id, level);
                            instance = thisInstance;
                            break;
                        }
                    }

                    if (instance == null) {
                        for (WeatherInstance thisInstance : state.getStillnessInstances()) {
                            if (thisInstance.getId().equals(id)) {
                                CoreHanXu.LOGGER.warn("[HX] Unable to kill stillness weather: {}", id);
                                return false;
                            }
                        }
                    }

                    if (instance == null) {
                        CoreHanXu.LOGGER.warn("[HX] Weather instance not found for kill: {}", id);
                        return false;
                    }

                    NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherEndEvent(level, instance));
                }
                else {
                    if (state.getActiveInstance(type).isNull()) {
                        return false;
                    }

                    // Kill this active instance.
                    state.getActiveInstance(type).ifPresent(instance -> {
                        state.onActiveEnd(type, level);
                        NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherEndEvent(level, instance));
                    });
                    // Stillness do nothing.
                }

                CoreHanXu.LOGGER.info("[HX] Killed weather: {} -> {}", id, level.dimension().location());
                return true;
            }
            case null, default -> {
                return false;
            }
        }
    }

    private static @NotNull WeatherType parseType(String type) {
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

    private static @NotNull WeatherPhase parsePhase(String phase) {
        return switch (phase) {
            case "active" -> WeatherPhase.ACTIVE;
            case "ready" -> WeatherPhase.READY;
            case "stillness" -> WeatherPhase.STILLNESS;
            case null, default -> WeatherPhase.IDLE;
        };
    }

    private static CompoundTag buildInstanceTag(WeatherInstance instance, WeatherState state) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", instance.getType().name().toLowerCase());
        tag.putInt("remaining", instance.getRemainingTicks());
        tag.putInt("initial", instance.getInitialTicks());
        tag.putInt("duration", instance.getDurationTicks());
        tag.putInt("stillness", instance.getStillnessTicks());
        tag.putString("phase", instance.getPhase().name().toLowerCase());

        tag.putBoolean("paused", state.doesPaused(instance.getId()));

        return tag;
    }

    private static boolean buildNewWeather(ServerLevel level, WeatherState state, WeatherDefinition definition, String id, int duration) {
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
        NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherStartEvent(level, instance));

        CoreHanXu.LOGGER.info("[HX] Started weather: {} -> {}", id, level.dimension());
        return true;
    }

    /**
     * <p><b>
     *     Weather Definition Interface
     * </b></p>
     * <p>
     *     Only type registered at enum {@link WeatherType} can be defined by implements this definition.
     * </p>
     * @since 0.7.0 (Internal Development)
     */
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
        void sendToPlayer(ServerPlayer player, WeatherInstance instance, String dimension);
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
            int oldTicks = this.durationTicks;
            int newTicks = definition.getMinimumDuration() + definition.getRandom().nextInt(definition.getMaximumDuration() - definition.getMinimumDuration() + 1);
            this.remainingTicks = oldTicks;
            this.initialTicks = oldTicks;
            this.durationTicks = newTicks;
        }

        public void ready() {
            this.phase = WeatherPhase.READY;
        }

        public void stillness() {
            this.phase = WeatherPhase.STILLNESS;
            int oldTicks = this.stillnessTicks;
            int newTicks = definition.getMinimumStillness() + definition.getRandom().nextInt(definition.getMaximumStillness() - definition.getMinimumStillness() + 1);
            this.remainingTicks = oldTicks;
            this.initialTicks = oldTicks;
            this.stillnessTicks = newTicks;
        }

        public void idle() {
            this.phase = WeatherPhase.IDLE;
        }

        public void reset() {
            this.remainingTicks = this.initialTicks;
        }

        public void rebuild(WeatherPhase phase, int remainingTicks, int initialTicks) {
            this.phase = phase;
            this.remainingTicks = remainingTicks;
            this.initialTicks = initialTicks;
            // Duration and stillness in register.
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

        public void setInitialTicks(int ticks) {
            this.initialTicks = ticks;
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

        public WeatherPhase getPhase() {
            return phase;
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

        public boolean doesIdle() {
            return phase == WeatherPhase.IDLE;
        }
    }

    public static class WeatherState {
        private final Map<WeatherType, WeatherInstance> activeInstances = new ConcurrentHashMap<>();
        private final Map<WeatherType, WeatherInstance> readyInstances = new ConcurrentHashMap<>();
        private final Map<String, WeatherInstance> stillnessInstances = new ConcurrentHashMap<>();
        private final Set<String> pausedIds = ConcurrentHashMap.newKeySet();

        public void onActiveEnd(WeatherType type, ServerLevel level) {
            NullableValue<WeatherInstance> nullableInstance = getActiveInstance(type);
            if (nullableInstance.isNull()) {
                return;
            }

            WeatherInstance instance = nullableInstance.get();

            instance.stillness();
            stillnessInstances.put(instance.getId(), instance);
            activeInstances.remove(type);

            tryActivateReady(type, level);

            NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherEndEvent(level, instance));
        }

        public void onStillnessEnd(String id, ServerLevel level) {
            NullableValue<WeatherInstance> nullableInstance = getStillnessInstance(id);
            if (nullableInstance.isNull()) {
                return;
            }

            WeatherInstance instance = nullableInstance.get();

            instance.ready();
            readyInstances.put(instance.getType(), instance);
            stillnessInstances.remove(id);

            tryActivateReady(instance.getType(), level);
        }

        public void onActiveEnd(String id, ServerLevel level) {
            for (WeatherInstance instance : activeInstances.values()) {
                if (instance.getId().equals(id)) {
                    WeatherType type = instance.getType();

                    instance.stillness();
                    stillnessInstances.put(id, instance);
                    activeInstances.remove(type);

                    tryActivateReady(type, level);
                }
            }
        }

        public void fallReadyToStillness(WeatherType type) {
            NullableValue<WeatherInstance> nullableInstance = getReadyInstance(type);
            if (nullableInstance.isNull()) {
                return;
            }
            WeatherInstance instance = nullableInstance.get();

            instance.stillness();
            stillnessInstances.put(instance.getId(), instance);
            readyInstances.remove(type);
        }

        public void fallAllReadyToStillness(WeatherType type) {
            for (WeatherInstance instance : readyInstances.values()) {
                if (instance.getType().equals(type)) {
                    instance.stillness();
                    stillnessInstances.put(instance.getId(), instance);
                    readyInstances.remove(type);
                }
            }
        }

        private void tryActivateReady(WeatherType type, ServerLevel level) {
            if (activeInstances.containsKey(type)) {
                return;
            }

            NullableValue<WeatherInstance> nullableInstance = getReadyInstance(type);
            if (nullableInstance.isNull()) {
                return;
            }

            WeatherInstance instance = nullableInstance.get();

            if (!instance.getDefinition().isAble(level)) {
                return;
            }

            instance.activate();
            activeInstances.put(type, instance);
            readyInstances.remove(type);

            NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherStartEvent(level, instance));
        }

        public void setActive(WeatherInstance instance) {
            if (instance == null) {
                return;
            }

            stillnessInstances.remove(instance.getId());

            instance.activate();
            activeInstances.put(instance.getType(), instance);
        }

        public void setReady(WeatherInstance instance) {
            if (instance == null) {
                return;
            }

            WeatherType type = instance.getType();

            if (readyInstances.containsKey(type)) {
                instance.stillness();
                stillnessInstances.put(instance.getId(), instance);
                CoreHanXu.LOGGER.info("[HX] Unable to mask the ready slot of the weather type {}", instance.getType());
                return;
            }

            stillnessInstances.remove(instance.getId());

            instance.ready();
            readyInstances.put(instance.getType(), instance);
        }

        public void maskReady(WeatherInstance instance) {
            if (instance == null) {
                return;
            }

            stillnessInstances.remove(instance.getId());

            instance.ready();
            readyInstances.put(instance.getType(), instance);
        }

        public void pause(String id) {
            pausedIds.add(id);
            CoreHanXu.LOGGER.info("[HX] Paused weather: {}", id);
        }

        public void pause(WeatherType type) {
            for (WeatherInstance instance : activeInstances.values()) {
                if (instance.getType().equals(type)) {
                    pausedIds.add(instance.getId());
                }
            }

            for (WeatherInstance instance : stillnessInstances.values()) {
                if (instance.getType().equals(type)) {
                    pausedIds.add(instance.getId());
                }
            }
            CoreHanXu.LOGGER.info("[HX] Paused weather type: {}", type.toString().toLowerCase());
        }

        public void resume(String id) {
            pausedIds.remove(id);
            CoreHanXu.LOGGER.info("[HX] Resumed weather: {}", id);
        }

        public void resume(WeatherType type) {
            for (WeatherInstance instance : activeInstances.values()) {
                if (instance.getType().equals(type)) {
                    pausedIds.remove(instance.getId());
                }
            }

            for (WeatherInstance instance : stillnessInstances.values()) {
                if (instance.getType().equals(type)) {
                    pausedIds.remove(instance.getId());
                }
            }

            CoreHanXu.LOGGER.info("[HX] Resumed weather type: {}", type.toString().toLowerCase());
        }

        public void putActiveInstance(WeatherType type, WeatherInstance instance) {
            activeInstances.put(type, instance);
        }

        public void putReadyInstance(WeatherType type, WeatherInstance instance) {
            readyInstances.put(type, instance);
        }

        public void putStillnessInstance(String id, WeatherInstance instance) {
            stillnessInstances.put(id, instance);
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

        public @NotNull NullableValue<WeatherInstance> getActiveInstance(WeatherType type) {
            return NullableValue.ofNullable(activeInstances.get(type));
        }

        public @NotNull NullableValue<WeatherInstance> getReadyInstance(WeatherType type) {
            return NullableValue.ofNullable(readyInstances.get(type));
        }

        public @NotNull NullableValue<WeatherInstance> getStillnessInstance(String id) {
            return NullableValue.ofNullable(stillnessInstances.get(id));
        }

        public @NotNull NullableValue<Set<WeatherInstance>> getStillnessInstance(WeatherType type) {
            Set<WeatherInstance> instances = new HashSet<>();
            for (WeatherInstance instance : stillnessInstances.values()) {
                if (instance.getType().equals(type)) {
                    instances.add(instance);
                }
            }

            return instances.isEmpty()? NullableValue.none() : NullableValue.ofNotNull(instances);
        }

        public boolean doesActive(WeatherType type) {
            return activeInstances.get(type) != null;
        }

        public boolean doesReady(WeatherType type) {
            return readyInstances.get(type) != null;
        }

        public boolean doesStillness(WeatherType type) {
            for (WeatherInstance instance : stillnessInstances.values()) {
                if (instance.getType().equals(type)) {
                    return true;
                }
            }
            return false;
        }

        public boolean doesExist(WeatherType type) {
            return doesActive(type) || doesReady(type) || doesStillness(type);
        }

        public boolean doesExist(String id) {
            for (WeatherInstance instance : activeInstances.values()) {
                if (instance.getId().equals(id)) {
                    return true;
                }
            }

            for (WeatherInstance instance : readyInstances.values()) {
                if (instance.getId().equals(id)) {
                    return true;
                }
            }

            for (WeatherInstance instance : stillnessInstances.values()) {
                if (instance.getId().equals(id)) {
                    return true;
                }
            }

            return false;
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
            return stillnessInstances.get(id) != null;
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

        public boolean doesPaused(String id) {
            return pausedIds.contains(id);
        }

        public boolean doesAnyPaused(WeatherType type) {
            for (String id : pausedIds) {
                for (WeatherInstance instance : activeInstances.values()) {
                    if (instance.getId().equals(id) && instance.getType().equals(type)) {
                        return true;
                    }
                }

                for (WeatherInstance instance : stillnessInstances.values()) {
                    if (instance.getId().equals(id) && instance.getType().equals(type)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
