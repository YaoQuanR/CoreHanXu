package core.yaoquan.hanxu.api;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.custom.BehaviorRegistry;
import core.yaoquan.hanxu.api.define.FilePath;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.registry.event.ModPayload;
import core.yaoquan.hanxu.util.Resolver;
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
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Attribute system API
 * @since 0.5.0
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
    public static boolean register(String attributeId, float maximum, float defaultValue) {
        if (apiAttributes.containsKey(attributeId) || commandAttributes.containsKey(attributeId)) {
            CoreHanXu.LOGGER.info("[HX] Rejected duplicate attribute: {}", attributeId);
            return false;
        }

        CustomAttribute attribute = new CustomAttribute(attributeId, maximum, defaultValue);
        commandAttributes.put(attributeId, attribute);

        CoreHanXu.LOGGER.info("[HX] Registered attribute by command: {}", attribute.getAttributeId());
        return true;
    }

    /**
     * For API and command unregister.
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

    public static CustomAttribute getAttributeDefinition(String attributeId, boolean fromApi) {
        if (fromApi) {
            return apiAttributes.get(attributeId);
        }
        else {
            return commandAttributes.get(attributeId);
        }
    }

    public static Map<String, CustomAttribute> getApiAttributes() {
        return apiAttributes;
    }

    public static Map<String, CustomAttribute> getCommandAttributes() {
        return commandAttributes;
    }

    /**
     * Get attribute value.
     * @param masterId          Required when becoming an instance timer,
     *                          use player id/"-global"/"-temporary" to define the master.
     *                          You can by checking {@link core.yaoquan.hanxu.util.Resolver} for details.
     * @param attributeId       Unique title of attribute.
     * @param fromApi           True false that where you use this function.
     * @return                  Received player value: float.
     *                          If this player not set the value yet, return default value from definition: float.
     */
    public static float getValue(UUID masterId, String attributeId, boolean fromApi) {
        CustomAttribute attribute = getAttributeDefinition(attributeId, fromApi);
        if (attribute == null) {
            CoreHanXu.LOGGER.warn("[HX] Unknown custom attribute for get value: {}", attributeId);
            return 0.0f;
        }

        // Receive value, create new concurrent hash map if null.
        Map<String, Float> playerValues = attributeValues
            .computeIfAbsent(masterId, k -> new ConcurrentHashMap<>());

        // Return received value, or return default value from definition if null.
        return playerValues.computeIfAbsent(attribute.getAttributeId(), k -> attribute.getDefaultValue());
    }

    /**
     * Get global's attribute value.
     * @param attributeId       Unique title of attribute.
     * @param fromApi           True false that where you use this function.
     * @return                  Received player value: float.
     *                          If this player not set the value yet, return default value from definition: float.
     */
    public static float getGlobalValue(String attributeId, boolean fromApi) {
        return getValue(General.GLOBAL_UUID, attributeId, fromApi);
    }

    /**
     * Set attribute value (Full direction trigger).
     * @param masterId          Required when becoming an instance timer,
     *                          use player id/"-global"/"-temporary" to define the master.
     *                          You can by checking {@link core.yaoquan.hanxu.util.Resolver} for details.
     * @param attributeId       Unique title of attribute.
     * @param value             Submit the new value for setter.
     * @param fromApi           True false that where you use this function.
     * @param direction         Define the trigger type for threshold callbacks.
     *                          You can by checking {@link ThresholdDirection} for details.
     * @return                  Does the setter success: boolean.
     */
    public static boolean setValue(UUID masterId, String attributeId, float value, boolean fromApi, ThresholdDirection direction) {
        CustomAttribute attribute = getAttributeDefinition(attributeId, fromApi);
        if (attribute == null) {
            CoreHanXu.LOGGER.warn("[HX] Unknown custom attribute for set value: {}", attributeId);
            return false;
        }

        float currentValue = getValue(masterId, attributeId, fromApi);
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
            checkAndRefreshDisplay(player, masterId, attributeId, fromApi);
        }

        return true;
    }

    /**
     * Set player's attribute value (Default: Point trigger).
     * @param masterId          Required when becoming an instance timer,
     *                          use player id/"-global"/"-temporary" to define the master.
     *                          You can by checking {@link core.yaoquan.hanxu.util.Resolver} for details.
     * @param attributeId       Unique title of attribute.
     * @param value             Submit the new value for setter.
     * @param fromApi           True false that where you use this function.
     * @return                  Does the setter success: boolean.
     */
    public static boolean setValue(UUID masterId, String attributeId, float value, boolean fromApi) {
        return setValue(masterId, attributeId, value, fromApi, ThresholdDirection.POINT);
    }

    /**
     * Set global's attribute value (Full direction trigger).
     * @param attributeId       Unique title of attribute.
     * @param value             Submit the new value for setter.
     * @param fromApi           True false that where you use this function.
     * @param direction         Define the trigger type for threshold callbacks.
     *                          You can by checking {@link ThresholdDirection} for details.
     * @return                  Does the setter success: boolean.
     */
    public static boolean setGlobalValue(String attributeId, float value, boolean fromApi, ThresholdDirection direction) {
        return setValue(General.GLOBAL_UUID, attributeId, value, fromApi, direction);
    }

    /**
     * Set global's attribute value (Default: Point trigger).
     * @param attributeId       Unique title of attribute.
     * @param value             Submit the new value for setter.
     * @param fromApi           True false that where you use this function.
     * @return                  Does the setter success: boolean.
     */
    public static boolean setGlobalValue(String attributeId, float value, boolean fromApi) {
        return setValue(General.GLOBAL_UUID, attributeId, value, fromApi, ThresholdDirection.POINT);
    }

    public static boolean addValue(UUID masterId, String attributeId, float value, boolean fromApi, ThresholdDirection direction) {
        float currentValue = getValue(masterId, attributeId, fromApi);
        return setValue(masterId, attributeId, currentValue + value, fromApi, direction);
    }

    public static boolean addValue(UUID masterId, String attributeId, float value, boolean fromApi) {
        float currentValue = getValue(masterId, attributeId, fromApi);
        return setValue(masterId, attributeId, currentValue + value, fromApi, ThresholdDirection.POINT);
    }

    public static boolean addGlobalValue(String attributeId, float value, boolean fromApi, ThresholdDirection direction) {
        float currentValue = getValue(General.GLOBAL_UUID, attributeId, fromApi);
        return setValue(General.GLOBAL_UUID, attributeId, currentValue + value, fromApi, direction);
    }

    public static boolean addGlobalValue(String attributeId, float value, boolean fromApi) {
        float currentValue = getValue(General.GLOBAL_UUID, attributeId, fromApi);
        return setValue(General.GLOBAL_UUID, attributeId, currentValue + value, fromApi, ThresholdDirection.POINT);
    }

    public static boolean reduceValue(UUID masterId, String attributeId, float value, boolean fromApi, ThresholdDirection direction) {
        float currentValue = getValue(masterId, attributeId, fromApi);
        return setValue(masterId, attributeId, currentValue - Math.abs(value), fromApi, direction);
    }

    public static boolean reduceValue(UUID masterId, String attributeId, float value, boolean fromApi) {
        float currentValue = getValue(masterId, attributeId, fromApi);
        return setValue(masterId, attributeId, currentValue - Math.abs(value), fromApi, ThresholdDirection.POINT);
    }

    public static boolean reduceGlobalValue(String attributeId, float value, boolean fromApi, ThresholdDirection direction) {
        float currentValue = getValue(General.GLOBAL_UUID, attributeId, fromApi);
        return setValue(General.GLOBAL_UUID, attributeId, currentValue - Math.abs(value), fromApi, direction);
    }

    public static boolean reduceGlobalValue(String attributeId, float value, boolean fromApi) {
        float currentValue = getValue(General.GLOBAL_UUID, attributeId, fromApi);
        return setValue(General.GLOBAL_UUID, attributeId, currentValue - Math.abs(value), fromApi, ThresholdDirection.POINT);
    }

    // Define values.
    public static class CustomAttribute {
        private final String attributeId;
        private final float maximum;
        private final float defaultValue;

        private static final Map<Float, String> thresholdCallbacks = new ConcurrentHashMap<>();
        private String zeroCallbackId;
        private String recoveryCurveId;

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
    }

    // Display out to F4 page (info page).
    public static void displayToInfoPage(ServerPlayer player, UUID masterId, String attributeId, boolean fromApi, boolean state) {
        if (masterId == null) {
            return;
        }

        String key = player.getUUID() + ":" + masterId + ":" + attributeId;

        if (state) {
            refreshDisplayList.add(key);
            float value = getValue(masterId, attributeId, fromApi);
            syncPacketToClient(player, masterId, attributeId, true, value, fromApi, Resolver.resolveTargetMasterName(masterId));
        }
        else {
            refreshDisplayList.remove(key);
            syncPacketToClient(player, masterId, attributeId, false, -1.0f, fromApi, Resolver.resolveTargetMasterName(masterId));
        }
    }

    // Submit packet into F4 display.
    public static void syncPacketToClient(ServerPlayer player,
                                          UUID masterId,
                                          String attributeId,
                                          boolean state,
                                          float value,
                                          boolean fromApi,
                                          String masterName) {
        PacketDistributor.sendToPlayer(
                player, new ModPayload.AttributeF4Packet(masterId, attributeId, state, value, fromApi, masterName)
        );
    }

    // Check if required to refresh the F4 attribute display.
    public static void checkAndRefreshDisplay(ServerPlayer player, UUID masterId, String attributeId, boolean fromApi) {
        String key = player.getUUID() + ":" + masterId + ":" + attributeId;

        if (refreshDisplayList.contains(key)) {
            displayToInfoPage(player, masterId, attributeId, fromApi, true);
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
            boolean fromApi = true;
            if (attribute == null) {
                attribute = commandAttributes.get(attributeId);
                fromApi = false;
            }
            if (attribute == null) {
                continue;
            }

            float value = getValue(masterId, attributeId, fromApi);

            syncPacketToClient(player, masterId, attributeId, true, value, fromApi, Resolver.resolveTargetMasterName(masterId));
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

                CustomAttribute attribute = apiAttributes.get(attributeId);
                if (attribute == null) {
                    attribute = commandAttributes.get(attributeId);
                }
                if (attribute == null || attribute.getRecoveryCurveId() == null) {
                    continue;
                }

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

    // Check if trigger threshold behavior.
    private static void checkThresholdTriggered(UUID masterId, CustomAttribute attribute, String attributeId,
                                                float currentValue, float newValue, ThresholdDirection direction) {
        for (Map.Entry<Float, String> thresholdCallback : attribute.getThresholdCallbacks().entrySet()) {
            float threshold = thresholdCallback.getKey();
            String callbackId = thresholdCallback.getValue();

            boolean triggered = false;
            String triggeredType = null;

            // Trigger: new (x) ~= threshold (x).
            if (Math.abs(newValue - threshold) < 0.01f) {
                triggered = true;
                triggeredType = "point";
            }
            // else determine if required.
            if (direction != ThresholdDirection.POINT && !triggered) {
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

        Map<String, Float> globalValues = attributeValues.get(General.GLOBAL_UUID);
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

    private static CompoundTag saveAttributeData(Map.Entry<String, Float> entry) {
        CompoundTag attributeDataTag = new CompoundTag();

        attributeDataTag.putFloat(entry.getKey(), entry.getValue());

        return attributeDataTag;
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
            NbtAccounter accounter = new NbtAccounter(32L * 1024 * 1024, 128);
            dataRoot = NbtIo.readCompressed(file, accounter);
        }
        catch (IOException e) {
            CoreHanXu.LOGGER.error("[HX] Failed to load global attributes", e);
            return;
        }

        CompoundTag allAttributesTag = dataRoot.getCompound(headKey).orElse(new CompoundTag());

        rebuildAttributeData(General.GLOBAL_UUID, allAttributesTag);
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

    // Register and tick attribute changes.
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickSync();
        tickRecovery();
    }
}
