package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.tool.MessagePublisher;
import core.yaoquan.hanxu.util.tool.Resolver;
import core.yaoquan.hanxu.util.type.MethodResult;
import core.yaoquan.hanxu.util.type.NullableValue;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class ExecuteStop {
    public static int executeTimer_Instance_Stop(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        return CommandMisc.commandOperateInstanceTimer(context, timerId, masterString, "stop");
    }

    public static int executeWeather_PauseId(CommandContext<CommandSourceStack> context) {
        String weatherId = StringArgumentType.getString(context, "weather_id");

        NullableValue<ServerLevel> nullableLevel = Resolver.resolveServerLevel(context);
        if (nullableLevel.isNull()) {
            return 0;
        }
        ServerLevel level = nullableLevel.get();

        MethodResult result = WeatherHolder.pauseWeather(level, weatherId);

        return result.matching(
                () -> {
                    MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_paused").withColor(General.Color.SUCCESS));
                    return 1;
                },
                (error, info) -> {
                    CommandError.displayWeatherErrorResult(context, error);
                    return 0;
                }
        );
    }

    public static int executeWeather_PauseType(CommandContext<CommandSourceStack> context) {
        String weatherType = StringArgumentType.getString(context, "weather_type");

        NullableValue<ServerLevel> nullableLevel = Resolver.resolveServerLevel(context);
        if (nullableLevel.isNull()) {
            return 0;
        }
        ServerLevel level = nullableLevel.get();

        WeatherHolder.WeatherType type = WeatherHolder.parseStringToType(weatherType);

        MethodResult result = WeatherHolder.pauseWeather(level, type);

        return result.matching(
                () -> {
                    MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_type_resumed").withColor(General.Color.SUCCESS));
                    return 1;
                },
                (error, info) -> {
                    CommandError.displayWeatherErrorResult(context, error);
                    return 0;
                }
        );
    }
}
