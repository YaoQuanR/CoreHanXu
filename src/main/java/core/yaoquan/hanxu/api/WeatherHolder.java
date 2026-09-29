package core.yaoquan.hanxu.api;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.define.Error;
import core.yaoquan.hanxu.api.define.FilePath;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.api.define.SaveDat;
import core.yaoquan.hanxu.api.event.*;
import core.yaoquan.hanxu.api.weather.Fog;
import core.yaoquan.hanxu.api.weather.ColoredRain;
import core.yaoquan.hanxu.registry.QuickSendPacket;
import core.yaoquan.hanxu.util.type.Exceptionable;
import core.yaoquan.hanxu.util.type.MethodResult;
import core.yaoquan.hanxu.util.type.NullableValue;
import core.yaoquan.hanxu.registry.event.payload.GeneralPayload;
import core.yaoquan.hanxu.util.tool.Cast;
import core.yaoquan.hanxu.util.tool.Creator;
import core.yaoquan.hanxu.util.tool.YamlReader;
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
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p><h3>
 *     Weather System API
 * </h3></p>
 * @since 0.7.0 (Internal Development)
 */
@EventBusSubscriber(modid = CoreHanXu.MOD_ID)
public final class WeatherHolder {
    public static class DefaultColor {
        public static final int RAIN = 0x4667C2;
        public static final int RAINY_SKY = 0x4D82A8;
        public static final int SNOW = 0xEDF8FF;
        public static final int FOG = 0xCCDDEE;
    }

    /**
     * <p>
     *     The enum type that contains all provided weather type
     *     by HanXu (Core) Powered Engine.
     *     It is the reference of the {@link WeatherDefinition}.
     * </p>
     * <p>
     *     For current version:
     *     <li>
     *         {@link Fog}: A classical Minecraft fog that allows to customize the distance, color, and ratio of transition.
     *     </li>
     *     <li>
     *         {@link ColoredRain}: A modifiable rain event that allows to customize the color of sky, rain, and snow.
     *         It also allows to modify the behavior of specific type of biome such as dry, rainy, and snowy biome 's overrides.
     *     </li>
     *     <li>
     *         Others that not yet support:
     *         Wind, Colored Moon, Particle Storm, Aurora, Void Fog.
     *         Definition: {@link core.yaoquan.hanxu.api.weather}.
     *         Null type is for special case.
     *     </li>
     * </p>
     */
    public enum WeatherType {
        FOG, COLORED_RAIN, WIND,
        COLORED_MOON, PARTICLE_STORM,
        AURORA, VOID_FOG,
        NULL,
    }

    /**
     * The enum type that defined the phase of weather instance behaviors.
     * It will be operated by {@link WeatherInstance}.
     * <li>
     *     Active: Phase that is in its working period. Contains duration ticks.
     * </li>
     * <li>
     *     Ready: Phase that not yet active. When the same type of weather stillness,
     *     ready instance will activate as active weather. Otherwise, it remains ready (waiting).
     * </li>
     * <li>
     *     Stillness: Phase that is in its cooldown period. Contains stillness ticks.
     * </li>
     * <li>
     *     Idle: Phase that never active before. It only used when the phase not specified.
     * </li>
     */
    public enum WeatherPhase {
        ACTIVE, READY, STILLNESS, IDLE,
    }

    public enum ModifyType {
        INITIAL,
        REMAINING,
        DURATION,
        STILLNESS,
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

    /**
     * Register a new weather definition for operation.
     * @param definition            The fundamental information of a weather definition,
     *                              where the definition is defined by all specific weathers.
     *                              <li>Please view the implement of {@link WeatherDefinition},
     *                              and the details at {@link core.yaoquan.hanxu.api.weather}.</li>
     */
    public static void register(WeatherDefinition definition) {
        apiWeathers.put(definition.getId(), definition);
        CoreHanXu.LOGGER.info("[HX] Registered weather definition: {}", definition.getId());
    }

    /**
     * Unregister a weather definition.
     * Only allows when operating stillness or idle phase weather.
     * Not able to unregister API weather, unless delete from mod codes and reboot.
     * @param definition            The fundamental information of a weather definition,
     *                              where the definition is defined by all specific weathers.
     *                              <li>Please view the implement of {@link WeatherDefinition},
     *                              and the package at {@link core.yaoquan.hanxu.api.weather} for details.</li>
     * @return                      Success or failure when:
     *                              <li>- Delete when weather are in ready or active phase -> "inUse", definition.getId().</li>
     *                              <li>- Weather not found in register -> "notFound", definition.getId().</li>
     */
    public static @NotNull MethodResult unregister(WeatherDefinition definition) {
        for (WeatherState state : weatherStates.values()) {
            if (state.doesPrepareOrUsing(definition.getId())) {
                CoreHanXu.LOGGER.warn("[HX] Reject to unregister a weather that in use: {}", definition.getId());
                return MethodResult.failure("inUse", definition.getId());
            }
        }

        if (commandWeathers.containsKey(definition.getId())) {
            commandWeathers.remove(definition.getId());
            CoreHanXu.LOGGER.info("[HX] Unregistered weather definition: {}", definition.getId());

            return MethodResult.success();
        }

        return MethodResult.failure("notFound", definition.getId());
    }

    /**
     * Unregister the weather definition and delete the YAML file.
     * Only allows when operating stillness or idle phase weather.
     * Not able to delete API weather, unless delete from mod codes and reboot.
     * @param id                    The defined id of weather definition.
     * @param targetPath            Storage path of YAML file.
     *                              Enum path: TO_GLOBAL or TO_WORLD.
     * @return                      Success or failure when:
     *                              <li>- Delete when weather are in ready or active phase -> "inUse", id.</li>
     *                              <li>- Weather not found in command register -> "notFound", id.</li>
     *                              <li>- YAML not found for this weather -> "yamlNotFound", id.</li>
     */
    public static @NotNull MethodResult unregisterAndDelete(String id, YamlReader.TargetPath targetPath) {
        NullableValue<WeatherDefinition> nullableDefinition = getCommandWeatherDefinition(id);
        if (nullableDefinition.isNull()) {
            return MethodResult.failure("notFound", id);
        }
        WeatherDefinition definition = nullableDefinition.get();

        MethodResult unregister = unregister(definition);

        if (unregister.isSuccess()) {
            try {
                YamlReader.delete("weather", id, targetPath);
                CoreHanXu.LOGGER.info("[HX] Deleted weather YAML: {}", id);
                return MethodResult.success();
            }
            catch (IOException e) {
                CoreHanXu.LOGGER.warn("[HX] Failed to unregister weather and delete weather YAML: {}", id, e);
                return MethodResult.failure("yamlNotFound", id);
            }
        }

        return unregister;
    }

    /**
     * Receive a weather definition.
     * @param id                    The defined id of weather definition.
     * @return                      A nullable value that returns when:
     *                              <li>- Registered weather -> Contains a presented definition.</li>
     *                              <li>- Weather is not registered -> None.</li>
     */
    public static @NotNull NullableValue<WeatherDefinition> getWeatherDefinition(String id) {
        if (apiWeathers.containsKey(id)) {
            return NullableValue.ofNotNull(apiWeathers.get(id));
        }
        if (commandWeathers.containsKey(id)) {
            return NullableValue.ofNotNull(commandWeathers.get(id));
        }
        return NullableValue.none();
    }

    /**
     * Receive a weather definition.
     * @param id                    The defined id of weather definition.
     * @return                      A nullable value that returns when:
     *                              <li>- Registered weather in api -> Contains a presented definition.</li>
     *                              <li>- Weather is not registered -> None.</li>
     */
    public static @NotNull NullableValue<WeatherDefinition> getApiWeatherDefinition(String id) {
        if (apiWeathers.containsKey(id)) {
            return NullableValue.ofNotNull(apiWeathers.get(id));
        }
        return NullableValue.none();
    }

    /**
     * Receive a weather definition.
     * @param id                    The defined id of weather definition.
     * @return                      A nullable value that returns when:
     *                              <li>- Registered weather in command -> Contains a presented definition.</li>
     *                              <li>- Weather is not registered -> None.</li>
     */
    public static @NotNull NullableValue<WeatherDefinition> getCommandWeatherDefinition(String id) {
        if (commandWeathers.containsKey(id)) {
            return NullableValue.ofNotNull(commandWeathers.get(id));
        }
        return NullableValue.none();
    }

    /**
     * Receive a weather state from the level.
     * @param level                 Level, or called dimension. A data set that from {@link ServerLevel}.
     * @return                      A nullable value that returns when:
     *                              <li>- A level that has registered weather -> Contains a presented state.</li>
     *                              <li>- A level that never run with this mod -> None.</li>
     */
    public static @NotNull NullableValue<WeatherState> getWeatherState(ServerLevel level) {
        ResourceKey<Level> key = level.dimension();
        return NullableValue.ofNullable(weatherStates.get(key));
    }

    /**
     * Receive a weather state from the level,
     * or build a new one when this level is first loaded.
     * @param level                 Level, or called dimension. A data set that from {@link ServerLevel}.
     */
    public static @NotNull WeatherState getWeatherStateOrNew(ServerLevel level) {
        ResourceKey<Level> key = level.dimension();
        return weatherStates.computeIfAbsent(key, k -> new WeatherState());
    }

    /**
     * Receive a weather state from the dimension key.
     * @param dimension             A resource key for level dimension that able to found by {@link ResourceKey}.
     * @return                      A nullable value that returns when:
     *                              <li>- A level that has registered weather -> Contains a presented state.</li>
     *                              <li>- A level that never run with this mod -> None.</li>
     */
    public static @NotNull NullableValue<WeatherState> getWeatherState(ResourceKey<Level> dimension) {
        return NullableValue.ofNullable(weatherStates.get(dimension));
    }

    /**
     * Restart as the initial state of weather.
     * It will automatically build a new weather instance if not activate before.
     * @param level                 The targeted level to operate. A data set that from {@link ServerLevel}.
     * @param id                    The defined id of weather definition.
     * @param duration              Specify the next duration ticks. Enter a negative value to ignore this modification.
     * @return                      Success or failure when:
     *                              <li>- Not found in registered weather -> "notFound", id.</li>
     *                              <li>- Operate when weather are in ready or active phase -> "inUse", id.</li>
     */
    public static @NotNull MethodResult restartWeather(ServerLevel level, String id, int duration) {
        NullableValue<WeatherDefinition> nullableDefinition = getWeatherDefinition(id);
        // Check if definition completed.
        if (nullableDefinition.isNull()) {
            CoreHanXu.LOGGER.warn("[HX] Unknown weather definition for activate: {}", id);
            return MethodResult.failure("notFound", id);
        }

        WeatherDefinition definition = nullableDefinition.get();

        WeatherState state = getWeatherStateOrNew(level);

        // Then check if this weather activated.
        if (state.doesActive(definition.getWeatherType())) {
            CoreHanXu.LOGGER.warn("[HX] Rejected to activate a activated weather type: {}", definition.getWeatherType());
            return MethodResult.failure("inUse", id);
        }

        // If it is not existed, build new weather instance.
        buildNewInstance(level, state, definition, id, duration);
        return MethodResult.success();
    }

    /**
     * Restart as the initial state of weather.
     * It will automatically build a new weather instance if not activate before.
     * @param level                 The targeted level to operate. A data set that from {@link ServerLevel}.
     * @param id                    The defined id of weather definition.
     * @return                      Success or failure when:
     *                              <li>- Not found in registered weather -> "notFound", id.</li>
     *                              <li>- Operate when weather are in ready or active phase -> "inUse", id.</li>
     */
    public static @NotNull MethodResult restartWeather(ServerLevel level, String id) {
        return restartWeather(level, id, -1);
    }

    /**
     * Force to activate a weather as active phase.
     * @param level                 The targeted level to operate. A data set that from {@link ServerLevel}.
     * @param id                    The defined id of weather definition.
     * @param duration              Specify the next duration ticks. Enter a negative to ignore this modification.
     * @return                      Success or failure when:
     *                              <li>- Not found in registered weather -> "notFound", id.</li>
     */
    public static @NotNull MethodResult startWeather(ServerLevel level, String id, int duration) {
        NullableValue<WeatherDefinition> nullableDefinition = getWeatherDefinition(id);
        if (nullableDefinition.isNull()) {
            CoreHanXu.LOGGER.warn("[HX] Unknown weather definition for start: {}", id);
            return MethodResult.failure("notFound", id);
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
        if (state.doesRegistered(id)) {
            findInstance(level, id).ifPresent(instance -> {
                if (instance.doesStillness()) {
                    state.setActive(instance);
                }
            });
            CoreHanXu.LOGGER.info("[HX] Started weather type: {}", type);

            // Post event:
            WeatherInstance instance = findInstance(level, id).get();

            NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherStartEvent(level, instance));
            return MethodResult.success();
        }

        buildNewInstance(level, state, definition, id, duration);
        return MethodResult.success();
    }

    /**
     * Force to activate a weather as active phase.
     * @param level                 The targeted level to operate. A data set that from {@link ServerLevel}.
     * @param id                    The defined id of weather definition.
     * @return                      Success or failure when:
     *                              <li>- Not found in registered weather -> "notFound", id.</li>
     */
    public static @NotNull MethodResult startWeather(ServerLevel level, String id) {
        return startWeather(level, id, -1);
    }

    /**
     * Unfreeze the weather process.
     * @param level                 The targeted level to operate. A data set that from {@link ServerLevel}.
     * @param type                  The {@link WeatherType} to operate.
     * @return                      Success or failure when:
     *                              <li>- Weather are not initialized at the targeted world -> "notInitialized", type.</li>
     *                              <li>- This type of weather are not initialized -> "notInitialized", type.</li>
     */
    public static @NotNull MethodResult resumeWeather(ServerLevel level, WeatherType type) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);
        if (nullableState.isNull()) {
            CoreHanXu.LOGGER.warn("[HX] Weather state not yet initialized for resume: {}", type);
            return MethodResult.failure("notInitialized", type.name().toLowerCase());
        }

        WeatherState state = nullableState.get();

        if (!state.doesRegistered(type)) {
            return MethodResult.failure("notInitialized", type.name().toLowerCase());
        }

        state.resume(type);

        // Post event:
        Collection<WeatherInstance> instances = new ArrayList<>();
        instances.addAll(state.getActiveInstances());
        instances.addAll(state.getStillnessInstances());

        instances.forEach(instance -> {
            if (instance.getType().equals(type)) {
                NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherResumeEvent(level, instance));
            }
        });

        return MethodResult.success();
    }

    /**
     * Unfreeze the weather process.
     * @param level                 The targeted level to operate. A data set that from {@link ServerLevel}.
     * @param id                    The defined id of weather definition.
     * @return                      Success or failure when:
     *                              <li>- Not found in registered weather -> "notFound", id.</li>
     *                              <li>- This type of weather are not initialized -> "notInitialized", type.</li>
     */
    public static @NotNull MethodResult resumeWeather(ServerLevel level, String id) {
        NullableValue<WeatherDefinition> nullableDefinition = getWeatherDefinition(id);
        if (nullableDefinition.isNull()) {
            CoreHanXu.LOGGER.warn("[HX] Unknown weather definition for resume: {}", id);
            return MethodResult.failure("notFound", id);
        }

        WeatherState state = getWeatherStateOrNew(level);

        if (!state.doesRegistered(id)) {
            return MethodResult.failure("notInitialized", id);
        }

        state.resume(id);

        // Post event:
        WeatherInstance instance = findInstance(level, id).get();

        NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherResumeEvent(level, instance));
        return MethodResult.success();
    }

    /**
     * Freeze the weather process.
     * @param level                 The targeted level to operate. A data set that from {@link ServerLevel}.
     * @param type                  The {@link WeatherType} to operate.
     * @return                      Success or failure when:
     *                              <li>- Weather not found in the state with the provided type -> "notFound", type.</li>
     *                              <li>- This type of weather are not initialized -> "notInitialized", type.</li>
     */
    public static @NotNull MethodResult pauseWeather(ServerLevel level, WeatherType type) {
        return pauseOrKillWeather(level, type, "pause");
    }

    /**
     * Freeze the weather process.
     * @param level                 The targeted level to operate. A data set that from {@link ServerLevel}.
     * @param id                    The defined id of weather definition.
     * @return                      Success or failure when:
     *                              <li>- Weather not found in the state with the provided id -> "notFound", id.</li>
     *                              <li>- This type of weather are not initialized -> "notInitialized", type.</li>
     */
    public static @NotNull MethodResult pauseWeather(ServerLevel level, String id) {
        return pauseOrKillWeather(level, id, "pause");
    }

    /**
     * Let the specific weather back to the stillness phase.
     * @param level                 The targeted level to operate. A data set that from {@link ServerLevel}.
     * @param type                  The {@link WeatherType} to operate.
     * @return                      Success or failure when:
     *                              <li>- Weather not found in the state with the provided type -> "notFound", type.</li>
     *                              <li>- This type of weather are not initialized -> "notInitialized", type.</li>
     */
    public static @NotNull MethodResult killWeather(ServerLevel level, WeatherType type) {
        return pauseOrKillWeather(level, type, "kill");
    }

    /**
     * Let the specific weather back to the stillness phase.
     * @param level                 The targeted level to operate. A data set that from {@link ServerLevel}.
     * @param id                    The defined id of weather definition.
     * @return                      Success or failure when:
     *                              <li>- Weather not found in the state with the provided id -> "notFound", id.</li>
     *                              <li>- This type of weather are not initialized -> "notInitialized", type.</li>
     */
    public static @NotNull MethodResult killWeather(ServerLevel level, String id) {
        return pauseOrKillWeather(level, id, "kill");
    }

    /**
     * Change the weather to ready phase.
     * @param level                 The targeted level to operate. A data set that from {@link ServerLevel}.
     * @param id                    The defined id of weather definition.
     * @return                      Success or failure when:
     *                              <li>- Not found in registered weather -> "notFound", id.</li>
     */
    public static @NotNull MethodResult prepareWeather(ServerLevel level, String id) {
        NullableValue<WeatherDefinition> nullableDefinition = getWeatherDefinition(id);
        if (nullableDefinition.isNull()) {
            CoreHanXu.LOGGER.warn("[HX] Unknown weather definition for prepare: {}", id);
            return MethodResult.failure("notFound", id);
        }

        WeatherDefinition definition = nullableDefinition.get();

        WeatherState state = getWeatherStateOrNew(level);
        WeatherType type = definition.getWeatherType();

        if (state.doesActive(type)) {
            CoreHanXu.LOGGER.warn("[HX] Activated an active phase weather (Remains no changed): {}", id);
            return MethodResult.success();
        }

        Random random = definition.getRandom();
        WeatherInstance instance = definition.createInstance(random);
        state.setReady(instance);

        CoreHanXu.LOGGER.info("[HX] Prepared weather: {} -> {}", id, definition.getWeatherType());
        return MethodResult.success();
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

    public static boolean doesWeatherInstanceExist(String id) {
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

    public static boolean doesWeatherInstanceExist(String id, String category) {
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

    /**
     * Find the instance by level and weather id.
     * @param level                 The targeted level to operate. A data set that from {@link ServerLevel}.
     * @param id                    The defined id of weather definition.
     * @return                      A nullable value that returns when:
     *                              <li>- A level that has registered weather -> Contains a presented state.</li>
     *                              <li>- A level that never run with this mod -> None.</li>
     */
    public static @NotNull NullableValue<WeatherInstance> findInstance(ServerLevel level, String id) {
        NullableValue<WeatherState> nullableState = getWeatherState(level);

        return switch (nullableState.situation()) {
            case NULL -> NullableValue.none();
            case SOME -> findInstance(nullableState.get(), id);
        };
    }

    /**
     * Find the instance by dimension string and weather id.
     * @param dimension             A resource key string for level dimension.
     * @param id                    The defined id of weather definition.
     * @return                      A nullable value that returns when:
     *                              <li>- A level that has registered weather -> Contains a presented state.</li>
     *                              <li>- A level that never run with this mod -> None.</li>
     */
    public static @NotNull NullableValue<WeatherInstance> findInstance(String dimension, String id) {
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension));
        return findInstance(dimensionKey, id);
    }

    /**
     * Find the instance by dimension resource key and weather id.
     * @param dimension             A resource key for level dimension that able to found by {@link ResourceKey}.
     * @param id                    The defined id of weather definition.
     * @return                      A nullable value that returns when:
     *                              <li>- A level that has registered weather -> Contains a presented state.</li>
     *                              <li>- A level that never run with this mod -> None.</li>
     */
    public static @NotNull NullableValue<WeatherInstance> findInstance(ResourceKey<Level> dimension, String id) {
        NullableValue<WeatherState> nullableState = getWeatherState(dimension);

        return switch (nullableState.situation()) {
            case NULL -> NullableValue.none();
            case SOME -> findInstance(nullableState.get(), id);
        };
    }

    /**
     * Modify the time ticks of weather instance.
     * @param level                 The targeted level to operate. A data set that from {@link ServerLevel}.
     * @param id                    The defined id of weather definition.
     * @param nextTicks             Target ticks to modify.
     * @param type                  Type of modification by {@link ModifyType}.
     * @return                      Success or failure when:
     *                              <li>- Instance not found -> "notFound", type.</li>
     */
    public static @NotNull MethodResult modifyWeatherTime(ServerLevel level, String id, int nextTicks, @NotNull ModifyType type) {
        return findInstance(level, id).matching(
                instance -> {
                    int modifyTicks = Math.max(0, nextTicks);
                    switch (type) {
                        case INITIAL -> instance.setInitialTicks(modifyTicks);
                        case REMAINING -> instance.setRemainingTicks(modifyTicks);
                        case DURATION -> instance.setDurationTicks(modifyTicks);
                        case STILLNESS -> instance.setStillnessTicks(modifyTicks);
                    }
                    return MethodResult.success();
                },
                () -> MethodResult.failure("notFound", type.name().toLowerCase())
        );
    }

    /// Parse string to {@link WeatherType}.
    public static @NotNull WeatherType parseType(String type) {
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

    /// Parse string to {@link WeatherPhase}.
    public static @NotNull WeatherPhase parsePhase(String phase) {
        return switch (phase) {
            case "active" -> WeatherPhase.ACTIVE;
            case "ready" -> WeatherPhase.READY;
            case "stillness" -> WeatherPhase.STILLNESS;
            case null, default -> WeatherPhase.IDLE;
        };
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

    /**
     * Set up the display state for player in F4 info page.
     * @param player                Server player that refer to {@link ServerPlayer}.
     * @param level                 Level, or called dimension. A data set that from {@link ServerLevel}.
     * @param id                    The defined id of weather definition.
     * @param state                 State of display on or off.
     */
    public static void displayToInfoPage(ServerPlayer player, ServerLevel level, String id, boolean state) {
        if (id == null || level == null) {
            return;
        }

        String key = level.dimension().location() + ":" + id;

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

            syncLostPacketToClient(player, id, level.dimension().location().toString(), false);
        }

        findInstance(level, id).matching(
                instance -> {
                    syncPacketToClient(player, instance, level.dimension().location().toString(), state);
                    return null;
                },
                () -> {
                    CoreHanXu.LOGGER.warn("[HX] Weather instance not found for display: {} -> {}", id, level.dimension().location());
                    return null;
                }
        );
    }

    /**
     * Pickup the weather state register where if the definition miss register before.
     * @return                      Success or failure when:
     *                              <li>- Where nothing to be reclaims -> "noUnclaims".</li>
     */
    public static @NotNull MethodResult pickupUnclaimedStates() {
        if (unclaimedStates.isEmpty()) {
            return MethodResult.failure("noUnclaims");
        }

        Set<String> unclaimedIds = new HashSet<>();
        for (Set<String> ids : unclaimedStates.values()) {
            unclaimedIds.addAll(ids);
        }
        if (unclaimedIds.isEmpty()) {
            return MethodResult.failure("noUnclaims");
        }

        loadAllLevelStates(unclaimedIds);
        return MethodResult.success();
    }

    /**
     * <p><b>
     *     Inner Method
     * </b></p>
     * <p>
     *     Pay for your own risk while using this function out of HanXu (Core) Powered Engine.
     * </p>
     */
    @ApiStatus.Internal
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

            Exceptionable<WeatherDefinition> exceptionableDefinition = Creator.createWeatherDefinition(
                    id,
                    type,
                    random,
                    remainingParameters
            );

            return NullableValue.ofNullable(
                    exceptionableDefinition.matching(
                            definition -> definition,
                            (error, info) -> {
                                switch (error) {
                                    case "unknownType" -> CoreHanXu.LOGGER.warn("[HX] Unknown weather type: {}", info);
                                    case "missingField" -> CoreHanXu.LOGGER.warn("[HX] Missing weather field: {}", info);
                                    default -> {}
                                }
                                return null;
                            }
                    )
            );
        }
        catch (IOException e) {
            CoreHanXu.LOGGER.warn("[HX] Failed to load YAML weather: {}", fileName, e);
            return NullableValue.none();
        }
    }

    /**
     * <p><b>
     *     Inner Method
     * </b></p>
     * <p>
     *     Pay for your own risk while using this function out of HanXu (Core) Powered Engine.
     * </p>
     */
    @ApiStatus.Internal
    public static void registerAllYamlWeathers() {
        List<Path> files = YamlReader.listOut("weather");
        for (Path file : files) {
            String fileName = file.getFileName().toString().replace(".yaml", "");

            registerYamlWeather(fileName);
        }
    }

    /**
     * <p><b>
     *     Inner Method
     * </b></p>
     * <p>
     *     Pay for your own risk while using this function out of HanXu (Core) Powered Engine.
     * </p>
     */
    @ApiStatus.Internal
    public static void registerYamlWeather(String fileName) {
        NullableValue<WeatherDefinition> nullableDefinition = loadYamlWeather(fileName);
        nullableDefinition.ifPresent(WeatherHolder::registerYamlWeather);
    }

    /**
     * <p><b>
     *     Inner Method
     * </b></p>
     * <p>
     *     Pay for your own risk while using this function out of HanXu (Core) Powered Engine.
     * </p>
     */
    @ApiStatus.Internal
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

    /**
     * <p><b>
     *     Inner Method
     * </b></p>
     * <p>
     *     Pay for your own risk while using this function out of HanXu (Core) Powered Engine.
     * </p>
     */
    @ApiStatus.Internal
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

    /**
     * <p><b>
     *     Inner Method
     * </b></p>
     * <p>
     *     Pay for your own risk while using this function out of HanXu (Core) Powered Engine.
     * </p>
     */
    @ApiStatus.Internal
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

    /**
     * <p><b>
     *     Inner Method
     * </b></p>
     * <p>
     *     Pay for your own risk while using this function out of HanXu (Core) Powered Engine.
     * </p>
     */
    @ApiStatus.Internal
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

    /**
     * <p><b>
     *     Inner Method
     * </b></p>
     * <p>
     *     Pay for your own risk while using this function out of HanXu (Core) Powered Engine.
     * </p>
     */
    @ApiStatus.Internal
    public static void loadAllLevelStates() {
        loadAllLevelStates(null);
    }

    // Submit packet into F4 display.
    /**
     * <p><b>
     *     Inner Method
     * </b></p>
     * <p>
     *     Pay for your own risk while using this function out of HanXu (Core) Powered Engine.
     * </p>
     */
    @ApiStatus.Internal
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
    /**
     * <p><b>
     *     Inner Method
     * </b></p>
     * <p>
     *     Pay for your own risk while using this function out of HanXu (Core) Powered Engine.
     * </p>
     */
    @ApiStatus.Internal
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

    // Update display every 2 ticks.
    /**
     * <p><b>
     *     Inner Method
     * </b></p>
     * <p>
     *     Pay for your own risk while using this function out of HanXu (Core) Powered Engine.
     * </p>
     */
    @ApiStatus.Internal
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

    /**
     * <p><b>
     *     Inner Method
     * </b></p>
     * <p>
     *     Pay for your own risk while using this function out of HanXu (Core) Powered Engine.
     * </p>
     */
    @ApiStatus.Internal
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;

        if (tickCounter > 99) {
            tickCounter = 0;
        }

        tickSync();
    }

    /**
     * <p><b>
     *     Inner Method
     * </b></p>
     * <p>
     *     Pay for your own risk while using this function out of HanXu (Core) Powered Engine.
     * </p>
     */
    @ApiStatus.Internal
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        NullableValue<WeatherState> nullableState = getWeatherState(level);
        if (nullableState.isNull()) {
            return;
        }

        WeatherState state = nullableState.get();

        for (WeatherInstance instance : state.getReadyInstances()) {
            WeatherType type = instance.getType();
            WeatherDefinition definition = instance.getDefinition();
            if (!state.doesPaused(instance.getId()) && definition.isAble(level)) {
                state.tryActivateReady(type, level);
            }
        }

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

    private static @NotNull MethodResult pauseOrKillWeather(@NotNull ServerLevel level, @NotNull Object key, @NotNull String category) {
        WeatherType type = key instanceof WeatherType? (WeatherType) key : null;
        String id = key instanceof String? (String) key : null;

        // Ensures the key will not be unsolvable class.
        if (type == null && id == null) {
            return MethodResult.failure("unexpected", key.toString());
        }

        NullableValue<WeatherState> nullableState = getWeatherState(level);
        if (nullableState.isNull()) {
            CoreHanXu.LOGGER.warn("[HX] Weather state not yet initialized for pause: {}", key);
            return MethodResult.failure("notInitialized", key.toString());
        }

        WeatherState state = nullableState.get();

        switch (category) {
            case "pause" -> {
                // Handle id:
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
                        return MethodResult.failure("notFound", id);
                    }

                    NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherPauseEvent(level, instance));
                }
                // Handle type:
                else {
                    boolean notFound = state.getActiveInstance(type).isNull() && state.getStillnessInstance(type).isNull();
                    if (notFound) {
                        return MethodResult.failure("notFound", type.name().toLowerCase());
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
                return MethodResult.success();
            }
            case "kill" -> {
                // Handle id:
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
                        for (WeatherInstance thisInstance : state.getReadyInstances()) {
                            if (thisInstance.getId().equals(id)) {
                                state.setStillness(thisInstance);
                                instance = thisInstance;
                                break;
                            }
                        }
                    }

                    if (instance == null) {
                        for (WeatherInstance thisInstance : state.getStillnessInstances()) {
                            if (thisInstance.getId().equals(id)) {
                                CoreHanXu.LOGGER.info("[HX] Killed a stillness weather (Remains no changed): {}", id);
                                return MethodResult.success();
                            }
                        }
                    }

                    if (instance == null) {
                        CoreHanXu.LOGGER.warn("[HX] Weather instance not found for kill: {}", id);
                        return MethodResult.failure("notFound", id);
                    }

                    NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherEndEvent(level, instance));
                }
                // Handle type:
                else {
                    if (state.getActiveInstance(type).isNull()) {
                        return MethodResult.failure("notFound", type.name().toLowerCase());
                    }

                    // Kill this active instance.
                    state.getActiveInstance(type).ifPresent(instance -> {
                        state.onActiveEnd(type, level);
                        NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherEndEvent(level, instance));
                    });
                    // And kill the ready instance.
                    state.getReadyInstance(type).ifPresent(instance -> {
                        state.setStillness(instance);
                        NeoForge.EVENT_BUS.post(new WeatherEvents.WeatherEndEvent(level, instance));
                    });
                    // Stillness do nothing.
                }

                CoreHanXu.LOGGER.info("[HX] Killed weather: {} -> {}", id, level.dimension().location());
                return MethodResult.success();
            }
            default -> {
                return MethodResult.failure("undefinedCategory", category);
            }
        }
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

    private static void buildNewInstance(ServerLevel level, WeatherState state, WeatherDefinition definition, String id, int duration) {
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
    }

    /**
     * <p><h3>
     *     Weather Definition Interface
     * </h3></p>
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

    /**
     * Manager that handling the instance of weather.
     * <p>
     *     It will be used for storage id, {@link WeatherType}, {@link WeatherDefinition},
     *     {@link WeatherPhase}, and the necessary time parameters for operations.
     * </p>
     * <p>
     *     Operates actively by using methods {@link #activate()}, {@link #stillness()},
     *     and other operation methods that within the instance by hand.
     *     It will be automatically executed to change the weather phase when finished the setup and start a weather.
     * </p>
     */
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
                    stillnessInstances.put(instance.getId(), instance);
                    activeInstances.remove(type);

                    tryActivateReady(type, level);

                    return;
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

        public void tryActivateReady(WeatherType type, ServerLevel level) {
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

        public void setStillness(WeatherInstance instance) {
            if (instance == null) {
                return;
            }

            activeInstances.remove(instance.getType());
            readyInstances.remove(instance.getType());

            instance.stillness();
            stillnessInstances.put(instance.getId(), instance);
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

        public boolean doesRegistered(WeatherType type) {
            return doesActive(type) || doesReady(type) || doesStillness(type);
        }

        public boolean doesRegistered(String id) {
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
