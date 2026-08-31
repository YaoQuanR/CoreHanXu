package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.PermissionHolder;
import core.yaoquan.hanxu.api.define.Error;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.registry.config.GeneralConfig;
import core.yaoquan.hanxu.util.MessagePublisher;
import core.yaoquan.hanxu.util.Resolver;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Random;
import java.util.UUID;

public class ExecutePermission {
    public static int executePermission_Check(CommandContext<CommandSourceStack> context, String target) {
        String playerId;
        try {
            playerId = StringArgumentType.getString(context, "player_id");
        }
        catch (IllegalArgumentException e) {
            if (context.getSource().getPlayer() != null) {
                playerId = context.getSource().getPlayer().getName().getString();
            }
            else {
                playerId = null;
            }
        }

        switch (target) {
            case "player" -> {
                UUID playerUUID = Resolver.resolveTargetUUID(context, playerId);
                if (playerUUID == null) {
                    MessagePublisher.sendFailureMessage(context, core.yaoquan.hanxu.api.define.Error.errorComponent(core.yaoquan.hanxu.api.define.Error.GeneralError.targetNotExist));
                    return 0;
                }
                else {
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    if (server == null) {
                        return 0;
                    }
                    ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
                    if (player == null) {
                        MessagePublisher.sendFailureMessage(context, core.yaoquan.hanxu.api.define.Error.errorComponent(core.yaoquan.hanxu.api.define.Error.GeneralError.targetNotExist));
                        return 0;
                    }

                    int permissionLevel = PermissionHolder.Storage.getPlayerPermissionLevel(player);
                    MessagePublisher.sendSystemMessage(context, Component.literal("[HX] " + permissionLevel).withColor(General.Color.TITLE));
                }
            }
            case "server" -> {
                int commandblockPermissionLevel = context.getSource().getLevel().getGameRules().getInt(PermissionHolder.Storage.nonPlayerSourcePermissionLevel);
                MessagePublisher.sendSystemMessage(context, Component.literal("[HX] " + commandblockPermissionLevel).withColor(General.Color.TITLE));
            }
            case "player_first_grant" -> {
                int autoAuthorizedPermissionLevel = GeneralConfig.setAutoPermissionLevelAuthorize.getAsInt();
                MessagePublisher.sendSystemMessage(context, Component.literal("[HX] " + autoAuthorizedPermissionLevel).withColor(General.Color.TITLE));
            }
            default -> {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.undefinedOperationCategory));
                return 0;
            }
        }
        return 1;
    }

    public static int executePermission_Set(CommandContext<CommandSourceStack> context) {
        String playerId = StringArgumentType.getString(context, "player_id");

        MinecraftServer server = context.getSource().getServer();
        ServerPlayer player;
        if (playerId.equals("-me") || playerId.equals("-m") || playerId.equals("-nearest") || playerId.equals("-n")) {
            if (context.getSource().getPlayer() != null) {
                player = context.getSource().getPlayer();
            }
            else {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.notPlayer));
                return 0;
            }
        }
        else if (playerId.equals("-random") || playerId.equals("-r")) {
            player = server.getPlayerList().getPlayers().get(new Random().nextInt(server.getPlayerList().getPlayers().size()));
        }
        else {
            player = server.getPlayerList().getPlayerByName(playerId);
            if (player == null) {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.targetNotExist));
                return 0;
            }
        }

        int newLevel = IntegerArgumentType.getInteger(context, "level");

        if (newLevel > 10) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.exceedMaximumPermissionLevel));
            return 0;
        }

        boolean editable = GeneralConfig.setEditablePlayerPermission.getAsBoolean();

        if (!editable) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.uneditablePlayerPermission));
            return 0;
        }

        PermissionHolder.Storage.setPlayerPermissionLevel(player, newLevel);

        MessagePublisher.sendSystemMessage(context, Component.literal("[HX] ✔ -> " + newLevel).withColor(General.Color.TITLE));
        return 1;
    }
}
