package core.yaoquan.hanxu.api;

import core.yaoquan.hanxu.CoreHanXu;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Map;
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

    // Register your new timer (Available to override old timer):
    public static void createTemplateTimer(String templateId, int durationTicks, Consumer<ServerPlayer> callback) {
        templateTimer.put(templateId, new TimerData(templateId, durationTicks, callback));
    }

    public static void createTemplateTimerInSeconds(String templateId, int durationSeconds, Consumer<ServerPlayer> callback) {
        int durationTicks = durationSeconds * TICKS_PER_SECOND;
        templateTimer.put(templateId, new TimerData(templateId, durationTicks, callback));
    }

    public static void createTemplateTimerInMinutes(String templateId, int durationMinutes, Consumer<ServerPlayer> callback) {
        int durationTicks = durationMinutes * TICKS_PER_MINUTE;
        templateTimer.put(templateId, new TimerData(templateId, durationTicks, callback));
    }

    public static void createTemplateTimerInHours(String templateId, int durationHours, Consumer<ServerPlayer> callback) {
        int durationTicks = durationHours * TICKS_PER_HOUR;
        templateTimer.put(templateId, new TimerData(templateId, durationTicks, callback));
    }
    
    // Register your timer to instance set.
    public static boolean registerToInstance(UUID masterId, String templateId) {
        TimerData templateTimerData = templateTimer.get(templateId);
        Map<String, TimerData> determineTimer = instantiatedTimer.computeIfAbsent(masterId, k -> new ConcurrentHashMap<>());
        
        // Check if available to copy.
        if (templateTimerData == null) {
            return false;
        }

        // Check if available to put.
        if (determineTimer.containsKey(templateId)) {
            return false;
        }
        
        // Else copy now.
        TimerData instanceTimerData = new TimerData(templateTimerData);
        instantiatedTimer.computeIfAbsent(masterId, k -> new ConcurrentHashMap<>()).put(templateId, instanceTimerData);
        
        return true;
    }

    // Method of using template timer.
    public static boolean deleteTemplateTimer(String templateId) {
        TimerData timerData = templateTimer.get(templateId);
        // Stop and remove timer.
        if (timerData != null) {
            timerData.stop();
            templateTimer.remove(templateId);
            return true;
        }

        return false;
    }

    // Method of using instance timer:
    public static boolean startInstanceTimer(UUID masterId, String templateId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);

        // Determine if map existed.
        if (instantiatedData == null) {
            return false;
        }
        // Else get inner data and also determine if existed.
        TimerData timerData = instantiatedData.get(templateId);
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
                }
            }
            return false;
        }
        return true;
    }

    public static boolean stopInstanceTimer(UUID masterId, String templateId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);

        // Determine if map existed.
        if (instantiatedData == null) {
            return false;
        }
        // Else get inner data and also determine if existed.
        TimerData timerData = instantiatedData.get(templateId);
        if (timerData == null) {
            return false;
        }
        // Else stop.
        timerData.stop();
        return true;
    }

    public static boolean resetInstanceTimer(UUID masterId, String templateId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);

        // Determine if map existed.
        if (instantiatedData == null) {
            return false;
        }
        // Else get inner data and also determine if existed.
        TimerData timerData = instantiatedData.get(templateId);
        if (timerData == null) {
            return false;
        }
        // Else stop.
        timerData.reset();
        return true;
    }

    public static boolean deleteInstanceTimer(UUID masterId, String templateId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);

        if (instantiatedData == null) {
            return false;
        }

        return instantiatedData.remove(templateId) != null;
    }

    // Method of getting timer's information:
    public static String returnTemplateId(String templateId) {
        TimerData timerData = templateTimer.get(templateId);
        return timerData != null? timerData.templateId : "Null";
    }

    public static String returnInstanceId(UUID masterId, String templateId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return "Null";
        }
        TimerData timerData = instantiatedData.get(templateId);
        return timerData != null? timerData.templateId : "Null";
    }

    public static int returnRemainingTimeFromTemplate(String templateId, String timeUnit) {
        TimerData timerData = templateTimer.get(templateId);
        return forReturnRemainingTime(timeUnit, timerData);
    }

    public static int returnInitialTimeFromTemplate(String templateId, String timeUnit) {
        TimerData timerData = templateTimer.get(templateId);
        return forReturnInitialTime(timeUnit, timerData);
    }

    public static int returnRemainingTimeFromInstance(UUID masterId, String templateId, String timeUnit) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return -1;
        }
        TimerData timerData = instantiatedData.get(templateId);
        return forReturnRemainingTime(timeUnit, timerData);
    }

    public static int returnInitialTimeFromInstance(UUID masterId, String templateId, String timeUnit) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return -1;
        }
        TimerData timerData = instantiatedData.get(templateId);
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

    public static boolean isTemplateTimerCounting(String templateId) {
        TimerData timerData = templateTimer.get(templateId);
        return timerData != null && timerData.isItCounting();
    }

    public static boolean isInstanceTimerCounting(UUID masterId, String templateId) {
        Map<String, TimerData> instantiatedData = instantiatedTimer.get(masterId);
        if (instantiatedData == null) {
            return false;
        }
        TimerData timerData = instantiatedData.get(templateId);
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
        private final String templateId;
        private final int initialTicks;
        private final Consumer<ServerPlayer> callback;
        private ServerPlayer player;
        private int remainingTicks;
        private boolean isCounting;

        TimerData(String templateId, int durationTicks, Consumer<ServerPlayer> callback) {
            this.templateId = templateId;
            this.initialTicks = durationTicks;
            this.callback = callback;
            this.remainingTicks = durationTicks;
            this.isCounting = false;
            this.player = null;
        }

        // Timer apply from template to instance (Copy).
        TimerData(TimerData timerData) {
            this.templateId = timerData.templateId;
            this.initialTicks = timerData.initialTicks;
            this.callback = timerData.callback;
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
            return templateId;
        }

        int returnRemainingTicks() {
            return remainingTicks;
        }

        int returnInitialTicks() {
            return initialTicks;
        }

        boolean isItCounting() {
            return isCounting;
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
}
