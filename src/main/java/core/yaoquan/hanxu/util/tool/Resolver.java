package core.yaoquan.hanxu.util.tool;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import core.yaoquan.hanxu.api.define.Error;
import core.yaoquan.hanxu.api.define.General;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static core.yaoquan.hanxu.api.define.Error.errorComponent;

public class Resolver {
    public static UUID resolveTargetUUID(CommandContext<CommandSourceStack> context, String targetString) {
        switch (targetString) {
            case "-global", "-g" -> {
                return General.TargetUUID.GLOBAL_UUID;
            }
            case "-temporary", "-t" -> {
                return General.TargetUUID.TEMPORARY_UUID;
            }
            case "-me", "-m" -> {
                if (context.getSource().getEntity() instanceof ServerPlayer player) {
                    return player.getUUID();
                }
                else {
                    return null;
                }
            }
            case "-random", "-r" -> {
                if (ServerLifecycleHooks.getCurrentServer() != null) {
                    List<ServerPlayer> playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();
                    if (playerList.isEmpty()) {
                        return null;
                    }

                    return playerList.get(new Random().nextInt(playerList.size())).getUUID();
                }
                return null;
            }
            case "-nearest", "-n" -> {
                if (context.getSource().getPlayer() != null) {
                    return context.getSource().getPlayer().getUUID();
                }
                else {
                    try {
                        EntitySelectorParser parser = new EntitySelectorParser(new StringReader("@p"), true);
                        EntitySelector selector = parser.parse();

                        Entity entity = selector.findSingleEntity(context.getSource());

                        if (entity instanceof ServerPlayer player) {
                            return player.getUUID();
                        }
                        else {
                            return null;
                        }
                    }
                    catch (CommandSyntaxException e) {
                        return null;
                    }
                }
            }
            case null -> {
                return null;
            }
            default -> {
                CommandSourceStack source = context.getSource();
                for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
                    if (player.getName().getString().equals(targetString)) {
                        return player.getUUID();
                    }
                }
                return null;
            }
        }
    }

    public static ServerPlayer resolveTargetPlayer(UUID targetUUID) {
        if (General.TargetUUID.GLOBAL_UUID.equals(targetUUID) || General.TargetUUID.TEMPORARY_UUID.equals(targetUUID)) {
            return null;
        }
        else {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                return server.getPlayerList().getPlayer(targetUUID);
            }
            else {
                return null;
            }
        }
    }

    public static String resolveTargetMasterName(UUID masterId) {
        if (General.TargetUUID.GLOBAL_UUID.equals(masterId)) {
            return "-global";
        }
        else if (General.TargetUUID.TEMPORARY_UUID.equals(masterId)) {
            return "-temporary";
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ServerPlayer player = server.getPlayerList().getPlayer(masterId);
            if (player != null) {
                return player.getName().getString();
            }
        }

        // If no pair target exist, return this.
        return "-not_found";
    }

    public enum CallbackField {
        attribute,
    }

    public static String resolveFullCallbackId(String callbackId) {
        /*
        * Resolve callback id as 3 parts: [function name]:[name space]:[callback name].
        * If id provided is not completed form, replace missing part as "custom".
        * You are advised to use your MOD_ID for name space; otherwise, same name callback id may be registered.
        */
        String[] callbackIdParts = callbackId.split(":", 3);
        if (callbackIdParts.length == 3) {
            String field = callbackIdParts[0];
            for (CallbackField f : CallbackField.values()) {
                if (f.name().equalsIgnoreCase(field)) {
                    return callbackId;
                }
            }
            return "custom:" + callbackIdParts[1] + ":" + callbackIdParts[2];
        }
        else if (callbackIdParts.length == 2) {
            String field = callbackIdParts[0];
            for (CallbackField f : CallbackField.values()) {
                if (f.name().equalsIgnoreCase(field)) {
                    return callbackIdParts[0] + ":custom:" + callbackIdParts[1];
                }
            }
            return "custom:" + callbackIdParts[0] + ":" + callbackIdParts[1];
        }
        else if (callbackIdParts.length == 1) {
            return "custom:custom:" + callbackIdParts[0];
        }
        else {
            return "custom:custom:unknown";
        }
    }

    public static String resolveTargetPlayerName(CommandContext<CommandSourceStack> context, String playerId) {
        if (!playerId.startsWith("-")) {
            return playerId;
        }

        String result = null;

        switch (playerId) {
            case "-me", "-m" -> {
                if (context.getSource().getPlayer() == null) {
                    MessagePublisher.sendFailureMessage(context, errorComponent(Error.GeneralError.invalidMeFieldUsed));
                    break;
                }
                result = context.getSource().getPlayer().getName().getString();
            }
            case "-random", "-r" -> {
                if (ServerLifecycleHooks.getCurrentServer() != null) {
                    List<ServerPlayer> playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();
                    if (playerList.isEmpty()) {
                       break;
                    }

                    result = playerList.get(new Random().nextInt(playerList.size())).getName().getString();
                }
            }
            case "-nearest", "-n" -> {
                if (context.getSource().getPlayer() != null) {
                    result = context.getSource().getPlayer().getName().getString();
                }
                else {
                    try {
                        EntitySelectorParser parser = new EntitySelectorParser(new StringReader("@p"), true);
                        EntitySelector selector = parser.parse();

                        Entity entity = selector.findSingleEntity(context.getSource());

                        if (entity instanceof ServerPlayer player) {
                            result = player.getName().getString();
                        }
                    }
                    catch (CommandSyntaxException ignored) {}
                }
            }
        }

        return result;
    }
}
