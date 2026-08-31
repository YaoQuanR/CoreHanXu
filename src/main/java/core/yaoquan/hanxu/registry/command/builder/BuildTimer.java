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

public class BuildTimer {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("timer")
            .then(
                Commands.literal("help")
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.guide.timer.get()))
                    .executes(ExecuteGuide::executeTimer_Help)
            )
            .then(
                Commands.literal("template")
                    .then(
                        Commands.literal("list")
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.information.timer.get()))
                            .executes(ExecuteInformation::executeTimer_Template_List)
                    )
                    .then(
                        Commands.literal("read")
                            .then(
                                Commands.argument("timer_id", StringArgumentType.word())
                                    .then(
                                        Commands.argument("category", StringArgumentType.word())
                                            .then(
                                                Commands.argument("time_unit", StringArgumentType.word())
                                                    .suggests(CommandSuggest::suggestUnit)
                                                    .executes(ExecuteInformation::executeTimer_Template_Read)
                                            )
                                            .suggests(CommandSuggest::suggestReadCategory)
                                            .executes(ExecuteInformation::executeTimer_Template_Read)
                                    )
                                    .suggests(CommandSuggest::suggestTemplateTimer)
                            )
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.information.timer.get()))
                    )
                    .then(
                        Commands.literal("create")
                            .then(
                                Commands.argument("timer_id", StringArgumentType.word())
                                    .then(
                                        Commands.argument("time_amount", IntegerArgumentType.integer(1))
                                            .then(
                                                Commands.argument("time_unit", StringArgumentType.word())
                                                    .then(
                                                        Commands.literal("null")
                                                            .executes(cs -> ExecuteCreate.executeTimer_Template_Create(cs, "null"))
                                                    )
                                                    .then(
                                                        Commands.literal("execute")
                                                            .then(
                                                                Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                    .executes(cs -> ExecuteCreate.executeTimer_Template_Create(cs, "execute"))
                                                            )
                                                    )
                                                    .then(
                                                        Commands.literal("remind")
                                                            .then(
                                                                Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                    .executes(cs -> ExecuteCreate.executeTimer_Template_Create(cs, "remind"))
                                                            )
                                                            .executes(cs -> ExecuteCreate.executeTimer_Template_Create(cs, "remind"))
                                                    )
                                                    .suggests(CommandSuggest::suggestUnit)
                                                    .executes(cs -> ExecuteCreate.executeTimer_Template_Create(cs, "null"))
                                            )
                                            .executes(cs -> ExecuteCreate.executeTimer_Template_Create(cs, "null"))
                                    )
                                    .suggests(CommandSuggest::suggestTemplateTimer)
                            )
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.create.timerCreate.get()))
                    )
                    .then(
                        Commands.literal("create_range")
                            .then(
                                Commands.argument("timer_id", StringArgumentType.word())
                                    .then(
                                        Commands.argument("time_first_range", IntegerArgumentType.integer(1))
                                            .then(
                                                Commands.argument("time_second_range", IntegerArgumentType.integer(1))
                                                    .then(
                                                        Commands.argument("time_unit", StringArgumentType.word())
                                                            .then(
                                                                Commands.literal("null")
                                                                    .then(
                                                                        Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                            .executes(cs -> ExecuteCreate.executeTimer_Template_CreateRange(cs, "null"))
                                                                    )
                                                                    .executes(cs -> ExecuteCreate.executeTimer_Template_CreateRange(cs, "null"))
                                                            )
                                                            .then(
                                                                Commands.literal("execute")
                                                                    .then(
                                                                        Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                            .executes(cs -> ExecuteCreate.executeTimer_Template_CreateRange(cs, "execute"))
                                                                    )
                                                            )
                                                            .then(
                                                                Commands.literal("remind")
                                                                    .then(
                                                                        Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                            .executes(cs -> ExecuteCreate.executeTimer_Template_CreateRange(cs, "remind"))
                                                                    )
                                                                    .executes(cs -> ExecuteCreate.executeTimer_Template_CreateRange(cs, "remind"))
                                                            )
                                                            .suggests(CommandSuggest::suggestUnit)
                                                            .executes(cs -> ExecuteCreate.executeTimer_Template_CreateRange(cs, "null"))
                                                    )
                                                    .executes(cs -> ExecuteCreate.executeTimer_Template_CreateRange(cs, "null"))
                                            )
                                    )
                                    .suggests(CommandSuggest::suggestTemplateTimer)
                            )
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.create.timerCreate.get()))
                    )
                    .then(
                        Commands.literal("delete")
                            .then(
                                Commands.argument("timer_id", StringArgumentType.word())
                                    .suggests(CommandSuggest::suggestTemplateTimer)
                                    .executes(ExecuteDelete::executeTimer_Template_Delete)
                            )
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.delete.timer.get()))
                    )
            .then(
                Commands.literal("instance")
                    .then(
                        Commands.literal("list")
                            .then(
                                Commands.argument("master_id", StringArgumentType.word())
                                    .suggests(CommandSuggest::suggestUUIDOwner)
                                    .executes(ExecuteInformation::executeTimer_Instance_List)
                            )
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.information.timer.get()))
                    )
                    .then(
                        Commands.literal("read")
                            .then(
                                Commands.argument("master_id", StringArgumentType.word())
                                    .then(
                                        Commands.argument("timer_id", StringArgumentType.word())
                                            .then(
                                                Commands.argument("category", StringArgumentType.word())
                                                    .then(
                                                        Commands.argument("time_unit", StringArgumentType.word())
                                                            .suggests(CommandSuggest::suggestUnit)
                                                            .executes(ExecuteInformation::executeTimer_Instance_Read)
                                                    )
                                                    .suggests(CommandSuggest::suggestReadCategory)
                                                    .executes(ExecuteInformation::executeTimer_Instance_Read)
                                            )
                                            .suggests(CommandSuggest::suggestInstanceTimer)
                                    )
                                    .suggests(CommandSuggest::suggestUUIDOwner)

                            )
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.information.timer.get()))
                    )
                    .then(
                        Commands.literal("apply")
                            .then(
                                Commands.argument("template_timer_id", StringArgumentType.word())
                                    .then(
                                        Commands.argument("apply_target", StringArgumentType.word())
                                            .suggests(CommandSuggest::suggestUUIDOwner)
                                            .executes(ExecuteCreate::executeTimer_Instance_Apply)
                                    )
                                    .suggests(CommandSuggest::suggestTemplateTimer)
                            )
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.create.timerApply.get()))
                    )
                    .then(
                        Commands.literal("create")
                            .then(
                                Commands.argument("timer_id", StringArgumentType.word())
                                    .then(
                                        Commands.argument("master_id", StringArgumentType.word())
                                            .then(
                                                Commands.argument("time_amount", IntegerArgumentType.integer())
                                                    .then(
                                                        Commands.argument("time_unit", StringArgumentType.word())
                                                            .then(
                                                                Commands.literal("null")
                                                                    .executes(cs -> ExecuteCreate.executeTimer_Instance_Create(cs, "null"))
                                                            )
                                                            .then(
                                                                Commands.literal("execute")
                                                                    .then(
                                                                        Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                            .executes(cs -> ExecuteCreate.executeTimer_Instance_Create(cs, "execute"))
                                                                    )
                                                            )
                                                            .then(
                                                                Commands.literal("remind")
                                                                    .then(
                                                                        Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                            .executes(cs -> ExecuteCreate.executeTimer_Instance_Create(cs, "remind"))
                                                                    )
                                                                    .executes(cs -> ExecuteCreate.executeTimer_Instance_Create(cs, "remind"))
                                                            )
                                                            .suggests(CommandSuggest::suggestUnit)
                                                            .executes(cs -> ExecuteCreate.executeTimer_Instance_Create(cs, "null"))
                                                    )
                                                    .executes(cs -> ExecuteCreate.executeTimer_Instance_Create(cs, "null"))
                                            )
                                            .suggests(CommandSuggest::suggestUUIDOwner)
                                    )
                                    .suggests(CommandSuggest::suggestTemplateTimer)
                            )
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.create.timerCreate.get()))
                    )
                    .then(
                        Commands.literal("create_range")
                            .then(
                                Commands.argument("timer_id", StringArgumentType.word())
                                    .then(
                                        Commands.argument("master_id", StringArgumentType.word())
                                            .then(
                                                Commands.argument("time_first_range", IntegerArgumentType.integer())
                                                    .then(
                                                        Commands.argument("time_second_range", IntegerArgumentType.integer())
                                                            .then(
                                                                Commands.argument("time_unit", StringArgumentType.word())
                                                                    .then(
                                                                        Commands.literal("null")
                                                                            .executes(cs -> ExecuteCreate.executeTimer_Instance_CreateRange(cs, "null"))
                                                                    )
                                                                    .then(
                                                                        Commands.literal("execute")
                                                                            .then(
                                                                                Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                    .executes(cs -> ExecuteCreate.executeTimer_Instance_CreateRange(cs, "execute"))
                                                                            )
                                                                    )
                                                                    .then(
                                                                        Commands.literal("remind")
                                                                            .then(
                                                                                Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                    .executes(cs -> ExecuteCreate.executeTimer_Instance_CreateRange(cs, "remind"))
                                                                            )
                                                                            .executes(cs -> ExecuteCreate.executeTimer_Instance_CreateRange(cs, "remind"))
                                                                    )
                                                                    .suggests(CommandSuggest::suggestUnit)
                                                                    .executes(cs -> ExecuteCreate.executeTimer_Instance_CreateRange(cs, "null"))
                                                            )
                                                            .executes(cs -> ExecuteCreate.executeTimer_Instance_CreateRange(cs, "null"))
                                                    )
                                            )
                                            .suggests(CommandSuggest::suggestUUIDOwner)
                                    )
                                    .suggests(CommandSuggest::suggestTemplateTimer)
                            )
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.create.timerCreate.get()))
                    )
                    .then(
                        Commands.literal("start")
                            .then(
                                Commands.argument("master_id", StringArgumentType.word())
                                    .then(
                                        Commands.argument("timer_id", StringArgumentType.word())
                                            .suggests(CommandSuggest::suggestInstanceTimer)
                                            .executes(ExecuteRun::executeTimer_Instance_Start)
                                    )
                                    .suggests(CommandSuggest::suggestUUIDOwner)
                            )
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.run.timer.get())))
                    )
                    .then(
                        Commands.literal("stop")
                            .then(
                                Commands.argument("master_id", StringArgumentType.word())
                                    .then(
                                        Commands.argument("timer_id", StringArgumentType.word())
                                            .suggests(CommandSuggest::suggestInstanceTimer)
                                            .executes(ExecuteStop::executeTimer_Instance_Stop)
                                    )
                                    .suggests(CommandSuggest::suggestUUIDOwner)
                            )
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.stop.timer.get()))
                    )
                    .then(
                        Commands.literal("reset")
                            .then(
                                Commands.argument("master_id", StringArgumentType.word())
                                    .then(
                                        Commands.argument("timer_id", StringArgumentType.word())
                                            .suggests(CommandSuggest::suggestInstanceTimer)
                                            .executes(ExecuteStatus::executeTimer_Instance_Reset)
                                    )
                                    .suggests(CommandSuggest::suggestUUIDOwner)
                            )
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.status.timerReset.get()))
                    )
                    .then(
                        Commands.literal("restart")
                            .then(
                                Commands.argument("master_id", StringArgumentType.word())
                                    .then(
                                        Commands.argument("timer_id", StringArgumentType.word())
                                            .suggests(CommandSuggest::suggestInstanceTimer)
                                            .executes(ExecuteStatus::executeTimer_Instance_Restart)
                                    )
                                    .suggests(CommandSuggest::suggestUUIDOwner)
                            )
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.status.timerRestart.get()))
                    )
                    .then(
                        Commands.literal("modify")
                            .then(
                                Commands.argument("master_id", StringArgumentType.word())
                                    .then(
                                        Commands.argument("timer_id", StringArgumentType.word())
                                            .then(
                                                Commands.literal("initial_time")
                                                    .then(
                                                        Commands.argument("time_amount", IntegerArgumentType.integer())
                                                            .then(
                                                                Commands.argument("time_unit", StringArgumentType.word())
                                                                    .suggests(CommandSuggest::suggestUnit)
                                                                    .executes(cs -> ExecuteModification.executeTimer_Instance_Modify(cs, "initial_time"))
                                                            )
                                                            .executes(cs -> ExecuteModification.executeTimer_Instance_Modify(cs, "initial_time"))
                                                    )
                                            )
                                            .then(
                                                Commands.literal("remaining_time")
                                                    .then(
                                                        Commands.argument("time_amount", IntegerArgumentType.integer())
                                                            .then(
                                                                Commands.argument("time_unit", StringArgumentType.word())
                                                                    .suggests(CommandSuggest::suggestUnit)
                                                                    .executes(cs -> ExecuteModification.executeTimer_Instance_Modify(cs, "remaining_time"))
                                                            )
                                                            .executes(cs -> ExecuteModification.executeTimer_Instance_Modify(cs, "remaining_time"))
                                                    )
                                            )
                                            .suggests(CommandSuggest::suggestInstanceTimer)
                                    )
                                    .suggests(CommandSuggest::suggestUUIDOwner)
                            )
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.modification.timerModify.get()))
                    )
                    .then(
                        Commands.literal("delete")
                            .then(
                                Commands.argument("master_id", StringArgumentType.word())
                                    .then(
                                        Commands.argument("timer_id", StringArgumentType.word())
                                            .suggests(CommandSuggest::suggestInstanceTimer)
                                            .executes(ExecuteDelete::executeTimer_Instance_Delete)
                                    )
                                    .suggests(CommandSuggest::suggestUUIDOwner)
                            )
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.delete.timer.get()))
                    )
                    .then(
                        Commands.literal("display")
                            .then(
                                Commands.argument("master_id", StringArgumentType.word())
                                    .then(
                                        Commands.argument("timer_id", StringArgumentType.word())
                                            .then(
                                                Commands.literal("true")
                                                    .executes(cs -> ExecuteDisplay.executeTimer_Instance_Display(cs, true))
                                            )
                                            .then(
                                                Commands.literal("false")
                                                    .executes(cs -> ExecuteDisplay.executeTimer_Instance_Display(cs, false))
                                            )
                                            .suggests(CommandSuggest::suggestInstanceTimer)
                                            .executes(cs -> ExecuteDisplay.executeTimer_Instance_Display(cs, true))
                                    )
                                    .suggests(CommandSuggest::suggestUUIDOwner)
                            )
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.display.timer.get()))
                    )
            )
            .then(
                Commands.literal("test")
                    .then(
                        Commands.literal("example")
                            .then(
                                Commands.argument("test_id", IntegerArgumentType.integer())
                                    .executes(TestHolder::executeTest_Timer)
                            )
                    )
                    .then(
                        Commands.literal("display")
                            .then(
                                Commands.argument("master_group", StringArgumentType.word())
                                    .then(
                                        Commands.argument("timer_id", StringArgumentType.word())
                                            .then(
                                                Commands.literal("true")
                                                    .executes(cs -> TestHolder.executeTest_TimerDisplay(cs, true))
                                            )
                                            .then(
                                                Commands.literal("false")
                                                    .executes(cs -> TestHolder.executeTest_TimerDisplay(cs, false))
                                            )
                                            .executes(cs -> TestHolder.executeTest_TimerDisplay(cs, true))
                                    )
                            )
                    )
                    .requires(TestHolder::hasPrivateTestPermission)
            )
            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.guide.timer.get()))
            .executes(ExecuteGuide::executeTimer);
    }
}
