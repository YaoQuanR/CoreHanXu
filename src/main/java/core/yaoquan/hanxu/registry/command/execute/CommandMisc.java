package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.*;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.type.MethodResult;
import core.yaoquan.hanxu.util.type.NullableValue;
import core.yaoquan.hanxu.util.tool.Creator;
import core.yaoquan.hanxu.util.tool.MessagePublisher;
import core.yaoquan.hanxu.util.tool.Resolver;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.function.Consumer;

import static core.yaoquan.hanxu.api.define.Error.*;

public class CommandMisc {
    static int commandCreateTemplateTimer(CommandContext<CommandSourceStack> context,
                                                  String timerId, String timeUnit, int timeAmount,
                                                  String titleParameter, String contentParameter) {
        // Create callback.
        Consumer<ServerPlayer> callback = Creator.createCallback(context, timerId, titleParameter, contentParameter);

        // Then register.
        switch (timeUnit) {
            case "t", "tick", "s", "second", "m", "minute", "h", "hour":
                TimeHolder.createTemplateTimer(timerId, timeAmount, timeUnit, callback, titleParameter, contentParameter, "core_hanxu-command");
                break;
            default:
                MessagePublisher.sendFailureMessage(context, Component.translatable("commands.core_hanxu.invalid_unit_argument"));
                return 0;
        }

        return 1;
    }

    static int commandCreateInstanceTimer(CommandContext<CommandSourceStack> context,
                                                  String timerId, String masterString,
                                                  String timeUnit, int timeAmount,
                                                  String titleParameter, String contentParameter) {
        // Reject invalid "-me" field used by non player source.
        if ((masterString.equals("-me") || masterString.equals("-m")) && context.getSource().getPlayer() == null) {
            MessagePublisher.sendFailureMessage(context,errorComponent(GeneralError.invalidMeFieldUsed));
            return 0;
        }

        // Analysis UUID.
        UUID masterId = Resolver.resolveTargetUUID(context, masterString);

        // Create callback.
        Consumer<ServerPlayer> callback = Creator.createCallback(context, timerId, titleParameter, contentParameter);
        if (callback == null) {
            return 0;
        }


        // Then register if time not yet created.
        switch (timeUnit) {
            case "t", "tick", "s", "second", "m", "minute", "h", "hour" -> {
                // Then register if timer not yet created.
                MethodResult result = TimeHolder.createInstanceTimer(masterId, timerId, timeAmount, timeUnit, callback, titleParameter, contentParameter, "core_hanxu-command");
                return result.matching(
                        () -> 1,
                        (error, info) -> {
                            CommandError.displayTimerErrorResult(context, error);
                            return 0;
                        }
                );
            }
            default -> {
                MessagePublisher.sendFailureMessage(context, Component.translatable("commands.core_hanxu.invalid_unit_argument"));
                return 0;
            }
        }
    }

    static int commandOperateInstanceTimer(CommandContext<CommandSourceStack> context,
                                           String timerId, String masterString,
                                           String categoryOfOperation) {
        // Get UUID.
        UUID masterId = Resolver.resolveTargetUUID(context, masterString);

        // Check if target master existed.
        if (masterId == null) {
            MessagePublisher.sendFailureMessage(context,errorComponent(GeneralError.targetNotExist));
            return 0;
        }

        switch (categoryOfOperation) {
            case "start" -> {
                // Then start.
                MethodResult result = TimeHolder.startInstanceTimer(masterId, timerId);
                if (result.isFailure()) {
                    CommandError.displayTimerErrorResult(context, result.getError());
                    return 0;
                }

                // Send success message.
                NullableValue<Integer> nullableStarting = TimeHolder.getRemainingTimeFromInstance(masterId, timerId, "tick");
                NullableValue<String> nullableTitle = TimeHolder.getInstanceTitleParameter(masterId, timerId);
                if (nullableStarting.isNull() || nullableTitle.isNull()) {
                    MessagePublisher.sendFailureMessage(context, errorComponent(TimerError.notExist));
                    return 0;
                }

                int startingTime = nullableStarting.get();
                String titleParameter = nullableTitle.get();

                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_started").withColor(General.Color.SUCCESS)
                );
                MessagePublisher.sendSystemMessage(context,
                        Component.literal(" (" + timerId + " -> " + masterString + "): " + startingTime + " tick(s) ->> " + titleParameter)
                                .withColor(General.Color.SUCCESS)
                );
            }
            case "stop" -> {
                // Then stop.
                MethodResult result = TimeHolder.stopInstanceTimer(masterId, timerId);
                if (result.isFailure()) {
                    CommandError.displayTimerErrorResult(context, result.getError());
                    return 0;
                }

                // Send success message.
                NullableValue<Integer> nullableRemaining = TimeHolder.getRemainingTimeFromInstance(masterId, timerId, "tick");
                if (nullableRemaining.isNull()) {
                    MessagePublisher.sendFailureMessage(context, errorComponent(TimerError.notExist));
                    return 0;
                }

                int remainingTime = nullableRemaining.get();
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_stopped").withColor(General.Color.SUCCESS)
                );
                MessagePublisher.sendSystemMessage(context,
                        Component.literal(" (" + timerId + " -> " + masterString + "): " + remainingTime + " tick(s)")
                                .withColor(General.Color.SUCCESS)
                );
            }
            case "reset" -> {
                // Then reset.
                MethodResult result = TimeHolder.resetInstanceTimer(masterId, timerId);
                if (result.isFailure()) {
                    CommandError.displayTimerErrorResult(context, result.getError());
                    return 0;
                }

                // Send success message.
                NullableValue<Integer> nullableInitial = TimeHolder.getInitialTimeFromInstance(masterId, timerId, "tick");
                if (nullableInitial.isNull()) {
                    MessagePublisher.sendFailureMessage(context, errorComponent(TimerError.notExist));
                    return 0;
                }

                int initialTime = nullableInitial.get();
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_reset").withColor(General.Color.SUCCESS)
                );
                MessagePublisher.sendSystemMessage(context,
                        Component.literal(" (" + timerId + " -> " + masterString + "): " + initialTime + " tick(s)")
                                .withColor(General.Color.SUCCESS)
                );
            }
            case "restart" -> {
                MethodResult result = TimeHolder.restartInstanceTimer(masterId, timerId);
                if (result.isFailure()) {
                    CommandError.displayTimerErrorResult(context, result.getError());
                    return 0;
                }

                // Send success message.
                NullableValue<Integer> nullableRestart = TimeHolder.getInitialTimeFromInstance(masterId, timerId, "tick");
                NullableValue<String> nullableTitle = TimeHolder.getInstanceTitleParameter(masterId, timerId);
                if (nullableRestart.isNull() || nullableTitle.isNull()) {
                    MessagePublisher.sendFailureMessage(context, errorComponent(TimerError.notExist));
                    return 0;
                }

                int restartTime = nullableRestart.get();
                String restartTitleParameter = nullableTitle.get();

                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_restart").withColor(General.Color.SUCCESS)
                );
                MessagePublisher.sendSystemMessage(context,
                        Component.literal("(" + timerId + " -> " + masterString + "): " + restartTime + " tick(s) ->> " + restartTitleParameter)
                                .withColor(General.Color.SUCCESS)
                );
            }
            case "delete" -> {
                // Then delete.
                MethodResult result = TimeHolder.deleteInstanceTimer(masterId, timerId);
                if (result.isFailure()) {
                    CommandError.displayTimerErrorResult(context, result.getError());
                    return 0;
                }

                // Send success message.
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_deleted")
                                .withColor(General.Color.SUCCESS)
                );
                MessagePublisher.sendSystemMessage(context,
                        Component.literal(" (" + timerId + " -> " + masterString + ")")
                                .withColor(General.Color.SUCCESS)
                );
            }
            default -> {
                MessagePublisher.sendFailureMessage(context,errorComponent(GeneralError.undefinedOperationCategory));
                return 0;
            }
        }

        // Refresh state of F4 display.
        if (context.getSource().getPlayer() != null) {
            TimeHolder.checkAndRefreshDisplay(context.getSource().getPlayer(), masterId, timerId);
        }

        return 1;
    }

    static int commandVariableExecution(CommandContext<CommandSourceStack> context, String variableName, String category) {
        // If passed.
        if (category.equals("execute")) {
            String command = StringArgumentType.getString(context, "command");
            if (!command.startsWith("/")) {
                command = "/" + command;
            }
            context.getSource().getServer().getCommands().performPrefixedCommand(context.getSource(), command);
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_executed").withColor(General.Color.CONTENT));
        }
        else if (category.equals("then")) {
            String targetVariableName = StringArgumentType.getString(context, "target_variable");
            String action = StringArgumentType.getString(context, "action");
            String target = StringArgumentType.getString(context, "target");
            String targetPlayerId = null;
            boolean getPlayerByContext = false;
            List<String> specialCases = List.of("-me", "-m", "-random", "-r", "-nearest", "-n");
            try {
                targetPlayerId = StringArgumentType.getString(context, "optional_player_id");

                if (specialCases.contains(targetPlayerId)) {
                    getPlayerByContext = true;
                }
            }
            catch (Exception e) {
                getPlayerByContext = true;
            }

            if (getPlayerByContext) {
                if (targetPlayerId == null) {
                    MessagePublisher.sendFailureMessage(context,errorComponent(GeneralError.missingIdField));
                    return 0;
                }
                targetPlayerId = Resolver.resolveTargetPlayerName(context, targetPlayerId);
                if (targetPlayerId == null) {
                    MessagePublisher.sendFailureMessage(context,errorComponent(GeneralError.targetNotExist));
                    return 0;
                }
            }

            // For special case.
            if (targetVariableName.equals("-self") || targetVariableName.equals("-s")) {
                if (variableName == null) {
                    MessagePublisher.sendFailureMessage(context,errorComponent(VariableError.selfFieldInScoreIf));
                    return 0;
                }
                targetVariableName = variableName;
            }

            if (!VariableHolder.doesExists(targetVariableName)) {
                MessagePublisher.sendFailureMessage(context,errorComponent(GeneralError.targetNotExist));
                return 0;
            }

            MethodResult result;
            switch (action) {
                case "set" -> result = VariableHolder.modifyVariable(targetVariableName, target);
                case "add" -> result = VariableHolder.addNumber(targetVariableName, target);
                case "reduce" -> result = VariableHolder.reduceNumber(targetVariableName, target);
                case "copy_from" -> result = VariableHolder.copyVariableFromScore(targetVariableName, targetPlayerId, target);
                case "copy_to" -> result = VariableHolder.copyScoreFromVariable(targetVariableName, targetPlayerId, target);
                case "mask" -> result = VariableHolder.maskVariableValue(targetVariableName, target);
                default -> result = MethodResult.failure("undefinedOperation");
            }

            if (result.isFailure()) {
                switch (result.getError()) {
                    case "notExist" ->
                        MessagePublisher.sendFailureMessage(context,errorComponent(VariableError.notExist));
                    case "invalidType" ->
                        MessagePublisher.sendFailureMessage(context,errorComponent(VariableError.invalidType));
                    case "invalidCasting" ->
                        MessagePublisher.sendFailureMessage(context,errorComponent(VariableError.invalidCasting));
                    case "invalidScoreCasting" ->
                        MessagePublisher.sendFailureMessage(context,errorComponent(VariableError.invalidScoreCasting));
                    case "mismatchType" ->
                        MessagePublisher.sendFailureMessage(context,errorComponent(VariableError.mismatchType));
                    case "undefinedOperation" ->
                        MessagePublisher.sendFailureMessage(context,errorComponent(GeneralError.undefinedOperationCategory));
                }
                return 0;
            }

            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_target_finished").withColor(General.Color.CONTENT));
        }

        return 1;
    }

    static int displayCommandTimerRead(CommandContext<CommandSourceStack> context,
                                       String timerId, String masterString, String timeUnit,
                                       String infoCategory, String timerCategory) {
        if (timerCategory.equals("template")) {
            switch (infoCategory) {
                case "remaining_time":
                    NullableValue<Integer> nullableRemaining = TimeHolder.getRemainingTimeFromTemplate(timerId, timeUnit);
                    if (nullableRemaining.isNull()) {
                        MessagePublisher.sendFailureMessage(context, errorComponent(TimerError.notExist));
                        return 0;
                    }

                    int remainingTime = nullableRemaining.get();
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.timer_read_remaining_time")
                                    .append(Component.literal(" (" + timerId + "): " + remainingTime + " " + timeUnit))
                                    .withColor(General.Color.TITLE)
                    );
                    break;
                case "initial_time":
                    NullableValue<Integer> nullableInitial = TimeHolder.getInitialTimeFromTemplate(timerId, timeUnit);
                    if (nullableInitial.isNull()) {
                        MessagePublisher.sendFailureMessage(context, errorComponent(TimerError.notExist));
                        return 0;
                    }

                    int initialTime = nullableInitial.get();
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.timer_read_initial_time")
                                    .append(Component.literal(" (" + timerId + "): " + initialTime + " " + timeUnit))
                                    .withColor(General.Color.TITLE)
                    );
                    break;
                case "state":
                    boolean isCounting = TimeHolder.isTemplateTimerCounting(timerId);

                    if (TimeHolder.getRemainingTimeFromTemplate(timerId, timeUnit).isPresent()) {
                        MessagePublisher.sendSystemMessage(context,
                                Component.translatable("commands.chx.timer_state")
                                        .append(Component.literal(" (" + timerId + "): " + (isCounting? "Counting" : "Stopping")))
                                        .withColor(General.Color.TITLE)
                        );
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, errorComponent(TimerError.notExist));
                        return 0;
                    }
                    break;
                case "end_behavior":
                    NullableValue<String> nullableTitle = TimeHolder.getTemplateTitleParameter(timerId);
                    NullableValue<String> nullableContent = TimeHolder.getTemplateContentParameter(timerId);

                    String titleParameter = nullableTitle.getOrElse("null");
                    String contentParameter = nullableContent.modify(str -> " : " + str).getOrElse("");

                    if (TimeHolder.getRemainingTimeFromTemplate(timerId, timeUnit).isPresent()) {
                        MessagePublisher.sendSystemMessage(context,
                                Component.translatable("commands.chx.timer_end_behavior")
                                        .append(Component.literal("[HX] (" + timerId + ") <<- "))
                                        .append(titleParameter)
                                        .append(contentParameter)
                                        .withColor(General.Color.TITLE)
                        );
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, errorComponent(TimerError.notExist));
                        return 0;
                    }
                    break;
                default:
                    return 0;
            }
            return 1;
        }
        else if (timerCategory.equals("instance")) {
            UUID masterId = Resolver.resolveTargetUUID(context, masterString);

            switch (infoCategory) {
                case "remaining_time":
                    NullableValue<Integer> nullableRemaining = TimeHolder.getRemainingTimeFromInstance(masterId, timerId, "tick");
                    if (nullableRemaining.isNull()) {
                        MessagePublisher.sendFailureMessage(context, errorComponent(TimerError.notExist));
                        return 0;
                    }

                    int remainingTime = nullableRemaining.get();
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.timer_read_remaining_time")
                                    .append(Component.literal(" (" + timerId + " -> " + masterString + "): " + remainingTime + " " + timeUnit))
                                    .withColor(General.Color.TITLE)
                    );

                    break;
                case "initial_time":
                    NullableValue<Integer> nullableInitial = TimeHolder.getInitialTimeFromInstance(masterId, timerId, timeUnit);
                    if (nullableInitial.isNull()) {
                        MessagePublisher.sendFailureMessage(context, errorComponent(TimerError.notExist));
                        return 0;
                    }

                    int initialTime = nullableInitial.get();
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.timer_read_initial_time")
                                    .append(Component.literal(" (" + timerId + "->" + masterString + "): " + initialTime + " " + timeUnit))
                                    .withColor(General.Color.TITLE)
                    );
                    break;
                case "state":
                    boolean isCounting = TimeHolder.isInstanceTimerCounting(masterId, timerId);
                    if (TimeHolder.getRemainingTimeFromInstance(masterId, timerId, timeUnit).isPresent()) {
                        MessagePublisher.sendSystemMessage(context,
                                Component.translatable("commands.chx.timer_state")
                                        .append(Component.literal(" (" + timerId + "): " + (isCounting? "Counting" : "Stopping")))
                                        .withColor(General.Color.TITLE)
                        );
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, errorComponent(TimerError.notExist));
                        return 0;
                    }
                    break;
                case "end_behavior":
                    NullableValue<String> nullableTitle = TimeHolder.getInstanceTitleParameter(masterId, timerId);
                    NullableValue<String> nullableContent = TimeHolder.getInstanceContentParameter(masterId, timerId);
                    if (nullableTitle.isNull() || nullableContent.isNull()) {
                        MessagePublisher.sendFailureMessage(context, errorComponent(TimerError.notExist));
                        return 0;
                    }

                    String titleParameter = nullableTitle.getOrElse("null");
                    String contentParameter = nullableContent.modify(str -> " : " + str).getOrElse("");

                    if (TimeHolder.getRemainingTimeFromInstance(masterId, timerId, timeUnit).isPresent()) {
                        MessagePublisher.sendSystemMessage(context,
                                Component.translatable("commands.chx.timer_end_behavior")
                                        .append(Component.literal("[HX] (" + timerId + " -> " + masterString + ") <<- "))
                                        .append(titleParameter)
                                        .append(contentParameter)
                                        .withColor(General.Color.TITLE)
                        );
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, errorComponent(TimerError.notExist));
                        return 0;
                    }
                    break;
                default:
                    return 0;
            }
            return 1;
        }
        else {
            MessagePublisher.sendFailureMessage(context,errorComponent(GeneralError.undefinedOperationCategory));
            return 0;
        }
    }

    static int displayTimerIdList(CommandContext<CommandSourceStack> context, String[] idList) {
        if (idList.length == 0) {
            MessagePublisher.sendFailureMessage(context, Component.translatable("commands.chx.fixed.empty"));
            return 0;
        }

        MessagePublisher.sendSystemMessage(context,
                Component.translatable("commands.chx.timer_list_title").withColor(General.Color.TITLE)
        );
        for (String id : idList) {
            MessagePublisher.sendSystemMessage(context, Component.literal(id).withColor(General.Color.CONTENT));
        }

        return 1;
    }

    static void displayTimerCreateMessage(CommandContext<CommandSourceStack> context, String timerId, int timeAmount, String timeUnit, String titleParameter, String contentParameter) {
        // Output message.
        MessagePublisher.sendSystemMessage(context,
                Component.translatable("commands.chx.timer_created")
                        .append(Component.literal(" " + timerId + " -> " + timeAmount + " " + timeUnit))
                        .withColor(General.Color.SUCCESS)
        );
        switch (titleParameter) {
            case "e", "execute":
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_with_execute_behavior")
                                .append(Component.literal(": " + contentParameter))
                                .withColor(General.Color.SUCCESS)
                );
                break;
            case "r", "remind":
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_with_remind_behavior")
                                .append(Component.literal(": " + contentParameter))
                                .withColor(General.Color.SUCCESS)
                );
                break;
            default:
                break;
        }
    }

    static void displaySceneIdList(CommandContext<CommandSourceStack> context, List<Component> displayList) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_list_title").withColor(General.Color.TITLE));
        for (Component line : displayList) {
            MessagePublisher.sendSystemMessage(context, line);
        }
    }

    static int displayAttributeIdList(CommandContext<CommandSourceStack> context, String[] attributeArrayList, boolean isApiAttribute) {
        MessagePublisher.sendSystemMessage(
            context,
            isApiAttribute?
                Component.translatable("commands.chx.attribute_api_list_title").withColor(General.Color.TITLE) :
                Component.translatable("commands.chx.attribute_yaml_list_title").withColor(General.Color.TITLE)
            );

        if (attributeArrayList.length == 0) {
            MessagePublisher.sendFailureMessage(
                context, Component.translatable("commands.chx.fixed.empty")
            );
            return 0;
        }

        for (String attribute : attributeArrayList) {
            MessagePublisher.sendSystemMessage(context, Component.literal(attribute).withColor(General.Color.CONTENT));
        }

        return 1;
    }

    static void displayAttributeCreateMessage(CommandContext<CommandSourceStack> context, String attributeId, float maximum, float defaultValue, boolean registered) {
        if (registered) {
            MessagePublisher.sendSystemMessage(
                context,
                Component.translatable("commands.chx.attribute_created")
                        .append(Component.literal(" " + attributeId + " -> " + maximum + " _ " + defaultValue))
                        .withColor(General.Color.SUCCESS)
            );
        }
        else {
            MessagePublisher.sendFailureMessage(context,errorComponent(AttributeError.sameNameFound));
        }
    }

    static int displayWeatherIdList(CommandContext<CommandSourceStack> context, String[] yamlList, String[] apiList) {
        if (yamlList.length == 0 && apiList.length == 0) {
            MessagePublisher.sendFailureMessage(context,errorComponent(WeatherError.emptyWeather));
            return 0;
        }

        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_yaml_list_title").withColor(General.Color.TITLE));

        if (yamlList.length == 0) {
            MessagePublisher.sendFailureMessage(context, Component.translatable("commands.chx.fixed.empty"));
        }
        else {
            for (String id : yamlList) {
                MessagePublisher.sendSystemMessage(context, Component.literal(id).withColor(General.Color.CONTENT));
            }
        }

        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_api_list_title").withColor(General.Color.TITLE));

        if (apiList.length == 0) {
            MessagePublisher.sendFailureMessage(context, Component.translatable("commands.chx.fixed.empty"));
        }
        else {
            for (String id : apiList) {
                MessagePublisher.sendSystemMessage(context, Component.literal(id).withColor(General.Color.CONTENT));
            }
        }

        return 1;
    }
}
