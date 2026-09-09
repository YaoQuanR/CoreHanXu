package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.AttributeHolder;
import core.yaoquan.hanxu.api.TimeHolder;
import core.yaoquan.hanxu.api.VariableHolder;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.api.custom.BehaviorRegistry;
import core.yaoquan.hanxu.api.define.Error;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.Creator;
import core.yaoquan.hanxu.util.MessagePublisher;
import core.yaoquan.hanxu.util.NullableValue;
import core.yaoquan.hanxu.util.Resolver;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.UUID;

import static core.yaoquan.hanxu.api.define.Error.errorComponent;

public class ExecuteModification {
    public static int executeTimer_Instance_Modify(CommandContext<CommandSourceStack> context, String category) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        UUID masterId = Resolver.resolveTargetUUID(context, masterString);

        if (masterId == null) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.targetNotExist));
            return 0;
        }

        int timeAmount = IntegerArgumentType.getInteger(context, "time_amount");
        String timeUnit;

        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        switch (timeUnit) {
            case "t", "tick", "s", "second", "m", "minute", "h", "hour":
                TimeHolder.ModifyCategory modifyCategory = category.equals("initial_time")? TimeHolder.ModifyCategory.INITIAL_TIME : TimeHolder.ModifyCategory.REMAINING_TIME;

                if (TimeHolder.modifyInstanceTimer(masterId, timerId, timeAmount, timeUnit, modifyCategory)) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.timer_success_modification")
                                    .append(Component.literal(" (" + timerId + " -> " + masterString + "): " + category + " " + timeAmount + " " + timeUnit))
                                    .withColor(General.Color.TITLE)
                    );
                    return 1;
                }
                else {
                    MessagePublisher.sendFailureMessage(context, errorComponent(Error.TimerError.notExist));
                    return 0;
                }
            default:
                MessagePublisher.sendFailureMessage(context, Component.translatable("commands.core_hanxu.invalid_unit_argument"));
                return 0;
        }
    }

    public static int executeAttribute_Modify(CommandContext<CommandSourceStack> context, String category) {
        String attributeId = StringArgumentType.getString(context, "attribute_id");
        String playerId = StringArgumentType.getString(context, "player_id");
        float value = FloatArgumentType.getFloat(context, "value");
        String direction;

        try {
            direction = StringArgumentType.getString(context, "direction");
        }
        catch (IllegalArgumentException e) {
            direction = "point";
        }

        UUID masterId = Resolver.resolveTargetUUID(context, playerId);

        if (masterId == null && !(playerId.equals("-all") || playerId.equals("-a"))) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.targetNotExist));
            return 0;
        }

        ServerPlayer player = null;
        if (masterId != null) {
            player = context.getSource().getServer().getPlayerList().getPlayer(masterId);
        }

        if (player == null && !(playerId.equals("-all") || playerId.equals("-a"))) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.targetNotExist));
            return 0;
        }

        if (!AttributeHolder.doesAttributeExist(attributeId)) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.AttributeError.notFound));
            return 0;
        }

        boolean isApiAttribute = AttributeHolder.doesAttributeExist(attributeId, "api");

        AttributeHolder.ThresholdDirection thresholdDirection;
        boolean success = false;

        switch (direction) {
            case "point" -> thresholdDirection = AttributeHolder.ThresholdDirection.POINT;
            case "up" -> thresholdDirection = AttributeHolder.ThresholdDirection.UP;
            case "down" -> thresholdDirection = AttributeHolder.ThresholdDirection.DOWN;
            case "flex" -> thresholdDirection = AttributeHolder.ThresholdDirection.FLEX;
            default -> {
                MessagePublisher.sendFailureMessage(context,
                        Component.translatable("commands.chx.attribute_default_direction")
                );
                thresholdDirection = AttributeHolder.ThresholdDirection.POINT;
                direction = "point";
            }
        }

        switch (category) {
            case "set" -> {
                if (playerId.equals("-all") || playerId.equals("-a")) {
                    for (ServerPlayer serverPlayer : context.getSource().getServer().getPlayerList().getPlayers()) {
                        success = AttributeHolder.setValue(serverPlayer.getUUID(), attributeId, value, isApiAttribute, thresholdDirection);
                        if (!success) {
                            break;
                        }
                    }
                }
                else {
                    success = AttributeHolder.setValue(masterId, attributeId, value, isApiAttribute, thresholdDirection);
                }
            }
            case "add" -> {
                if (value == 0) {
                    break;
                }

                if (playerId.equals("-all") || playerId.equals("-a")) {
                    for (ServerPlayer serverPlayer : context.getSource().getServer().getPlayerList().getPlayers()) {
                        success = AttributeHolder.addValue(serverPlayer.getUUID(), attributeId, value, isApiAttribute, thresholdDirection);
                        if (!success) {
                            break;
                        }
                    }
                }
                else {
                    success = AttributeHolder.addValue(masterId, attributeId, value, isApiAttribute, thresholdDirection);
                }
            }
            case "reduce" -> {
                if (value == 0) {
                    break;
                }
                else if (value < 0) {
                    value = -value;
                }

                if (playerId.equals("-all")) {
                    for (ServerPlayer serverPlayer : context.getSource().getServer().getPlayerList().getPlayers()) {
                        success = AttributeHolder.reduceValue(serverPlayer.getUUID(), attributeId, value, isApiAttribute, thresholdDirection);
                        if (!success) {
                            break;
                        }
                    }
                }
                else {
                    success = AttributeHolder.reduceValue(masterId, attributeId, value, isApiAttribute, thresholdDirection);
                }
            }
        }

        if (value == 0) {
            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.attribute_no_changes")
                            .withColor(General.Color.CONTENT)
            );
            return 1;
        }

        if (success) {
            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.attribute_modified")
                            .withColor(General.Color.CONTENT)
            );
            MessagePublisher.sendSystemMessage(context,
                    Component.literal("(" + attributeId + " -> " + playerId + "): " + category + " " + value + " (" + direction + ")")
                            .withColor(General.Color.CONTENT)
            );
            return 1;
        }
        else {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.AttributeError.notFound));
            return 0;
        }
    }

    public static int executeAttribute_Recovery(CommandContext<CommandSourceStack> context, String category) {
        String attributeId = StringArgumentType.getString(context, "attribute_id");
        String finalCallbackId;

        if (!AttributeHolder.doesAttributeExist(attributeId)) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.AttributeError.notFound));
            return 0;
        }

        boolean isApiAttribute = AttributeHolder.doesAttributeExist(attributeId, "api");

        NullableValue<AttributeHolder.CustomAttribute> nullableAttribute = AttributeHolder.getAttributeDefinition(attributeId, isApiAttribute);
        if (nullableAttribute.isNull()) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.AttributeError.notFound));
            return 0;
        }

        AttributeHolder.CustomAttribute attribute = nullableAttribute.get();

        if (category.equals("simple")) {
            if (attribute.doesRecoveryRegistered() && isApiAttribute) {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.AttributeError.tryToOverrideApiRecovery));
                return 0;
            }

            int interval = IntegerArgumentType.getInteger(context, "interval");
            String intervalUnit = StringArgumentType.getString(context, "interval_unit");
            float value = FloatArgumentType.getFloat(context, "value");
            String direction;
            try {
                direction = StringArgumentType.getString(context, "direction");
            }
            catch (IllegalArgumentException e) {
                direction = "point";
            }

            AttributeHolder.ThresholdDirection thresholdDirection;
            switch (direction) {
                case "up" -> thresholdDirection = AttributeHolder.ThresholdDirection.UP;
                case "down" -> thresholdDirection = AttributeHolder.ThresholdDirection.DOWN;
                case "flex" -> thresholdDirection = AttributeHolder.ThresholdDirection.FLEX;
                default -> thresholdDirection = AttributeHolder.ThresholdDirection.POINT;
            }

            String callbackId = "attribute:core_hanxu-command:" + attributeId + "-recovery";

            BehaviorRegistry.register(callbackId, (player, parameters) -> {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                if (server != null) {
                    for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                        if (serverPlayer != null) {
                            AttributeHolder.addValue(serverPlayer.getUUID(), attributeId, value, isApiAttribute, thresholdDirection);
                        }
                    }
                }
            });

            switch (intervalUnit) {
                case "t", "tick" -> {}
                case "s", "second" -> interval *= 20;
                case "m", "minute" -> interval *= (20 * 60);
                case "h", "hour" -> interval *= (20 * 3600);
                default -> {
                    MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.invalidUnitArgument));
                    return 0;
                }
            }

            attribute.setRecovery(callbackId).setRecoveryIntervalTicks(interval);

            if (!isApiAttribute) {
                AttributeHolder.saveYamlAttribute(
                    attributeId,
                    AttributeHolder.UpdateCategory.recovery,
                    0f,
                    callbackId,
                    "simple",
                    interval + ":" + value + ":" + direction
                );
            }

            finalCallbackId = callbackId;
        }
        else if (category.equals("api")) {
            String callbackId = StringArgumentType.getString(context, "callback_id");

            if (!BehaviorRegistry.isRegistered(callbackId)) {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.AttributeError.tryToRegisterUnExistApiRecovery));
                return 0;
            }

            attribute.setRecovery(callbackId);

            if (!isApiAttribute) {
                AttributeHolder.saveYamlAttribute(
                    attributeId,
                    AttributeHolder.UpdateCategory.recovery,
                    0f,
                    callbackId,
                    "api",
                    null
                );
            }

            finalCallbackId = callbackId;
        }
        else {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.undefinedOperationCategory));
            return 0;
        }

        // Output message.
        MessagePublisher.sendSystemMessage(context,
                Component.translatable("commands.chx.attribute_recovery_registered")
                        .append(Component.literal(" " + attributeId + " <<- " + finalCallbackId))
                        .withColor(General.Color.CONTENT)
        );

        return 1;
    }

    public static int executeAttribute_Define(CommandContext<CommandSourceStack> context, String category) {
        String attributeId = StringArgumentType.getString(context, "attribute_id");
        float threshold = FloatArgumentType.getFloat(context, "threshold");
        String content = null, callbackId = null;
        String finalCallbackId;

        if (category.equals("remind") || category.equals("execute")) {
            try {
                content = StringArgumentType.getString(context, "content");
            }
            catch (IllegalArgumentException ignored) {}
        }
        else if (category.equals("api")) {
            try {
                callbackId = StringArgumentType.getString(context, "callback_id");
            }
            catch (IllegalArgumentException e) {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.missingIdField));
                return 0;
            }
        }

        NullableValue<AttributeHolder.CustomAttribute> nullableAttribute = AttributeHolder.getCommandAttribute(attributeId);
        if (nullableAttribute.isNull()) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.AttributeError.notFound));
            return 0;
        }

        AttributeHolder.CustomAttribute attribute = nullableAttribute.get();

        switch (category) {
            case "remind", "execute" -> {
                String fullCallbackId = "attribute:core_hanxu-command:" + attributeId + "-" + threshold;
                attribute.onThreshold(threshold, fullCallbackId);
                Creator.registerCallback(fullCallbackId, category, content);
                // Update YAML document.
                AttributeHolder.saveYamlAttribute(
                        attributeId,
                        AttributeHolder.UpdateCategory.normalThreshold,
                        threshold,
                        fullCallbackId,
                        category,
                        content
                );

                finalCallbackId = fullCallbackId;
            }
            case "api" -> {
                attribute.onThreshold(threshold, callbackId);
                Creator.registerCallback(callbackId, category, content);
                AttributeHolder.saveYamlAttribute(
                        attributeId,
                        AttributeHolder.UpdateCategory.normalThreshold,
                        threshold,
                        callbackId,
                        category,
                        content
                );

                finalCallbackId = callbackId;
            }
            default -> {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.undefinedOperationId));
                return 0;
            }
        }

        // Output message.
        MessagePublisher.sendSystemMessage(
                context, Component.translatable("commands.chx.attribute_threshold_modified")
                        .append(Component.literal(" " + attributeId + " -> " + finalCallbackId + " (" + threshold + ") -> " + category))
                        .withColor(General.Color.SUCCESS)
        );

        return 1;
    }

    public static int executeVariable_Modify(CommandContext<CommandSourceStack> context) {
        String variableName = StringArgumentType.getString(context, "variable_name");
        String category = StringArgumentType.getString(context, "category");
        String newValue = StringArgumentType.getString(context, "new_value");

        if (!VariableHolder.doesExists(variableName)) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.VariableError.notExist));
            return 0;
        }

        NullableValue<String> nullableType = VariableHolder.getType(variableName);
        if (nullableType.isNull()) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.VariableError.invalidType));
            return 0;
        }

        try {
            switch (category) {
                case "set" -> VariableHolder.modifyVariable(variableName, newValue);
                case "add" -> VariableHolder.addNumber(variableName, newValue);
                case "reduce" -> VariableHolder.reduceNumber(variableName, newValue);
                case "same" -> {
                    if (newValue.equals("-self") || newValue.equals("-s")) {
                        newValue = variableName;
                    }
                    VariableHolder.toSameValue(variableName, newValue);
                }
                default -> {
                    MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.undefinedOperationCategory));
                    return 0;
                }
            }
        }
        catch (NullPointerException e) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.VariableError.notExist));
            return 0;
        }
        catch (NumberFormatException e) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.VariableError.invalidType));
            return 0;
        }

        MessagePublisher.sendSystemMessage(context,
                Component.translatable("commands.chx.variable_modified")
                        .append(" " + variableName + " " + category + " " + newValue + " = " + VariableHolder.getStringFrom(variableName).getOrElse("?"))
                        .withColor(General.Color.CONTENT)
        );

        return 1;
    }

    public static int executeVariable_Copy(CommandContext<CommandSourceStack> context, boolean copyFromScore) {
        String variableName = StringArgumentType.getString(context, "variable_name");
        String playerId = StringArgumentType.getString(context, "player_id");
        String scoreName = StringArgumentType.getString(context, "score_name");

        if (!VariableHolder.doesExists(variableName)) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.VariableError.notExist));
            return 0;
        }

        ServerPlayer player;

        if (playerId.equals("-me") || playerId.equals("-m")) {
            player = context.getSource().getPlayer();
        }
        else {
            player = context.getSource().getServer().getPlayerList().getPlayerByName(playerId);
        }

        if (player == null) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.targetNotExist));
            return 0;
        }

        if (copyFromScore) {
            try {
                VariableHolder.copyVariableFromScore(variableName, player.getScoreboardName(), scoreName);
            }
            catch (NumberFormatException e) {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.VariableError.invalidType));
                return 0;
            }
            catch (NullPointerException | IllegalArgumentException e) {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.targetNotExist));
                return 0;
            }
            catch (IllegalStateException e) {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.unexpected));
                return 0;
            }

            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.variable_copy_from_score")
                            .append(" " + variableName + " <- " + scoreName)
                            .withColor(General.Color.SUCCESS)
            );
        }
        else {
            try {
                VariableHolder.copyScoreFromVariable(variableName, player.getScoreboardName(), scoreName);
            }
            catch (NumberFormatException e) {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.VariableError.invalidType));
                return 0;
            }
            catch (NullPointerException | IllegalArgumentException e) {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.targetNotExist));
                return 0;
            }
            catch (IllegalStateException e) {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.unexpected));
                return 0;
            }

            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.variable_copy_to_score")
                            .append(" " + variableName + " -> " + scoreName)
                            .withColor(General.Color.SUCCESS)
            );
        }

        return 1;
    }

    public static int executeVariable_String(CommandContext<CommandSourceStack> context, String category) {
        String variableName = StringArgumentType.getString(context, "variable_name");

        try {
            switch (category) {
                case "to_lower" -> VariableHolder.stringToLowerCase(variableName);
                case "to_upper" -> VariableHolder.stringToUpperCase(variableName);
                default -> {
                    MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.undefinedOperationCategory));
                    return 0;
                }
            }
        }
        catch (NullPointerException e) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.VariableError.notExist));
            return 0;
        }

        MessagePublisher.sendSystemMessage(context,
                Component.translatable("commands.chx.variable_case")
                        .append(Component.literal(" " + VariableHolder.getStringFrom(variableName).getOrElse("?")))
                        .withColor(General.Color.CONTENT)
        );

        return 1;
    }

    public static int executeWeather_Modify(CommandContext<CommandSourceStack> context, String category) {
        String weatherId = StringArgumentType.getString(context, "weather_id");
        int timeAmount = IntegerArgumentType.getInteger(context, "time_amount");

        NullableValue<ServerLevel> nullableLevel = CommandMisc.findServerLevel(context);
        if (nullableLevel.isNull()) {
            return 0;
        }
        ServerLevel level = nullableLevel.get();

        boolean success = switch (category) {
            case "initial" -> WeatherHolder.modifyWeatherTime(level, weatherId, timeAmount, WeatherHolder.ModifyType.INITIAL);
            case "remaining" -> WeatherHolder.modifyWeatherTime(level, weatherId, timeAmount, WeatherHolder.ModifyType.REMAINING);
            case "duration" -> WeatherHolder.modifyWeatherTime(level, weatherId, timeAmount, WeatherHolder.ModifyType.DURATION);
            case "stillness" -> WeatherHolder.modifyWeatherTime(level, weatherId, timeAmount, WeatherHolder.ModifyType.STILLNESS);
            default -> false;
        };

        if (success) {
            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.weather_time_modified")
                            .append(" (" + level + " -> " + weatherId + "): " + category + " -> " + timeAmount)
                            .withColor(General.Color.CONTENT)
            );
        }
        else {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.WeatherError.notFound));
        }

        return success? 1 : 0;
    }
}
