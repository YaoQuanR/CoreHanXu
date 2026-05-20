package core.yaoquan.hanxu.util;

import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.define.General;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.UUID;

public class Resolver {
    public static UUID resolveTargetUUID(CommandContext<CommandSourceStack> context, String targetString) {
        if ("-global".equals(targetString) || "-g".equals(targetString)) {
            return General.GLOBAL_UUID;
        }
        else if ("-temporary".equals(targetString) || "-t".equals(targetString)) {
            return General.TEMPORARY_UUID;
        }
        else if ("-me".equals(targetString) || "-m".equals(targetString)) {
            if (context.getSource().getEntity() instanceof ServerPlayer player) {
                return player.getUUID();
            }
            else {
                return null;
            }
        }

        CommandSourceStack source = context.getSource();
        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            if (player.getName().getString().equals(targetString)) {
                return player.getUUID();
            }
        }

        return null;
    }

    public static ServerPlayer resolveTargetPlayer(UUID targetUUID) {
        if (General.GLOBAL_UUID.equals(targetUUID) || General.TEMPORARY_UUID.equals(targetUUID)) {
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
        if (General.GLOBAL_UUID.equals(masterId)) {
            return "-global";
        }
        else if (General.TEMPORARY_UUID.equals(masterId)) {
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
}
