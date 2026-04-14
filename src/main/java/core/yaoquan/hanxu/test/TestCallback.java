package core.yaoquan.hanxu.test;

import core.yaoquan.hanxu.api.custom.TimerCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.function.Consumer;

// Define custom callback behavior.
public class TestCallback implements TimerCallback {
    @Override
    public String getMasterGroupId() {
        return "core_hanxu-test";
    }

    @Override
    public Consumer<ServerPlayer> createCustomCallback(String timerId, String endBehaviorTitle, String behaviorContent) {
        return switch (timerId) {
            case "test2" -> player -> {
                if (player == null) {
                    return;
                }
                player.sendSystemMessage(Component.literal("[HX] From the darkness erosion..."));

                player.addEffect(new MobEffectInstance(
                        MobEffects.BLINDNESS, 100, 0
                ));

                player.setHealth(player.getMaxHealth() / 2);
            };
            default -> player -> {};
        };
    }
}
