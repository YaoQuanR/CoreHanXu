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

        NullableValue<ServerLevel> nullableLevel = Resolver.resolveServerLevel(context);
        if (nullableLevel.isNull()) {
            return 0;
        }
        ServerLevel level = nullableLevel.get();

        MethodResult result = WeatherHolder.restartWeather(level, weatherId);

        return result.matching(
                () -> {
                    MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_restarted").withColor(General.Color.SUCCESS));
                    return 1;
                },
                (error, info) -> {
                    CommandError.displayWeatherErrorResult(context, error);
                    return 0;
                }
        );
    }

    public static int executeWeather_Ready(CommandContext<CommandSourceStack> context) {
        String weatherId = StringArgumentType.getString(context, "weather_id");

        NullableValue<ServerLevel> nullableLevel = Resolver.resolveServerLevel(context);
        if (nullableLevel.isNull()) {
            return 0;
        }
        ServerLevel level = nullableLevel.get();

        MethodResult result = WeatherHolder.prepareWeather(level, weatherId);

        return result.matching(
                () -> {
                    MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_ready").withColor(General.Color.SUCCESS));
                    return 1;
                },
                (error, info) -> {
                    CommandError.displayWeatherErrorResult(context, error);
                    return 0;
                }
        );
    }

    public static int executeWeather_Kill(CommandContext<CommandSourceStack> context) {
        String weatherId = StringArgumentType.getString(context, "weather_id");

        NullableValue<ServerLevel> nullableLevel = Resolver.resolveServerLevel(context);
        if (nullableLevel.isNull()) {
            return 0;
        }
        ServerLevel level = nullableLevel.get();

        MethodResult result = WeatherHolder.killWeather(level, weatherId);
        
        return result.matching(
                () -> {
                    MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_killed").withColor(General.Color.SUCCESS));
                    return 1;
                },
                (error, info) -> {
                    CommandError.displayWeatherErrorResult(context, error);
                    return 0;
                }
        );
    }
}
