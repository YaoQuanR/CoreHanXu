package core.yaoquan.hanxu.api.custom;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.util.Resolver;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p><b>
 *     Behavior Registry
 * </b></p>
 * <p>
 *     Callback behavior register method.
 * </p>
 * @since 0.5.0 (Internal Development)
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
     * Register a callback for callback behavior.
     * @param functionName      Part of the callback id, define what function system used this callback.
     * @param nameSpace         Usually used your mod id or other name space for avoiding naming conflict.
     * @param callbackName      The callback name.
     * @param callback          Define the Java code callback by using lambda (player, parameter) -> {}.
     *                          Please view {@link BehaviorCallback}.
     */
    public static void register(String functionName, String nameSpace, String callbackName, BehaviorCallback callback) {
        String fullCallbackId = functionName + ":" + nameSpace + ":" + callbackName;
        register(fullCallbackId, callback);
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

    /**
     * Execute the callback behavior.
     * @param player            Player that from {@link ServerPlayer}.
     * @param functionName      Part of the callback id, define what function system used this callback.
     * @param nameSpace         Usually used your mod id or other name space for avoiding naming conflict.
     * @param callbackName      The callback name.
     * @param parameters        Accept custom map for advanced conditions.
     */
    public static void execute(ServerPlayer player, String functionName, String nameSpace, String callbackName, Map<String, String> parameters) {
        String fullCallbackId = functionName + ":" + nameSpace + ":" + callbackName;
        execute(player, fullCallbackId, parameters);
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
