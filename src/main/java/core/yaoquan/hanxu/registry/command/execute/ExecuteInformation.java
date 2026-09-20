package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.*;
import core.yaoquan.hanxu.api.define.FilePath;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.tool.MessagePublisher;
import core.yaoquan.hanxu.util.type.NullableValue;
import core.yaoquan.hanxu.util.tool.Resolver;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static core.yaoquan.hanxu.api.define.Error.*;

public class ExecuteInformation {
    public static int executeLicense_State(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof Player player) {
            boolean state = PermissionHolder.Storage.getLicenseState(player);
            MessagePublisher.sendSystemMessage(context, Component.literal("[HX] " + state).withColor(General.Color.TITLE));
            return 1;
        }
        return 0;
    }

    public static int executeAdvancedLicense_State(CommandContext<CommandSourceStack> context) {
        String playerId = StringArgumentType.getString(context, "player_id");
        UUID playerUUID = Resolver.resolveTargetUUID(context, playerId);
        if (playerUUID == null) {
            MessagePublisher.sendFailureMessage(context, core.yaoquan.hanxu.api.define.Error.errorComponent(core.yaoquan.hanxu.api.define.Error.GeneralError.targetNotExist));
            return 0;
        }
        else {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) {
                return 0;
            }
            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
            if (player == null) {
                MessagePublisher.sendFailureMessage(context, errorComponent(GeneralError.targetNotExist));
                return 0;
            }
            boolean state = PermissionHolder.Storage.getLicenseState(player);
            MessagePublisher.sendSystemMessage(context, Component.literal("[HX] " + state).withColor(General.Color.TITLE));
            return 1;
        }
    }

    public static int executeTimer_Template_Read(CommandContext<CommandSourceStack> context) {
        // Receive arguments.
        String timerId = StringArgumentType.getString(context, "timer_id");
        String infoCategory = StringArgumentType.getString(context, "category");
        String timeUnit;

        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        return CommandMisc.displayCommandTimerRead(context, timerId, "", timeUnit, infoCategory, "template");
    }

    public static int executeTimer_Template_List(CommandContext<CommandSourceStack> context) {
        String[] templateIds = TimeHolder.getAllTemplateIds();

        return CommandMisc.displayTimerIdList(context, templateIds);
    }

    public static int executeTimer_Instance_Read(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");
        String infoCategory = StringArgumentType.getString(context, "category");
        String timeUnit;

        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        return CommandMisc.displayCommandTimerRead(context, timerId, masterString, timeUnit, infoCategory, "instance");
    }

    public static int executeTimer_Instance_List(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");

        UUID masterId = Resolver.resolveTargetUUID(context, masterString);

        if (masterId == null) {
            MessagePublisher.sendFailureMessage(context, errorComponent(GeneralError.targetNotExist));
            return 0;
        }

        String[] instanceIds = TimeHolder.getAllInstanceIds(masterId);

        return CommandMisc.displayTimerIdList(context, instanceIds);
    }

    public static int executeScene_List(CommandContext<CommandSourceStack> context) {
        List<Component> displayList = new ArrayList<>();

        // Scan global path.
        Path globalPath = FilePath.getGlobalPath().resolve("scene");
        if (Files.isDirectory(globalPath)) {
            try (Stream<Path> stream = Files.list(globalPath)) {
                stream.filter(p -> p.toString().endsWith(".yaml")).forEach(p -> {
                    displayList.add(Component.literal("(global): " + p.getFileName().toString()).withColor(General.Color.CONTENT));
                });
            }
            catch (IOException ignored) {}
        }

        // Scan world path.
        Path worldPath = FilePath.getWorldPath();
        if (worldPath != null) {
            Path worldScenePath = worldPath.resolve("scene");
            if (Files.isDirectory(worldScenePath)) {
                try (Stream<Path> stream = Files.list(worldScenePath)) {
                    stream.filter(p -> p.toString().endsWith(".yaml")).forEach(p -> {
                        displayList.add(Component.literal("(world): " + p.getFileName().toString()).withColor(General.Color.CONTENT));
                    });
                }
                catch (IOException ignored) {}
            }
        }

        // Then list out.
        if (displayList.isEmpty()) {
            MessagePublisher.sendFailureMessage(context, errorComponent(SceneError.notFound));
        }
        else {
            CommandMisc.displaySceneIdList(context, displayList);
        }

        return 1;
    }

    public static int executeAttribute_List(CommandContext<CommandSourceStack> context) {
        Map<String, AttributeHolder.CustomAttribute> commandAttributes = AttributeHolder.getCommandAttributes();
        String[] commandAttributeList = commandAttributes.keySet().toArray(new String[0]);

        return CommandMisc.displayAttributeIdList(context, commandAttributeList, false);
    }

    public static int executeAdvancedAttribute_List(CommandContext<CommandSourceStack> context) {
        Map<String, AttributeHolder.CustomAttribute> apiAttributes = AttributeHolder.getApiAttributes();
        Map<String, AttributeHolder.CustomAttribute> commandAttributes = AttributeHolder.getCommandAttributes();

        String[] commandAttributeList = commandAttributes.keySet().toArray(new String[0]);
        String[] apiAttributeList = apiAttributes.keySet().toArray(new String[0]);

        int returnValue1 = CommandMisc.displayAttributeIdList(context, commandAttributeList, false);
        int returnValue2 = CommandMisc.displayAttributeIdList(context, apiAttributeList, true);

        if (returnValue1 == 1 || returnValue2 == 1) {
            return 1;
        }
        else {
            return 0;
        }
    }

    public static int executeAttribute_Read(CommandContext<CommandSourceStack> context, String category) {
        String attributeId = StringArgumentType.getString(context, "attribute_id");

        if (!AttributeHolder.doesAttributeExist(attributeId)) {
            MessagePublisher.sendFailureMessage(context, errorComponent(AttributeError.notFound));
            return 0;
        }

        boolean isApiAttribute;
        AttributeHolder.CustomAttribute attribute;

        if (AttributeHolder.doesAttributeExist(attributeId, "command")) {
            isApiAttribute = false;
        }
        else if (AttributeHolder.doesAttributeExist(attributeId, "yaml")) {
            isApiAttribute = false;
        }
        else if (AttributeHolder.doesAttributeExist(attributeId, "api")) {
            isApiAttribute = true;
        }
        else {
            MessagePublisher.sendFailureMessage(context, errorComponent(GeneralError.undefinedOperationId));
            return 0;
        }

        NullableValue<AttributeHolder.CustomAttribute> nullableAttribute;
        nullableAttribute = isApiAttribute?
                AttributeHolder.getApiAttribute(attributeId) :
                AttributeHolder.getCommandAttribute(attributeId);

        if (nullableAttribute.isNull()) {
            MessagePublisher.sendFailureMessage(context, errorComponent(AttributeError.notFound));
            return 0;
        }

        attribute = nullableAttribute.get();

        switch (category) {
            case "threshold_all" -> {
                Map<Float, String> thresholds = attribute.getThresholdCallbacks();

                if (thresholds.isEmpty()) {
                    MessagePublisher.sendFailureMessage(context, errorComponent(AttributeError.noThreshold));
                    return 0;
                }

                MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_threshold_found").withColor(General.Color.CONTENT));
                for (Map.Entry<Float, String> threshold : thresholds.entrySet()) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.literal(threshold.getKey().toString() + " -> " + threshold.getValue())
                                .withColor(General.Color.CONTENT)
                    );
                }
            }
            case "threshold_specific" -> {
                Map<Float, String> thresholds = attribute.getThresholdCallbacks();

                if (thresholds.isEmpty()) {
                    MessagePublisher.sendFailureMessage(context, errorComponent(AttributeError.noThreshold));
                    return 0;
                }

                float value = FloatArgumentType.getFloat(context, "threshold_value");

                if (!thresholds.containsKey(value)) {
                    MessagePublisher.sendFailureMessage(context, errorComponent(AttributeError.noSpecificThreshold));
                    return 0;
                }

                MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_threshold_found").withColor(General.Color.CONTENT));
                MessagePublisher.sendSystemMessage(context, Component.literal(value + " -> " + thresholds.get(value)));
            }
            case "zero" -> {
                String zeroId = attribute.getZeroCallbackId();
                if (zeroId == null) {
                    MessagePublisher.sendFailureMessage(context, errorComponent(AttributeError.noZero));
                    return 0;
                }

                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.attribute_zero")
                                .append(Component.literal(" " + zeroId))
                                .withColor(General.Color.CONTENT)
                );
            }
            case "recovery" -> {
                String recoveryId = attribute.getRecoveryCurveId();
                if (recoveryId == null) {
                    MessagePublisher.sendFailureMessage(context, errorComponent(AttributeError.noRecovery));
                    return 0;
                }

                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.attribute_recovery")
                                .append(Component.literal(" " + recoveryId))
                                .withColor(General.Color.CONTENT)
                );
            }
            case "maximum" -> {
                float maximum = attribute.getMaximum();
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.attribute_maximum")
                                .append(Component.literal(" " + maximum))
                                .withColor(General.Color.CONTENT)
                );
            }
            case "recovery_interval" -> {
                int interval = attribute.getRecoveryIntervalTicks();
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.attribute_recovery_interval")
                                .append(Component.literal(" " + interval))
                                .withColor(General.Color.CONTENT)
                );
            }
            case "value" -> {
                String playerId;
                try {
                    playerId = StringArgumentType.getString(context, "player_id");
                }
                catch (IllegalArgumentException e) {
                    if (context.getSource().getPlayer() != null) {
                        playerId = context.getSource().getPlayer().getName().toString();
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, errorComponent(AttributeError.noIdFieldProvidedByNonPlayer));
                        return 0;
                    }
                }

                if ((playerId.equals("-me") || playerId.equals("-m")) && context.getSource().getPlayer() == null) {
                    MessagePublisher.sendFailureMessage(context, errorComponent(GeneralError.invalidMeFieldUsed));
                    return 0;
                }

                UUID masterId = Resolver.resolveTargetUUID(context, playerId);

                float value = AttributeHolder.getValue(masterId, attributeId, isApiAttribute);
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.attribute_value")
                                .append(" " + value + " (" + playerId + ")")
                                .withColor(General.Color.CONTENT)
                );
            }
            case "group" -> {
                if (isApiAttribute) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.attribute_read_group")
                                    .append(" " + attributeId + " -> api")
                                    .withColor(General.Color.CONTENT)
                    );
                }
                else {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.attribute_read_group")
                                    .append(" " + attributeId + " -> command (yaml)")
                                    .withColor(General.Color.CONTENT)
                    );
                }
            }
            default -> {
                MessagePublisher.sendFailureMessage(context, errorComponent(GeneralError.undefinedOperationId));
                return 0;
            }
        }

        return 1;
    }

    public static int executeVariable_List(CommandContext<CommandSourceStack> context, String category) {
        if (category.equals("variables")) {
            Set<String> variableNames = VariableHolder.getAllRegisteredVariables();

            if (variableNames.isEmpty()) {
                MessagePublisher.sendFailureMessage(context, errorComponent(VariableError.emptyVariable));
                return 0;
            }

            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_list_title").withColor(General.Color.TITLE));
            for (String variableName : variableNames) {
                MessagePublisher.sendSystemMessage(context, Component.literal(variableName).withColor(General.Color.CONTENT));
            }
        }
        else if (category.equals("values")) {
            Map<String, String> allVariables = VariableHolder.getAllVariableAsString();
            if (allVariables.isEmpty()) {
                MessagePublisher.sendFailureMessage(context, errorComponent(VariableError.emptyVariable));
                return 0;
            }

            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_list_title").withColor(General.Color.TITLE));
            for (String variableName : allVariables.keySet()) {
                MessagePublisher.sendSystemMessage(context, Component.literal(variableName + " -> " + allVariables.get(variableName)).withColor(General.Color.CONTENT));
            }
        }

        return 1;
    }

    public static int executeVariable_Read(CommandContext<CommandSourceStack> context) {
        String variableName = StringArgumentType.getString(context, "variable_name");

        if (!VariableHolder.doesExists(variableName)) {
            MessagePublisher.sendFailureMessage(context, errorComponent(VariableError.notExist));
            return 0;
        }

        String variableType = VariableHolder.getType(variableName).getOrElse("?");
        String variableValue = VariableHolder.getStringFrom(variableName).getOrElse("?");

        MessagePublisher.sendSystemMessage(context,
            Component.translatable("commands.chx.variable_read")
                .append(" " + variableType + " " + variableName + ": " + variableValue)
                .withColor(General.Color.CONTENT)
        );

        return 1;
    }

    public static int executeLoot_List(CommandContext<CommandSourceStack> context) {
        Set<String> tableIds = LootHolder.getRegisteredTableIds();

        if (tableIds.isEmpty()) {
            MessagePublisher.sendFailureMessage(context, errorComponent(LootError.emptyTable));
            return 0;
        }

        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_list_title").withColor(General.Color.TITLE));

        for (String tableId : tableIds) {
            MessagePublisher.sendSystemMessage(context, Component.literal(tableId).withColor(General.Color.CONTENT));
        }

        return 1;
    }

    public static int executeLoot_Read(CommandContext<CommandSourceStack> context) {
        String tableId = StringArgumentType.getString(context, "table_id");

        List<Component> lines = LootHolder.readLootTable(tableId);

        if (lines.size() < 2) {
            MessagePublisher.sendSystemMessage(context, lines.getFirst());
            return 0;
        }

        for (Component line : lines) {
            MessagePublisher.sendSystemMessage(context, line);
        }

        return 1;
    }

    public static int executeWeather_List(CommandContext<CommandSourceStack> context) {
        Map<String, WeatherHolder.WeatherDefinition> apiDefinitions = WeatherHolder.getApiWeatherDefinitions();
        Map<String, WeatherHolder.WeatherDefinition> commandDefinitions = WeatherHolder.getCommandWeatherDefinitions();

        String[] apiWeatherList = apiDefinitions.keySet().toArray(new String[0]);
        String[] commandWeatherList = commandDefinitions.keySet().toArray(new String[0]);

        return CommandMisc.displayWeatherIdList(context, commandWeatherList, apiWeatherList);
    }

    public static int executeWeather_Read(CommandContext<CommandSourceStack> context) {
        String weatherId = StringArgumentType.getString(context, "weather_id");

        NullableValue<ServerLevel> nullableLevel = Resolver.resolveServerLevel(context);
        if (nullableLevel.isNull()) {
            return 0;
        }
        ServerLevel level = nullableLevel.get();

        List<Component> lines = WeatherHolder.readWeatherInstance(level, weatherId);

        for (Component line : lines) {
            MessagePublisher.sendSystemMessage(context, line);
        }

        return 1;
    }
}
