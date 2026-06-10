package core.yaoquan.hanxu.api.custom;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.util.Resolver;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Register your callback behavior by here.
 */
public class BehaviorRegistry {
    private static final Map<String, BehaviorCallback> callbacks = new ConcurrentHashMap<>();

    /**
     * Register a callback for callback behavior.
     * @param callbackId        Accept format of [function name]:[name space]:[callback name].
     * @param callback          Define the Java code callback by using lambda (player, parameter) -> {}.
     *                          Please view {@link BehaviorCallback}.
     */
    public static void register(String callbackId, BehaviorCallback callback) {
        String finalCallbackId = Resolver.resolveFullCallbackId(callbackId);
        if (callbacks.containsKey(finalCallbackId)) {
            CoreHanXu.LOGGER.warn("[HX] Overriding registered callback id found: {}", finalCallbackId);
        }
        callbacks.put(finalCallbackId, callback);
    }

    /**
     * Execute the callback behavior.
     * @param player            Player that from {@link ServerPlayer}.
     * @param callbackId        Accept format of [function name]:[name space]:[callback name] for execution.
     * @param parameters        Accept custom map for advanced conditions.
     */
    public static void execute(ServerPlayer player, String callbackId, Map<String, String> parameters) {
        String finalCallbackId = Resolver.resolveFullCallbackId(callbackId);
        BehaviorCallback callback = callbacks.get(finalCallbackId);
        if (callback != null) {
            callback.execute(player, parameters);
        }
        else {
            CoreHanXu.LOGGER.warn("[HX] No callback found: {}", finalCallbackId);
        }
    }

    public static boolean isRegistered(String callbackId) {
        String finalCallbackId = Resolver.resolveFullCallbackId(callbackId);
        return callbacks.containsKey(finalCallbackId);
    }

    public static void unregister(String callbackId) {
        String finalCallbackId = Resolver.resolveFullCallbackId(callbackId);
        callbacks.remove(finalCallbackId);
    }

    public static Map<String, BehaviorCallback> getCallbacks() {
        return callbacks;
    }

    public static BehaviorCallback getCallback(String callbackId) {
        String finalCallbackId = Resolver.resolveFullCallbackId(callbackId);
        return callbacks.get(finalCallbackId);
    }

    @FunctionalInterface
    public interface BehaviorCallback {
        void execute(ServerPlayer player, Map<String, String> parameters);
    }
}
