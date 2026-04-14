package core.yaoquan.hanxu.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class MessagePublisher {
    public static void sendSystemMessage(CommandContext<CommandSourceStack> context, Component component) {
        context.getSource().sendSystemMessage(component);
    }

    public static void sendFailureMessage(CommandContext<CommandSourceStack> context, Component component) {
        context.getSource().sendFailure(component);
    }
}
