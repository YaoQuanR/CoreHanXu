package core.yaoquan.hanxu.api;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.custom.TimerCallback;
import core.yaoquan.hanxu.api.define.FilePath;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.registry.event.ModPayload;
import core.yaoquan.hanxu.util.Converter;
import core.yaoquan.hanxu.util.Creator;
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Timer system API
 * @since 0.2.0 (Internal Development)
 */
@EventBusSubscriber(modid = CoreHanXu.MOD_ID)
public class TimeHolder {
    // Unit transform.
    public static final int TICKS_PER_SECOND = 20;
    public static final int TICKS_PER_MINUTE = 20 * 60;
    public static final int TICKS_PER_HOUR = 20 * 3600;

    // Storage template timer (in safety method).
    private static final Map<String, TimerData> templateTimer = new ConcurrentHashMap<>();
    // Storage instantiated timer (in safety method).
    private static final Map<UUID, Map<String, TimerData>> instantiatedTimer = new ConcurrentHashMap<>();
    // Storage recovery end behavior.
    private static final Map<String, TimerCallback> callbacks = new ConcurrentHashMap<>();
    // Storage debug display list.
    private static final Set<String> refreshDisplayList = ConcurrentHashMap.newKeySet();

    // Register your new timer to template (Available to override old timer):
    /**
     * Create a new template timer.
     * You are required to register to instance for operation by {@link #createInstanceFromTemplate(UUID, String)}.
     * @param timerId           Unique title of timer.
     * @param durationTime      Time durations.
     * @param timeUnit          Flexible use by: tick/second/minute/hour.
     * @param callback          Execute callback behavior when time run out.
     * @param endBehavior       If you are using command callback generator,
     *                          remind/execute/null is required to fill in for recreate callback.
     * @param behaviorContent   Also required when using command callback,
     *                          remind: display information context; execute: command execution; null: nothing.
     * @param masterGroup       Required when rebuild callback behavior,
     *                          depends on mods definition of {@link TimerCallback}.
     */
    public static void createTemplateTimer(String timerId, int durationTime, String timeUnit, Consumer<ServerPlayer> callback, String endBehavior, String behaviorContent, String masterGroup) {
        // Convert.
        int durationTicks = Converter.convertToTicks(durationTime, timeUnit);

        // Check if callback = null (For API define).
        if (callback == null && masterGroup != null) {
            TimerCallback timerCallback = getCallback(masterGroup);
            if (timerCallback != null) {
                callback = timerCallback.createCustomCallback(timerId, endBehavior, behaviorContent);
            }
        }

        // Then create.
        templateTimer.put(timerId, new TimerData(timerId, durationTicks, callback, endBehavior, behaviorContent, masterGroup));
    }

    // Register your new timer to instance (For immediately use).
    /**
     * Create new instance timer for immediately use.
     * You are required to define an owner of timer (or called "master") when create an instance timer.
     * @param masterId          Required when becoming an instance timer,
     *                          use player id/"-global"/"-temporary" to define the master.
     *                          You can by checking {@link core.yaoquan.hanxu.util.Resolver} for details.
     * @param timerId           Unique title of timer.
     * @param durationTime      Time durations.
     * @param timeUnit          Flexible use by: tick/second/minute/hour.
     * @param callback          Execute callback behavior when time run out.
     * @param endBehavior       If you are using command callback generator,
     *                          remind/execute/null is required to fill in for recreate callback.
     * @param behaviorContent   Also required when using command callback,
     *                          remind: display information context; execute: command execution; null: nothing.
     * @param masterGroup       Required when rebuild callback behavior,
     *                          depends on mods definition of {@link TimerCallback}.
     * @return                  Does the creation success: boolean.
     */
    public static boolean createInstanceTimer(UUID masterId, String timerId, int durationTime, String timeUnit, Consumer<ServerPlayer> callback, String endBehavior, String behaviorContent, String masterGroup) {
        // Check if timer already existed.
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData != null && instantiatedData.containsKey(timerId)) {
            return false;
        }

        // Check if callback = null (For API define).
        if (callback == null) {
            CoreHanXu.LOGGER.info("[HX] Rebuild callback from API TimerCallback.");
            TimerCallback timerCallback = getCallback(masterGroup);
            if (timerCallback != null) {
                callback = timerCallback.createCustomCallback(timerId, endBehavior, behaviorContent);
            }
        }

        // Convert.
        int durationTicks = Converter.convertToTicks(durationTime, timeUnit);

        // Create timer.
        TimerData instanceTimer = new TimerData(timerId, durationTicks, callback, endBehavior, behaviorContent, masterGroup);

        // Then put into instance.
        instantiatedTimer.computeIfAbsent(masterId, key -> new ConcurrentHashMap<>()).put(timerId, instanceTimer);

        return true;
    }

    // Register your timer to instance set.
    /**
     * Use this function to instance the template timer.
     * @param masterId          Required when becoming an instance timer,
     *                          use player id/"-global"/"-temporary" to define the master.
     *                          You can by checking {@link core.yaoquan.hanxu.util.Resolver} for details.
     * @param timerId           Unique title of timer.
     * @return                  Does the creation success: boolean.
     */
    public static boolean createInstanceFromTemplate(UUID masterId, String timerId) {
        TimerData templateTimerData = templateTimer.get(timerId);
        Map<String, TimerData> determineTimer = instantiatedTimer.computeIfAbsent(masterId, k -> new ConcurrentHashMap<>());
        
        // Check if available to copy.
        if (templateTimerData == null) {
            return false;
        }

        // Check if available to put.
        if (determineTimer.containsKey(timerId)) {
            return false;
        }
        
        // Else copy now.
        TimerData instanceTimerData = new TimerData(templateTimerData);
        instantiatedTimer.computeIfAbsent(masterId, k -> new ConcurrentHashMap<>()).put(timerId, instanceTimerData);
        
        return true;
    }

    // Method of using template timer.
    public static boolean deleteTemplateTimer(String timerId) {
        TimerData timerData = templateTimer.get(timerId);
        // Stop and remove timer.
        if (timerData != null) {
            timerData.stop();
            templateTimer.remove(timerId);
            return true;
        }

        return false;
    }

    // Method of using instance timer:
    public static boolean startInstanceTimer(UUID masterId, String timerId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);

        // Determine if map existed.
        if (instantiatedData == null) {
            return false;
        }
        // Else get inner data and also determine if existed.
        TimerData timerData = instantiatedData.get(timerId);
        if (timerData == null) {
            return false;
        }
        // Else determine if remaining time reach to 0.
        if (timerData.getRemainingTicks() <= 0) {
            return false;
        }
        // Else start depend on master id.
        if (masterId.equals(General.GLOBAL_UUID) || masterId.equals(General.TEMPORARY_UUID)) {
            timerData.start(null);
        }
        else {
            MinecraftServer minecraftServer = ServerLifecycleHooks.getCurrentServer();
            if (minecraftServer != null) {
                ServerPlayer player = minecraftServer.getPlayerList().getPlayer(masterId);
                if (player != null) {
                    timerData.start(player);
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public static boolean stopInstanceTimer(UUID masterId, String timerId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);

        // Determine if map existed.
        if (instantiatedData == null) {
            return false;
        }
        // Else get inner data and also determine if existed.
        TimerData timerData = instantiatedData.get(timerId);
        if (timerData == null) {
            return false;
        }
        // Else stop.
        timerData.stop();
        return true;
    }

    public static boolean resetInstanceTimer(UUID masterId, String timerId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);

        // Determine if map existed.
        if (instantiatedData == null) {
            return false;
        }
        // Else get inner data and also determine if existed.
        TimerData timerData = instantiatedData.get(timerId);
        if (timerData == null) {
            return false;
        }
        // Else stop.
        timerData.reset();
        return true;
    }

    public static boolean restartInstanceTimer(UUID masterId, String timerId) {
        boolean isReset = resetInstanceTimer(masterId, timerId);
        boolean isStart = startInstanceTimer(masterId, timerId);
        return isReset && isStart;
    }

    public static boolean deleteInstanceTimer(UUID masterId, String timerId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);

        if (instantiatedData == null) {
            return false;
        }

        return instantiatedData.remove(timerId) != null;
    }

    /**
     * Modify instance timer.
     * The modified timer will NOT auto stop.
     * @param masterId          Required when becoming an instance timer,
     *                          use player id/"-global"/"-temporary" to define the master.
     *                          You can by checking {@link core.yaoquan.hanxu.util.Resolver} for details.
     * @param timerId           Unique title of timer.
     * @param newTime           The new time.
     * @param timeUnit          Flexible use by: tick/second/minute/hour.
     * @param category          Use it for identify what operation required to do:
     *                          "initial_time" or "remaining_time".
     */
    public static boolean modifyInstanceTimer(UUID masterId, String timerId, int newTime, String timeUnit, String category) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return false;
        }
        TimerData timerData = instantiatedData.get(timerId);
        if (timerData == null) {
            return false;
        }

        int newTicks = Converter.convertToTicks(newTime, timeUnit);

        // Modify
        timerData.modify(newTicks, category);

        return true;
    }

    // Method of getting timer's information:
    public static String getTemplateId(String timerId) {
        TimerData timerData = templateTimer.get(timerId);
        return timerData != null? timerData.timerId : "Null";
    }

    public static String getTemplateEndBehavior(String timerId) {
        TimerData timerData = templateTimer.get(timerId);
        return timerData.getEndBehavior();
    }

    public static String getTemplateBehaviorContent(String timerId) {
        TimerData timerData = templateTimer.get(timerId);
        return timerData.getBehaviorContent();
    }

    public static String getInstanceId(UUID masterId, String timerId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return "Null";
        }
        TimerData timerData = instantiatedData.get(timerId);
        return timerData != null? timerData.timerId : "Null";
    }

    public static String getInstanceEndBehavior(UUID masterId, String timerId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return "Not Found";
        }
        TimerData timerData = instantiatedData.get(timerId);
        return timerData.getEndBehavior();
    }

    public static String getInstanceBehaviorContent(UUID masterId, String timerId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return "Not Found";
        }
        TimerData timerData = instantiatedData.get(timerId);
        return timerData.getBehaviorContent();
    }

    public static int getRemainingTimeFromTemplate(String timerId, String timeUnit) {
        TimerData timerData = templateTimer.get(timerId);
        return getRemainingTicks(timeUnit, timerData);
    }

    public static int getInitialTimeFromTemplate(String timerId, String timeUnit) {
        TimerData timerData = templateTimer.get(timerId);
        return getInitialTicks(timeUnit, timerData);
    }

    public static int getRemainingTimeFromInstance(UUID masterId, String timerId, String timeUnit) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return -1;
        }
        TimerData timerData = instantiatedData.get(timerId);
        return getRemainingTicks(timeUnit, timerData);
    }

    public static int getInitialTimeFromInstance(UUID masterId, String timerId, String timeUnit) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return -1;
        }
        TimerData timerData = instantiatedData.get(timerId);
        return getInitialTicks(timeUnit, timerData);
    }

    public static boolean isTemplateTimerCounting(String timerId) {
        TimerData timerData = templateTimer.get(timerId);
        return timerData != null && timerData.isItCounting();
    }

    public static boolean isInstanceTimerCounting(UUID masterId, String timerId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return false;
        }
        TimerData timerData = instantiatedData.get(timerId);
        return timerData != null && timerData.isItCounting();
    }

    // Collect all registered timer by id and return:
    public static String[] getAllTemplateIds() {
        return templateTimer.keySet().toArray(new String[0]);
    }

    public static String[] getAllInstanceIds(UUID masterId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return new String[0];
        }
        return instantiatedData.keySet().toArray(new String[0]);
    }

    // Display out to F4 page (info page).
    public static void displayToInfoPage(ServerPlayer player, UUID masterId, String timerId, boolean state) {
        if (masterId == null) {
            return;
        }

        int remainingTicks = getRemainingTimeFromInstance(masterId, timerId, "tick");
        boolean isCounting = isInstanceTimerCounting(masterId, timerId);
        String masterName = Resolver.resolveTargetMasterName(masterId);
        String key = player.getUUID() + ":" + masterId + ":" + timerId;

        if (state) {
            refreshDisplayList.add(key);
        }
        else {
            refreshDisplayList.remove(key);
        }

        CoreHanXu.LOGGER.info("[HX] Display debug timer info. State: {}", state);

        PacketDistributor.sendToPlayer(
                player, new ModPayload.TimerF4Packet(masterId, timerId, state, remainingTicks, isCounting, masterName)
        );
    }

    // Check if required to refresh the F4 timer display.
    public static void checkAndRefreshDisplay(ServerPlayer player, UUID masterId, String timerId) {
        String key = player.getUUID() + ":" + masterId.toString() + ":" + timerId;

        CoreHanXu.LOGGER.info("[HX] About to refresh display: {}", key);

        if (refreshDisplayList.contains(key)) {
            CoreHanXu.LOGGER.info("[HX] Refresh check: true");
            displayToInfoPage(player, masterId, timerId, true);
        }
        else {
            CoreHanXu.LOGGER.info("[HX] Refresh check: false");
        }
    }

    // Save and load methods:
    public static void registerCallback(TimerCallback callback) {
        callbacks.put(callback.getMasterGroupId(), callback);
    }

    public static TimerCallback getCallback(String modId) {
        return callbacks.get(modId);
    }

    public static void saveInstanceTimerForPlayer(ServerPlayer player) {
        String headKey = "core.yaoquan.hanxu.player_instance_timers";
        CompoundTag dataRoot = player.getPersistentData();
        CompoundTag allTimersTag = new CompoundTag();

        // Get all specific player's timer, then save.
        Map<String, TimerData> specificPlayerInstanceTimers = instantiatedTimer.get(player.getUUID());
        if (specificPlayerInstanceTimers != null) {
            // For each set, save arguments.
            for (var entry : specificPlayerInstanceTimers.entrySet()) {
                // Put timer's data into NBT tag.
                CompoundTag timerDataTag = saveTimerData(entry);

                // Then save.
                allTimersTag.put(entry.getKey(), timerDataTag);
            }
        }

        dataRoot.put(headKey, allTimersTag);
    }

    public static void saveInstanceTimerForGlobal(ServerLevel level) {
        String headKey = "core.yaoquan.hanxu.global_instance_timers";
        CompoundTag dataRoot = new CompoundTag();
        CompoundTag allTimersTag = new CompoundTag();

        Map<String, TimerData> globalInstanceTimers = instantiatedTimer.get(General.GLOBAL_UUID);
        if (globalInstanceTimers != null) {
            for (var entry : globalInstanceTimers.entrySet()) {
                // Put timer's data into NBT tag.
                CompoundTag timerDataTag = saveTimerData(entry);

                // Then save.
                allTimersTag.put(entry.getKey(), timerDataTag);
            }
        }

        dataRoot.put(headKey, allTimersTag);

        Path file = FilePath.getModDataPath(level);
        try {
            NbtIo.writeCompressed(dataRoot, file.toFile().toPath());
        }
        catch (IOException e) {
            CoreHanXu.LOGGER.error("[HX] Failed to save global timers", e);
        }
    }

    public static void loadInstanceTimerForPlayer(ServerPlayer player) {
        String headKey = "core.yaoquan.hanxu.player_instance_timers";
        CompoundTag dataRoot = player.getPersistentData();
        CompoundTag allTimersTag = dataRoot.getCompound(headKey).orElse(new CompoundTag());

        rebuildTimerData(allTimersTag, player.getUUID());
    }

    public static void loadInstanceTimerForGlobal(ServerLevel level) {
        String headKey = "core.yaoquan.hanxu.global_instance_timers";
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
            CoreHanXu.LOGGER.error("[HX] Failed to load global timers", e);
            return;
        }

        CompoundTag allTimersTag = dataRoot.getCompound(headKey).orElse(new CompoundTag());

        rebuildTimerData(allTimersTag, General.GLOBAL_UUID);
    }

    private static int getRemainingTicks(String timeUnit, TimerData timerData) {
        return switch (timeUnit) {
            case "t", "tick" -> timerData != null? timerData.getRemainingTicks() : -1;
            case "s", "second" -> timerData != null? timerData.getRemainingTicks() / TICKS_PER_SECOND : -1;
            case "m", "minute" -> timerData != null? timerData.getRemainingTicks() / TICKS_PER_MINUTE : -1;
            case "h", "hour" -> timerData != null? timerData.getRemainingTicks() / TICKS_PER_HOUR : -1;
            default -> -1;
        };
    }

    private static int getInitialTicks(String timeUnit, TimerData timerData) {
        return switch (timeUnit) {
            case "t", "tick" -> timerData != null? timerData.getInitialTicks() : -1;
            case "s", "second" -> timerData != null? timerData.getInitialTicks() / TICKS_PER_SECOND : -1;
            case "m", "minute" -> timerData != null? timerData.getInitialTicks() / TICKS_PER_MINUTE : -1;
            case "h", "hour" -> timerData != null? timerData.getInitialTicks() / TICKS_PER_HOUR : -1;
            default -> -1;
        };
    }

    private static CompoundTag saveTimerData(Map.Entry<String, TimerData> entry) {
        CompoundTag timerDataTag = new CompoundTag();
        TimerData timerData = entry.getValue();

        timerDataTag.putString("timer_id", timerData.getTimerId());
        timerDataTag.putInt("remaining_ticks", timerData.getRemainingTicks());
        timerDataTag.putInt("initial_ticks", timerData.getInitialTicks());
        timerDataTag.putBoolean("is_counting", timerData.isItCounting());
        timerDataTag.putString("end_behavior", timerData.getEndBehavior());
        if (timerData.getBehaviorContent() != null) {
            timerDataTag.putString("behavior_content", timerData.getBehaviorContent());
        }
        if (timerData.getMasterGroup() != null) {
            timerDataTag.putString("master_group", timerData.getMasterGroup());
        }
        else {
            timerDataTag.putString("master_group", "core_hanxu-command");
        }

        return timerDataTag;
    }

    private static void rebuildTimerData(CompoundTag allTimersTag, UUID masterId) {
        for (String eachTimerId : allTimersTag.keySet()) {
            CompoundTag timerTag = allTimersTag.getCompound(eachTimerId).orElse(new CompoundTag());

            String timerId = timerTag.getString("timer_id").orElse(eachTimerId);

            int remainingTicks = timerTag.getInt("remaining_ticks").orElse(0);
            int initialTicks = timerTag.getInt("initial_ticks").orElse(0);

            boolean isCounting = timerTag.getBoolean("is_counting").orElse(false);

            String endBehavior = timerTag.getString("end_behavior").orElse("null");
            String behaviorContent = timerTag.getString("behavior_content").orElse(null);
            String masterGroup = timerTag.getString("master_group").orElse("core_hanxu-command");

            // Rebuild timer data.
            Consumer<ServerPlayer> callback = rebuildCallback(masterGroup, timerId, endBehavior, behaviorContent);
            // Skip when no callback.
            if (callback == null) {
                continue;
            }

            // Rebuild timer data.
            TimerData rebuildTimer = new TimerData(timerId, initialTicks, callback, endBehavior, behaviorContent, masterGroup, isCounting);
            rebuildTimer.remainingTicks = remainingTicks;

            // Then recover.
            Map<String, TimerData> instanceTimers = instantiatedTimer.computeIfAbsent(masterId, k -> new ConcurrentHashMap<>());
            instanceTimers.put(timerId, rebuildTimer);
        }
    }

    private static Consumer<ServerPlayer> rebuildCallback(String masterGroup, String timerId, String endBehavior, String behaviorContent) {
        if (masterGroup.equals("core_hanxu-command")) {
            return Creator.createCallback(null, timerId, endBehavior, behaviorContent);
        }
        else {
            TimerCallback callback = getCallback(masterGroup);
            if (callback != null) {
                return callback.createCustomCallback(timerId, endBehavior, behaviorContent);
            }
            CoreHanXu.LOGGER.warn("[HX] Timer's callback was failed to get!");
            return null;
        }
    }

    // Register timers into game.
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // Double foreach for event tick recall.
        for (Map<String, TimerData> map : instantiatedTimer.values()) {
            for (TimerData timerData : map.values()) {
                timerData.tickRecall();
            }
        }
    }

    // Define an actual timer data system.
    private static class TimerData {
        private final String timerId;
        private final Consumer<ServerPlayer> callback;
        private final String endBehavior;
        private final String behaviorContent;
        private final String masterGroup;
        private ServerPlayer player;
        private int initialTicks;
        private int remainingTicks;
        private boolean isCounting;

        TimerData(String timerId, int durationTicks, Consumer<ServerPlayer> callback, String endBehavior, String behaviorContent, String masterGroup) {
            this.timerId = timerId;
            this.initialTicks = durationTicks;
            this.callback = callback;
            this.endBehavior = endBehavior;
            this.behaviorContent = behaviorContent;
            this.remainingTicks = durationTicks;
            this.masterGroup = masterGroup;
            this.isCounting = false;
            this.player = null;
        }

        // Timer with controllable starting state.
        TimerData(String timerId, int durationTicks, Consumer<ServerPlayer> callback, String endBehavior, String behaviorContent, String masterGroup, boolean isCounting) {
            this.timerId = timerId;
            this.initialTicks = durationTicks;
            this.callback = callback;
            this.endBehavior = endBehavior;
            this.behaviorContent = behaviorContent;
            this.remainingTicks = durationTicks;
            this.masterGroup = masterGroup;
            this.isCounting = isCounting;
            this.player = null;
        }

        // Timer apply from template to instance (Copy).
        TimerData(TimerData timerData) {
            this.timerId = timerData.timerId;
            this.initialTicks = timerData.initialTicks;
            this.callback = timerData.callback;
            this.endBehavior = timerData.endBehavior;
            this.behaviorContent = timerData.behaviorContent;
            this.remainingTicks = timerData.remainingTicks;
            this.masterGroup = timerData.masterGroup;
            this.isCounting = false;
            this.player = null;
        }

        void start(ServerPlayer player) {
            this.player = player;
            this.isCounting = true;
        }

        void stop() {
            this.player = null;
            this.isCounting = false;
        }

        void reset() {
            this.remainingTicks = this.initialTicks;
            this.isCounting = false;
        }

        void modify(int newTicks, String category) {
            switch (category) {
                case "initial_time":
                    this.initialTicks = newTicks;
                    break;
                case "remaining_time":
                    this.remainingTicks = newTicks;
                    break;
                default:
                    break;
            }
        }

        void tickRecall() {
            // Pass when not start counting.
            if (!this.isCounting) {
                return;
            }

            // Decrease 1 tick, then determine if run out of time.
            if (--remainingTicks <= 0) {
                this.isCounting = false;
                // Time to act something.
                callback.accept(player);
            }
        }

        String getTimerId() {
            return timerId;
        }

        int getRemainingTicks() {
            return remainingTicks;
        }

        int getInitialTicks() {
            return initialTicks;
        }

        String getEndBehavior() {
            return endBehavior;
        }

        String getBehaviorContent() {
            return behaviorContent;
        }

        String getMasterGroup() {
            return masterGroup;
        }

        boolean isItCounting() {
            return isCounting;
        }
    }
}
