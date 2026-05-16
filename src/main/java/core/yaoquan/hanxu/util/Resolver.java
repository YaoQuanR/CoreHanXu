package core.yaoquan.hanxu.util;

import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.TimeHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class Resolver {
    public static UUID resolveTargetUUID(CommandContext<CommandSourceStack> context, String targetString) {
        if ("-global".equals(targetString) || "-g".equals(targetString)) {
            return TimeHolder.GLOBAL_UUID;
        }
        else if ("-temporary".equals(targetString) || "-t".equals(targetString)) {
            return TimeHolder.TEMPORARY_UUID;
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
}
