package core.yaoquan.hanxu.test;

import core.yaoquan.hanxu.api.custom.TimerCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.function.Consumer;

// Define custom callback behavior.
public class TestCallback implements TimerCallback {
    @Override
    public String getMasterGroupId() {
        return "core_hanxu-test";
    }

    @Override
    public Consumer<ServerPlayer> createCustomCallback(String timerId, String endBehaviorTitle, String behaviorContent) {
        return player -> {
            if (player == null) {
                return;
            }
            player.sendSystemMessage(Component.literal("[HX] From the darkness erosion..."));

            player.addEffect(new MobEffectInstance(
                    MobEffects.BLINDNESS, 100, 0
            ));

            player.setHealth(player.getMaxHealth() / 2);
        };
    }
}
