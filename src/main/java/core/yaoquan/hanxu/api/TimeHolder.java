package core.yaoquan.hanxu.api;

import core.yaoquan.hanxu.CoreHanXu;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@EventBusSubscriber(modid = CoreHanXu.MOD_ID)
public class TimeHolder {
    // Unit transform.
    public static final int TICKS_PER_SECOND = 20;
    public static final int TICKS_PER_MINUTE = 20 * 60;
    public static final int TICKS_PER_HOUR = 20 * 3600;

    // UUID constant.
    public static final UUID GLOBAL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final UUID TEMPORARY_UUID = UUID.fromString("00000000-0000-0000-0000-00000000000f");

    // Storage template timer (in safety method).
    private static final Map<String, TimerData> templateTimer = new ConcurrentHashMap<>();
    // Storage instantiated timer (in safety method).
    private static final Map<UUID, Map<String, TimerData>> instantiatedTimer = new ConcurrentHashMap<>();
    // Storage debug display timer.
    private static final Set<String> infoDisplayTimer = ConcurrentHashMap.newKeySet();

    // Register your new timer to template (Available to override old timer):
    public static void createTemplateTimer(String timerId, int durationTime, String timeUnit, Consumer<ServerPlayer> callback, String endBehavior, String behaviorContent) {
        // Convert.
        int durationTicks = convertToTicks(durationTime, timeUnit);

        // Then create.
        templateTimer.put(timerId, new TimerData(timerId, durationTicks, callback, endBehavior, behaviorContent));
    }

    // Register your new timer to instance (For immediately use).
    public static boolean createInstanceTimer(UUID masterId, String timerId, int durationTime, String timeUnit, Consumer<ServerPlayer> callback, String endBehavior, String behaviorContent) {
        // Check if timer already existed.
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData != null && instantiatedData.containsKey(timerId)) {
            return false;
        }

        // Convert.
        int durationTicks = convertToTicks(durationTime, timeUnit);

        // Create timer.
        TimerData instanceTimer = new TimerData(timerId, durationTicks, callback, endBehavior, behaviorContent);

        // Then put into instance.
        instantiatedTimer.computeIfAbsent(masterId, key -> new ConcurrentHashMap<>()).put(timerId, instanceTimer);

        return true;
    }

    // Tool method.
    public static int convertToTicks(int durationTime, String timeUnit) {
        return switch (timeUnit) {
            case "second" -> durationTime * TICKS_PER_SECOND;
            case "minute" -> durationTime * TICKS_PER_MINUTE;
            case "hour" -> durationTime * TICKS_PER_HOUR;
            default -> durationTime;
        };
    }
    
    // Register your timer to instance set.
    public static boolean registerToInstance(UUID masterId, String timerId) {
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
        // Else start depend on master id.
        if (masterId.equals(GLOBAL_UUID) || masterId.equals(TEMPORARY_UUID)) {
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

    public static boolean deleteInstanceTimer(UUID masterId, String timerId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);

        if (instantiatedData == null) {
            return false;
        }

        return instantiatedData.remove(timerId) != null;
    }

    public static boolean modifyInstanceTimer(UUID masterId, String timerId, int newTime, String timeUnit, String category) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return false;
        }
        TimerData timerData = instantiatedData.get(timerId);
        if (timerData == null) {
            return false;
        }

        int newTicks = convertToTicks(newTime, timeUnit);

        // Modify
        timerData.modify(newTicks, category);

        return true;
    }

    // Method of getting timer's information:
    public static String returnTemplateId(String timerId) {
        TimerData timerData = templateTimer.get(timerId);
        return timerData != null? timerData.timerId : "Null";
    }

    public static String returnTemplateEndBehavior(String timerId) {
        TimerData timerData = templateTimer.get(timerId);
        return timerData.returnEndBehavior();
    }

    public static String returnTemplateBehaviorContent(String timerId) {
        TimerData timerData = templateTimer.get(timerId);
        return timerData.returnBehaviorContent();
    }

    public static String returnInstanceEndBehavior(UUID masterId, String timerId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return "Not Found";
        }
        TimerData timerData = instantiatedData.get(timerId);
        return timerData.returnEndBehavior();
    }

    public static String returnInstanceBehaviorContent(UUID masterId, String timerId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return "Not Found";
        }
        TimerData timerData = instantiatedData.get(timerId);
        return timerData.returnBehaviorContent();
    }

    public static String returnInstanceId(UUID masterId, String timerId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return "Null";
        }
        TimerData timerData = instantiatedData.get(timerId);
        return timerData != null? timerData.timerId : "Null";
    }

    public static int returnRemainingTimeFromTemplate(String timerId, String timeUnit) {
        TimerData timerData = templateTimer.get(timerId);
        return forReturnRemainingTime(timeUnit, timerData);
    }

    public static int returnInitialTimeFromTemplate(String timerId, String timeUnit) {
        TimerData timerData = templateTimer.get(timerId);
        return forReturnInitialTime(timeUnit, timerData);
    }

    public static int returnRemainingTimeFromInstance(UUID masterId, String timerId, String timeUnit) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return -1;
        }
        TimerData timerData = instantiatedData.get(timerId);
        return forReturnRemainingTime(timeUnit, timerData);
    }

    public static int returnInitialTimeFromInstance(UUID masterId, String timerId, String timeUnit) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return -1;
        }
        TimerData timerData = instantiatedData.get(timerId);
        return forReturnInitialTime(timeUnit, timerData);
    }

    private static int forReturnRemainingTime(String timeUnit, TimerData timerData) {
        return switch (timeUnit) {
            case "t", "tick" -> timerData != null? timerData.returnRemainingTicks() : -1;
            case "s", "second" -> timerData != null? timerData.returnRemainingTicks() / TICKS_PER_SECOND : -1;
            case "m", "minute" -> timerData != null? timerData.returnRemainingTicks() / TICKS_PER_MINUTE : -1;
            case "h", "hour" -> timerData != null? timerData.returnRemainingTicks() / TICKS_PER_HOUR : -1;
            default -> -1;
        };
    }

    private static int forReturnInitialTime(String timeUnit, TimerData timerData) {
        return switch (timeUnit) {
            case "t", "tick" -> timerData != null? timerData.returnInitialTicks() : -1;
            case "s", "second" -> timerData != null? timerData.returnInitialTicks() / TICKS_PER_SECOND : -1;
            case "m", "minute" -> timerData != null? timerData.returnInitialTicks() / TICKS_PER_MINUTE : -1;
            case "h", "hour" -> timerData != null? timerData.returnInitialTicks() / TICKS_PER_HOUR : -1;
            default -> -1;
        };
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

    // Collect all registered timer by id and return.
    public static String[] returnAllTemplateIds() {
        return templateTimer.keySet().toArray(new String[0]);
    }

    public static String[] returnAllInstanceIds(UUID masterId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return new String[0];
        }
        return instantiatedData.keySet().toArray(new String[0]);
    }

    // Define an actual timer data system.
    private static class TimerData {
        private final String timerId;
        private final Consumer<ServerPlayer> callback;
        private final String endBehavior;
        private final String behaviorContent;
        private ServerPlayer player;
        private int initialTicks;
        private int remainingTicks;
        private boolean isCounting;

        TimerData(String timerId, int durationTicks, Consumer<ServerPlayer> callback, String endBehavior, String behaviorContent) {
            this.timerId = timerId;
            this.initialTicks = durationTicks;
            this.callback = callback;
            this.endBehavior = endBehavior;
            this.behaviorContent = behaviorContent;
            this.remainingTicks = durationTicks;
            this.isCounting = false;
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
            this.isCounting = false;
            this.player = null;
        }

        void start(ServerPlayer player) {
            this.player = player;
            this.remainingTicks = this.initialTicks;
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

        String returnTemplateId() {
            return timerId;
        }

        int returnRemainingTicks() {
            return remainingTicks;
        }

        int returnInitialTicks() {
            return initialTicks;
        }

        String returnEndBehavior() {
            return endBehavior;
        }

        String returnBehaviorContent() {
            return behaviorContent;
        }

        boolean isItCounting() {
            return isCounting;
        }
    }

    // Display out to F4 page (info page).
    public static void displayToInfoPage(UUID masterId, String timerId, boolean state) {
        String masterIdString = masterId.toString();
        String key = masterIdString + ":" + timerId;

        if (state) {
            infoDisplayTimer.add(key);
        }
        else {
            infoDisplayTimer.remove(key);
        }
    }

    public static boolean isInfoDisplay(UUID masterId, String timerId) {
        String masterIdString = masterId.toString();
        String key = masterIdString + ":" + timerId;

        return infoDisplayTimer.contains(key);
    }

    public static Set<String> returnAllInfoKeys() {
        return infoDisplayTimer;
    }

    // Save data.
    public static void saveInstanceTimerForPlayer(ServerPlayer player) {

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
}
