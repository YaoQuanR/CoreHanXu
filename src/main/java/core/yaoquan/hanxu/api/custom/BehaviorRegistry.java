package core.yaoquan.hanxu.api.custom;

import core.yaoquan.hanxu.CoreHanXu;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Register your callback behavior here.
 */
public class BehaviorRegistry {
    private static final Map<String, BehaviorCallback> callbacks = new ConcurrentHashMap<>();

    public static void register(String callbackId, BehaviorCallback callback) {
        if (callbacks.containsKey(callbackId)) {
            CoreHanXu.LOGGER.warn("[HX] Overriding registered callback id found: {}", callbackId);
        }
        callbacks.put(callbackId, callback);
    }

    public static void execute(ServerPlayer player, String callbackId, Map<String, String> parameters) {
        BehaviorCallback callback = callbacks.get(callbackId);
        if (callback != null) {
            callback.execute(player, parameters);
        }
        else {
            CoreHanXu.LOGGER.warn("[HX] No callback found: {}", callbackId);
        }
    }

    public static boolean isRegistered(String callbackId) {
        return callbacks.containsKey(callbackId);
    }

    public static void unregister(String callbackId) {
        callbacks.remove(callbackId);
    }

    @FunctionalInterface
    public interface BehaviorCallback {
        void execute(ServerPlayer player, Map<String, String> parameters);
    }
}
