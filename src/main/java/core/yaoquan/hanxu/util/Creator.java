package core.yaoquan.hanxu.util;

import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.CoreHanXu;
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
                        String callbackCommand = behaviorContent.startsWith("/") ? behaviorContent : ("/" + behaviorContent);
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
                                Component.translatable("commands.chx-a.timer_time_out")
                                    .append(Component.literal(" " + timerId))
                                    .withColor(0xFFD700)
                            );
                        }
                        // Else broadcast to everyone.
                        else {
                            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                            if (server != null) {
                                server.getPlayerList().getPlayers().forEach(p -> {
                                    p.sendSystemMessage(Component.translatable("commands.chx-a.timer_time_out")
                                        .append(Component.literal(" " + timerId))
                                        .withColor(0xFFD700));
                                });
                            }
                        }
                    };
                    MessagePublisher.sendSystemMessage(context, Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_default_end_behavior").withColor(0xFFD700));
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
}
