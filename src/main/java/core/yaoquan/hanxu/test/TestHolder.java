package core.yaoquan.hanxu.test;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.AttributeHolder;
import core.yaoquan.hanxu.api.TimeHolder;
import core.yaoquan.hanxu.api.custom.BehaviorRegistry;
import core.yaoquan.hanxu.api.define.Error;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.NullableValue;
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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static core.yaoquan.hanxu.api.define.Error.*;

public class TestHolder {
    private static final Set<String> privateTestList = ConcurrentHashMap.newKeySet();

    static {
        privateTestList.add("Dev");
        privateTestList.add("YaoQuanR");
        // Join test group by here, or add by code.
    }

    // Permission check.
    public static boolean hasPrivateTestPermission(CommandSourceStack source) {
        return source.getEntity() instanceof Player player && privateTestList.contains(player.getName().getString());
    }

    public static int executeTest_Timer(CommandContext<CommandSourceStack> context) {
        int testId = IntegerArgumentType.getInteger(context, "test_id");

        if (!(context.getSource().getEntity() instanceof ServerPlayer serverPlayer)) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(GeneralError.notPlayer));
            return 0;
        }

        switch (testId) {
            case 1:
                // Single timer define.
                // If you did not finish API TimerCallback, callback will become null after restart the server.
                Consumer<ServerPlayer> customCallback = player -> {
                    // Message.
                    player.sendSystemMessage(Component.literal("[HX] From the emperor's madness!").withColor(General.Color.TEST));

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
                if (TimeHolder.getInstanceId(General.TargetUUID.GLOBAL_UUID, "test1").isPresent()) {
                    TimeHolder.deleteInstanceTimer(
                            General.TargetUUID.GLOBAL_UUID,
                            "test1"
                    );
                }
                else if (TimeHolder.getInstanceId(serverPlayer.getUUID(), "test1").isPresent()) {
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
                if (TimeHolder.getInstanceId(serverPlayer.getUUID(), "test2").isPresent()) {
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

                if (TimeHolder.getInstanceId(serverPlayer.getUUID(), "test3").isPresent()) {
                    TimeHolder.deleteInstanceTimer(
                            General.TargetUUID.GLOBAL_UUID,
                            "test3"
                    );
                }

                TimeHolder.createInstanceTimer(
                        General.TargetUUID.GLOBAL_UUID,
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
                        General.TargetUUID.GLOBAL_UUID,
                        "test3",
                        true
                );

                break;
            case 4:
                // Now create player timer, with auto restart feature.

                if (TimeHolder.getInstanceId(serverPlayer.getUUID(), "test4").isPresent()) {
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

                if (TimeHolder.getInstanceId(serverPlayer.getUUID(), "test5").isPresent()) {
                    TimeHolder.deleteTemplateTimer(
                            "test5"
                    );
                    TimeHolder.deleteInstanceTimer(
                            serverPlayer.getUUID(),
                                "test5"
                    );
                    TimeHolder.deleteInstanceTimer(
                            General.TargetUUID.GLOBAL_UUID,
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
                        General.TargetUUID.GLOBAL_UUID,
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
                        General.TargetUUID.GLOBAL_UUID,
                        "test5",
                        true
                );

                break;
            default:
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(GeneralError.undefinedOperationId));
                return 0;
        }

        return 1;
    }

    // For display inner timer (included ":").
    public static int executeTest_TimerDisplay(CommandContext<CommandSourceStack> context, boolean state) {
        String masterGroup = StringArgumentType.getString(context, "master_group");
        String timerId = StringArgumentType.getString(context, "timer_id");

        if (!(context.getSource().getEntity() instanceof ServerPlayer serverPlayer)) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(GeneralError.notPlayer));
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
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(GeneralError.notPlayer));
            return 0;
        }

        // A set that contains some tool value for determination.
        final Set<String> triggered = ConcurrentHashMap.newKeySet();

        switch (testId) {
            case 1:
                // Unregister when existed.
                NullableValue<AttributeHolder.CustomAttribute> nullableAttribute = AttributeHolder.getAttributeDefinition("core_hanxu-test:run_value", true);
                nullableAttribute.ifPresent(AttributeHolder::unregister);

                // Remember: Register your own attribute when server start!

                // Define a attribute.
                AttributeHolder.CustomAttribute attribute1 = new AttributeHolder.CustomAttribute(
                    "core_hanxu-test:run_value", 60, 10)
                    // Chain factory: define threshold and zero behaviors.
                    // It must be satisfied of the rule [function name]:[name space]:[threshold name].
                    .onThreshold(20, "attribute:core_hanxu-test:give_sword")
                    .onThreshold(40, "attribute:core_hanxu-test:give_speed")
                    .onZero("attribute:core_hanxu-test:clear_all")
                    .setRecovery("attribute:core_hanxu-test:recovery1")
                    // It defined how many ticks for interval to recovery (default = 1).
                    .setRecoveryIntervalTicks(1);

                // Then register.
                AttributeHolder.register(attribute1);

                // Register and define the callback behaviors.
                BehaviorRegistry.register("attribute:core_hanxu-test:give_sword", (player, parameters) -> {
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
                    player.sendSystemMessage(Component.literal("[HX] Received reward sword!").withColor(General.Color.TEST));
                });

                BehaviorRegistry.register("attribute:core_hanxu-test:give_speed", (player, parameters) -> {
                    if (player == null) {
                        return;
                    }

                    // Set a state that avoid to trigger repeatedly.
                    if (triggered.contains("core_hanxu-test:give_speed")) {
                        return;
                    }

                    player.addEffect(new MobEffectInstance(MobEffects.SPEED, 160, 1, true, false));
                    player.sendSystemMessage(Component.literal("[HX] Received reward speed!").withColor(General.Color.TEST));

                    triggered.add("core_hanxu-test:give_speed");
                });

                // Register zero behavior as same method.
                BehaviorRegistry.register("attribute:core_hanxu-test:clear_all", (player, parameters) -> {
                    if (player == null) {
                        return;
                    }
                    player.removeAllEffects();
                    player.getInventory().clearOrCountMatchingItems(
                        item -> item.is(Items.DIAMOND_SWORD), 64, player.inventoryMenu.getCraftSlots()
                    );
                    player.sendSystemMessage(Component.literal("[HX] Clear all rewards!").withColor(General.Color.TEST));
                });

                // Register recovery curve behavior as same method.
                BehaviorRegistry.register("attribute:core_hanxu-test:recovery1", (player, parameters) -> {
                    if (player == null) {
                        return;
                    }
                    // You can get parameter from map (All are string).
                    float currentValue = Float.parseFloat(parameters.get("current_value"));

                    // Using UP direction will avoid trigger when decreasing value (Normal: POINT).
                    // Increase when running.
                    if (player.isSprinting() && player.tickCount % 20 == 0) {
                        AttributeHolder.addValue(player.getUUID(), "core_hanxu-test:run_value", 3f, true, AttributeHolder.ThresholdDirection.UP);
                    }
                    // Decrease behavior.
                    else if (!player.isSprinting()) {
                        AttributeHolder.reduceValue(player.getUUID(), "core_hanxu-test:run_value", 0.15f, true, AttributeHolder.ThresholdDirection.UP);
                    }

                    // Allowed to trigger again when satisfied.
                    if (currentValue <= 30) {
                        triggered.remove("core_hanxu-test:give_speed");
                    }
                });

                // Then set display.
                AttributeHolder.displayToInfoPage(
                    serverPlayer,
                    serverPlayer.getUUID(),
                    "core_hanxu-test:run_value",
                    true,
                    true
                );

                MessagePublisher.sendSystemMessage(context, Component.literal("[HX] Registered attribute 1!").withColor(General.Color.TEST));

                break;
            case 2:
                // For test purpose: Register a callback and wait for command based attribute trigger this api callback.
                BehaviorRegistry.register("attribute:core_hanxu-test:from_api", (player, parameters) -> {
                    if (player == null) {
                        return;
                    }

                    player.sendSystemMessage(Component.literal("[HX] This 'from_api' threshold was successfully triggered.").withColor(General.Color.TEST));
                });

                MessagePublisher.sendSystemMessage(context, Component.literal("[HX] This 'from_api' threshold was registered.").withColor(General.Color.TEST));

                break;
            case 3:
                // For test purpose: Only register a recovery.
                String extra;
                try {
                    extra = StringArgumentType.getString(context, "extra");
                }
                catch (IllegalArgumentException e) {
                    extra = "core_hanxu-test:run_value";
                }

                final String attributeId = extra.split(":", 2)[0];
                boolean fromApi = extra.split(":", 2)[1].equals("true");

                BehaviorRegistry.register("attribute:core_hanxu-test:recovery1", (player, parameters) -> {
                    if (player == null) {
                        return;
                    }
                    float currentValue = Float.parseFloat(parameters.get("current_value"));

                    if (player.isSprinting() && player.tickCount % 20 == 0) {
                        AttributeHolder.addValue(player.getUUID(), attributeId, 3f, fromApi, AttributeHolder.ThresholdDirection.UP);
                    }
                    else if (!player.isSprinting()) {
                        AttributeHolder.reduceValue(player.getUUID(), attributeId, 0.15f, fromApi, AttributeHolder.ThresholdDirection.UP);
                    }

                    if (currentValue <= 30) {
                        triggered.remove("core_hanxu-test:give_speed");
                    }
                });

                MessagePublisher.sendSystemMessage(context, Component.literal("[HX] Execute attribute test 3 and build recovery.").withColor(General.Color.TEST));
                break;
            default:
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(GeneralError.undefinedOperationId));
                return 0;
        }

        return 1;
    }

    public static int executeTest_AttributeYamlDisplay(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getPlayer() == null) {
            return 0;
        }

        MessagePublisher.sendSystemMessage(context, Component.literal("[HX] Now display all attributes to F4 debugging page.").withColor(General.Color.TEST));
        if (AttributeHolder.getCommandAttributes().isEmpty()) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(GeneralError.targetNotExist));
        }
        else {
            // Then display.
            for (Map.Entry<String, AttributeHolder.CustomAttribute> entry : AttributeHolder.getCommandAttributes().entrySet()) {
                String id = entry.getKey();
                AttributeHolder.CustomAttribute attribute = entry.getValue();

                if (attribute == null) {
                    continue;
                }

                AttributeHolder.displayToInfoPage(
                    context.getSource().getPlayer(),
                    context.getSource().getPlayer().getUUID(),
                    id,
                    false,
                    true
                );
            }
        }

        return 1;
    }

    public static int executeTest_Weather(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getPlayer() == null) {
            return 0;
        }

        MessagePublisher.sendSystemMessage(context, Component.literal("[HX] Not register test weather and show in F4 page.").withColor(General.Color.TEST));

        TestWeather.testFog();
        TestWeather.displayTestFog();
        return 1;
    }
}
