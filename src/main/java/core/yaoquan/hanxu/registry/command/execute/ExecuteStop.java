package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.api.define.Error;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.MessagePublisher;
import core.yaoquan.hanxu.util.NullableValue;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class ExecuteStop {
    public static int executeTimer_Instance_Stop(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        return CommandUtils.commandOperateInstanceTimer(context, timerId, masterString, "stop");
    }

    public static int executeWeather_PauseId(CommandContext<CommandSourceStack> context) {
        String weatherId = StringArgumentType.getString(context, "weather_id");

        NullableValue<ServerLevel> nullableLevel = CommandUtils.findServerLevel(context);
        if (nullableLevel.isNull()) {
            return 0;
        }
        ServerLevel level = nullableLevel.get();

        boolean success = WeatherHolder.pauseWeather(level, weatherId);

        if (success) {
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_paused").withColor(General.Color.SUCCESS));
        }
        else {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.WeatherError.onReadyOrNotFound));
        }

        return success? 1 : 0;
    }

    public static int executeWeather_PauseType(CommandContext<CommandSourceStack> context) {
        String weatherType = StringArgumentType.getString(context, "weather_type");

        NullableValue<ServerLevel> nullableLevel = CommandUtils.findServerLevel(context);
        if (nullableLevel.isNull()) {
            return 0;
        }
        ServerLevel level = nullableLevel.get();

        WeatherHolder.WeatherType type = WeatherHolder.parseStringToType(weatherType);

        boolean success = WeatherHolder.pauseWeather(level, type);

        if (success) {
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_type_resumed").withColor(General.Color.SUCCESS));
        }
        else {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.WeatherError.onReadyOrNotFound));
        }

        return success? 1 : 0;
    }
}
