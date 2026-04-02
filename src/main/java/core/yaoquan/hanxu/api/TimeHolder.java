package core.yaoquan.hanxu.api;

import core.yaoquan.hanxu.CoreHanXu;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@EventBusSubscriber(modid = CoreHanXu.MOD_ID)
public class TimeHolder {
    // Unit transform.
    public static final int TICKS_PER_SECOND = 20;
    public static final int TICKS_PER_MINUTE = 20 * 60;
    public static final int TICKS_PER_HOUR = 20 * 3600;

    // Storage all timers (in safety method).
    private static final Map<String, TimerData> registeredTimer = new ConcurrentHashMap<>();

    // Register your new timer (Available to override old timer):
    public static void registerTimer(String timerId, int durationTicks, Consumer<ServerPlayer> callback) {
        registeredTimer.put(timerId, new TimerData(timerId, durationTicks, callback));
    }

    public static void registerTimerInSeconds(String timerId, int durationSeconds, Consumer<ServerPlayer> callback) {
        int durationTicks = durationSeconds * TICKS_PER_SECOND;
        registeredTimer.put(timerId, new TimerData(timerId, durationTicks, callback));
    }

    public static void registerTimerInMinutes(String timerId, int durationMinutes, Consumer<ServerPlayer> callback) {
        int durationTicks = durationMinutes * TICKS_PER_MINUTE;
        registeredTimer.put(timerId, new TimerData(timerId, durationTicks, callback));
    }

    public static void registerTimerInHours(String timerId, int durationHours, Consumer<ServerPlayer> callback) {
        int durationTicks = durationHours * TICKS_PER_HOUR;
        registeredTimer.put(timerId, new TimerData(timerId, durationTicks, callback));
    }

    // Method of using timer:
    public static void startTimer(String timerId, ServerPlayer player) {
        TimerData timerData = registeredTimer.get(timerId);
        // Start timer when existed.
        if (timerData != null) {
            timerData.start(player);
        }
    }

    public static void stopTimer(String timerId) {
        TimerData timerData = registeredTimer.get(timerId);
        // Stop timer when existed.
        if (timerData != null) {
            timerData.stop();
        }
    }

    public static void resetTimer(String timerId) {
        TimerData timerData = registeredTimer.get(timerId);
        // Reset timer when existed.
        if (timerData != null) {
            // Reminder: Timer will stop counting, and reset to the initial ticks.
            timerData.reset();
        }
    }

    public static void removeTimer(String timerId) {
        TimerData timerData = registeredTimer.get(timerId);
        // Stop and remove timer.
        if (timerData != null) {
            timerData.stop();
            registeredTimer.remove(timerId);
        }
    }

    // Method of getting timer's information:
    public static String returnTimerId(String timerId) {
        TimerData timerData = registeredTimer.get(timerId);
        return timerData != null? timerData.timerId : "Null";
    }

    public static int returnRemainingTicks(String timerId) {
        TimerData timerData = registeredTimer.get(timerId);
        return timerData != null? timerData.returnRemainingTicks() : -1;
    }

    public static int returnRemainingSeconds(String timerId) {
        TimerData timerData = registeredTimer.get(timerId);
        return timerData != null? timerData.returnRemainingTicks() * TICKS_PER_SECOND : -1;
    }

    public static int returnRemainingMinutes(String timerId) {
        TimerData timerData = registeredTimer.get(timerId);
        return timerData != null? timerData.returnRemainingTicks() * TICKS_PER_MINUTE : -1;
    }

    public static int returnRemainingHours(String timerId) {
        TimerData timerData = registeredTimer.get(timerId);
        return timerData != null? timerData.returnRemainingTicks() * TICKS_PER_HOUR : -1;
    }

    public static int returnInitialTicks(String timerId) {
        TimerData timerData = registeredTimer.get(timerId);
        return timerData != null? timerData.returnInitialTicks() : -1;
    }

    public static int returnInitialSeconds(String timerId) {
        TimerData timerData = registeredTimer.get(timerId);
        return timerData != null? timerData.returnInitialTicks() * TICKS_PER_SECOND : -1;
    }

    public static int returnInitialMinutes(String timerId) {
        TimerData timerData = registeredTimer.get(timerId);
        return timerData != null? timerData.returnInitialTicks() * TICKS_PER_MINUTE : -1;
    }

    public static int returnInitialHours(String timerId) {
        TimerData timerData = registeredTimer.get(timerId);
        return timerData != null? timerData.returnInitialTicks() * TICKS_PER_HOUR : -1;
    }

    public static boolean isItCounting(String timerId) {
        TimerData timerData = registeredTimer.get(timerId);
        return timerData != null && timerData.isItCounting();
    }

    // Collect all registered timer by id and return.
    public static String[] returnAllTimerIds() {
        return registeredTimer.keySet().toArray(new String[0]);
    }

    // Define an actual timer data system.
    private static class TimerData {
        private final String timerId;
        private final int initialTicks;
        private final Consumer<ServerPlayer> callback;
        private ServerPlayer player;
        private int remainingTicks;
        private boolean isCounting;

        TimerData(String timerId, int durationTicks, Consumer<ServerPlayer> callback) {
            this.timerId = timerId;
            this.initialTicks = durationTicks;
            this.callback = callback;
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

        String returnTimerId() {
            return timerId;
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
        registeredTimer.values().forEach(TimerData::tickRecall);
    }
}
