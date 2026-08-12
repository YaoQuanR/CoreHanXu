package core.yaoquan.hanxu.api;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.custom.BehaviorRegistry;
import core.yaoquan.hanxu.api.define.FilePath;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.api.solution.NullableValue;
import core.yaoquan.hanxu.registry.event.payload.GeneralPayload;
import core.yaoquan.hanxu.util.Creator;
import core.yaoquan.hanxu.util.Resolver;
import core.yaoquan.hanxu.util.YamlReader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static core.yaoquan.hanxu.api.define.Error.*;

/**
 * <p><b>
 *     Attribute system API
 * </b></p>
 * <p>
 *     Attribute system is a heavy system that storage value, threshold behavior,
 *     zero callback, and recovery system.
 *     The primarily use of attribute system is provided for players and entities.
 * </p>
 * @since 0.5.0 (Internal Development)
 */
@EventBusSubscriber(modid = CoreHanXu.MOD_ID)
public class AttributeHolder {
    // Storage attribute definitions.
    private static final Map<String, CustomAttribute> apiAttributes = new ConcurrentHashMap<>();
    private static final Map<String, CustomAttribute> commandAttributes = new ConcurrentHashMap<>();
    // Storage attributes.
    private static final Map<UUID, Map<String, Float>> attributeValues = new ConcurrentHashMap<>();
    // Storage debug display list.
    private static final Set<String> refreshDisplayList = ConcurrentHashMap.newKeySet();
    // Define subscribed info's sync frequency.
    private static int tickCounter = 0;

    /**
     * Define when did threshold should be triggered.
     */
    public enum ThresholdDirection {
        /// Trigger when the value raise from below to meet or exceed the threshold.
        UP,
        /// Trigger when the value falls from above to meet or below the threshold.
        DOWN,
        /// Trigger when the value pass through the threshold in either direction.
        FLEX,
        /// Default. Only trigger when the value meet the threshold.
        POINT
    }

    public enum UpdateCategory {
        normalThreshold,
        zeroThreshold,
        recovery,
    }

    /**
     * For API register.
     * @param attribute         Create a new CustomAttribute {@link CustomAttribute}
     *                          included custom callback definitions by fluent factory.
     */
    public static void register(CustomAttribute attribute) {
        apiAttributes.put(attribute.getAttributeId(), attribute);
        CoreHanXu.LOGGER.info("[HX] Registered attribute: {}", attribute.getAttributeId());
    }

    /// For API unregister.
    public static void unregister(CustomAttribute attribute) {
        apiAttributes.remove(attribute.getAttributeId());
        CoreHanXu.LOGGER.info("[HX] Unregistered attribute: {}", attribute.getAttributeId());
    }

    /**
     * For command register.
     * @param attributeId       Unique title of attribute.
     * @param maximum           Define the maximum changeable value of attribute.
     * @param defaultValue      Define the start value of attribute.
     * @return                  Does register success: boolean.
     */
    public static boolean register(String attributeId, float maximum, float defaultValue, YamlReader.TargetPath targetPath) {
        if (apiAttributes.containsKey(attributeId) || commandAttributes.containsKey(attributeId)) {
            CoreHanXu.LOGGER.info("[HX] Rejected duplicate attribute: {}", attributeId);
            return false;
        }

        CustomAttribute attribute = new CustomAttribute(attributeId, maximum, defaultValue);
        commandAttributes.put(attributeId, attribute);

        saveYamlAttributeSkeleton(attributeId, maximum, defaultValue, targetPath);

        CoreHanXu.LOGGER.info("[HX] Registered attribute by command: {}", attribute.getAttributeId());
        return true;
    }

    /**
     * For Command source unregister.
     * @param attributeId       Unique title of attribute.
     * @return                  Does unregister success: boolean.
     */
    public static boolean unregister(String attributeId) {
        if (commandAttributes.containsKey(attributeId)) {
            commandAttributes.remove(attributeId);

            CoreHanXu.LOGGER.info("[HX] Unregistered attribute by string: {}", attributeId);
            return true;
        }
        return false;
    }

    /**
     * For command unregister (delete YAML).
     * @param attributeId       Unique title of attribute.
     * @param targetPath        Enum path: TO_GLOBAL or TO_WORLD.
     * @return                  Does unregister success: boolean.
     */
    public static boolean unregisterAndDelete(String attributeId, YamlReader.TargetPath targetPath) {
        boolean unregister = unregister(attributeId);

        if (unregister) {
            try {
                YamlReader.delete("attribute", attributeId, targetPath);
                CoreHanXu.LOGGER.info("[HX] Deleted attribute YAML: {}", attributeId);
                return true;
            }
            catch (IOException e) {
                CoreHanXu.LOGGER.warn("[HX] Failed to unregister attribute and deleted YAML: {}", attributeId, e);
            }
        }
        return false;
    }

    public static @NotNull NullableValue<CustomAttribute> getAttributeDefinition(String attributeId, boolean isApiAttribute) {
        if (isApiAttribute) {
            return NullableValue.ofNullable(apiAttributes.get(attributeId));
        }
        else {
            return NullableValue.ofNullable(commandAttributes.get(attributeId));
        }
    }

    public static Map<String, CustomAttribute> getApiAttributes() {
        return apiAttributes;
    }

    public static Map<String, CustomAttribute> getCommandAttributes() {
        return commandAttributes;
    }

    public static @NotNull NullableValue<CustomAttribute> getApiAttribute(String attributeId) {
        return NullableValue.ofNullable(apiAttributes.get(attributeId));
    }

    public static @NotNull NullableValue<CustomAttribute> getCommandAttribute(String attributeId) {
        return NullableValue.ofNullable(commandAttributes.get(attributeId));
    }

    /**
     * Get attribute value.
     * @param masterId          Use player id/"-global"/"-temporary" to define the master.
     *                          You can by checking {@link core.yaoquan.hanxu.util.Resolver} for details.
     * @param attributeId       Unique title of attribute.
     * @param isApiAttribute    True false that where you use this function.
     * @return                  Received player value: float.
     *                          If this player not set the value yet, return default value from definition: float.
     */
    public static float getValue(UUID masterId, String attributeId, boolean isApiAttribute) {
        return getAttributeDefinition(attributeId, isApiAttribute)
                .matching(
                        attribute -> {
                            // Receive value, create new concurrent hash map if null.
                            Map<String, Float> playerValues = attributeValues
                                    .computeIfAbsent(masterId, k -> new ConcurrentHashMap<>());

                            // Return received value, or return default value from definition if null.
                            return playerValues.computeIfAbsent(attribute.getAttributeId(), k -> attribute.getDefaultValue());
                        },
                        () -> {
                            CoreHanXu.LOGGER.warn("[HX] Unknown custom attribute for get value: {}", attributeId);
                            return 0.0f;
                        }
                );
    }

    /**
     * Get global's attribute value.
     * @param attributeId       Unique title of attribute.
     * @param isApiAttribute    True false that where you use this function.
     * @return                  Received player value: float.
     *                          If this player not set the value yet, return default value from definition: float.
     */
    public static float getGlobalValue(String attributeId, boolean isApiAttribute) {
        return getValue(General.TargetUUID.GLOBAL_UUID, attributeId, isApiAttribute);
    }

    /**
     * Set attribute value (Full direction trigger).
     * @param masterId          Use player id/"-global"/"-temporary" to define the master.
     *                          You can by checking {@link core.yaoquan.hanxu.util.Resolver} for details.
     * @param attributeId       Unique title of attribute.
     * @param value             Submit the new value for setter.
     * @param isApiAttribute    True false that where you use this function.
     * @param direction         Define the trigger type for threshold callbacks.
     *                          You can by checking {@link ThresholdDirection} for details.
     * @return                  Does the setter success: boolean.
     */
    public static boolean setValue(UUID masterId, String attributeId, float value, boolean isApiAttribute, ThresholdDirection direction) {
        NullableValue<CustomAttribute> nullableAttribute = getAttributeDefinition(attributeId, isApiAttribute);
        if (nullableAttribute.isNull()) {
            CoreHanXu.LOGGER.warn("[HX] Unknown custom attribute for set value: {}", attributeId);
            return false;
        }

        CustomAttribute attribute = nullableAttribute.get();

        float currentValue = getValue(masterId, attributeId, isApiAttribute);
        float newValue = Math.min(attribute.getMaximum(), Math.max(value, 0.0f));

        if (Float.compare(currentValue, newValue) == 0) {
            return false;
        }

        Map<String, Float> values = attributeValues
            .computeIfAbsent(masterId, k -> new ConcurrentHashMap<>());

        values.put(attribute.getAttributeId(), newValue);

        if (direction == null) {
            direction = ThresholdDirection.POINT;
        }

        // Determine if meet the threshold.
        checkThresholdTriggered(masterId, attribute, attributeId, currentValue, newValue, direction);

        // Determine if reached to zero.
        checkZeroTriggered(masterId, attribute, attributeId, currentValue, newValue);

        // Then sync.
        ServerPlayer player = Resolver.resolveTargetPlayer(masterId);
        if (player != null) {
            checkAndRefreshDisplay(player, masterId, attributeId, isApiAttribute);
        }

        return true;
    }

    /**
     * Set player's attribute value (Default: Point trigger).
     * @param masterId          Use player id/"-global"/"-temporary" to define the master.
     *                          You can by checking {@link core.yaoquan.hanxu.util.Resolver} for details.
     * @param attributeId       Unique title of attribute.
     * @param value             Submit the new value for setter.
     * @param isApiAttribute    True false that where you use this function.
     * @return                  Does the setter success: boolean.
     */
    public static boolean setValue(UUID masterId, String attributeId, float value, boolean isApiAttribute) {
        return setValue(masterId, attributeId, value, isApiAttribute, ThresholdDirection.POINT);
    }

    /**
     * Set global's attribute value (Full direction trigger).
     * @param attributeId       Unique title of attribute.
     * @param value             Submit the new value for setter.
     * @param isApiAttribute    True false that where you use this function.
     * @param direction         Define the trigger type for threshold callbacks.
     *                          You can by checking {@link ThresholdDirection} for details.
     * @return                  Does the setter success: boolean.
     */
    public static boolean setGlobalValue(String attributeId, float value, boolean isApiAttribute, ThresholdDirection direction) {
        return setValue(General.TargetUUID.GLOBAL_UUID, attributeId, value, isApiAttribute, direction);
    }

    /**
     * Set global's attribute value (Default: Point trigger).
     * @param attributeId       Unique title of attribute.
     * @param value             Submit the new value for setter.
     * @param isApiAttribute    True false that where you use this function.
     * @return                  Does the setter success: boolean.
     */
    public static boolean setGlobalValue(String attributeId, float value, boolean isApiAttribute) {
        return setValue(General.TargetUUID.GLOBAL_UUID, attributeId, value, isApiAttribute, ThresholdDirection.POINT);
    }

    public static boolean addValue(UUID masterId, String attributeId, float value, boolean isApiAttribute, ThresholdDirection direction) {
        float currentValue = getValue(masterId, attributeId, isApiAttribute);
        return setValue(masterId, attributeId, currentValue + value, isApiAttribute, direction);
    }

    public static boolean addValue(UUID masterId, String attributeId, float value, boolean isApiAttribute) {
        float currentValue = getValue(masterId, attributeId, isApiAttribute);
        return setValue(masterId, attributeId, currentValue + value, isApiAttribute, ThresholdDirection.POINT);
    }

    public static boolean addGlobalValue(String attributeId, float value, boolean isApiAttribute, ThresholdDirection direction) {
        float currentValue = getValue(General.TargetUUID.GLOBAL_UUID, attributeId, isApiAttribute);
        return setValue(General.TargetUUID.GLOBAL_UUID, attributeId, currentValue + value, isApiAttribute, direction);
    }

    public static boolean addGlobalValue(String attributeId, float value, boolean isApiAttribute) {
        float currentValue = getValue(General.TargetUUID.GLOBAL_UUID, attributeId, isApiAttribute);
        return setValue(General.TargetUUID.GLOBAL_UUID, attributeId, currentValue + value, isApiAttribute, ThresholdDirection.POINT);
    }

    public static boolean reduceValue(UUID masterId, String attributeId, float value, boolean isApiAttribute, ThresholdDirection direction) {
        float currentValue = getValue(masterId, attributeId, isApiAttribute);
        return setValue(masterId, attributeId, currentValue - Math.abs(value), isApiAttribute, direction);
    }

    public static boolean reduceValue(UUID masterId, String attributeId, float value, boolean isApiAttribute) {
        float currentValue = getValue(masterId, attributeId, isApiAttribute);
        return setValue(masterId, attributeId, currentValue - Math.abs(value), isApiAttribute, ThresholdDirection.POINT);
    }

    public static boolean reduceGlobalValue(String attributeId, float value, boolean isApiAttribute, ThresholdDirection direction) {
        float currentValue = getValue(General.TargetUUID.GLOBAL_UUID, attributeId, isApiAttribute);
        return setValue(General.TargetUUID.GLOBAL_UUID, attributeId, currentValue - Math.abs(value), isApiAttribute, direction);
    }

    public static boolean reduceGlobalValue(String attributeId, float value, boolean isApiAttribute) {
        float currentValue = getValue(General.TargetUUID.GLOBAL_UUID, attributeId, isApiAttribute);
        return setValue(General.TargetUUID.GLOBAL_UUID, attributeId, currentValue - Math.abs(value), isApiAttribute, ThresholdDirection.POINT);
    }

    // Load YAML data for import.
    /**
     * Get the YAML attribute data from sub path "attribute" for all .yaml documents.
     * @param fileName          The file name of YAML.
     * @return                  New attribute class data: NullableValue<\Attribute>.
     */
    public static NullableValue<Attribute> loadYamlAttribute(String fileName) {
        try {
            Map<String, Object> attributeData = YamlReader.read("attribute", fileName);

            // Check if the id equals to file name.
            Attribute attribute = parseAttributeData(attributeData);
            String yamlFileName = attribute.id;
            if (yamlFileName != null && !yamlFileName.equals(fileName)) {
                CoreHanXu.LOGGER.warn("{}{} ≠ {}", returnCodeError(CodeError.mismatchFileElement), fileName, yamlFileName);
                return NullableValue.none();
            }

            return NullableValue.ofNullable(attribute);
        }
        catch (IOException e) {
            CoreHanXu.LOGGER.warn("[HX] Failed to load YAML attribute: {} ", fileName, e);
            return NullableValue.none();
        }
    }

    /**
     * Delete YAML attribute from selected target.
     * @param fileName            As same as file name.
     * @param targetPath          Enum path: TO_GLOBAL or TO_WORLD.
     * @return                    Does the delete success: boolean.
     */
    public static boolean deleteYamlAttribute(String fileName, YamlReader.TargetPath targetPath) {
        try {
            YamlReader.delete("attribute", fileName, targetPath);
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    public static boolean doesYamlAttributeExist(String fileName) {
        try {
            YamlReader.read("attribute", fileName);
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    public static boolean doesYamlAttributeExist(String fileName, YamlReader.TargetPath targetPath) {
        return YamlReader.doesFileExist(targetPath, "attribute", fileName);
    }

    public static boolean doesAttributeExist(String attributeId) {
        return commandAttributes.containsKey(attributeId) || apiAttributes.containsKey(attributeId);
    }

    public static boolean doesAttributeExist(String attributeId, String attributeCategory) {
        return switch (attributeCategory) {
            case "api" -> apiAttributes.containsKey(attributeId);
            case "command", "yaml" -> commandAttributes.containsKey(attributeId);
            default -> false;
        };
    }

    // Display out to F4 page (info page).
    public static void displayToInfoPage(ServerPlayer player, UUID masterId, String attributeId, boolean isApiAttribute, boolean state) {
        if (masterId == null) {
            return;
        }

        String key = player.getUUID() + ":" + masterId + ":" + attributeId;

        if (state) {
            refreshDisplayList.add(key);
            float value = getValue(masterId, attributeId, isApiAttribute);
            syncPacketToClient(player, masterId, attributeId, true, value, isApiAttribute, Resolver.resolveTargetMasterName(masterId));
        }
        else {
            refreshDisplayList.remove(key);
            syncPacketToClient(player, masterId, attributeId, false, -1.0f, isApiAttribute, Resolver.resolveTargetMasterName(masterId));
        }
    }

    // Submit packet into F4 display.
    public static void syncPacketToClient(ServerPlayer player,
                                          UUID masterId,
                                          String attributeId,
                                          boolean state,
                                          float value,
                                          boolean isApiAttribute,
                                          String masterName) {
        CoreHanXu.LOGGER.info("[HX] Sync packet: Attribute system for display: {} -> {} (state:{})", attributeId, player.getName(), state);

        PacketDistributor.sendToPlayer(
                player, new GeneralPayload.AttributeF4Packet(masterId, attributeId, state, value, isApiAttribute, masterName)
        );
    }

    // Check if required to refresh the F4 attribute display.
    public static void checkAndRefreshDisplay(ServerPlayer player, UUID masterId, String attributeId, boolean isApiAttribute) {
        String key = player.getUUID() + ":" + masterId + ":" + attributeId;

        if (refreshDisplayList.contains(key)) {
            displayToInfoPage(player, masterId, attributeId, isApiAttribute, true);
        }
    }

    // Update display every 5 ticks.
    public static void tickSync() {
        tickCounter++;
        if (tickCounter < 5) {
            return;
        }
        tickCounter = 0;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        for (String key : refreshDisplayList) {
            String[] parts = key.split(":", 3);
            UUID playerId = UUID.fromString(parts[0]);
            UUID masterId = UUID.fromString(parts[1]);
            String attributeId = parts[2];

            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player == null) {
                continue;
            }

            CustomAttribute attribute = apiAttributes.get(attributeId);
            boolean isApiAttribute = true;
            if (attribute == null) {
                attribute = commandAttributes.get(attributeId);
                isApiAttribute = false;
            }
            if (attribute == null) {
                continue;
            }

            float value = getValue(masterId, attributeId, isApiAttribute);

            syncPacketToClient(player, masterId, attributeId, true, value, isApiAttribute, Resolver.resolveTargetMasterName(masterId));
        }
    }

    public static void tickRecovery() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        for (UUID masterId : attributeValues.keySet()) {
            Map<String, Float> values = attributeValues.get(masterId);
            if (values == null) {
                continue;
            }

            for (Map.Entry<String, Float> value : values.entrySet()) {
                String attributeId = value.getKey();
                float currentValue = value.getValue();

                NullableValue<CustomAttribute> nullableAttribute = NullableValue.ofNullable(apiAttributes.get(attributeId))
                        .getOrOther(NullableValue.ofNullable(commandAttributes.get(attributeId)));

                if (nullableAttribute.isNull()) {
                    continue;
                }

                CustomAttribute attribute = nullableAttribute.get();
                if (attribute.getRecoveryCurveId() == null || attribute.recoveryIntervalTicks <= 0) {
                    continue;
                }

                attribute.countTickRecovery();

                if (attribute.recoveryTickCounter == attribute.recoveryIntervalTicks) {
                    attribute.resetTickRecovery();

                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("master_id", masterId.toString());
                    parameters.put("master_name", Resolver.resolveTargetMasterName(masterId));
                    parameters.put("attribute_id", attributeId);
                    parameters.put("threshold", "0"); // Meaningless.
                    parameters.put("current_value", String.valueOf(currentValue));
                    parameters.put("new_value", String.valueOf(currentValue)); // Meaningless.
                    parameters.put("direction", "point"); // Meaningless.

                    ServerPlayer player = Resolver.resolveTargetPlayer(masterId);
                    BehaviorRegistry.execute(player, attribute.getRecoveryCurveId(), parameters);
                }
            }
        }
    }

    public static void saveAttributeToPlayer(ServerPlayer player) {
        String headKey = "core.yaoquan.hanxu.player_attributes";
        CompoundTag dataRoot = player.getPersistentData();
        CompoundTag allAttributesTag = new CompoundTag();

        Map<String, Float> playerValues = attributeValues.get(player.getUUID());
        if (playerValues != null) {
            for (var entry : playerValues.entrySet()) {
                CompoundTag attributeDataTag = saveAttributeData(entry);

                allAttributesTag.put(entry.getKey(), attributeDataTag);
            }
        }

        dataRoot.put(headKey, allAttributesTag);
    }

    public static void saveAttributeToGlobal(ServerLevel level) {
        String headKey = "core.yaoquan.hanxu.global_attributes";
        CompoundTag dataRoot = new CompoundTag();
        CompoundTag allAttributesTag = new CompoundTag();

        Map<String, Float> globalValues = attributeValues.get(General.TargetUUID.GLOBAL_UUID);
        if (globalValues != null) {
            for (var entry : globalValues.entrySet()) {
                CompoundTag attributeDataTag = saveAttributeData(entry);

                allAttributesTag.put(entry.getKey(), attributeDataTag);
            }
        }

        dataRoot.put(headKey, allAttributesTag);

        Path file = FilePath.getModDataPath(level);
        try {
            NbtIo.writeCompressed(dataRoot, file.toFile().toPath());
        }
        catch (IOException e) {
            CoreHanXu.LOGGER.error("[HX] Failed to save global attribute", e);
        }
    }

    public static void loadAttributeForPlayer(ServerPlayer player) {
        String headKey = "core.yaoquan.hanxu.player_attributes";
        CompoundTag dataRoot = player.getPersistentData();
        CompoundTag allAttributesTag = dataRoot.getCompound(headKey).orElse(new CompoundTag());

        rebuildAttributeData(player.getUUID(), allAttributesTag);
    }

    public static void loadAttributeForGlobal(ServerLevel level) {
        String headKey = "core.yaoquan.hanxu.global_attributes";
        Path file = FilePath.getModDataPath(level);

        // Skip load if not exist.
        if (!file.toFile().exists()) {
            return;
        }

        CompoundTag dataRoot;
        try {
            // Limited to 32MB -> 128 Depth.
            NbtAccounter accounter = General.Standard.newNbtAccounter();
            dataRoot = NbtIo.readCompressed(file, accounter);
        }
        catch (IOException e) {
            CoreHanXu.LOGGER.error("[HX] Failed to load global attributes", e);
            return;
        }

        CompoundTag allAttributesTag = dataRoot.getCompound(headKey).orElse(new CompoundTag());

        rebuildAttributeData(General.TargetUUID.GLOBAL_UUID, allAttributesTag);
    }

    public static void registerAllYamlAttributes() {
        List<Path> files = YamlReader.listOut("attribute");
        for (Path file : files) {
            String fileName = file.getFileName().toString().replace(".yaml", "");

            NullableValue<Attribute> nullableAttribute = loadYamlAttribute(fileName);

            nullableAttribute.ifPresent(attribute -> registerYamlAttribute(attribute, false));
        }
    }

    @SuppressWarnings("unchecked")
    public static void saveYamlAttribute(String attributeId, UpdateCategory updateCategory, float threshold, String callbackId, String behavior, String content) {
        Path globalFile = FilePath.getGlobalPath().resolve("attribute").resolve(attributeId + ".yaml");
        Path worldRoot = FilePath.getWorldPath();
        Path worldFile = worldRoot != null? worldRoot.resolve("attribute").resolve(attributeId + ".yaml") : null;
        Path targetFile;
        YamlReader.TargetPath targetPath;

        if (worldFile != null && Files.exists(worldFile)) {
            targetFile = worldFile;
            targetPath = YamlReader.TargetPath.TO_WORLD;
        }
        else {
            targetFile = globalFile;
            targetPath = YamlReader.TargetPath.TO_GLOBAL;
        }

        Map<String, Object> yamlAttributeData;

        if (Files.exists(targetFile)) {
            try {
                yamlAttributeData = YamlReader.read(targetFile);
            }
            catch (IOException e) {
                CoreHanXu.LOGGER.warn("[HX] Failed to read attribute data: {}", targetFile);
                return;
            }
        }
        else {
            CustomAttribute attribute = commandAttributes.get(attributeId);
            if (attribute == null) {
                CoreHanXu.LOGGER.warn("[HX] No defined YAML (command) attribute found: {}", attributeId);
                return;
            }
            yamlAttributeData = new LinkedHashMap<>();
            yamlAttributeData.put("id", attributeId);
            yamlAttributeData.put("maximum", attribute.getMaximum());
            yamlAttributeData.put("default", attribute.getDefaultValue());
            yamlAttributeData.put("recovery_interval", attribute.getRecoveryIntervalTicks());
        }

        switch (updateCategory) {
            case normalThreshold -> {
                if (threshold > 0.0f) {
                    List<Map<String, Object>> thresholds =
                        (List<Map<String, Object>>) yamlAttributeData.getOrDefault("thresholds", new ArrayList<>());

                    boolean exists = thresholds.stream().anyMatch(existing ->
                            ((Number) existing.get("threshold")).floatValue() == threshold &&
                                    existing.get("id").equals(callbackId) &&
                                    existing.get("behavior").equals(behavior) &&
                                    Objects.equals(existing.get("content"), content)
                    );

                    if (!exists) {
                        Map<String, Object> newThreshold = new LinkedHashMap<>();
                        newThreshold.put("threshold", threshold);
                        newThreshold.put("id", callbackId);
                        newThreshold.put("behavior", behavior);
                        newThreshold.put("content", content);

                        thresholds.add(newThreshold);
                        yamlAttributeData.put("thresholds", thresholds);
                    }
                    else {
                        String ignoredThreshold = threshold + " " + callbackId + " -> " + behavior + " + " + content;
                        CoreHanXu.LOGGER.warn("[HX] Ignored to save for a completely same threshold: {}", ignoredThreshold);
                    }
                }
                else {
                    CoreHanXu.LOGGER.warn("[HX] Threshold is non-positive, but using normal threshold category: {}", attributeId);
                    return;
                }
            }
            case zeroThreshold -> {
                if (threshold == 0.0f) {
                    Map<String, Object> zero = new LinkedHashMap<>();

                    zero.put("id", callbackId);
                    zero.put("behavior", behavior);
                    zero.put("content", content);

                    yamlAttributeData.put("zero", zero);
                }
                else {
                    CoreHanXu.LOGGER.warn("[HX] Threshold is non-zero, but using zero threshold category: {}", attributeId);
                    return;
                }
            }
            case recovery -> {
                Map<String, Object> recovery = new LinkedHashMap<>();

                recovery.put("id", callbackId);
                recovery.put("behavior", behavior);

                if (content != null) {
                    recovery.put("recovery_interval", Integer.valueOf(content.split(":", 3)[0]));
                    recovery.put("value", Float.valueOf(content.split(":", 3)[1]));
                    recovery.put("direction", content.split(":", 3)[2]);
                }

                yamlAttributeData.put("recovery", recovery);
            }
        }

        // Then save.
        try {
            YamlReader.save("attribute", attributeId, yamlAttributeData, targetPath);
        }
        catch (IOException e) {
            CoreHanXu.LOGGER.warn("[HX] Failed to save YAML attribute: {}", targetFile);
        }
    }

    // Check if trigger threshold behavior.
    private static void checkThresholdTriggered(UUID masterId, CustomAttribute attribute, String attributeId,
                                                float currentValue, float newValue, ThresholdDirection direction) {
        for (Map.Entry<Float, String> thresholdCallback : attribute.getThresholdCallbacks().entrySet()) {
            float threshold = thresholdCallback.getKey();
            String callbackId = thresholdCallback.getValue();

            boolean triggered = false;
            String triggeredType = null;

            // Trigger: new (x) ~= threshold (x).
            if (Math.abs(newValue - threshold) < 0.01f && direction == ThresholdDirection.POINT) {
                triggered = true;
                triggeredType = "point";
            }
            // else determine if required.
            if (direction != ThresholdDirection.POINT) {
                // Trigger: current (x=0) -> threshold (x+1) -> new (x+2).
                boolean triggeredByUp = currentValue <= threshold && newValue > threshold;
                // Trigger: new (x-2) <- threshold (x-1) <- current (x=0).
                boolean triggeredByDown = currentValue >= threshold && newValue < threshold;

                switch (direction) {
                    case UP:
                        if (triggeredByUp) {
                            triggered = true;
                            triggeredType = "up";
                        }
                        break;
                    case DOWN:
                        if (triggeredByDown) {
                            triggered = true;
                            triggeredType = "down";
                        }
                        break;
                    case FLEX:
                        if (triggeredByUp || triggeredByDown) {
                            triggered = true;
                            triggeredType = "flex";
                        }
                        break;
                    default:
                        triggeredType = "unknown";
                        break;
                }
            }

            // Send parameters to defined callback. Receive storage data by using snake case.
            if (triggered) {
                Map<String, String> parameters = new HashMap<>();
                String masterName = Resolver.resolveTargetMasterName(masterId);

                parameters.put("master_id", masterId.toString());
                parameters.put("master_name", masterName);
                parameters.put("attribute_id", attributeId);
                parameters.put("threshold", String.valueOf(threshold));
                parameters.put("current_value", String.valueOf(currentValue));
                parameters.put("new_value", String.valueOf(newValue));
                parameters.put("direction", triggeredType);

                // Call execute.
                BehaviorRegistry.execute(Resolver.resolveTargetPlayer(masterId), callbackId, parameters);
            }
        }
    }

    private static void checkZeroTriggered(UUID masterId, CustomAttribute attribute, String attributeId,
                                           float currentValue, float newValue) {
        if (currentValue > 0.0f && newValue <= 0.0f && attribute.getZeroCallbackId() != null) {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("master_id", masterId.toString());
            parameters.put("master_name", Resolver.resolveTargetMasterName(masterId));
            parameters.put("attribute_id", attributeId);
            parameters.put("threshold", "0");
            parameters.put("current_value", String.valueOf(currentValue));
            parameters.put("new_value", String.valueOf(newValue));
            parameters.put("direction", "point");

            // Call execute.
            BehaviorRegistry.execute(Resolver.resolveTargetPlayer(masterId), attribute.getZeroCallbackId(), parameters);
        }
    }

    @SuppressWarnings("unchecked")
    private static Attribute parseAttributeData(Map<String, Object> attributeData) {
        // Read general.
        Attribute attribute = new Attribute();
        attribute.id = (String) attributeData.getOrDefault("id", null);
        attribute.maximum = ((Number) attributeData.getOrDefault("maximum", 100.0f)).floatValue();
        attribute.defaultValue = ((Number) attributeData.getOrDefault("default", 1.0f)).floatValue();

        // Read threshold node.
        List<Map<String, Object>> thresholds = (List<Map<String, Object>>) attributeData.get("thresholds");
        if (thresholds != null) {
            attribute.thresholdCallbacks = new ArrayList<>();
            Set<String> thisThresholds = new HashSet<>();
            for (Map<String, Object> thresholdCallback : thresholds) {
                ThresholdNode thresholdNode = new ThresholdNode();
                Number threshold = (Number) thresholdCallback.getOrDefault("threshold", null);

                if (threshold == null) {
                    continue;
                }

                thresholdNode.threshold = threshold.floatValue();
                thresholdNode.callbackId = (String) thresholdCallback.getOrDefault("id", null);
                thresholdNode.behavior = (String) thresholdCallback.getOrDefault("behavior", "null");
                thresholdNode.content = (String) thresholdCallback.getOrDefault("content", null);

                // Skip a completely same threshold.
                String thisKey = thresholdNode.threshold + " " + thresholdNode.callbackId + " -> " + thresholdNode.behavior + " + " + thresholdNode.content;
                if (thisThresholds.contains(thisKey)) {
                    CoreHanXu.LOGGER.warn("[HX] Ignored to parse a completely same threshold: {}", thisKey);
                    continue;
                }
                thisThresholds.add(thisKey);

                if (thresholdNode.callbackId != null) {
                    attribute.thresholdCallbacks.add(thresholdNode);
                }
            }
        }
        else {
            attribute.thresholdCallbacks = new ArrayList<>();
        }

        // Read zero node.
        Map<String, Object> zero = (Map<String, Object>) attributeData.get("zero");
        if (zero != null) {
            attribute.zeroCallbackId = (String) zero.getOrDefault("id", null);
            attribute.zeroCallbackBehavior = (String) zero.getOrDefault("behavior", "null");
            attribute.zeroCallbackContent = (String) zero.getOrDefault("content", "/say MISSING ARGUMENT.");
        }
        else {
            attribute.zeroCallbackId = null;
            attribute.zeroCallbackBehavior = "null";
            attribute.zeroCallbackContent = "/say MISSING ARGUMENT.";
        }

        // Read recovery node.
        Map<String, Object> recovery = (Map<String, Object>) attributeData.get("recovery");
        if (recovery != null) {
            attribute.recoveryCurveId = (String) recovery.getOrDefault("id", null);
            attribute.recoveryCurveBehavior = (String) recovery.getOrDefault("behavior", "simple");
            attribute.recoveryCurveValue = ((Number) recovery.getOrDefault("value", 0.0f)).floatValue();
            attribute.recoveryCurveDirection = (String) recovery.getOrDefault("direction", "point");
            attribute.recoveryIntervalTicks = (Integer) recovery.getOrDefault("recovery_interval", 1);
        }
        else {
            attribute.recoveryCurveId = null;
            attribute.recoveryCurveBehavior = "simple";
            attribute.recoveryCurveValue = 0.0f;
            attribute.recoveryCurveDirection = "point";
            attribute.recoveryIntervalTicks = 1;
        }

        return attribute;
    }

    private static CompoundTag saveAttributeData(Map.Entry<String, Float> entry) {
        CompoundTag attributeDataTag = new CompoundTag();

        attributeDataTag.putFloat(entry.getKey(), entry.getValue());

        return attributeDataTag;
    }

    // Rebuild data.
    private static void rebuildAttributeData(UUID masterId, CompoundTag allAttributesTag) {
        Map<String, Float> ownerValues = new ConcurrentHashMap<>();

        for (String eachAttributeId : allAttributesTag.keySet()) {
            CompoundTag attributeTag = allAttributesTag.getCompound(eachAttributeId).orElse(new CompoundTag());
            ownerValues.put(eachAttributeId, attributeTag.getFloat(eachAttributeId).orElse(0.0f));
        }
        attributeValues.put(masterId, ownerValues);
    }

    // Register all YAML attributes.
    private static void registerYamlAttribute(Attribute attribute, boolean isOverride) {
        if (isOverride) {
            // Only command/YAML will override their attributes.
            commandAttributes.remove(attribute.id);
        }
        if (apiAttributes.containsKey(attribute.id) || commandAttributes.containsKey(attribute.id)) {
            CoreHanXu.LOGGER.warn("[HX] Rejected duplicate attribute: {}", attribute.id);
            return;
        }

        CustomAttribute customAttribute = new CustomAttribute(attribute.id, attribute.maximum, attribute.defaultValue).setRecoveryIntervalTicks(attribute.recoveryIntervalTicks);
        commandAttributes.put(attribute.id, customAttribute);

        for (ThresholdNode thresholdNode : attribute.thresholdCallbacks) {
            if (thresholdNode == null) {
                continue;
            }

            customAttribute.onThreshold(thresholdNode.threshold, thresholdNode.callbackId);
            Creator.registerCallback(thresholdNode.callbackId, thresholdNode.behavior, thresholdNode.content);
        }

        if (attribute.zeroCallbackId != null && !attribute.zeroCallbackId.isEmpty()) {
            Creator.registerCallback(attribute.zeroCallbackId, attribute.zeroCallbackBehavior, attribute.zeroCallbackContent);
            customAttribute.onZero(attribute.zeroCallbackId);
        }

        if (attribute.recoveryCurveId != null && !attribute.recoveryCurveId.isEmpty()) {
            String behavior = attribute.recoveryCurveBehavior;
            if (behavior.equals("simple")) {
                Creator.registerCallback(attribute.recoveryCurveId, "recovery", attribute.id + ":" + attribute.recoveryCurveValue + ":" + attribute.recoveryCurveDirection);
            }
            else if (behavior.equals("api")) {
                Creator.registerCallback(attribute.recoveryCurveId, "recovery", null);
            }
            else {
                CoreHanXu.LOGGER.warn("[HX] Unknown recovery behavior for attribute, 'simple' mode used: {}", behavior);
                Creator.registerCallback(attribute.recoveryCurveId, "recovery", attribute.id + ":" + attribute.recoveryCurveValue + ":" + attribute.recoveryCurveDirection);
            }
            customAttribute.setRecovery(attribute.recoveryCurveId);
        }
    }

    private static void saveYamlAttributeSkeleton(String attributeId, float maximum, float defaultValue, YamlReader.TargetPath targetPath) {
        Map<String, Object> skeleton = new LinkedHashMap<>();
        skeleton.put("id", attributeId);
        skeleton.put("maximum", maximum);
        skeleton.put("default", defaultValue);

        try {
            YamlReader.save("attribute", attributeId, skeleton, targetPath);
        }
        catch (IOException e) {
            CoreHanXu.LOGGER.warn("[HX] Failed to save YAML attribute skeleton: {}", attributeId, e);
        }
    }

    // Register and tick attribute changes.
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickSync();
        tickRecovery();
    }

    // Define values.
    public static class CustomAttribute {
        private final String attributeId;
        private final float maximum;
        private final float defaultValue;

        private final Map<Float, String> thresholdCallbacks = new ConcurrentHashMap<>();
        private String zeroCallbackId;
        private String recoveryCurveId;

        private int recoveryIntervalTicks = 1;
        private int recoveryTickCounter = 0;

        public CustomAttribute(String attributeId, float maximum, float defaultValue) {
            this.attributeId = attributeId;
            this.maximum = maximum;
            this.defaultValue = defaultValue;
        }

        public CustomAttribute onThreshold(float thresholdValue, String callbackId) {
            thresholdCallbacks.put(thresholdValue, callbackId);
            return this;
        }

        public CustomAttribute onZero(String callbackId) {
            this.zeroCallbackId = callbackId;
            return this;
        }

        public CustomAttribute setRecovery(String callbackId) {
            this.recoveryCurveId = callbackId;
            return this;
        }

        public CustomAttribute setRecoveryIntervalTicks(int intervalTicks) {
            this.recoveryIntervalTicks = intervalTicks;
            return this;
        }

        public void countTickRecovery() {
            recoveryTickCounter++;
        }

        public void resetTickRecovery() {
            recoveryTickCounter = 0;
        }

        public boolean doesRecoveryRegistered() {
            return recoveryCurveId != null && !recoveryCurveId.isEmpty();
        }

        public String getAttributeId() {
            return attributeId;
        }

        public float getMaximum() {
            return maximum;
        }

        public float getDefaultValue() {
            return defaultValue;
        }

        public Map<Float, String> getThresholdCallbacks() {
            return thresholdCallbacks;
        }

        public String getZeroCallbackId() {
            return zeroCallbackId;
        }

        public String getRecoveryCurveId() {
            return recoveryCurveId;
        }

        public int getRecoveryIntervalTicks() {
            return recoveryIntervalTicks;
        }
    }

    // Define YAML nodes.
    public static class Attribute {
        public String id;
        public Float maximum;
        public Float defaultValue;
        public List<ThresholdNode> thresholdCallbacks;
        public String zeroCallbackId;
        public String recoveryCurveId;
        public String zeroCallbackBehavior;
        public String zeroCallbackContent;
        public String recoveryCurveBehavior;
        public Float recoveryCurveValue;
        public String recoveryCurveDirection;
        public Integer recoveryIntervalTicks;
    }

    public static class ThresholdNode {
        public Float threshold;
        public String callbackId;
        public String behavior;
        public String content;
    }
}
