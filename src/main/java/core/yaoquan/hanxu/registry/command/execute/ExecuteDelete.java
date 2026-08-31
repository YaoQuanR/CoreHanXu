package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.*;
import core.yaoquan.hanxu.api.define.Error;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.JsonReader;
import core.yaoquan.hanxu.util.MessagePublisher;
import core.yaoquan.hanxu.util.YamlReader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static core.yaoquan.hanxu.api.define.Error.errorComponent;

public class ExecuteDelete {
    public static int executeTimer_Template_Delete(CommandContext<CommandSourceStack> context) {
        // Receive argument.
        String timerId = StringArgumentType.getString(context, "timer_id");

        boolean isDeleted = TimeHolder.deleteTemplateTimer(timerId);
        if (isDeleted) {
            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.timer_deleted")
                            .append(Component.literal(" (" + timerId + ")"))
                            .withColor(General.Color.TITLE)
            );
        } else {
            MessagePublisher.sendFailureMessage(context, errorComponent(Error.TimerError.notExist));
        }

        return 1;
    }

    public static int executeTimer_Instance_Delete(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        return CommandUtils.commandOperateInstanceTimer(context, timerId, masterString, "delete");
    }

    public static int executeScene_Delete(CommandContext<CommandSourceStack> context, String specifiedPath) {
        String sceneName = StringArgumentType.getString(context, "scene_name");

        // Check and delete.
        if (specifiedPath.equals("try")) {
            boolean worldSceneExist = SceneHolder.doesSceneExist(sceneName, YamlReader.TargetPath.TO_WORLD);
            boolean globalSceneExist = SceneHolder.doesSceneExist(sceneName, YamlReader.TargetPath.TO_GLOBAL);
            if (worldSceneExist && globalSceneExist) {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.SceneError.sameNameFound));
                return 0;
            }
            else if (worldSceneExist) {
                specifiedPath = "world";
            }
            else if (globalSceneExist) {
                specifiedPath = "global";
            }
            else {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.SceneError.notFound));
                return 0;
            }
        }
        switch (specifiedPath) {
            case "world":
                if (SceneHolder.deleteScene(sceneName, YamlReader.TargetPath.TO_WORLD)) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.scene_deleted")
                                    .withColor(General.Color.CONTENT));
                    break;
                }
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.SceneError.failedToDelete));
                return 0;
            case "global":
                if (SceneHolder.deleteScene(sceneName, YamlReader.TargetPath.TO_GLOBAL)) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.scene_deleted")
                                    .withColor(General.Color.CONTENT));
                    break;
                }
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.SceneError.failedToDelete));
                return 0;
        }

        return 1;
    }

    public static int executeAttribute_Delete(CommandContext<CommandSourceStack> context, String specifiedPath) {
        String attributeId = StringArgumentType.getString(context, "attribute_id");

        if (AttributeHolder.getApiAttributes().containsKey(attributeId)) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.AttributeError.tryToModifyApiTarget));
            return 0;
        }

        if (!AttributeHolder.doesYamlAttributeExist(attributeId)) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.AttributeError.notFound));
            return 0;
        }
        if (!AttributeHolder.doesAttributeExist(attributeId, "command")) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.AttributeError.notFound));
            return 0;
        }

        if (specifiedPath.equals("try")) {
            boolean worldAttributeExist = AttributeHolder.doesYamlAttributeExist(attributeId, YamlReader.TargetPath.TO_WORLD);
            boolean globalAttributeExist = AttributeHolder.doesYamlAttributeExist(attributeId, YamlReader.TargetPath.TO_GLOBAL);

            if (worldAttributeExist && globalAttributeExist) {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.AttributeError.sameNameFound));
                return 0;
            }
            else if (worldAttributeExist) {
                specifiedPath = "world";
            }
            else if (globalAttributeExist) {
                specifiedPath = "global";
            }
        }

        switch (specifiedPath) {
            case "world":
                if (AttributeHolder.unregisterAndDelete(attributeId, YamlReader.TargetPath.TO_WORLD)) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.attribute_deleted")
                                    .withColor(General.Color.CONTENT)
                    );
                    break;
                }
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.AttributeError.failedToDelete));
                return 0;
            case "global":
                if (AttributeHolder.unregisterAndDelete(attributeId, YamlReader.TargetPath.TO_GLOBAL)) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.attribute_deleted")
                                    .withColor(General.Color.CONTENT)
                    );
                    break;
                }
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.AttributeError.failedToDelete));
                return 0;
        }

        return 1;
    }

    public static int executeVariable_Delete(CommandContext<CommandSourceStack> context) {
        String variableName = StringArgumentType.getString(context, "variable_name");

        if (variableName.equals("-all") || variableName.equals("-a")) {
            VariableHolder.deleteAllVariables();
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_all_deleted").withColor(General.Color.SUCCESS));
        }
        else {
            String variableType = VariableHolder.getType(variableName).getOrElse("?");
            if (VariableHolder.deleteVariable(variableName)) {
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.variable_deleted")
                                .append(" " + variableName + " (" +  variableType + ")")
                                .withColor(General.Color.SUCCESS)
                );
            }
            else {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.VariableError.notExist));
                return 0;
            }
        }

        return 1;
    }

    public static int executeLoot_Delete(CommandContext<CommandSourceStack> context, String specifiedPath) {
        String tableId = StringArgumentType.getString(context, "table_id");

        boolean yamlFile = tableId.endsWith(".yaml");

        tableId = tableId.replace(".yaml", "").replace(".json", "");

        // Check and delete.
        if (specifiedPath.equals("try")) {
            boolean worldTableExist, globalTableExist;
            if (yamlFile) {
                worldTableExist = LootHolder.doesFileLootTableExists(tableId, YamlReader.TargetPath.TO_WORLD);
                globalTableExist = LootHolder.doesFileLootTableExists(tableId, YamlReader.TargetPath.TO_GLOBAL);

            }
            else {
                worldTableExist = LootHolder.doesFileLootTableExists(tableId, JsonReader.TargetPath.TO_WORLD);
                globalTableExist = LootHolder.doesFileLootTableExists(tableId, JsonReader.TargetPath.TO_GLOBAL);
            }

            if (worldTableExist && globalTableExist) {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.LootError.sameNameFound));
                return 0;
            }
            else if (worldTableExist) {
                specifiedPath = "world";
            }
            else if (globalTableExist) {
                specifiedPath = "global";
            }
            else {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.LootError.tableNotExist));
                return 0;
            }
        }
        switch (specifiedPath) {
            case "world":
                if (yamlFile? LootHolder.deleteFileLootTable(tableId, YamlReader.TargetPath.TO_WORLD) : LootHolder.deleteFileLootTable(tableId, JsonReader.TargetPath.TO_WORLD)) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.loot_deleted")
                                    .withColor(General.Color.CONTENT)
                    );
                    break;
                }

                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.LootError.failedToDelete));
                return 0;
            case "global":
                if (yamlFile? LootHolder.deleteFileLootTable(tableId, YamlReader.TargetPath.TO_GLOBAL) : LootHolder.deleteFileLootTable(tableId, JsonReader.TargetPath.TO_GLOBAL)) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.loot_deleted")
                                    .withColor(General.Color.CONTENT)
                    );
                    break;
                }
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.LootError.failedToDelete));
                return 0;
        }

        return 1;
    }

    public static int executeWeather_Delete(CommandContext<CommandSourceStack> context, String specificPath) {
        String weatherId = StringArgumentType.getString(context, "weather_id");

        if (WeatherHolder.doesWeatherExist(weatherId, "api")) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.WeatherError.tryToModifyApiTarget));
            return 0;
        }

        if (!WeatherHolder.doesWeatherExist(weatherId)) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.WeatherError.notFound));
            return 0;
        }

        if (specificPath.equals("try")) {
            boolean worldWeatherExist, globalWeatherExist;
            worldWeatherExist = WeatherHolder.doesYamlWeatherExist(weatherId, YamlReader.TargetPath.TO_WORLD);
            globalWeatherExist = WeatherHolder.doesYamlWeatherExist(weatherId, YamlReader.TargetPath.TO_GLOBAL);

            if (worldWeatherExist && globalWeatherExist) {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.WeatherError.sameNameFound));
                return 0;
            }
            else if (worldWeatherExist) {
                specificPath = "world";
            }
            else if (globalWeatherExist) {
                specificPath = "global";
            }
            else {
                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.WeatherError.notFound));
                return 0;
            }
        }

        switch (specificPath) {
            case "world":
                if (WeatherHolder.unregisterAndDelete(weatherId, YamlReader.TargetPath.TO_WORLD)) {
                    MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_deleted").withColor(General.Color.SUCCESS));
                    break;
                }

                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.WeatherError.failedToDelete));
                return 0;
            case "global":
                if (WeatherHolder.unregisterAndDelete(weatherId, YamlReader.TargetPath.TO_GLOBAL)) {
                    MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_deleted").withColor(General.Color.SUCCESS));
                    break;
                }

                MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.WeatherError.failedToDelete));
                return 0;
        }

        return 1;
    }
}
