package core.yaoquan.hanxu.test;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.AttributeHolder;
import core.yaoquan.hanxu.api.TimeHolder;
import core.yaoquan.hanxu.api.custom.BehaviorRegistry;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.Creator;
import core.yaoquan.hanxu.util.MessagePublisher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static core.yaoquan.hanxu.api.define.Error.*;

public class TestHolder {
    private static final Set<String> PRIVATE_TEST_LIST = ConcurrentHashMap.newKeySet();

    static {
        PRIVATE_TEST_LIST.add("Dev");
        PRIVATE_TEST_LIST.add("YaoQuanR");
        // Join test group by here, or add by code.
    }

    // Permission check.
    public static boolean hasPrivateTestPermission(CommandSourceStack source) {
        return source.getEntity() instanceof Player player && PRIVATE_TEST_LIST.contains(player.getName().getString());
    }

    public static int executeTest_Timer(CommandContext<CommandSourceStack> context) {
        int testId = IntegerArgumentType.getInteger(context, "test_id");

        if (!(context.getSource().getEntity() instanceof ServerPlayer serverPlayer)) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.notPlayer));
            return 0;
        }

        switch (testId) {
            case 1:
                // Single timer define.
                // If you did not finish API TimerCallback, callback will become null after restart the server.
                Consumer<ServerPlayer> customCallback = player -> {
                    // Message.
                    player.sendSystemMessage(Component.literal("[HX] From the emperor's madness!"));

                    // Get player's positions.
                    double x = player.getX();
                    double y = player.getY();
                    double z = player.getZ();

                    if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
                        return;
                    }

                    // Build commands.
                    String command1 = String.format(
                            "/summon area_effect_cloud %d %d %d {Radius:5,Duration:300,custom_particle:{type:angry_villager},potion_contents:{potion:blindness}}",
                            (int) x, (int) y - 1, (int) z
                    );
                    String command2 = String.format(
                            "/summon lightning_bolt %d %d %d",
                            (int) x, (int) y - 1, (int) z
                    );

                    // When generating commands, make sure server is on.
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    if (server != null) {
                        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command1);
                        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command2);
                    }
                };

                // Then create timer:
                // Delete old timer for new (For test usage), normally advised to delete old and mask it, or else use "reset".
                if (TimeHolder.getInstanceId(General.GLOBAL_UUID, "test1") != null) {
                    TimeHolder.deleteInstanceTimer(
                            General.GLOBAL_UUID,
                            "test1"
                    );
                }
                else if (TimeHolder.getInstanceId(serverPlayer.getUUID(), "test1") != null) {
                    TimeHolder.deleteInstanceTimer(
                            serverPlayer.getUUID(),
                            "test1"
                    );
                }
                // Major creation.
                TimeHolder.createInstanceTimer(
                        serverPlayer.getUUID(),
                        "test1",
                        8,
                        "second",
                        customCallback,
                        "test",
                        "custom_action",
                        "core_hanxu-test"
                );
                // For debugging (F4 in default).
                TimeHolder.displayToInfoPage(
                        serverPlayer,
                        serverPlayer.getUUID(),
                        "test1",
                        true
                );

                break;
            case 2:
                // Register timer callback when server start.
                // Check: TestCallback & CoreHanXu .java

                // Delete old timer for new (For test usage), normally advised to delete old and mask it, or else use "reset".
                if (TimeHolder.getInstanceId(serverPlayer.getUUID(), "test2") != null) {
                    TimeHolder.deleteInstanceTimer(
                            serverPlayer.getUUID(),
                            "test2"
                    );
                }

                // Create timer.
                TimeHolder.createInstanceTimer(
                        serverPlayer.getUUID(),
                        "test2",
                        8,
                        "second",
                        // Wait to fill in by custom callback definition.
                        null,
                        "test",
                        "custom_action",
                        // Important, this value should be the same within "getMasterGroupId()":"masterGroup".
                        "core_hanxu-test"
                );
                TimeHolder.displayToInfoPage(
                        serverPlayer,
                        serverPlayer.getUUID(),
                        "test2",
                        true
                );

                break;
            case 3:
                // Now create global timer.

                if (TimeHolder.getInstanceId(serverPlayer.getUUID(), "test3") != null) {
                    TimeHolder.deleteInstanceTimer(
                            General.GLOBAL_UUID,
                            "test3"
                    );
                }

                TimeHolder.createInstanceTimer(
                        General.GLOBAL_UUID,
                        "test3",
                        8,
                        "second",
                        null,
                        "test",
                        "custom_behavior",
                        "core_hanxu-test"
                );
                TimeHolder.displayToInfoPage(
                        serverPlayer,
                        General.GLOBAL_UUID,
                        "test3",
                        true
                );

                break;
            case 4:
                // Now create player timer, with auto restart feature.

                if (TimeHolder.getInstanceId(serverPlayer.getUUID(), "test4") != null) {
                    TimeHolder.deleteInstanceTimer(
                            serverPlayer.getUUID(),
                            "test4"
                    );
                }

                TimeHolder.createInstanceTimer(
                        serverPlayer.getUUID(),
                        "test4",
                        40,
                        "tick",
                        null,
                        "test",
                        "auto_restart_behavior",
                        "core_hanxu-test"
                );
                TimeHolder.displayToInfoPage(
                        serverPlayer,
                        serverPlayer.getUUID(),
                        "test4",
                        true
                );

                break;
            case 5:
                // Now using template timer feature for creation, which use command's callback creator.

                if (TimeHolder.getInstanceId(serverPlayer.getUUID(), "test5") != null) {
                    TimeHolder.deleteTemplateTimer(
                            "test5"
                    );
                    TimeHolder.deleteInstanceTimer(
                            serverPlayer.getUUID(),
                                "test5"
                    );
                    TimeHolder.deleteInstanceTimer(
                            General.GLOBAL_UUID,
                                "test5"
                    );
                }

                // Using command's creator (It can also be produce by custom callback definition).
                Consumer<ServerPlayer> callback = Creator.createCallback(context, "test5", "remind", "using command's callback creator");

                // Create template.
                TimeHolder.createTemplateTimer(
                        "test5",
                        12,
                        "second",
                        callback,
                        "remind",
                        "using command's callback creator",
                        "core_hanxu-command" // Using command rebuilder.
                );

                // Then apply (register).
                TimeHolder.createInstanceFromTemplate(
                        serverPlayer.getUUID(),
                        "test5"
                );
                TimeHolder.createInstanceFromTemplate(
                        General.GLOBAL_UUID,
                        "test5"
                );

                // Then register to display.
                TimeHolder.displayToInfoPage(
                        serverPlayer,
                        serverPlayer.getUUID(),
                        "test5",
                        true
                );
                TimeHolder.displayToInfoPage(
                        serverPlayer,
                        General.GLOBAL_UUID,
                        "test5",
                        true
                );

                break;
            default:
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.undefinedOperationId));
                return 0;
        }

        return 1;
    }

    // For display inner timer (included ":").
    public static int executeTest_TimerDisplay(CommandContext<CommandSourceStack> context, boolean state) {
        String masterGroup = StringArgumentType.getString(context, "master_group");
        String timerId = StringArgumentType.getString(context, "timer_id");

        if (!(context.getSource().getEntity() instanceof ServerPlayer serverPlayer)) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.notPlayer));
            return 0;
        }

        TimeHolder.displayToInfoPage(
                serverPlayer,
                serverPlayer.getUUID(),
                masterGroup + ":" + timerId,
                state
        );

        return 1;
    }

    public static int executeTest_Attribute(CommandContext<CommandSourceStack> context) {
        int testId = IntegerArgumentType.getInteger(context, "test_id");

        if (!(context.getSource().getEntity() instanceof ServerPlayer serverPlayer)) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.notPlayer));
            return 0;
        }

        // A set that contains some tool value for determination.
        final Set<String> triggered = ConcurrentHashMap.newKeySet();

        switch (testId) {
            case 1:
                // Unregister when existed.
                AttributeHolder.CustomAttribute currentAttribute = AttributeHolder.getAttributeDefinition("tut-run_value", true);
                if (currentAttribute != null) {
                    AttributeHolder.unregister(currentAttribute);
                }

                // Define a attribute.
                AttributeHolder.CustomAttribute attribute1 = new AttributeHolder.CustomAttribute(
                    "tut-run_value", 80, 10)
                    // Fluent factory: define threshold and zero behaviors.
                    .onThreshold(20, "tut-give_sword")
                    .onThreshold(50, "tut-give_speed")
                    .onZero("tut-clear_all")
                    .setRecovery("tut-recovery1");

                // Then register.
                AttributeHolder.register(attribute1);

                // Register and define the callback behaviors.
                BehaviorRegistry.register("tut-give_sword", (player, parameters) -> {
                    /*
                       Consider as server or console if "player" is null.
                       Parameters is a map that receive elements from system.
                       In attribute system, this elements will be provided as String:
                       master_id, master_name, attribute_id, threshold, current_value, new_value, direction.
                    */
                    if (player == null) {
                        return;
                    }

                    // Reject to give another item repeatedly.
                    if (player.getInventory().contains(new ItemStack(Items.DIAMOND_SWORD))) {
                        return;
                    }

                    player.getInventory().add(new ItemStack(Items.DIAMOND_SWORD));
                    player.sendSystemMessage(Component.literal("[HX] Received reward sword!"));
                });

                BehaviorRegistry.register("tut-give_speed", (player, parameters) -> {
                    if (player == null) {
                        return;
                    }

                    // Set a state that avoid to trigger repeatedly.
                    if (triggered.contains("tut-give_speed")) {
                        return;
                    }

                    player.addEffect(new MobEffectInstance(MobEffects.SPEED, 160, 1, true, false));
                    player.sendSystemMessage(Component.literal("[HX] Received reward speed!"));

                    triggered.add("tut-give_speed");
                });

                // Register zero behavior as same method.
                BehaviorRegistry.register("tut-clear_all", (player, parameters) -> {
                    if (player == null) {
                        return;
                    }
                    player.removeAllEffects();
                    player.getInventory().clearOrCountMatchingItems(
                        item -> item.is(Items.DIAMOND_SWORD), 64, player.inventoryMenu.getCraftSlots()
                    );
                    player.sendSystemMessage(Component.literal("[HX] Clear all rewards!"));
                });

                // Register recovery curve behavior as same method.
                BehaviorRegistry.register("tut-recovery1", (player, parameters) -> {
                    if (player == null) {
                        return;
                    }
                    // You can get parameter from map (All are string).
                    float currentValue = Float.parseFloat(parameters.get("current_value"));

                    // Decrease behavior (In every tick).
                    AttributeHolder.reduceValue(player.getUUID(), "tut-run_value", 0.05f, true);

                    // Increase when running.
                    if (player.isSprinting() && player.tickCount % 20 == 0) {
                        AttributeHolder.addValue(player.getUUID(), "tut-run_value", 3f, true);
                    }

                    // Allowed to trigger again when satisfied.
                    if (currentValue <= 40) {
                        triggered.remove("tut-give_speed");
                    }
                });

                // Then set display.
                AttributeHolder.displayToInfoPage(
                    serverPlayer,
                    serverPlayer.getUUID(),
                    "tut-run_value",
                    true,
                    true
                );

                MessagePublisher.sendSystemMessage(context, Component.literal("[HX] Registered attribute 1!"));

                break;
            default:
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.undefinedOperationId));
                return 0;
        }

        return 1;
    }
}
