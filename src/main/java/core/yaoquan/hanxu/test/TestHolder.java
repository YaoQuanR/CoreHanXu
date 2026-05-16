package core.yaoquan.hanxu.test;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.TimeHolder;
import core.yaoquan.hanxu.util.Creator;
import core.yaoquan.hanxu.util.MessagePublisher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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
                if (TimeHolder.getInstanceId(TimeHolder.GLOBAL_UUID, "test1") != null) {
                    TimeHolder.deleteInstanceTimer(
                            TimeHolder.GLOBAL_UUID,
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
                        true,
                        false
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
                        true,
                        false
                );

                break;
            case 3:
                // Now create global timer.

                if (TimeHolder.getInstanceId(serverPlayer.getUUID(), "test3") != null) {
                    TimeHolder.deleteInstanceTimer(
                            TimeHolder.GLOBAL_UUID,
                            "test3"
                    );
                }

                TimeHolder.createInstanceTimer(
                        TimeHolder.GLOBAL_UUID,
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
                        TimeHolder.GLOBAL_UUID,
                        "test3",
                        true,
                        false
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
                        true,
                        false
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
                            TimeHolder.GLOBAL_UUID,
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
                        TimeHolder.GLOBAL_UUID,
                        "test5"
                );

                // Then register to display.
                TimeHolder.displayToInfoPage(
                        serverPlayer,
                        serverPlayer.getUUID(),
                        "test5",
                        true,
                        false
                );
                TimeHolder.displayToInfoPage(
                        serverPlayer,
                        TimeHolder.GLOBAL_UUID,
                        "test5",
                        true,
                        false
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
                state,
                false
        );

        return 1;
    }
}
