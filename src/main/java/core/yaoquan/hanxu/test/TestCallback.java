package core.yaoquan.hanxu.test;

import core.yaoquan.hanxu.api.TimeHolder;
import core.yaoquan.hanxu.api.custom.TimerCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
            case "test3" -> player -> {
                // Get server when using global master.
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                if (server == null) {
                    return;
                }

                // Do not use @s/p.
                String command1 = "/weather rain 1200";
                String command2 = "/effect give @a levitation 5 2";

                server.getCommands().performPrefixedCommand(
                        server.createCommandSourceStack(),
                        command1
                );

                server.getCommands().performPrefixedCommand(
                        server.createCommandSourceStack(),
                        command2
                );
            };
            case "test4" -> player -> {
                if (player == null) {
                    return;
                }

                // Get inventory and increase item's damage (or broke).
                for (ItemStack item : player.getInventory().getNonEquipmentItems()) {
                    if (item.getItem() == Items.GOLDEN_SWORD) {
                        int newDamage = item.getDamageValue() + 1;
                        if (newDamage >= item.getMaxDamage()) {
                            // Set overlay to true: display at action bar.
                            player.sendSystemMessage(Component.literal("Item broke."), true);
                            item.shrink(1);
                        }
                        else {
                            // Set overlay to true: display at action bar.
                            player.sendSystemMessage(Component.literal("Item damaged."), true);
                            item.setDamageValue(newDamage);
                        }
                    }
                }

                // Reset.
                TimeHolder.resetInstanceTimer(player.getUUID(), "test4");
                TimeHolder.startInstanceTimer(player.getUUID(), "test4");
            };
            default -> player -> {};
        };
    }
}
