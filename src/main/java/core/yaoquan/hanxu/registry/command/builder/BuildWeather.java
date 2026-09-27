package core.yaoquan.hanxu.registry.command.builder;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import core.yaoquan.hanxu.api.PermissionHolder;
import core.yaoquan.hanxu.registry.command.CommandSuggest;
import core.yaoquan.hanxu.registry.command.execute.*;
import core.yaoquan.hanxu.registry.config.PermissionConfig;
import core.yaoquan.hanxu.test.TestHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

class BuildWeather {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("weather")
            .then(
                Commands.literal("help")
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.guide.weather.get()))
                    .executes(ExecuteGuide::executeWeather_Help)
            )
            .then(
                Commands.literal("list")
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.information.weather.get()))
                    .executes(ExecuteInformation::executeWeather_List)
            )
            .then(
                Commands.literal("read")
                    .then(
                        Commands.argument("weather_id", StringArgumentType.string())
                            .then(
                                Commands.argument("level", StringArgumentType.string())
                                    .suggests(CommandSuggest::suggestDimension)
                                    .executes(ExecuteInformation::executeWeather_Read)
                            )
                            .suggests(CommandSuggest::suggestWeather)
                            .executes(ExecuteInformation::executeWeather_Read)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.information.weather.get()))
            )
            .then(
                Commands.literal("start")
                    .then(
                        Commands.argument("weather_id", StringArgumentType.string())
                            .then(
                                Commands.argument("level", StringArgumentType.string())
                                    .suggests(CommandSuggest::suggestDimension)
                                    .executes(ExecuteRun::executeWeather_Start)
                            )
                            .suggests(CommandSuggest::suggestWeather)
                            .executes(ExecuteRun::executeWeather_Start)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.run.weatherStart.get()))
            )
            .then(
                Commands.literal("resume")
                    .then(
                        Commands.literal("id")
                            .then(
                                Commands.argument("weather_id", StringArgumentType.string())
                                    .then(
                                        Commands.argument("level", StringArgumentType.string())
                                            .suggests(CommandSuggest::suggestDimension)
                                            .executes(ExecuteRun::executeWeather_ResumeId)
                                    )
                                    .suggests(CommandSuggest::suggestWeather)
                                    .executes(ExecuteRun::executeWeather_ResumeId)
                            )
                    )
                    .then(
                        Commands.literal("type")
                            .then(
                                Commands.argument("weather_type", StringArgumentType.word())
                                    .then(
                                        Commands.argument("level", StringArgumentType.string())
                                            .suggests(CommandSuggest::suggestDimension)
                                            .executes(ExecuteRun::executeWeather_ResumeType)
                                    )
                                    .suggests(CommandSuggest::suggestWeatherType)
                                    .executes(ExecuteRun::executeWeather_ResumeType)
                            )
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.run.weatherResume.get()))
            )
            .then(
                Commands.literal("pause")
                    .then(
                        Commands.literal("id")
                            .then(
                                Commands.argument("weather_id", StringArgumentType.string())
                                    .then(
                                        Commands.argument("level", StringArgumentType.string())
                                            .suggests(CommandSuggest::suggestDimension)
                                            .executes(ExecuteStop::executeWeather_PauseId)
                                    )
                                    .suggests(CommandSuggest::suggestWeather)
                                    .executes(ExecuteStop::executeWeather_PauseId)
                            )
                    )
                    .then(
                        Commands.literal("type")
                            .then(
                                Commands.argument("weather_type", StringArgumentType.word())
                                    .then(
                                        Commands.argument("level", StringArgumentType.string())
                                            .suggests(CommandSuggest::suggestDimension)
                                            .executes(ExecuteStop::executeWeather_PauseType)
                                    )
                                    .suggests(CommandSuggest::suggestWeatherType)
                                    .executes(ExecuteStop::executeWeather_PauseType)
                            )
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.stop.weather.get()))
            )
            .then(
                Commands.literal("restart")
                    .then(
                        Commands.argument("weather_id", StringArgumentType.string())
                            .then(
                                Commands.argument("level", StringArgumentType.string())
                                    .suggests(CommandSuggest::suggestDimension)
                                    .executes(ExecuteStatus::executeWeather_Restart)
                            )
                            .suggests(CommandSuggest::suggestWeather)
                            .executes(ExecuteStatus::executeWeather_Restart)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.status.weatherRestart.get()))
            )
            .then(
                Commands.literal("ready")
                    .then(
                        Commands.argument("weather_id", StringArgumentType.string())
                            .then(
                                Commands.argument("level", StringArgumentType.string())
                                    .suggests(CommandSuggest::suggestDimension)
                                    .executes(ExecuteStatus::executeWeather_Ready)
                            )
                            .suggests(CommandSuggest::suggestWeather)
                            .executes(ExecuteStatus::executeWeather_Ready)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.status.weatherReady.get()))
            )
            .then(
                Commands.literal("kill")
                    .then(
                        Commands.argument("weather_id", StringArgumentType.string())
                            .then(
                                Commands.argument("level", StringArgumentType.string())
                                    .suggests(CommandSuggest::suggestDimension)
                                    .executes(ExecuteStatus::executeWeather_Kill)
                            )
                            .suggests(CommandSuggest::suggestWeather)
                            .executes(ExecuteStatus::executeWeather_Kill)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.status.weatherKill.get()))
            )
            .then(
                Commands.literal("reload")
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.reload.weather.get()))
                    .executes(ExecuteReload::executeWeather_Reload)
            )
            .then(
                Commands.literal("create")
                    .then(
                        Commands.argument("to_path", StringArgumentType.word())
                            .suggests(CommandSuggest::suggestSavePath)
                            .executes(ExecuteCreate::executeWeather_Create)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.create.weather.get()))
            )
            .then(
                Commands.literal("delete")
                    .then(
                        Commands.argument("weather_id", StringArgumentType.string())
                            .then(
                                Commands.literal("world")
                                    .executes(cs -> ExecuteDelete.executeWeather_Delete(cs, "world"))
                            )
                            .then(
                                Commands.literal("global")
                                    .executes(cs -> ExecuteDelete.executeWeather_Delete(cs, "global"))
                            )
                            .suggests(CommandSuggest::suggestWeather)
                            .executes(cs -> ExecuteDelete.executeWeather_Delete(cs, "try"))
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.delete.weather.get()))
            )
            .then(
                Commands.literal("modify")
                    .then(
                        Commands.argument("weather_id", StringArgumentType.string())
                            .then(
                                Commands.argument("level", StringArgumentType.string())
                                    .then(
                                        Commands.literal("initial_ticks")
                                            .then(
                                                Commands.argument("time_amount", IntegerArgumentType.integer())
                                                    .executes(cs -> ExecuteModification.executeWeather_Modify(cs, "initial"))
                                            )
                                    )
                                    .then(
                                        Commands.literal("remaining_ticks")
                                            .then(
                                                Commands.argument("time_amount", IntegerArgumentType.integer())
                                                    .executes(cs -> ExecuteModification.executeWeather_Modify(cs, "remaining"))
                                            )
                                    )
                                    .then(
                                        Commands.literal("duration_ticks")
                                            .then(
                                                Commands.argument("time_amount", IntegerArgumentType.integer())
                                                    .executes(cs -> ExecuteModification.executeWeather_Modify(cs, "duration"))
                                            )
                                    )
                                    .then(
                                        Commands.literal("stillness_ticks")
                                            .then(
                                                Commands.argument("time_amount", IntegerArgumentType.integer())
                                                    .executes(cs -> ExecuteModification.executeWeather_Modify(cs, "stillness"))
                                            )
                                    )
                                    .suggests(CommandSuggest::suggestDimension)
                            )
                            .suggests(CommandSuggest::suggestWeather)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.modification.weatherModify.get()))
            )
            .then(
                Commands.literal("template")
                    .then(
                        Commands.literal("fog")
                            .executes(cs -> ExecuteTemplate.executeWeather_Template(cs, "fog"))
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.template.weather.get()))
            )
            .then(
                Commands.literal("display")
                    .then(
                        Commands.argument("weather_id", StringArgumentType.string())
                            .then(
                                Commands.argument("level", StringArgumentType.string())
                                    .then(
                                        Commands.literal("true")
                                            .executes(cs -> ExecuteDisplay.executeWeather_Display(cs, true))
                                    )
                                    .then(
                                        Commands.literal("false")
                                            .executes(cs -> ExecuteDisplay.executeWeather_Display(cs, false))
                                    )
                                    .suggests(CommandSuggest::suggestDimension)
                                    .executes(cs -> ExecuteDisplay.executeWeather_Display(cs, true))
                            )
                            .suggests(CommandSuggest::suggestWeather)
                            .executes(cs -> ExecuteDisplay.executeWeather_Display(cs, true))
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.display.weather.get()))
            )
            .then(
                Commands.literal("test")
                    .requires(TestHolder::hasPrivateTestPermission)
                    .executes(TestHolder::executeTest_Weather)
            )
            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.guide.weather.get()))
            .executes(ExecuteGuide::executeWeather);
    }
}
