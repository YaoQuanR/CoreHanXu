package core.yaoquan.hanxu.api.custom;

import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

/**
 * <p><h3>
 *     Timer Callback Registry
 * </h3></p>
 * <p>
 *     Override this interface for your callback definition.
 *     Only timer will use this callback registry.
 * </p>
 * @since 0.2.0 (Internal Development)
 */
public interface TimerCallback {
    String getMasterGroupId();

    /**
     * You are required to override those methods to create your custom callback for timer's end behavior.
     * Here is an example.
     *
     * <pre>{@code
     * public class XBehavior implements TimerCallback {
     *     @Override
     *     public String getMasterGroupId() {
     *         return "mod_X_end_behavior";
     *     }
     *
     *     @Override
     *     public Consumer<ServerPlayer> createCustomCallback(String timerId, String endBehaviorTitle, String behaviorContent) {
     *         return switch (timerId) {
     *             case "timer_1" -> CustomLogic.startX();
     *             case "timer_2" -> CustomLogic.startPhase2();
     *             default -> null;
     *         };
     *      }
     *  }
     *  }</pre>
     *
     * @param timerId             Unique title of timer.
     * @param titleParameter      If you are using command callback generator,
     *                            remind/execute/null is required to fill in for recreate callback.
     * @param contentParameter    Also required when using command callback,
     *                            remind: display information context; execute: command execution; null: nothing.
     * @return                    Generated callback: {@code Consumer<ServerPlayer>}.
     */
    Consumer<ServerPlayer> createCustomCallback(String timerId, String titleParameter, String contentParameter);
}
