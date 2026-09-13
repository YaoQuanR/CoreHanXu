package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.MessagePublisher;
import core.yaoquan.hanxu.util.NullableValue;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import static core.yaoquan.hanxu.api.define.Error.*;

public class ExecuteStatus {
    public static int executeTimer_Instance_Reset(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        return CommandMisc.commandOperateInstanceTimer(context, timerId, masterString, "reset");
    }

    public static int executeTimer_Instance_Restart(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        return CommandMisc.commandOperateInstanceTimer(context, timerId, masterString, "restart");
    }

    public static int executeWeather_Restart(CommandContext<CommandSourceStack> context) {
        String weatherId = StringArgumentType.getString(context, "weather_id");

        NullableValue<ServerLevel> nullableLevel = CommandMisc.findServerLevel(context);
        if (nullableLevel.isNull()) {
            return 0;
        }
        ServerLevel level = nullableLevel.get();

        if (!WeatherHolder.doesWeatherDefinitionExist(weatherId)) {
            MessagePublisher.sendFailureMessage(context, core.yaoquan.hanxu.api.define.Error.errorComponent(core.yaoquan.hanxu.api.define.Error.WeatherError.notFound));
            return 0;
        }

        boolean success = WeatherHolder.restartWeather(level, weatherId);

        if (success) {
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_restarted").withColor(General.Color.SUCCESS));
        }
        else {
            MessagePublisher.sendFailureMessage(context, errorComponent(WeatherError.alreadyActivated));
        }

        return success? 1 : 0;
    }

    public static int executeWeather_Ready(CommandContext<CommandSourceStack> context) {
        String weatherId = StringArgumentType.getString(context, "weather_id");

        NullableValue<ServerLevel> nullableLevel = CommandMisc.findServerLevel(context);
        if (nullableLevel.isNull()) {
            return 0;
        }
        ServerLevel level = nullableLevel.get();

        if (!WeatherHolder.doesWeatherDefinitionExist(weatherId)) {
            MessagePublisher.sendFailureMessage(context, errorComponent(WeatherError.notFound));
            return 0;
        }

        boolean success = WeatherHolder.prepareWeather(level, weatherId);

        if (success) {
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_ready").withColor(General.Color.SUCCESS));
        }
        else {
            MessagePublisher.sendFailureMessage(context, errorComponent(WeatherError.alreadyActivated));
        }

        return success? 1 : 0;
    }

    public static int executeWeather_Kill(CommandContext<CommandSourceStack> context) {
        String weatherId = StringArgumentType.getString(context, "weather_id");

        NullableValue<ServerLevel> nullableLevel = CommandMisc.findServerLevel(context);
        if (nullableLevel.isNull()) {
            return 0;
        }
        ServerLevel level = nullableLevel.get();

        boolean success = WeatherHolder.killWeather(level, weatherId);

        if (success) {
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_killed").withColor(General.Color.SUCCESS));
        }
        else {
            MessagePublisher.sendFailureMessage(context, errorComponent(WeatherError.onReadyOrNotFound));
        }

        return success? 1 : 0;
    }
}
