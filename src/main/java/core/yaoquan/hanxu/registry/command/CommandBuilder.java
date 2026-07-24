package core.yaoquan.hanxu.registry.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import core.yaoquan.hanxu.api.PermissionHolder;
import core.yaoquan.hanxu.registry.config.PermissionConfig;
import core.yaoquan.hanxu.test.TestHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandBuilder {
    // Register core command chx to here:
    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("chx")
                // Subcommands.
                .then(
                    Commands.literal("help")
                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_HELP.getAsInt()))
                        .executes(CommandExecute::executeHelp)
                )
                .then(
                    Commands.literal("detail")
                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_DETAIL.getAsInt()))
                        .executes(CommandExecute::executeDetail)
                )
                .then(
                    Commands.literal("license")
                        .then(
                            Commands.literal("agree")
                                .requires(cs -> PermissionHolder.Verify.hasPermission(cs,0))
                                .executes(CommandExecute::executeLicense_Agree)
                        )
                        .then(
                            Commands.literal("origin")
                                .requires(cs -> PermissionHolder.Verify.hasPermission(cs,0))
                                .executes(CommandExecute::executeLicense_Origin)
                        )
                        .then(
                            Commands.literal("state")
                                .then(
                                    Commands.argument("player_id", StringArgumentType.string())
                                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_LICENSE_ADVANCED_STATE.getAsInt()))
                                        .suggests(CommandSuggest::suggestPlayer)
                                        .executes(CommandExecute::executeAdvancedLicense_State)
                                )
                                .executes(CommandExecute::executeLicense_State)
                        )
                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs,0))
                        .executes(CommandExecute::executeLicense)
                )
                .then(
                    Commands.literal("permission")
                        .then(
                            Commands.literal("player")
                                .then(
                                    Commands.argument("player_id", StringArgumentType.string())
                                        .then(
                                            Commands.literal("set")
                                                .then(
                                                    Commands.argument("level", IntegerArgumentType.integer())
                                                        .executes(CommandExecute::executePermission_Set)
                                                )
                                        )
                                        .suggests(CommandSuggest::suggestPlayer)
                                        .executes(cs -> CommandExecute.executePermission_Check(cs, "player"))
                                )
                                .executes(cs -> CommandExecute.executePermission_Check(cs, "player"))
                        )
                        .then(
                            Commands.literal("server")
                                .executes(cs -> CommandExecute.executePermission_Check(cs, "server"))
                        )
                        .then(
                            Commands.literal("player_first_grant")
                                .executes(cs -> CommandExecute.executePermission_Check(cs, "player_first_grant"))
                        )
                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_PERMISSION_LEVEL.getAsInt()))
                )
                .then(
                    Commands.literal("timer")
                        .then(
                            Commands.literal("help")
                                .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_TIMER_HELP.getAsInt()))
                                .executes(CommandExecute::executeTimer_Help)
                        )
                        .then(
                            Commands.literal("template")
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
                                                                        .executes(cs -> CommandExecute.executeTimer_Template_Create(cs, "null"))
                                                                )
                                                                .then(
                                                                    Commands.literal("execute")
                                                                        .then(
                                                                            Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                .executes(cs -> CommandExecute.executeTimer_Template_Create(cs, "execute"))
                                                                        )
                                                                )
                                                                .then(
                                                                    Commands.literal("remind")
                                                                        .then(
                                                                            Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                .executes(cs -> CommandExecute.executeTimer_Template_Create(cs, "remind"))
                                                                        )
                                                                        .executes(cs -> CommandExecute.executeTimer_Template_Create(cs, "remind"))
                                                                )
                                                                .suggests(CommandSuggest::suggestUnit)
                                                                .executes(cs -> CommandExecute.executeTimer_Template_Create(cs, "null"))
                                                        )
                                                        .executes(cs -> CommandExecute.executeTimer_Template_Create(cs, "null"))
                                                )
                                                .suggests(CommandSuggest::suggestTemplateTimer)
                                        )
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
                                                                                        .executes(cs -> CommandExecute.executeTimer_Template_CreateRange(cs, "null"))
                                                                                )
                                                                                .executes(cs -> CommandExecute.executeTimer_Template_CreateRange(cs, "null"))
                                                                        )
                                                                        .then(
                                                                            Commands.literal("execute")
                                                                                .then(
                                                                                    Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                        .executes(cs -> CommandExecute.executeTimer_Template_CreateRange(cs, "execute"))
                                                                                )
                                                                        )
                                                                        .then(
                                                                            Commands.literal("remind")
                                                                                .then(
                                                                                    Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                        .executes(cs -> CommandExecute.executeTimer_Template_CreateRange(cs, "remind"))
                                                                                )
                                                                                .executes(cs -> CommandExecute.executeTimer_Template_CreateRange(cs, "remind"))
                                                                        )
                                                                        .suggests(CommandSuggest::suggestUnit)
                                                                        .executes(cs -> CommandExecute.executeTimer_Template_CreateRange(cs, "null"))
                                                                )
                                                                .executes(cs -> CommandExecute.executeTimer_Template_CreateRange(cs, "null"))
                                                        )
                                                )
                                                .suggests(CommandSuggest::suggestTemplateTimer)
                                        )
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
                                                                .executes(CommandExecute::executeTimer_Template_Read)
                                                        )
                                                        .suggests(CommandSuggest::suggestReadCategory)
                                                        .executes(CommandExecute::executeTimer_Template_Read)
                                                )
                                                .suggests(CommandSuggest::suggestTemplateTimer)
                                        )
                                )
                                .then(
                                    Commands.literal("delete")
                                        .then(
                                            Commands.argument("timer_id", StringArgumentType.word())
                                                .suggests(CommandSuggest::suggestTemplateTimer)
                                                .executes(CommandExecute::executeTimer_Template_Delete)
                                        )
                                )
                                .then(
                                    Commands.literal("list")
                                        .executes(CommandExecute::executeTimer_Template_List)
                                )
                                .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_TIMER_TEMPLATE.getAsInt()))
                        )
                        .then(
                            Commands.literal("instance")
                                .then(
                                    Commands.literal("apply")
                                        .then(
                                            Commands.argument("template_timer_id", StringArgumentType.word())
                                                .then(
                                                    Commands.argument("apply_target", StringArgumentType.word())
                                                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_TIMER_INSTANCE_CREATE.getAsInt()))
                                                        .suggests(CommandSuggest::suggestUUIDOwner)
                                                        .executes(CommandExecute::executeTimer_Instance_Apply)
                                                )
                                                .suggests(CommandSuggest::suggestTemplateTimer)
                                        )
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
                                                                                .executes(cs -> CommandExecute.executeTimer_Instance_Create(cs, "null"))
                                                                        )
                                                                        .then(
                                                                            Commands.literal("execute")
                                                                                .then(
                                                                                    Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                        .executes(cs -> CommandExecute.executeTimer_Instance_Create(cs, "execute"))
                                                                                )
                                                                        )
                                                                        .then(
                                                                            Commands.literal("remind")
                                                                                .then(
                                                                                    Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                        .executes(cs -> CommandExecute.executeTimer_Instance_Create(cs, "remind"))
                                                                                )
                                                                                .executes(cs -> CommandExecute.executeTimer_Instance_Create(cs, "remind"))
                                                                        )
                                                                        .suggests(CommandSuggest::suggestUnit)
                                                                        .executes(cs -> CommandExecute.executeTimer_Instance_Create(cs, "null"))
                                                                )
                                                                .executes(cs -> CommandExecute.executeTimer_Instance_Create(cs, "null"))
                                                        )
                                                        .suggests(CommandSuggest::suggestUUIDOwner)
                                                )
                                                .suggests(CommandSuggest::suggestTemplateTimer)
                                        )
                                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_TIMER_INSTANCE_CREATE.getAsInt()))
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
                                                                                        .executes(cs -> CommandExecute.executeTimer_Instance_CreateRange(cs, "null"))
                                                                                )
                                                                                .then(
                                                                                    Commands.literal("execute")
                                                                                        .then(
                                                                                            Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                                .executes(cs -> CommandExecute.executeTimer_Instance_CreateRange(cs, "execute"))
                                                                                        )
                                                                                )
                                                                                .then(
                                                                                    Commands.literal("remind")
                                                                                        .then(
                                                                                            Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                                .executes(cs -> CommandExecute.executeTimer_Instance_CreateRange(cs, "remind"))
                                                                                        )
                                                                                        .executes(cs -> CommandExecute.executeTimer_Instance_CreateRange(cs, "remind"))
                                                                                )
                                                                                .suggests(CommandSuggest::suggestUnit)
                                                                                .executes(cs -> CommandExecute.executeTimer_Instance_CreateRange(cs, "null"))
                                                                        )
                                                                        .executes(cs -> CommandExecute.executeTimer_Instance_CreateRange(cs, "null"))
                                                                )
                                                        )
                                                        .suggests(CommandSuggest::suggestUUIDOwner)
                                                )
                                                .suggests(CommandSuggest::suggestTemplateTimer)
                                        )
                                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_TIMER_INSTANCE_CREATE.getAsInt()))
                                )
                                .then(
                                    Commands.literal("start")
                                        .then(
                                            Commands.argument("master_id", StringArgumentType.word())
                                                .then(
                                                    Commands.argument("timer_id", StringArgumentType.word())
                                                        .suggests(CommandSuggest::suggestInstanceTimer)
                                                        .executes(CommandExecute::executeTimer_Instance_Start)
                                                )
                                                .suggests(CommandSuggest::suggestUUIDOwner)
                                        )
                                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_TIMER_INSTANCE_RUN.getAsInt()))
                                )
                                .then(
                                    Commands.literal("stop")
                                        .then(
                                            Commands.argument("master_id", StringArgumentType.word())
                                                .then(
                                                    Commands.argument("timer_id", StringArgumentType.word())
                                                        .suggests(CommandSuggest::suggestInstanceTimer)
                                                        .executes(CommandExecute::executeTimer_Instance_Stop)
                                                )
                                                .suggests(CommandSuggest::suggestUUIDOwner)
                                        )
                                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_TIMER_INSTANCE_RUN.getAsInt()))
                                )
                                .then(
                                    Commands.literal("reset")
                                        .then(
                                            Commands.argument("master_id", StringArgumentType.word())
                                                .then(
                                                    Commands.argument("timer_id", StringArgumentType.word())
                                                        .suggests(CommandSuggest::suggestInstanceTimer)
                                                        .executes(CommandExecute::executeTimer_Instance_Reset)
                                                )
                                                .suggests(CommandSuggest::suggestUUIDOwner)
                                        )
                                )
                                .then(
                                    Commands.literal("restart")
                                        .then(
                                            Commands.argument("master_id", StringArgumentType.word())
                                                .then(
                                                    Commands.argument("timer_id", StringArgumentType.word())
                                                        .suggests(CommandSuggest::suggestInstanceTimer)
                                                        .executes(CommandExecute::executeTimer_Instance_Restart)
                                                )
                                                .suggests(CommandSuggest::suggestUUIDOwner)
                                        )
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
                                                                                .executes(cs -> CommandExecute.executeTimer_Instance_Modify(cs, "initial_time"))
                                                                        )
                                                                        .executes(cs -> CommandExecute.executeTimer_Instance_Modify(cs, "initial_time"))
                                                                )
                                                        )
                                                        .then(
                                                            Commands.literal("remaining_time")
                                                                .then(
                                                                    Commands.argument("time_amount", IntegerArgumentType.integer())
                                                                        .then(
                                                                            Commands.argument("time_unit", StringArgumentType.word())
                                                                                .suggests(CommandSuggest::suggestUnit)
                                                                                .executes(cs -> CommandExecute.executeTimer_Instance_Modify(cs, "remaining_time"))
                                                                        )
                                                                        .executes(cs -> CommandExecute.executeTimer_Instance_Modify(cs, "remaining_time"))
                                                                )
                                                        )
                                                        .suggests(CommandSuggest::suggestInstanceTimer)
                                                )
                                                .suggests(CommandSuggest::suggestUUIDOwner)
                                        )
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
                                                                        .executes(CommandExecute::executeTimer_Instance_Read)
                                                                )
                                                                .suggests(CommandSuggest::suggestReadCategory)
                                                                .executes(CommandExecute::executeTimer_Instance_Read)
                                                        )
                                                        .suggests(CommandSuggest::suggestInstanceTimer)
                                                )
                                                .suggests(CommandSuggest::suggestUUIDOwner)

                                        )
                                )
                                .then(
                                    Commands.literal("delete")
                                        .then(
                                            Commands.argument("master_id", StringArgumentType.word())
                                                .then(
                                                    Commands.argument("timer_id", StringArgumentType.word())
                                                        .suggests(CommandSuggest::suggestInstanceTimer)
                                                        .executes(CommandExecute::executeTimer_Instance_Delete)
                                                )
                                                .suggests(CommandSuggest::suggestUUIDOwner)
                                        )
                                )
                                .then(
                                    Commands.literal("list")
                                        .then(
                                            Commands.argument("master_id", StringArgumentType.word())
                                                .suggests(CommandSuggest::suggestUUIDOwner)
                                                .executes(CommandExecute::executeTimer_Instance_List)
                                        )
                                )
                                .then(
                                    Commands.literal("display")
                                        .then(
                                            Commands.argument("master_id", StringArgumentType.word())
                                                .then(
                                                    Commands.argument("timer_id", StringArgumentType.word())
                                                        .then(
                                                            Commands.literal("true")
                                                                .executes(cs -> CommandExecute.executeTimer_Instance_Display(cs, true))
                                                        )
                                                        .then(
                                                            Commands.literal("false")
                                                                .executes(cs -> CommandExecute.executeTimer_Instance_Display(cs, false))
                                                        )
                                                        .suggests(CommandSuggest::suggestInstanceTimer)
                                                        .executes(cs -> CommandExecute.executeTimer_Instance_Display(cs, true))
                                                )
                                                .suggests(CommandSuggest::suggestUUIDOwner)
                                        )
                                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_TIMER_F4.getAsInt()))
                                )
                                .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_TIMER_INSTANCE_OTHERS.getAsInt()))
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
                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_TIMER_HELP.getAsInt()))
                        .executes(CommandExecute::executeTimer)
                )
                .then(
                    Commands.literal("scene")
                        .then(
                            Commands.literal("help")
                                .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_SCENE_HELP.getAsInt()))
                                .executes(CommandExecute::executeScene_Help)
                        )
                        .then(
                            Commands.literal("list")
                                .executes(CommandExecute::executeScene_List)
                        )
                        .then(
                            Commands.literal("play")
                                .then(
                                    Commands.argument("scene_name", StringArgumentType.string())
                                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_SCENE_PLAY.getAsInt()))
                                        .suggests(CommandSuggest::suggestScene)
                                        .executes(CommandExecute::executeScene_Play)
                                )
                        )
                        .then(
                            Commands.literal("broadcast")
                                .then(
                                    Commands.argument("scene_name", StringArgumentType.string())
                                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_SCENE_PLAY.getAsInt()))
                                        .suggests(CommandSuggest::suggestScene)
                                        .executes(CommandExecute::executeScene_Broadcast)
                                )
                        )
                        .then(
                            Commands.literal("delete")
                                .then(
                                    Commands.argument("scene_name", StringArgumentType.string())
                                        .then(
                                            Commands.literal("world")
                                                .executes(cs -> CommandExecute.executeScene_Delete(cs, "world"))
                                        )
                                        .then(
                                            Commands.literal("global")
                                                .executes(cs -> CommandExecute.executeScene_Delete(cs, "global"))
                                        )
                                        .suggests(CommandSuggest::suggestScene)
                                        .executes(cs -> CommandExecute.executeScene_Delete(cs, "try"))
                                )
                        )
                        .then(
                            Commands.literal("template")
                                .executes(CommandExecute::executeScene_Template)
                        )
                        .then(
                            Commands.literal("create")
                                .then(
                                    Commands.argument("to_path", StringArgumentType.string())
                                        .suggests(CommandSuggest::suggestSavePath)
                                        .executes(CommandExecute::executeScene_Create)
                                )
                        )
                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_SCENE_OTHERS.getAsInt()))
                        .executes(CommandExecute::executeScene)
                )
                .then(
                    Commands.literal("attribute")
                        .then(
                            Commands.literal("help")
                                .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_ATTRIBUTE_HELP.getAsInt()))
                                .executes(CommandExecute::executeAttribute_Help)
                        )
                        .then(
                            Commands.literal("list")
                                .then(
                                    Commands.literal("api")
                                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs,10))
                                        .executes(CommandExecute::executeAdvancedAttribute_List)
                                )
                                .executes(CommandExecute::executeAttribute_List)
                        )
                        .then(
                            Commands.literal("create")
                                .then(
                                    Commands.argument("attribute_id", StringArgumentType.word())
                                        .then(
                                            Commands.argument("to_path", StringArgumentType.string())
                                                .then(
                                                    Commands.argument("maximum", FloatArgumentType.floatArg())
                                                        .then(
                                                            Commands.argument("default_value", FloatArgumentType.floatArg())
                                                                .executes(CommandExecute::executeAttribute_Create)
                                                        )
                                                        .executes(CommandExecute::executeAttribute_Create)
                                                )
                                                .suggests(CommandSuggest::suggestSavePath)
                                                .executes(CommandExecute::executeAttribute_Create)
                                        )
                                        .suggests(CommandSuggest::suggestYamlAttribute)
                                )
                                .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_ATTRIBUTE_CREATE.getAsInt()))
                        )
                        .then(
                            Commands.literal("define")
                                .then(
                                    Commands.argument("attribute_id", StringArgumentType.string())
                                        .then(
                                            Commands.argument("threshold", FloatArgumentType.floatArg())
                                                .then(
                                                    Commands.literal("remind")
                                                        .then(
                                                            Commands.argument("content", StringArgumentType.greedyString())
                                                                .executes(cs -> CommandExecute.executeAttribute_Define(cs, "remind"))
                                                        )
                                                )
                                                .then(
                                                    Commands.literal("execute")
                                                        .then(
                                                            Commands.argument("content", StringArgumentType.greedyString())
                                                                .executes(cs -> CommandExecute.executeAttribute_Define(cs, "execute"))
                                                        )
                                                )
                                                .then(
                                                    Commands.literal("api")
                                                        .then(
                                                            Commands.argument("callback_id", StringArgumentType.string())
                                                                .executes(cs -> CommandExecute.executeAttribute_Define(cs, "api"))
                                                        )
                                                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs,10))
                                                )
                                        )
                                        .suggests(CommandSuggest::suggestYamlAttribute)
                                )
                                .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_ATTRIBUTE_CREATE.getAsInt()))
                        )
                        .then(
                            Commands.literal("delete")
                                .then(
                                    Commands.argument("attribute_id", StringArgumentType.string())
                                        .then(
                                            Commands.literal("world")
                                                .executes(cs -> CommandExecute.executeAttribute_Delete(cs, "world"))
                                        )
                                        .then(
                                            Commands.literal("global")
                                                .executes(cs -> CommandExecute.executeAttribute_Delete(cs, "global"))
                                        )
                                        .suggests(CommandSuggest::suggestYamlAttribute)
                                        .executes(cs -> CommandExecute.executeAttribute_Delete(cs, "try"))
                                )
                        )
                        .then(
                            Commands.literal("read")
                                .then(
                                    Commands.argument("attribute_id", StringArgumentType.string())
                                        .then(
                                            Commands.literal("threshold")
                                                .then(
                                                    Commands.literal("all")
                                                        .executes(cs -> CommandExecute.executeAttribute_Read(cs, "threshold_all"))
                                                )
                                                .then(
                                                    Commands.literal("specific")
                                                        .then(
                                                            Commands.argument("threshold_value", FloatArgumentType.floatArg())
                                                                .executes(cs -> CommandExecute.executeAttribute_Read(cs, "threshold_specific"))
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("zero")
                                                .executes(cs -> CommandExecute.executeAttribute_Read(cs, "zero"))
                                        )
                                        .then(
                                            Commands.literal("recovery")
                                                .executes(cs -> CommandExecute.executeAttribute_Read(cs, "recovery"))
                                        )
                                        .then(
                                            Commands.literal("maximum")
                                                .executes(cs -> CommandExecute.executeAttribute_Read(cs, "maximum"))
                                        )
                                        .then(
                                            Commands.literal("recovery_interval")
                                                .executes(cs -> CommandExecute.executeAttribute_Read(cs, "recovery_interval"))
                                        )
                                        .then(
                                            Commands.literal("value")
                                                .then(
                                                    Commands.argument("player_id", StringArgumentType.string())
                                                        .suggests(CommandSuggest::suggestPlayer)
                                                        .executes(cs -> CommandExecute.executeAttribute_Read(cs, "value"))
                                                )
                                                .executes(cs -> CommandExecute.executeAttribute_Read(cs, "value"))
                                        )
                                        .then(
                                            Commands.literal("group")
                                                .executes(cs -> CommandExecute.executeAttribute_Read(cs, "group"))
                                        )
                                        .suggests(CommandSuggest::suggestYamlAttribute)
                                )
                        )
                        .then(
                            Commands.literal("modify")
                                .then(
                                    Commands.argument("attribute_id", StringArgumentType.string())
                                        .then(
                                            Commands.argument("player_id", StringArgumentType.string())
                                                .then(
                                                    Commands.literal("set")
                                                        .then(
                                                            Commands.argument("value", FloatArgumentType.floatArg())
                                                                .then(
                                                                    Commands.argument("direction", StringArgumentType.word())
                                                                        .suggests(CommandSuggest::suggestAttributeDirection)
                                                                        .executes(cs -> CommandExecute.executeAttribute_Modify(cs, "set"))
                                                                )
                                                                .executes(cs -> CommandExecute.executeAttribute_Modify(cs, "set"))
                                                        )
                                                )
                                                .then(
                                                    Commands.literal("add")
                                                        .then(
                                                            Commands.argument("value", FloatArgumentType.floatArg())
                                                                .then(
                                                                    Commands.argument("direction", StringArgumentType.word())
                                                                        .suggests(CommandSuggest::suggestAttributeDirection)
                                                                        .executes(cs -> CommandExecute.executeAttribute_Modify(cs, "add"))
                                                                )
                                                                .executes(cs -> CommandExecute.executeAttribute_Modify(cs, "add"))
                                                        )
                                                )
                                                .then(
                                                    Commands.literal("reduce")
                                                        .then(
                                                            Commands.argument("value", FloatArgumentType.floatArg())
                                                                .then(
                                                                    Commands.argument("direction", StringArgumentType.word())
                                                                        .suggests(CommandSuggest::suggestAttributeDirection)
                                                                        .executes(cs -> CommandExecute.executeAttribute_Modify(cs, "reduce"))
                                                                )
                                                                .executes(cs -> CommandExecute.executeAttribute_Modify(cs, "reduce"))
                                                        )
                                                )
                                                .suggests(CommandSuggest::suggestPlayer)
                                        )
                                        .suggests(CommandSuggest::suggestYamlAttribute)
                                )
                        )
                        .then(
                            Commands.literal("recovery")
                                .then(
                                    Commands.argument("attribute_id", StringArgumentType.string())
                                        .then(
                                            Commands.literal("simple")
                                                .then(
                                                    Commands.argument("interval", IntegerArgumentType.integer())
                                                        .then(
                                                            Commands.argument("interval_unit", StringArgumentType.word())
                                                                .then(
                                                                    Commands.argument("value", FloatArgumentType.floatArg())
                                                                        .then(
                                                                            Commands.argument("direction", StringArgumentType.word())
                                                                                .suggests(CommandSuggest::suggestAttributeDirection)
                                                                                .executes(cs -> CommandExecute.executeAttribute_Recovery(cs, "simple"))
                                                                        )
                                                                        .executes(cs -> CommandExecute.executeAttribute_Recovery(cs, "simple"))
                                                                )
                                                                .suggests(CommandSuggest::suggestUnit)
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("api")
                                                .then(
                                                    Commands.argument("callback_id", StringArgumentType.string())
                                                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, 10))
                                                        .executes(cs -> CommandExecute.executeAttribute_Recovery(cs, "api"))
                                                )
                                        )
                                        .suggests(CommandSuggest::suggestYamlAttribute)
                                )
                        )
                        .then(
                            Commands.literal("display")
                                .then(
                                    Commands.argument("attribute_id", StringArgumentType.string())
                                        .then(
                                            Commands.literal("true")
                                                .executes(cs -> CommandExecute.executeAttribute_Display(cs, true))
                                        )
                                        .then(
                                            Commands.literal("false")
                                                .executes(cs -> CommandExecute.executeAttribute_Display(cs, false))
                                        )
                                        .suggests(CommandSuggest::suggestAllAttribute)
                                        .executes(cs -> CommandExecute.executeAttribute_Display(cs, true))
                                )
                                .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_ATTRIBUTE_F4.getAsInt()))
                        )
                        .then(
                            Commands.literal("test")
                                .then(
                                    Commands.literal("example")
                                        .then(
                                            Commands.argument("test_id", IntegerArgumentType.integer())
                                                    .then(
                                                        Commands.argument("extra", StringArgumentType.string())
                                                            .executes(TestHolder::executeTest_Attribute)
                                                    )
                                                .executes(TestHolder::executeTest_Attribute)
                                        )
                                )
                                .then(
                                    Commands.literal("yaml_display")
                                        .executes(TestHolder::executeTest_AttributeYamlDisplay)
                                )
                                .requires(TestHolder::hasPrivateTestPermission)
                        )
                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_ATTRIBUTE_OTHERS.getAsInt()))
                        .executes(CommandExecute::executeAttribute)
                )
                .then(
                    Commands.literal("variable")
                        .then(
                            Commands.literal("help")
                                .executes(CommandExecute::executeVariable_Help)
                        )
                        .then(
                            Commands.literal("list")
                                .then(
                                    Commands.literal("variables")
                                        .executes(cs -> CommandExecute.executeVariable_List(cs, "variables"))
                                )
                                .then(
                                    Commands.literal("values")
                                        .executes(cs -> CommandExecute.executeVariable_List(cs, "values"))
                                )
                        )
                        .then(
                            Commands.literal("read")
                                .then(
                                    Commands.argument("variable_name", StringArgumentType.string())
                                        .suggests(CommandSuggest::suggestAllVariable)
                                        .executes(CommandExecute::executeVariable_Read)
                                )
                        )
                        .then(
                            Commands.literal("create")
                                .then(
                                    Commands.argument("variable_type", StringArgumentType.word())
                                        .then(
                                            Commands.argument("variable_name", StringArgumentType.string())
                                                .then(
                                                    Commands.argument("variable_value", StringArgumentType.string())
                                                        .then(
                                                            Commands.literal("-override")
                                                                .executes(cs -> CommandExecute.executeVariable_Create(cs, true))
                                                        )
                                                        .executes(cs -> CommandExecute.executeVariable_Create(cs, false))
                                                )
                                                .suggests(CommandSuggest::suggestAllVariable)
                                        )
                                        .suggests(CommandSuggest::suggestVariableType)
                                )
                        )
                        .then(
                            Commands.literal("delete")
                                .then(
                                    Commands.argument("variable_name", StringArgumentType.string())
                                        .suggests(CommandSuggest::suggestAllVariableWithAll)
                                        .executes(CommandExecute::executeVariable_Delete)
                                )
                        )
                        .then(
                            Commands.literal("copy")
                                .then(
                                    Commands.argument("variable_name", StringArgumentType.string())
                                        .then(
                                            Commands.literal("from")
                                                .then(
                                                    Commands.argument("player_id", StringArgumentType.string())
                                                        .then(
                                                            Commands.argument("score_name", StringArgumentType.string())
                                                                .suggests(CommandSuggest::suggestAllScoreboardName)
                                                                .executes(cs -> CommandExecute.executeVariable_Copy(cs, true))
                                                        )
                                                        .suggests(CommandSuggest::suggestPlayer)
                                                )
                                        )
                                        .then(
                                            Commands.literal("to")
                                                .then(
                                                    Commands.argument("player_id", StringArgumentType.string())
                                                        .then(
                                                            Commands.argument("score_name", StringArgumentType.string())
                                                                .suggests(CommandSuggest::suggestAllScoreboardName)
                                                                .executes(cs -> CommandExecute.executeVariable_Copy(cs, false))
                                                        )
                                                        .suggests(CommandSuggest::suggestPlayer)
                                                )
                                        )
                                        .suggests(CommandSuggest::suggestAllVariable)
                                )
                        )
                        .then(
                            Commands.literal("if")
                                .then(
                                    Commands.literal("value")
                                        .then(
                                            Commands.argument("variable_name", StringArgumentType.string())
                                                .then(
                                                    Commands.argument("compare_sign", StringArgumentType.string())
                                                        .then(
                                                            Commands.argument("compare_value", StringArgumentType.string())
                                                                .then(
                                                                    Commands.literal("execute")
                                                                        .then(
                                                                            Commands.argument("command", StringArgumentType.greedyString())
                                                                                .executes(cs -> CommandExecute.executeVariable_If_Value(cs, "execute"))
                                                                        )
                                                                )
                                                                .then(
                                                                    Commands.literal("then")
                                                                        .then(
                                                                            Commands.argument("target_variable", StringArgumentType.string())
                                                                                .then(
                                                                                    Commands.argument("action", StringArgumentType.string())
                                                                                        .then(
                                                                                            Commands.argument("target", StringArgumentType.string())
                                                                                                .then(
                                                                                                    Commands.argument("optional_player_id", StringArgumentType.string())
                                                                                                        .suggests(CommandSuggest::suggestPlayer)
                                                                                                )
                                                                                                .executes(cs -> CommandExecute.executeVariable_If_Value(cs, "then"))
                                                                                        )
                                                                                        .suggests(CommandSuggest::suggestVariableIfThenAction)
                                                                                )
                                                                                .suggests(CommandSuggest::suggestAllVariableWithSelf)
                                                                        )
                                                                )
                                                        )
                                                        .suggests(CommandSuggest::suggestVariableCompareSign)
                                                )
                                                .suggests(CommandSuggest::suggestAllVariable)
                                        )
                                )
                                .then(
                                    Commands.literal("score")
                                        .then(
                                            Commands.argument("player_id", StringArgumentType.string())
                                                .then(
                                                    Commands.argument("score_name", StringArgumentType.string())
                                                        .then(
                                                            Commands.argument("compare_sign", StringArgumentType.string())
                                                                .then(
                                                                    Commands.argument("compare_value", IntegerArgumentType.integer())
                                                                        .then(
                                                                            Commands.literal("execute")
                                                                                .then(
                                                                                    Commands.argument("command", StringArgumentType.greedyString())
                                                                                        .executes(cs -> CommandExecute.executeVariable_If_Score(cs, "execute"))
                                                                                )
                                                                        )
                                                                        .then(
                                                                            Commands.literal("then")
                                                                                .then(
                                                                                    Commands.argument("target_variable", StringArgumentType.string())
                                                                                        .then(
                                                                                            Commands.argument("action", StringArgumentType.string())
                                                                                                .then(
                                                                                                    Commands.argument("target", StringArgumentType.string())
                                                                                                        .then(
                                                                                                            Commands.argument("optional_player_id", StringArgumentType.string())
                                                                                                                .suggests(CommandSuggest::suggestPlayer)
                                                                                                        )
                                                                                                        .executes(cs -> CommandExecute.executeVariable_If_Score(cs, "then"))
                                                                                                )
                                                                                                .suggests(CommandSuggest::suggestVariableIfThenAction)
                                                                                        )
                                                                                        .suggests(CommandSuggest::suggestAllVariable)
                                                                                )
                                                                        )
                                                                )
                                                                .suggests(CommandSuggest::suggestVariableCompareSign)
                                                        )
                                                        .suggests(CommandSuggest::suggestAllScoreboardName)
                                                )
                                                .suggests(CommandSuggest::suggestPlayer)
                                        )
                                )
                                .then(
                                    Commands.literal("margin")
                                        .then(
                                            Commands.argument("variable_name", StringArgumentType.string())
                                                .then(
                                                    Commands.literal("%")
                                                        .then(
                                                            Commands.argument("margin_value", StringArgumentType.word())
                                                                .then(
                                                                    Commands.literal("=")
                                                                        .then(
                                                                            Commands.argument("compare_value", StringArgumentType.string())
                                                                                .then(
                                                                                    Commands.literal("execute")
                                                                                        .then(
                                                                                            Commands.argument("command", StringArgumentType.greedyString())
                                                                                                .executes(cs -> CommandExecute.executeVariable_If_Margin(cs, "execute"))
                                                                                        )
                                                                                )
                                                                                .then(
                                                                                    Commands.literal("then")
                                                                                        .then(
                                                                                            Commands.argument("target_variable", StringArgumentType.string())
                                                                                                .then(
                                                                                                    Commands.argument("action", StringArgumentType.string())
                                                                                                        .then(
                                                                                                            Commands.argument("target", StringArgumentType.string())
                                                                                                                .then(
                                                                                                                    Commands.argument("optional_player_id", StringArgumentType.string())
                                                                                                                        .suggests(CommandSuggest::suggestPlayer)
                                                                                                                )
                                                                                                                .executes(cs -> CommandExecute.executeVariable_If_Margin(cs, "then"))
                                                                                                        )
                                                                                                        .suggests(CommandSuggest::suggestVariableIfThenAction)
                                                                                                )
                                                                                                .suggests(CommandSuggest::suggestAllVariableWithSelf)
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                                .then(
                                                                    Commands.literal("==")
                                                                        .then(
                                                                            Commands.argument("compare_value", StringArgumentType.string())
                                                                                .then(
                                                                                    Commands.literal("execute")
                                                                                        .then(
                                                                                            Commands.argument("command", StringArgumentType.greedyString())
                                                                                                .executes(cs -> CommandExecute.executeVariable_If_Margin(cs, "execute"))
                                                                                        )
                                                                                )
                                                                                .then(
                                                                                    Commands.literal("then")
                                                                                        .then(
                                                                                            Commands.argument("target_variable", StringArgumentType.string())
                                                                                                .then(
                                                                                                    Commands.argument("action", StringArgumentType.string())
                                                                                                        .then(
                                                                                                            Commands.argument("target", StringArgumentType.string())
                                                                                                                .then(
                                                                                                                    Commands.argument("optional_player_id", StringArgumentType.string())
                                                                                                                        .suggests(CommandSuggest::suggestPlayer)
                                                                                                                )
                                                                                                                .executes(cs -> CommandExecute.executeVariable_If_Margin(cs, "then"))
                                                                                                        )
                                                                                                        .suggests(CommandSuggest::suggestVariableIfThenAction)
                                                                                                )
                                                                                                .suggests(CommandSuggest::suggestAllVariableWithSelf)
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                                .suggests(CommandSuggest::suggestAllVariable)
                                        )
                                )
                        )
                        .then(
                            Commands.literal("string")
                                .then(
                                    Commands.argument("variable_name", StringArgumentType.string())
                                        .then(
                                            Commands.literal("to_lower")
                                                .executes(cs -> CommandExecute.executeVariable_String(cs, "to_lower"))
                                        )
                                        .then(
                                            Commands.literal("to_upper")
                                                .executes(cs -> CommandExecute.executeVariable_String(cs, "to_upper"))
                                        )
                                        .suggests(CommandSuggest::suggestAllVariable)
                                )
                        )
                        .then(
                            Commands.literal("modify")
                                .then(
                                    Commands.argument("variable_name", StringArgumentType.string())
                                        .then(
                                            Commands.argument("category", StringArgumentType.word())
                                                .then(
                                                    Commands.argument("new_value", StringArgumentType.string())
                                                        .executes(CommandExecute::executeVariable_Modify)
                                                )
                                                .suggests(CommandSuggest::suggestVariableModifyAction)
                                        )
                                        .suggests(CommandSuggest::suggestAllVariable)
                                )
                        )
                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_VARIABLE_OPERATIONS.getAsInt()))
                        .executes(CommandExecute::executeVariable)
                )
                .then(
                    Commands.literal("loot")
                        .then(
                            Commands.literal("help")
                                .executes(CommandExecute::executeLoot_Help)
                        )
                        .then(
                            Commands.literal("list")
                                .executes(CommandExecute::executeLoot_List)
                        )
                        .then(
                            Commands.literal("read")
                                .then(
                                    Commands.argument("table_id", StringArgumentType.string())
                                        .suggests(CommandSuggest::suggestAllFileLootTable)
                                        .executes(CommandExecute::executeLoot_Read)
                                )
                        )
                        .then(
                            Commands.literal("give")
                                .then(
                                    Commands.argument("player_id", StringArgumentType.string())
                                        .then(
                                            Commands.argument("table_id", StringArgumentType.string())
                                                .then(
                                                    Commands.literal("-first_item")
                                                        .executes(cs -> CommandExecute.executeLoot_Give(cs, "first_item"))
                                                )
                                                .then(
                                                    Commands.literal("-with_condition")
                                                        .executes(cs -> CommandExecute.executeLoot_Give(cs, "with_condition"))
                                                )
                                                .then(
                                                    Commands.literal("-ignore")
                                                        .then(
                                                            Commands.argument("ignore_item", StringArgumentType.greedyString())
                                                                .executes(cs -> CommandExecute.executeLoot_Give(cs, "ignore"))
                                                        )
                                                )
                                                .suggests(CommandSuggest::suggestAllFileLootTable)
                                                .executes(cs -> CommandExecute.executeLoot_Give(cs, "default"))
                                        )
                                        .suggests(CommandSuggest::suggestPlayer)
                                )
                        )
                        .then(
                            Commands.literal("fill")
                                .then(
                                    Commands.argument("container_x", IntegerArgumentType.integer())
                                        .then(
                                            Commands.argument("container_y", IntegerArgumentType.integer())
                                                .then(
                                                    Commands.argument("container_z", IntegerArgumentType.integer())
                                                        .then(
                                                            Commands.argument("table_id", StringArgumentType.string())
                                                                .then(
                                                                    Commands.literal("ignore_condition")
                                                                        .then(
                                                                            Commands.literal("-sorted")
                                                                                .then(
                                                                                    Commands.literal("-ignore")
                                                                                        .then(
                                                                                            Commands.argument("ignore_item", StringArgumentType.greedyString())
                                                                                                .executes(cs -> CommandExecute.executeLoot_Fill(cs, true, "sorted-ignore"))
                                                                                        )
                                                                                )
                                                                                .executes(cs -> CommandExecute.executeLoot_Fill(cs, true, "sorted"))
                                                                        )
                                                                        .then(
                                                                            Commands.literal("-ignore")
                                                                                .then(
                                                                                    Commands.argument("ignore_item", StringArgumentType.greedyString())
                                                                                        .executes(cs -> CommandExecute.executeLoot_Fill(cs, true, "ignore"))
                                                                                )
                                                                        )
                                                                        .executes(cs -> CommandExecute.executeLoot_Fill(cs, true, "default"))
                                                                )
                                                                .then(
                                                                    Commands.literal("with_condition")
                                                                        .then(
                                                                            Commands.literal("-sorted")
                                                                                .then(
                                                                                    Commands.literal("-ignore")
                                                                                        .then(
                                                                                            Commands.argument("ignore_item", StringArgumentType.greedyString())
                                                                                                .executes(cs -> CommandExecute.executeLoot_Fill(cs, false, "sorted-ignore"))
                                                                                        )
                                                                                )
                                                                                .executes(cs -> CommandExecute.executeLoot_Fill(cs, false, "sorted"))
                                                                        )
                                                                        .then(
                                                                            Commands.literal("-ignore")
                                                                                .then(
                                                                                    Commands.argument("ignore_item", StringArgumentType.greedyString())
                                                                                        .executes(cs -> CommandExecute.executeLoot_Fill(cs, false, "ignore"))
                                                                                )
                                                                        )
                                                                        .executes(cs -> CommandExecute.executeLoot_Fill(cs, false, "default"))
                                                                )
                                                                .suggests(CommandSuggest::suggestAllFileLootTable)
                                                        )
                                                        .suggests(CommandSuggest::suggestHitBlockPositionInZ)
                                                )
                                                .suggests(CommandSuggest::suggestHitBlockPositionInYZ)
                                        )
                                        .suggests(CommandSuggest::suggestHitBlockPosition)
                                )
                        )
                        .then(
                            Commands.literal("create")
                                .then(
                                    Commands.argument("to_path", StringArgumentType.word())
                                        .suggests(CommandSuggest::suggestSavePath)
                                        .executes(CommandExecute::executeLoot_Create)
                                )
                        )
                        .then(
                            Commands.literal("delete")
                                .then(
                                    Commands.argument("table_id", StringArgumentType.string())
                                        .then(
                                            Commands.literal("world")
                                                .executes(cs -> CommandExecute.executeLoot_Delete(cs, "world"))
                                        )
                                        .then(
                                            Commands.literal("global")
                                                .executes(cs -> CommandExecute.executeLoot_Delete(cs, "global"))
                                        )
                                        .suggests(CommandSuggest::suggestAllFileLootTable)
                                        .executes(cs -> CommandExecute.executeLoot_Delete(cs, "try"))
                                )
                        )
                        .then(
                            Commands.literal("template")
                                .executes(CommandExecute::executeLoot_Template)
                        )
                        .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_LOOT_OPERATIONS.getAsInt()))
                        .executes(CommandExecute::executeLoot)
                )
                .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.SET_PERMISSION_HELP.getAsInt()))
                .executes(CommandExecute::executeBare)
        );
    }
}
