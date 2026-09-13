package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.VariableHolder;
import core.yaoquan.hanxu.api.define.Error;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.Exceptionable;
import core.yaoquan.hanxu.util.MessagePublisher;
import core.yaoquan.hanxu.util.Resolver;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;

import static core.yaoquan.hanxu.api.define.Error.*;

public class ExecuteCondition {
    public static int executeVariable_If_Value(CommandContext<CommandSourceStack> context, String category) {
        String variableName = StringArgumentType.getString(context, "variable_name");
        String compareSign = StringArgumentType.getString(context, "compare_sign");
        String compareValue = StringArgumentType.getString(context, "compare_value");

        if (!VariableHolder.doesExists(variableName)) {
            MessagePublisher.sendFailureMessage(context, errorComponent(Error.VariableError.notExist));
            return 0;
        }

        Exceptionable<Boolean> result;
        switch (compareSign) {
            // [Existing value (Variable)] {Sign} [Compare value].
            case "=", "==" -> result = VariableHolder.doesEquals(variableName, compareValue);
            case "!=", "≠" -> result = VariableHolder.doesDifference(variableName, compareValue);
            case ">" -> result = VariableHolder.doesVariableGreater(variableName, compareValue, false);
            case ">=", "≥" -> result = VariableHolder.doesVariableAtLeast(variableName, compareValue);
            case "<" -> result = VariableHolder.doesVariableSmaller(variableName, compareValue, false);
            case "<=", "≤" -> result = VariableHolder.doesVariableAtMost(variableName, compareValue);
            case "instanceof" -> result = VariableHolder.doesInstanceof(variableName, compareValue);
            case "contains" -> result = VariableHolder.doesContains(variableName, compareValue);
            case "length" -> {
                int length;
                try {
                    length = Integer.parseInt(compareValue);
                }
                catch (NumberFormatException e) {
                    result = Exceptionable.exception("invalidCasting");
                    break;
                }
                result = VariableHolder.doesLengthEquals(variableName, length);
            }
            case "starts_with" -> result = VariableHolder.doesStartsWith(variableName, compareValue);
            case "ends_with" -> result = VariableHolder.doesEndsWith(variableName, compareValue);
            default -> {
                MessagePublisher.sendFailureMessage(context, errorComponent(Error.GeneralError.undefinedOperationCategory));
                return 0;
            }
        }

        boolean pass;
        if (result.isExcept()) {
            switch (result.getError()) {
                case "notExist" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(Error.VariableError.notExist));
                case "invalidType" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(Error.VariableError.invalidType));
                case "invalidCasting" ->
                    MessagePublisher.sendFailureMessage(context, errorComponent(Error.VariableError.invalidCasting));
            }
            return 0;
        }

        pass = result.getUsual();

        if (!pass) {
            // Failed to pass comparison.
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_failed_comparison").withColor(General.Color.CONTENT));
            return 1;
        }

        return CommandMisc.commandVariableExecution(context, variableName, category);
    }

    public static int executeVariable_If_Score(CommandContext<CommandSourceStack> context, String category) {
        String playerId = StringArgumentType.getString(context, "player_id");
        String scoreName = StringArgumentType.getString(context, "score_name");
        String compareSign = StringArgumentType.getString(context, "compare_sign");
        int compareValue = IntegerArgumentType.getInteger(context, "compare_value");

        MinecraftServer server = context.getSource().getServer();
        ServerScoreboard scoreboard = server.getScoreboard();
        Objective objective = scoreboard.getObjective(scoreName);
        if (objective == null) {
            MessagePublisher.sendFailureMessage(context, errorComponent(Error.GeneralError.unexpected));
            return 0;
        }

        // Resolve special cases.
        playerId = Resolver.resolveTargetPlayerName(context, playerId);

        int scoreValue;

        ScoreHolder scoreHolder = ScoreHolder.forNameOnly(playerId);
        scoreValue = scoreboard.getOrCreatePlayerScore(scoreHolder, objective).get();

        boolean success;
        try {
            switch (compareSign) {
                case "=", "==" -> success = scoreValue == compareValue;
                case "!=", "≠" -> success = scoreValue != compareValue;
                case ">" -> success = scoreValue > compareValue;
                case ">=", "≥" -> success = scoreValue >= compareValue;
                case "<" -> success = scoreValue < compareValue;
                case "<=", "≤" -> success = scoreValue <= compareValue;
                case "instanceof" -> success = false;
                case "contains" -> success = String.valueOf(scoreValue).contains(String.valueOf(compareValue));
                case "length" -> success = String.valueOf(scoreValue).length() == compareValue;
                case "starts_with" -> success = String.valueOf(scoreValue).startsWith(String.valueOf(compareValue));
                case "ends_with" -> success = String.valueOf(scoreValue).endsWith(String.valueOf(compareValue));
                default -> {
                    MessagePublisher.sendFailureMessage(context, errorComponent(Error.GeneralError.undefinedOperationCategory));
                    return 0;
                }
            }
        }
        catch (NullPointerException e) {
            MessagePublisher.sendFailureMessage(context, errorComponent(Error.VariableError.notExist));
            return 0;
        } catch (NumberFormatException e) {
            MessagePublisher.sendFailureMessage(context, errorComponent(Error.VariableError.invalidType));
            return 0;
        }

        if (!success) {
            // Failed to pass comparison.
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_failed_comparison").withColor(General.Color.CONTENT));
            return 1;
        }

        return CommandMisc.commandVariableExecution(context, null, category);
    }

    public static int executeVariable_If_Margin(CommandContext<CommandSourceStack> context, String category) {
        String variableName = StringArgumentType.getString(context, "variable_name");
        String marginValue = StringArgumentType.getString(context, "margin_value");
        String compareValue = StringArgumentType.getString(context, "compare_value");

        if (!VariableHolder.doesExists(variableName)) {
            MessagePublisher.sendFailureMessage(context, errorComponent(Error.VariableError.notExist));
            return 0;
        }

        // This method only accept format: /chx variable margin_equals [variable_name] % [margin_value] {=/==} [compare_value] ...
        Exceptionable<Boolean> result = VariableHolder.doesMarginEquals(variableName, marginValue, compareValue);

        return result.matching(
                success -> {
                    if (success) {
                        return CommandMisc.commandVariableExecution(context, variableName, category);
                    }
                    else {
                        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_failed_comparison").withColor(General.Color.CONTENT));
                        return 1;
                    }
                },
                (error, info) -> {
                    CommandMisc.displayVariableErrorResult(context, error);
                    return 0;
                }
        );
    }
}
