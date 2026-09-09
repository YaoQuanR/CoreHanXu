package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.*;
import core.yaoquan.hanxu.api.define.Error;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static core.yaoquan.hanxu.api.define.Error.errorComponent;

public class ExecuteCreate {
    public static int executeTimer_Instance_Apply(CommandContext<CommandSourceStack> context) {
        // Receive arguments.
        String templateTimerId = StringArgumentType.getString(context, "template_timer_id");
        String applyTarget = StringArgumentType.getString(context, "apply_target");

        // Analysis to UUID.
        UUID targetUUID = Resolver.resolveTargetUUID(context, applyTarget);

        String displayTarget;
        if (Objects.equals(applyTarget, "0")) {
            displayTarget = "Global";
        }
        else if (Objects.equals(applyTarget, "1")) {
            displayTarget = "Temporary";
        }
        else {
            displayTarget = applyTarget;
        }

        // Determine if target exist.
        if (targetUUID == null) {
            MessagePublisher.sendFailureMessage(context,
                Error.errorComponent(Error.GeneralError.targetNotExist)
            );
            return 0;
        }

        // Determine if template timer exist and if instance timer exist, then register (Copy).
        if (TimeHolder.createInstanceFromTemplate(targetUUID, templateTimerId)) {
            MessagePublisher.sendSystemMessage(context,
                Component.translatable("commands.chx.timer_instantiated")
                    .append(Component.literal(" " + templateTimerId + " -> " + displayTarget))
                    .withColor(General.Color.SUCCESS)
            );
        }
        else {
            MessagePublisher.sendFailureMessage(context, errorComponent(Error.TimerError.notExistOrAlreadyInstantiated));
            return 0;
        }

        return 1;
    }

    public static int executeTimer_Template_Create(CommandContext<CommandSourceStack> context, String titleParameter) {
        // Receive arguments.
        String timerId = StringArgumentType.getString(context, "timer_id");
        int timeAmount = IntegerArgumentType.getInteger(context, "time_amount");
        String timeUnit;
        String contentParameter;

        // If no unit, receive time data as ticks.
        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        try {
            contentParameter = StringArgumentType.getString(context, "behavior_content");
        }
        catch (IllegalArgumentException e) {
            contentParameter = null;
        }

        // Check if timer exist.
        if (TimeHolder.getRemainingTimeFromTemplate(timerId, timeUnit).isPresent()) {
            MessagePublisher.sendFailureMessage(context, Component.translatable("commands.chx.timer_already_exist"));
            return 0;
        }

        return CommandMisc.commandCreateTemplateTimer(context, timerId, timeUnit, timeAmount, titleParameter, contentParameter);
    }

    public static int executeTimer_Template_CreateRange(CommandContext<CommandSourceStack> context, String titleParameter) {
        // Receive arguments.
        String timerId = StringArgumentType.getString(context, "timer_id");
        int timeFirstRange = IntegerArgumentType.getInteger(context, "time_first_range");
        int timeSecondRange = IntegerArgumentType.getInteger(context, "time_second_range");
        String timeUnit;
        String contentParameter;

        // If no unit, receive time data as ticks.
        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        try {
            contentParameter = StringArgumentType.getString(context, "behavior_content");
        }
        catch (IllegalArgumentException e) {
            contentParameter = null;
        }

        int selectedTimeAmount = Converter.convertFromRangeToRandom(timeFirstRange, timeSecondRange);

        // Check if timer exist.
        if (TimeHolder.getRemainingTimeFromTemplate(timerId, timeUnit).isPresent()) {
            MessagePublisher.sendFailureMessage(context, Component.translatable("commands.chx.timer_already_exist"));
            return 0;
        }

        return CommandMisc.commandCreateTemplateTimer(context, timerId, timeUnit, selectedTimeAmount, titleParameter, contentParameter);
    }

    public static int executeTimer_Instance_Create(CommandContext<CommandSourceStack> context, String titleParameter) {
        // Receive arguments.
        String timerId = StringArgumentType.getString(context, "timer_id");
        String masterString = StringArgumentType.getString(context, "master_id");
        int timeAmount = IntegerArgumentType.getInteger(context, "time_amount");
        String timeUnit;
        String contentParameter;

        // Receive optional arguments.
        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        try {
            contentParameter = StringArgumentType.getString(context, "behavior_content");
        }
        catch (IllegalArgumentException e) {
            contentParameter = null;
        }

        int returnValue = CommandMisc.commandCreateInstanceTimer(context, timerId, masterString, timeUnit, timeAmount, titleParameter, contentParameter);

        if (returnValue == 1) {
            CommandMisc.displayTimerCreateMessage(context, timerId, timeAmount, timeUnit, titleParameter, contentParameter);
            return 1;
        }
        else {
            return 0;
        }
    }

    public static int executeTimer_Instance_CreateRange(CommandContext<CommandSourceStack> context, String titleParameter) {
        // Receive arguments.
        String timerId = StringArgumentType.getString(context, "timer_id");
        String masterString = StringArgumentType.getString(context, "master_id");
        int timeFirstRange = IntegerArgumentType.getInteger(context, "time_first_range");
        int timeSecondRange = IntegerArgumentType.getInteger(context, "time_second_range");
        String timeUnit;
        String contentParameter;

        // If no unit, receive time data as ticks.
        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        try {
            contentParameter = StringArgumentType.getString(context, "behavior_content");
        }
        catch (IllegalArgumentException e) {
            contentParameter = null;
        }

        int selectedTimeAmount = Converter.convertFromRangeToRandom(timeFirstRange, timeSecondRange);

        int returnValue = CommandMisc.commandCreateInstanceTimer(context, timerId, masterString, timeUnit, selectedTimeAmount, titleParameter, contentParameter);

        if (returnValue == 1) {
            CommandMisc.displayTimerCreateMessage(context, timerId, selectedTimeAmount, timeUnit, titleParameter, contentParameter);
            return 1;
        }
        else {
            return 0;
        }
    }

    public static int executeScene_Create(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            MessagePublisher.sendFailureMessage(context, core.yaoquan.hanxu.api.define.Error.errorComponent(core.yaoquan.hanxu.api.define.Error.GeneralError.notPlayer));
            return 0;
        }

        String toPath = StringArgumentType.getString(context, "to_path");

        if (!toPath.equals("world") && !toPath.equals("global")) {
            MessagePublisher.sendFailureMessage(context, core.yaoquan.hanxu.api.define.Error.errorComponent(core.yaoquan.hanxu.api.define.Error.GeneralError.undefinedSavePath));
            return 0;
        }

        // Then read book from player's main hand.
        ItemStack book = player.getMainHandItem();
        if (book.isEmpty() || (!book.is(Items.WRITABLE_BOOK) && !book.is(Items.WRITTEN_BOOK))) {
            MessagePublisher.sendFailureMessage(context, core.yaoquan.hanxu.api.define.Error.errorComponent(core.yaoquan.hanxu.api.define.Error.GeneralError.mainHandItemNotTarget));
            return 0;
        }

        /*
        * Then verify if valid submission.
        * 1. Does content empty?
        * 2. Does field "id" existed?
        * 3. Does same name scene found?
        * 4. Does field "type" and "dialogs" existed?
        * Then try to submit.
        */

        String yamlContent = YamlReader.read(book);
        if (yamlContent == null || yamlContent.isEmpty()) {
            MessagePublisher.sendFailureMessage(context, core.yaoquan.hanxu.api.define.Error.errorComponent(core.yaoquan.hanxu.api.define.Error.GeneralError.noContentFound));
            return 0;
        }

        String sceneId = YamlReader.readSpecificField(yamlContent, "id");
        if (sceneId.isEmpty()) {
            MessagePublisher.sendFailureMessage(context, core.yaoquan.hanxu.api.define.Error.errorComponent(core.yaoquan.hanxu.api.define.Error.GeneralError.missingIdField));
            return 0;
        }

        YamlReader.TargetPath targetPath = toPath.equals("world")? YamlReader.TargetPath.TO_WORLD : YamlReader.TargetPath.TO_GLOBAL;

        if (SceneHolder.doesSceneExist(sceneId, targetPath)) {
            MessagePublisher.sendFailureMessage(context, core.yaoquan.hanxu.api.define.Error.errorComponent(core.yaoquan.hanxu.api.define.Error.SceneError.alreadyExist));
            return 0;
        }

        if (!yamlContent.contains("type:") || !yamlContent.contains("dialogs:")) {
            MessagePublisher.sendFailureMessage(context, core.yaoquan.hanxu.api.define.Error.errorComponent(core.yaoquan.hanxu.api.define.Error.GeneralError.uncompletedContent));
            return 0;
        }

        try {
            // Prase data to map for storage.
            Map<String, Object> yamlMap = YamlReader.stringToMap(yamlContent);
            YamlReader.save("scene", sceneId, yamlMap, targetPath);
            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.scene_created")
                            .append(" " + sceneId + " -> " + targetPath)
                            .withColor(General.Color.SUCCESS)
            );
        }
        catch (Exception e) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.SceneError.failedToSave));
            return 0;
        }

        return 1;
    }

    public static int executeAttribute_Create(CommandContext<CommandSourceStack> context) {
        String attributeId = StringArgumentType.getString(context, "attribute_id");
        String toPath = StringArgumentType.getString(context, "to_path");
        YamlReader.TargetPath targetPath;
        float maximum, defaultValue;
        try {
            maximum = FloatArgumentType.getFloat(context, "maximum");
        }
        catch (Exception e) {
            maximum = 100.0f;
        }
        try {
            defaultValue = FloatArgumentType.getFloat(context, "default_value");
        }
        catch (Exception e) {
            defaultValue = 0.0f;
        }

        targetPath = toPath.equals("global")? YamlReader.TargetPath.TO_GLOBAL : YamlReader.TargetPath.TO_WORLD;

        boolean registered = AttributeHolder.register(attributeId, maximum, defaultValue, targetPath);

        CommandMisc.displayAttributeCreateMessage(context, attributeId, maximum, defaultValue, registered);

        if (registered) {
            AttributeHolder.registerAllYamlAttributes();
            return 1;
        }
        else {
            return 0;
        }
    }

    public static int executeVariable_Create(CommandContext<CommandSourceStack> context, boolean override) {
        String variableType = StringArgumentType.getString(context, "variable_type");
        String variableName = StringArgumentType.getString(context, "variable_name");
        String variableValue = StringArgumentType.getString(context, "variable_value");

        if (VariableHolder.doesExists(variableName) && !override) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.VariableError.alreadyExist));
            return 0;
        }

        if (override) {
            NullableValue<String> nullableType = VariableHolder.getType(variableName);
            if (nullableType.isNull()) {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.VariableError.invalidType));
                return 0;
            }

            String actualType = nullableType.get();

            if (!actualType.equals(variableType)) {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.VariableError.invalidType));
                return 0;
            }
        }

        if (variableName.startsWith("-") || variableName.startsWith("@")) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.invalidFieldForName));
            return 0;
        }

        if (VariableHolder.createVariable(variableName, variableType, variableValue, override)) {
            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.variable_created")
                            .append(" " + variableType + " " + variableName + " <<- " + variableValue)
                            .withColor(General.Color.SUCCESS)
            );
        }
        else {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.VariableError.invalidType));
            return 0;
        }

        return 1;
    }

    public static int executeLoot_Create(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.notPlayer));
            return 0;
        }

        String toPath = StringArgumentType.getString(context, "to_path");
        if (!toPath.equals("world") && !toPath.equals("global")) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.undefinedSavePath));
            return 0;
        }

        ItemStack book = player.getMainHandItem();
        if (book.isEmpty() || (!book.is(Items.WRITABLE_BOOK) && !book.is(Items.WRITTEN_BOOK))) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.mainHandItemNotTarget));
            return 0;
        }

        String yamlContent = YamlReader.read(book);
        if (yamlContent == null || yamlContent.isEmpty()) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.noContentFound));
            return 0;
        }

        String tableId = YamlReader.readSpecificField(yamlContent, "id");
        if (tableId.isEmpty()) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.missingIdField));
            return 0;
        }

        YamlReader.TargetPath targetPath = toPath.equals("world")? YamlReader.TargetPath.TO_WORLD : YamlReader.TargetPath.TO_GLOBAL;

        if (LootHolder.doesFileLootTableExists(tableId, targetPath)) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.LootError.alreadyExist));
            return 0;
        }

        if (!yamlContent.contains("pools:")) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.uncompletedContent));
            return 0;
        }

        try {
            Map<String, Object> yamlMap = YamlReader.stringToMap(yamlContent);
            YamlReader.save("loot", tableId, yamlMap, targetPath);
            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.loot_created")
                            .append(" " + tableId + " -> " + targetPath)
                            .withColor(General.Color.SUCCESS)
            );
        }
        catch (Exception e) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.LootError.failedToSave));
            return 0;
        }

        return 1;
    }

    public static int executeWeather_Create(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.notPlayer));
            return 0;
        }

        String toPath = StringArgumentType.getString(context, "to_path");
        if (!toPath.equals("world") && !toPath.equals("global")) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.undefinedSavePath));
            return 0;
        }

        ItemStack book = player.getMainHandItem();
        if (book.isEmpty() || (!book.is(Items.WRITABLE_BOOK) && !book.is(Items.WRITTEN_BOOK))) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.mainHandItemNotTarget));
            return 0;
        }

        String yamlContent = YamlReader.read(book);
        if (yamlContent == null || yamlContent.isEmpty()) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.noContentFound));
            return 0;
        }

        String weatherId = YamlReader.readSpecificField(yamlContent, "id");
        if (weatherId.isEmpty()) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.missingIdField));
            return 0;
        }

        YamlReader.TargetPath targetPath = toPath.equals("world")? YamlReader.TargetPath.TO_WORLD : YamlReader.TargetPath.TO_GLOBAL;

        if (WeatherHolder.doesWeatherExist(weatherId)) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.WeatherError.alreadyExist));
            return 0;
        }

        if (!yamlContent.contains("type:")) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.uncompletedContent));
            return 0;
        }

        try {
            Map<String, Object> yamlMap = YamlReader.stringToMap(yamlContent);
            YamlReader.save("weather", weatherId, yamlMap, targetPath);
            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.weather_created")
                            .append(" " + weatherId + " -> " + targetPath)
                            .withColor(General.Color.SUCCESS)
            );

            WeatherHolder.registerYamlWeather(weatherId);
        }
        catch (Exception e) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.WeatherError.failedToSave));
            return 0;
        }

        return 1;
    }
}
