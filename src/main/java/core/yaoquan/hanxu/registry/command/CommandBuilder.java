package core.yaoquan.hanxu.registry.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import core.yaoquan.hanxu.api.PermissionHolder;
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
                        .requires(cs -> PermissionHolder.hasPermission(cs,0))
                        .executes(CommandExecute::executeHelp)
                )
                .then(
                    Commands.literal("detail")
                        .requires(cs -> PermissionHolder.hasPermission(cs,0))
                        .executes(CommandExecute::executeDetail)
                )
                .then(
                    Commands.literal("license")
                        .then(
                            Commands.literal("agree")
                                .requires(cs -> PermissionHolder.hasPermission(cs,0))
                                .executes(CommandExecute::executeLicense_Agree)
                        )
                        .then(
                            Commands.literal("origin")
                                .requires(cs -> PermissionHolder.hasPermission(cs,0))
                                .executes(CommandExecute::executeLicense_Origin)
                        )
                        .requires(cs -> PermissionHolder.hasPermission(cs,0))
                        .executes(CommandExecute::executeLicense)
                )
                // Final execute father command.
                .executes(CommandExecute::executeBare)
        );

        dispatcher.register(
            Commands.literal("chx-a")
                // Subcommands.
                .then(
                    Commands.literal("help")
                        .requires(cs -> PermissionHolder.hasPermission(cs,2))
                        .executes(CommandExecute::executeAdminHelp)
                )
                .then(
                    Commands.literal("detail")
                        .requires(cs -> PermissionHolder.hasPermission(cs,2))
                        .executes(CommandExecute::executeDetail)
                )
                .then(
                    Commands.literal("license")
                        .then(
                            Commands.literal("agree")
                                .requires(cs -> PermissionHolder.hasPermission(cs,2))
                                .executes(CommandExecute::executeLicense_Agree)
                        )
                        .then(
                            Commands.literal("origin")
                                .requires(cs -> PermissionHolder.hasPermission(cs,2))
                                .executes(CommandExecute::executeLicense_Origin)
                        )
                        .then(
                            Commands.literal("state")
                                .then(
                                    Commands.argument("player_id", StringArgumentType.word())
                                        .suggests(CommandSuggest::suggestPlayer)
                                        .executes(CommandExecute::executeAdminLicense_State)
                                )
                                .requires(cs -> PermissionHolder.hasPermission(cs,2))
                        )
                        .requires(cs -> PermissionHolder.hasPermission(cs,2))
                        .executes(CommandExecute::executeLicense)
                )
                .then(
                    Commands.literal("permission")
                        .then(
                            Commands.literal("player")
                                .then(
                                    Commands.argument("player_id", StringArgumentType.string())
                                        .suggests(CommandSuggest::suggestPlayer)
                                        .executes(cs -> CommandExecute.executeAdminPermissionCheck(cs, "player"))
                                )
                        )
                        .then(
                            Commands.literal("server")
                                .executes(cs -> CommandExecute.executeAdminPermissionCheck(cs, "server"))
                        )
                        .then(
                            Commands.literal("player_override")
                                .executes(cs -> CommandExecute.executeAdminPermissionCheck(cs, "player_override"))
                        )
                )
                .then(
                    Commands.literal("timer")
                        .then(
                            Commands.literal("help")
                                    .executes(CommandExecute::executeAdminTimer_Help)
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
                                                                        .executes(cs -> CommandExecute.executeAdminTimer_Template_Create(cs, "null"))
                                                                )
                                                                .then(
                                                                    Commands.literal("execute")
                                                                        .then(
                                                                            Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                .executes(cs -> CommandExecute.executeAdminTimer_Template_Create(cs, "execute"))
                                                                        )
                                                                )
                                                                .then(
                                                                    Commands.literal("remind")
                                                                        .then(
                                                                            Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                .executes(cs -> CommandExecute.executeAdminTimer_Template_Create(cs, "remind"))
                                                                        )
                                                                        .executes(cs -> CommandExecute.executeAdminTimer_Template_Create(cs, "remind"))
                                                                )
                                                                .suggests(CommandSuggest::suggestUnit)
                                                                .executes(cs -> CommandExecute.executeAdminTimer_Template_Create(cs, "null"))
                                                        )
                                                        .executes(cs -> CommandExecute.executeAdminTimer_Template_Create(cs, "null"))
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
                                                                                        .executes(cs -> CommandExecute.executeAdminTimer_Template_CreateRange(cs, "null"))
                                                                                )
                                                                                .executes(cs -> CommandExecute.executeAdminTimer_Template_CreateRange(cs, "null"))
                                                                        )
                                                                        .then(
                                                                            Commands.literal("execute")
                                                                                .then(
                                                                                    Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                        .executes(cs -> CommandExecute.executeAdminTimer_Template_CreateRange(cs, "execute"))
                                                                                )
                                                                        )
                                                                        .then(
                                                                            Commands.literal("remind")
                                                                                .then(
                                                                                    Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                        .executes(cs -> CommandExecute.executeAdminTimer_Template_CreateRange(cs, "remind"))
                                                                                )
                                                                                .executes(cs -> CommandExecute.executeAdminTimer_Template_CreateRange(cs, "remind"))
                                                                        )
                                                                        .suggests(CommandSuggest::suggestUnit)
                                                                        .executes(cs -> CommandExecute.executeAdminTimer_Template_CreateRange(cs, "null"))
                                                                )
                                                                .executes(cs -> CommandExecute.executeAdminTimer_Template_CreateRange(cs, "null"))
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
                                                                .executes(CommandExecute::executeAdminTimer_Template_Read)
                                                        )
                                                        .suggests(CommandSuggest::suggestReadCategory)
                                                        .executes(CommandExecute::executeAdminTimer_Template_Read)
                                                )
                                                .suggests(CommandSuggest::suggestTemplateTimer)
                                        )
                                )
                                .then(
                                    Commands.literal("delete")
                                        .then(
                                            Commands.argument("timer_id", StringArgumentType.word())
                                                .suggests(CommandSuggest::suggestTemplateTimer)
                                                .executes(CommandExecute::executeAdminTimer_Template_Delete)
                                        )
                                )
                                .then(
                                    Commands.literal("list")
                                        .executes(CommandExecute::executeAdminTimer_Template_List)
                                )
                        )
                        .then(
                            Commands.literal("instance")
                                .then(
                                    Commands.literal("apply")
                                        .then(
                                            Commands.argument("template_timer_id", StringArgumentType.word())
                                                .then(
                                                    Commands.argument("apply_target", StringArgumentType.word())
                                                        .suggests(CommandSuggest::suggestUUIDOwner)
                                                        .executes(CommandExecute::executeAdminTimer_Instance_Apply)
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
                                                                                .executes(cs -> CommandExecute.executeAdminTimer_Instance_Create(cs, "null"))
                                                                        )
                                                                        .then(
                                                                            Commands.literal("execute")
                                                                                .then(
                                                                                    Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                        .executes(cs -> CommandExecute.executeAdminTimer_Instance_Create(cs, "execute"))
                                                                                )
                                                                        )
                                                                        .then(
                                                                            Commands.literal("remind")
                                                                                .then(
                                                                                    Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                        .executes(cs -> CommandExecute.executeAdminTimer_Instance_Create(cs, "remind"))
                                                                                )
                                                                                .executes(cs -> CommandExecute.executeAdminTimer_Instance_Create(cs, "remind"))
                                                                        )
                                                                        .suggests(CommandSuggest::suggestUnit)
                                                                        .executes(cs -> CommandExecute.executeAdminTimer_Instance_Create(cs, "null"))
                                                                )
                                                                .executes(cs -> CommandExecute.executeAdminTimer_Instance_Create(cs, "null"))
                                                        )
                                                        .suggests(CommandSuggest::suggestUUIDOwner)
                                                )
                                                .suggests(CommandSuggest::suggestTemplateTimer)
                                        )
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
                                                                                        .executes(cs -> CommandExecute.executeAdminTimer_Instance_CreateRange(cs, "null"))
                                                                                )
                                                                                .then(
                                                                                    Commands.literal("execute")
                                                                                        .then(
                                                                                            Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                                .executes(cs -> CommandExecute.executeAdminTimer_Instance_CreateRange(cs, "execute"))
                                                                                        )
                                                                                )
                                                                                .then(
                                                                                    Commands.literal("remind")
                                                                                        .then(
                                                                                            Commands.argument("behavior_content", StringArgumentType.greedyString())
                                                                                                .executes(cs -> CommandExecute.executeAdminTimer_Instance_CreateRange(cs, "remind"))
                                                                                        )
                                                                                        .executes(cs -> CommandExecute.executeAdminTimer_Instance_CreateRange(cs, "remind"))
                                                                                )
                                                                                .suggests(CommandSuggest::suggestUnit)
                                                                                .executes(cs -> CommandExecute.executeAdminTimer_Instance_CreateRange(cs, "null"))
                                                                        )
                                                                        .executes(cs -> CommandExecute.executeAdminTimer_Instance_CreateRange(cs, "null"))
                                                                )
                                                        )
                                                        .suggests(CommandSuggest::suggestUUIDOwner)
                                                )
                                                .suggests(CommandSuggest::suggestTemplateTimer)
                                        )
                                )
                                .then(
                                    Commands.literal("start")
                                        .then(
                                            Commands.argument("master_id", StringArgumentType.word())
                                                .then(
                                                    Commands.argument("timer_id", StringArgumentType.word())
                                                        .suggests(CommandSuggest::suggestInstanceTimer)
                                                        .executes(CommandExecute::executeAdminTimer_Instance_Start)
                                                )
                                                .suggests(CommandSuggest::suggestUUIDOwner)
                                        )
                                )
                                .then(
                                    Commands.literal("stop")
                                        .then(
                                            Commands.argument("master_id", StringArgumentType.word())
                                                .then(
                                                    Commands.argument("timer_id", StringArgumentType.word())
                                                        .suggests(CommandSuggest::suggestInstanceTimer)
                                                        .executes(CommandExecute::executeAdminTimer_Instance_Stop)
                                                )
                                                .suggests(CommandSuggest::suggestUUIDOwner)
                                        )
                                )
                                .then(
                                    Commands.literal("reset")
                                        .then(
                                            Commands.argument("master_id", StringArgumentType.word())
                                                .then(
                                                    Commands.argument("timer_id", StringArgumentType.word())
                                                        .suggests(CommandSuggest::suggestInstanceTimer)
                                                        .executes(CommandExecute::executeAdminTimer_Instance_Reset)
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
                                                                                .executes(cs -> CommandExecute.executeAdminTimer_Instance_Modify(cs, "initial_time"))
                                                                        )
                                                                        .executes(cs -> CommandExecute.executeAdminTimer_Instance_Modify(cs, "initial_time"))
                                                                )
                                                        )
                                                        .then(
                                                            Commands.literal("remaining_time")
                                                                .then(
                                                                    Commands.argument("time_amount", IntegerArgumentType.integer())
                                                                        .then(
                                                                            Commands.argument("time_unit", StringArgumentType.word())
                                                                                .suggests(CommandSuggest::suggestUnit)
                                                                                .executes(cs -> CommandExecute.executeAdminTimer_Instance_Modify(cs, "remaining_time"))
                                                                        )
                                                                        .executes(cs -> CommandExecute.executeAdminTimer_Instance_Modify(cs, "remaining_time"))
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
                                                                        .executes(CommandExecute::executeAdminTimer_Instance_Read)
                                                                )
                                                                .suggests(CommandSuggest::suggestReadCategory)
                                                                .executes(CommandExecute::executeAdminTimer_Instance_Read)
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
                                                        .executes(CommandExecute::executeAdminTimer_Instance_Delete)
                                                )
                                                .suggests(CommandSuggest::suggestUUIDOwner)
                                        )
                                )
                                .then(
                                    Commands.literal("list")
                                        .then(
                                            Commands.argument("master_id", StringArgumentType.word())
                                                .suggests(CommandSuggest::suggestUUIDOwner)
                                                .executes(CommandExecute::executeAdminTimer_Instance_List)
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
                                                                .executes(cs -> CommandExecute.executeAdminDisplay_Info_Timer(cs, true))
                                                        )
                                                        .then(
                                                            Commands.literal("false")
                                                                .executes(cs -> CommandExecute.executeAdminDisplay_Info_Timer(cs, false))
                                                        )
                                                        .executes(cs -> CommandExecute.executeAdminDisplay_Info_Timer(cs, true))
                                                        .suggests(CommandSuggest::suggestInstanceTimer)
                                                )
                                                .suggests(CommandSuggest::suggestInfoTimer)
                                        )
                                )
                        )
                        .requires(cs -> PermissionHolder.hasPermission(cs,2))
                        .executes(CommandExecute::executeAdminTimer)
                )
                .requires(cs -> PermissionHolder.hasPermission(cs,0))
                .executes(CommandExecute::executeAdminBare)
        );
    }
}
