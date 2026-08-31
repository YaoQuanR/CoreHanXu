package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.PermissionHolder;
import core.yaoquan.hanxu.api.define.Error;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.MessagePublisher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ExecuteConfirmation {
    public static int executeLicense_Agree(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof Player player) {
            if (!PermissionHolder.Storage.getLicenseState(player)) {
                MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_agree").withColor(General.Color.CONTENT));
                player.getPersistentData()
                    .putBoolean("core.yaoquan.hanxu.agreed_license", true);
            }
            else {
                MessagePublisher.sendFailureMessage(context, core.yaoquan.hanxu.api.define.Error.errorComponent(core.yaoquan.hanxu.api.define.Error.GeneralError.licenseAlreadyAgreed));
            }
        }
        else {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.notPlayer));
        }
        return 1;
    }
}
