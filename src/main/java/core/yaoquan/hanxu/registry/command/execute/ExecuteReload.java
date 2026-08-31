package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.MessagePublisher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ExecuteReload {
    public static int executeWeather_Reload(CommandContext<CommandSourceStack> context) {
        WeatherHolder.registerAllYamlWeathers();
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_reloaded").withColor(General.Color.CONTENT));

        if (WeatherHolder.pickupUnclaimedStates()) {
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_pickup").withColor(General.Color.CONTENT));
        }
        else {
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_non_unclaims").withColor(General.Color.CONTENT));
        }

        return 1;
    }
}
