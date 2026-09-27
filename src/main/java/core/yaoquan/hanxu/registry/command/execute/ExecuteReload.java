package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.tool.MessagePublisher;
import core.yaoquan.hanxu.util.type.MethodResult;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ExecuteReload {
    public static int executeWeather_Reload(CommandContext<CommandSourceStack> context) {
        WeatherHolder.registerAllYamlWeathers();
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_reloaded").withColor(General.Color.CONTENT));

        MethodResult result = WeatherHolder.pickupUnclaimedStates();

        return result.matching(
                () -> {
                    MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_pickup").withColor(General.Color.CONTENT));
                    return 1;
                },
                (error, info) -> {
                    CommandError.displayWeatherErrorResult(context, error);
                    return 0;
                }
        );
    }
}
