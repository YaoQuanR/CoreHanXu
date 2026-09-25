package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.*;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.tool.JsonReader;
import core.yaoquan.hanxu.util.tool.MessagePublisher;
import core.yaoquan.hanxu.util.type.MethodResult;
import core.yaoquan.hanxu.util.tool.YamlReader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static core.yaoquan.hanxu.api.define.Error.*;

public class ExecuteDelete {
    public static int executeTimer_Template_Delete(CommandContext<CommandSourceStack> context) {
        // Receive argument.
        String timerId = StringArgumentType.getString(context, "timer_id");

        MethodResult result = TimeHolder.deleteTemplateTimer(timerId);
        return result.matching(
                () -> {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.timer_deleted")
                                    .append(Component.literal(" (" + timerId + ")"))
                                    .withColor(General.Color.TITLE)
                    );
                    return 1;
                },
                (error, info) -> {
                    MessagePublisher.sendFailureMessage(context, errorComponent(TimerError.notExist));
                    return 0;
                }
        );
    }

    public static int executeTimer_Instance_Delete(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        return CommandMisc.commandOperateInstanceTimer(context, timerId, masterString, "delete");
    }

    public static int executeScene_Delete(CommandContext<CommandSourceStack> context, String specifiedPath) {
        String sceneName = StringArgumentType.getString(context, "scene_name");

        // Check and delete.
        if (specifiedPath.equals("try")) {
            boolean worldSceneExist = SceneHolder.doesSceneExist(sceneName, YamlReader.TargetPath.TO_WORLD);
            boolean globalSceneExist = SceneHolder.doesSceneExist(sceneName, YamlReader.TargetPath.TO_GLOBAL);
            if (worldSceneExist && globalSceneExist) {
                MessagePublisher.sendFailureMessage(context, errorComponent(SceneError.sameNameFound));
                return 0;
            }
            else if (worldSceneExist) {
                specifiedPath = "world";
            }
            else if (globalSceneExist) {
                specifiedPath = "global";
            }
            else {
                MessagePublisher.sendFailureMessage(context, errorComponent(SceneError.notFound));
                return 0;
            }
        }

        MethodResult result = switch (specifiedPath) {
            case "world" -> SceneHolder.deleteScene(sceneName, YamlReader.TargetPath.TO_WORLD);
            case "global" -> SceneHolder.deleteScene(sceneName, YamlReader.TargetPath.TO_GLOBAL);
            default -> MethodResult.failure("undefinedCategory");
        };

        return result.matching(
                () -> {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.scene_deleted")
                                    .withColor(General.Color.CONTENT));
                    return 1;
                },
                (error, info) -> {
                    CommandError.displaySceneErrorResult(context, error);
                    return 0;
                }
        );
    }

    public static int executeAttribute_Delete(CommandContext<CommandSourceStack> context, String specifiedPath) {
        String attributeId = StringArgumentType.getString(context, "attribute_id");

        if (AttributeHolder.getApiAttributes().containsKey(attributeId)) {
            MessagePublisher.sendFailureMessage(context, errorComponent(AttributeError.tryToModifyApiTarget));
            return 0;
        }

        if (!AttributeHolder.doesYamlAttributeExist(attributeId)) {
            MessagePublisher.sendFailureMessage(context, errorComponent(AttributeError.notFound));
            return 0;
        }
        if (!AttributeHolder.doesAttributeExist(attributeId, "command")) {
            MessagePublisher.sendFailureMessage(context, errorComponent(AttributeError.notFound));
            return 0;
        }

        if (specifiedPath.equals("try")) {
            boolean worldAttributeExist = AttributeHolder.doesYamlAttributeExist(attributeId, YamlReader.TargetPath.TO_WORLD);
            boolean globalAttributeExist = AttributeHolder.doesYamlAttributeExist(attributeId, YamlReader.TargetPath.TO_GLOBAL);

            if (worldAttributeExist && globalAttributeExist) {
                MessagePublisher.sendFailureMessage(context, errorComponent(AttributeError.sameNameFound));
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
            case "world" -> {
                MethodResult result = AttributeHolder.unregisterAndDelete(attributeId, YamlReader.TargetPath.TO_WORLD);
                if (result.isSuccess()) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.attribute_deleted")
                                    .withColor(General.Color.CONTENT)
                    );
                    break;
                }
                CommandError.displayAttributeErrorResult(context, result.getError());
                return 0;
            }

            case "global" -> {
                MethodResult result = AttributeHolder.unregisterAndDelete(attributeId, YamlReader.TargetPath.TO_GLOBAL);
                if (result.isSuccess()) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.attribute_deleted")
                                    .withColor(General.Color.CONTENT)
                    );
                    break;
                }
                CommandError.displayAttributeErrorResult(context, result.getError());
                return 0;
            }
        }

        return 1;
    }

    public static int executeVariable_Delete(CommandContext<CommandSourceStack> context) {
        String variableName = StringArgumentType.getString(context, "variable_name");

        if (variableName.equals("-all") || variableName.equals("-a")) {
            VariableHolder.deleteAllVariables();
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_all_deleted").withColor(General.Color.SUCCESS));

            return 1;
        }
        else {
            String variableType = VariableHolder.getType(variableName).getOrElse("?");

            MethodResult result = VariableHolder.deleteVariable(variableName);

            return result.matching(
                    () -> {
                        MessagePublisher.sendSystemMessage(context,
                                Component.translatable("commands.chx.variable_deleted")
                                        .append(" " + variableName + " (" +  variableType + ")")
                                        .withColor(General.Color.SUCCESS)
                        );

                        return 1;
                    },
                    (error, info) -> {
                        CommandError.displayVariableErrorResult(context, error);
                        return 0;
                    }
            );
        }
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
                MessagePublisher.sendFailureMessage(context, errorComponent(LootError.sameNameFound));
                return 0;
            }
            else if (worldTableExist) {
                specifiedPath = "world";
            }
            else if (globalTableExist) {
                specifiedPath = "global";
            }
            else {
                MessagePublisher.sendFailureMessage(context, errorComponent(LootError.tableNotExist));
                return 0;
            }
        }

        MethodResult result = switch (specifiedPath) {
            case "world" -> yamlFile? LootHolder.deleteFileLootTable(tableId, YamlReader.TargetPath.TO_WORLD) : LootHolder.deleteFileLootTable(tableId, JsonReader.TargetPath.TO_WORLD);
            case "global" -> yamlFile? LootHolder.deleteFileLootTable(tableId, YamlReader.TargetPath.TO_GLOBAL) : LootHolder.deleteFileLootTable(tableId, JsonReader.TargetPath.TO_GLOBAL);
            default -> MethodResult.failure("undefinedCategory");
        };

        return result.matching(
                () -> {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.loot_deleted")
                                    .withColor(General.Color.CONTENT)
                    );
                    return 1;
                },
                (error, info) -> {
                    CommandError.displayLootErrorResult(context, error);
                    return 0;
                }
        );
    }

    public static int executeWeather_Delete(CommandContext<CommandSourceStack> context, String specificPath) {
        String weatherId = StringArgumentType.getString(context, "weather_id");

        if (WeatherHolder.doesWeatherInstanceExist(weatherId, "api")) {
            MessagePublisher.sendFailureMessage(context, errorComponent(WeatherError.tryToModifyApiTarget));
            return 0;
        }

        if (specificPath.equals("try")) {
            boolean worldWeatherExist, globalWeatherExist;
            worldWeatherExist = WeatherHolder.doesYamlWeatherExist(weatherId, YamlReader.TargetPath.TO_WORLD);
            globalWeatherExist = WeatherHolder.doesYamlWeatherExist(weatherId, YamlReader.TargetPath.TO_GLOBAL);

            if (worldWeatherExist && globalWeatherExist) {
                MessagePublisher.sendFailureMessage(context, errorComponent(WeatherError.sameNameFound));
                return 0;
            }
            else if (worldWeatherExist) {
                specificPath = "world";
            }
            else if (globalWeatherExist) {
                specificPath = "global";
            }
            else {
                MessagePublisher.sendFailureMessage(context, errorComponent(WeatherError.notFound));
                return 0;
            }
        }

        YamlReader.TargetPath targetPath = specificPath.equals("world")? YamlReader.TargetPath.TO_WORLD : YamlReader.TargetPath.TO_GLOBAL;
        MethodResult result = WeatherHolder.unregisterAndDelete(weatherId, targetPath);

        return result.matching(
                () -> {
                    MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_deleted").withColor(General.Color.SUCCESS));
                    return 1;
                },
                (error, info) -> {
                    CommandError.displayWeatherErrorResult(context, error);
                    return 0;
                }
        );
    }
}
