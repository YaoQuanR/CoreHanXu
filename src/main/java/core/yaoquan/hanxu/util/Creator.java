package core.yaoquan.hanxu.util;

import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.AttributeHolder;
import core.yaoquan.hanxu.api.custom.BehaviorRegistry;
import core.yaoquan.hanxu.api.define.Error;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import static core.yaoquan.hanxu.api.define.Error.returnGeneralError;

public class Creator {
    /**
     * Use this method to create callback behavior, it is same to command timer creation.
     * @param context           CommandSourceStack from command builder {@link com.mojang.brigadier.context}.
     * @param timerId           Unique title of timer.
     * @param endBehavior       If you are using command callback generator,
     *                          remind/execute/null is required to fill in for recreate callback.
     * @param behaviorContent   Also required when using command callback,
     *                          remind: display information context; execute: command execution; null: nothing.
     * @return                  Generated callback: Consumer<\ServerPlayer>.
     */
    public static Consumer<ServerPlayer> createCallback(CommandContext<CommandSourceStack> context, String timerId, String endBehavior, String behaviorContent) {
        // Build callback according to endBehavior from command;
        // ?(You are advised to use API "createTemplateTimer"/"createInstanceTimer" to build advanced timer behavior).
        Consumer<ServerPlayer> callback;
        switch (endBehavior) {
            case "e", "execute":
                // Pass create only if selector used @r/a/e, @p will replace by player id.
                if (behaviorContent.contains("@s")) {
                    MessagePublisher.sendFailureMessage(context, returnGeneralError(Error.GeneralError.invalidSelectorUsed));
                    return null;
                }
                else if (behaviorContent.contains("@p")) {
                    if (context != null) {
                        MessagePublisher.sendFailureMessage(context, returnGeneralError(Error.GeneralError.selectorToNearestUsed));
                    }
                }

                // Then register command execution into source stack;
                // !(If NO online player exist, selector which used @r/a/p will lose their effect on command execution).
                callback = player -> {
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    if (server != null) {
                        String callbackCommand = behaviorContent.startsWith("/")? behaviorContent : ("/" + behaviorContent);
                        if (callbackCommand.contains("@p") && player != null) {
                            List<ServerPlayer> players = server.getPlayerList().getPlayers();
                            ServerPlayer nearest = players.stream().min(Comparator.comparing(p -> p.distanceTo(player))).orElse(player);
                            callbackCommand = callbackCommand.replace("@p", nearest.getName().getString());
                        }
                        else if (callbackCommand.contains("@p") && player == null) {
                            callbackCommand = callbackCommand.replace("@p", "@a");
                        }

                        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), callbackCommand);
                    }
                };
                break;
            case "r", "remind":
                // Send message when time out:
                // Modified information.
                if (behaviorContent != null) {
                    callback = player -> {
                        if (player != null) {
                            player.sendSystemMessage(Component.literal(behaviorContent));
                        }
                        // Else broadcast to everyone.
                        else {
                            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                            if (server != null) {
                                server.getPlayerList().getPlayers().forEach(p -> {
                                    p.sendSystemMessage(Component.literal(behaviorContent));
                                });
                            }
                        }
                    };
                }
                // Or default information.
                else {
                    callback = player -> {
                        if (player != null) {
                            player.sendSystemMessage(
                                Component.translatable("commands.chx.timer_time_out")
                                    .append(Component.literal(" " + timerId))
                                    .withColor(0xFFD700)
                            );
                        }
                        // Else broadcast to everyone.
                        else {
                            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                            if (server != null) {
                                server.getPlayerList().getPlayers().forEach(p -> {
                                    p.sendSystemMessage(Component.translatable("commands.chx.timer_time_out")
                                        .append(Component.literal(" " + timerId))
                                        .withColor(0xFFD700));
                                });
                            }
                        }
                    };
                    MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_default_remind").withColor(0xFFD700));
                }
                break;
            case "n", "null":
                // Nothing to do, same as default.
            default:
                callback = player -> {};
                break;
        }

        return callback;
    }

    /// Register callback for commands/YAML's behaviors.
    public static void registerCallback(String callbackId, String callbackBehavior, String callbackContent) {
        if (callbackId != null && !callbackId.isEmpty()) {
            String fullCallbackId;
            if (callbackId.split(":").length == 3) {
                fullCallbackId = callbackId;
            }
            else {
                fullCallbackId = "attribute:core_hanxu-command:" + callbackId;
            }
            if (callbackContent != null && !callbackContent.isEmpty()) {
                switch (callbackBehavior) {
                    case "e", "execute":
                        BehaviorRegistry.register(
                            fullCallbackId, (player, parameters) -> {
                                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                                if (server != null && player != null) {
                                    String content = callbackContent;
                                    if (content.contains("@s") || content.contains("@p")) {
                                        content = content.replace("@s", player.getName().getString());
                                    }
                                    if (!content.startsWith("/")) {
                                        content = "/" + content;
                                    }
                                    server.getCommands().performPrefixedCommand(
                                        player.createCommandSourceStack(), content
                                    );
                                }
                            }
                        );
                        return;
                    case "r", "remind":
                        BehaviorRegistry.register(
                            fullCallbackId, (player, parameters) -> {
                                if (player != null) {
                                    player.sendSystemMessage(Component.literal(callbackContent));
                                }
                            }
                        );
                        return;
                    case "recovery":
                        String[] recoveryData = callbackContent.split(":", 3);
                        String attributeId = recoveryData[0];
                        float value = Float.parseFloat(recoveryData[1]);

                        AttributeHolder.ThresholdDirection thresholdDirection = getThresholdDirection(recoveryData);

                        BehaviorRegistry.register(
                            fullCallbackId, (player, parameters) -> {
                                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                                if (server != null) {
                                    for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                                        if (serverPlayer != null) {
                                            AttributeHolder.addValue(serverPlayer.getUUID(), attributeId, value, false, thresholdDirection);
                                        }
                                    }
                                }
                            }
                        );
                        return;
                    case "n", "null":
                        return;
                    default:
                        break;
                }
            }
            else {
                switch (callbackBehavior) {
                    case "e", "execute", "r", "remind":
                        BehaviorRegistry.register(
                            fullCallbackId, (player, parameters) -> {
                                if (player != null) {
                                    player.sendSystemMessage(
                                        Component.translatable("commands.chx.attribute_reached_threshold")
                                                .append(Component.literal(" " + callbackId))
                                                .withColor(0xFFD700)
                                    );
                                }
                            }
                        );
                        return;
                    case "n", "null":
                        return;
                    default:
                        break;
                }
            }

            // Mod will try to load callback if existed.
            if (callbackBehavior.equals("a") || callbackBehavior.equals("api")) {
                BehaviorRegistry.BehaviorCallback callback = BehaviorRegistry.getCallback(callbackId);
                if (callback != null) {
                    BehaviorRegistry.register(
                        callbackId, BehaviorRegistry.getCallback(callbackId)
                    );
                }
                else {
                    CoreHanXu.LOGGER.warn("[HX] Failed to register behavior callback: {}", callbackId);
                }
            }
        }
    }

    private static AttributeHolder.ThresholdDirection getThresholdDirection(String[] recoveryData) {
        String direction = recoveryData[2];

        AttributeHolder.ThresholdDirection thresholdDirection;
        switch (direction) {
            case "up" -> thresholdDirection = AttributeHolder.ThresholdDirection.UP;
            case "down" -> thresholdDirection = AttributeHolder.ThresholdDirection.DOWN;
            case "flex" -> thresholdDirection = AttributeHolder.ThresholdDirection.FLEX;
            default -> thresholdDirection = AttributeHolder.ThresholdDirection.POINT;
        }
        return thresholdDirection;
    }
}
