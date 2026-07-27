package core.yaoquan.hanxu.api.custom;

import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

/**
 * Override this interface for your callback definition.
 */
public interface TimerCallback {
    String getMasterGroupId();
    Consumer<ServerPlayer> createCustomCallback(String timerId, String titleParameter, String contentParameter);

    /* You are required to override those methods to create your custom callback for timer's end behavior.
    *  Here is an example.
    *
    *  public class XBehavior implements TimerCallback {
    *      @Override
    *      public String getMasterGroupId() {
    *          return "mod_X_end_behavior";
    *      }
    *
    *      @Override
    *      public Consumer<ServerPlayer> createCustomCallback(String timerId, String endBehaviorTitle, String behaviorContent) {
    *          return switch (timerId) {
    *              case "timer_1" -> CustomLogic.startX();
    *              case "timer_2" -> CustomLogic.startPhase2();
    *              default -> null;
    *          }
    *      };
    *  }
    *
    */
}
