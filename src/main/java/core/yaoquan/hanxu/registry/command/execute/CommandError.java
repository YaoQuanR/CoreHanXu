package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.util.tool.MessagePublisher;
import net.minecraft.commands.CommandSourceStack;

import static core.yaoquan.hanxu.api.define.Error.*;

public class CommandError {
    static void displayTimerErrorResult(CommandContext<CommandSourceStack> context, String error) {
        switch (error) {
            case "masterNotExist" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(TimerError.masterNotExist));
            case "timerNotExist" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(TimerError.notExist));
            case "timerTimedOut" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(TimerError.timerTimedOut));
            case "playerOffline" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(GeneralError.playerOffline));
            default ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(GeneralError.unexpected));
        }
    }

    static void displayVariableErrorResult(CommandContext<CommandSourceStack> context, String error) {
        switch (error) {
            case "duplicated" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(VariableError.duplicated));
            case "invalidCasting" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(VariableError.invalidCasting));
            case "notExist" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(VariableError.notExist));
            case "serverOffline" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(GeneralError.serverOffline));
            case "unknownScoreObjective" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(GeneralError.unknownScoreObjective));
            case "invalidType" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(VariableError.invalidType));
            case "invalidScoreCasting" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(VariableError.invalidScoreCasting));
            case "mismatchType" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(VariableError.mismatchType));
            default ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(GeneralError.unexpected));
        }
    }

    static void displayWeatherErrorResult(CommandContext<CommandSourceStack> context, String error) {
        switch (error) {
            case "inUse" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(WeatherError.inUse));
            case "notFound" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(WeatherError.notFound));
            case "yamlNotFound" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(WeatherError.yamlNotFound));
            case "notInitialized" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(WeatherError.notInitialized));
            case "undefinedCategory" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(GeneralError.undefinedOperationCategory));
            default ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(GeneralError.unexpected));
        }
    }
}
