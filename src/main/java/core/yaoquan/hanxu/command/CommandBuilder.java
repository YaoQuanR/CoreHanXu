package core.yaoquan.hanxu.command;

import com.mojang.brigadier.Command;
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
                        .executes(CommandExecute::executeCommandHelp)
                )
                .then(
                    Commands.literal("detail")
                        .requires(cs -> PermissionHolder.hasPermission(cs,0))
                        .executes(CommandExecute::executeCommandDetail)
                )
                .then(
                    Commands.literal("license")
                        .then(
                            Commands.literal("agree")
                                .requires(cs -> PermissionHolder.hasPermission(cs,0))
                                .executes(CommandExecute::executeCommandLicense_Agree)
                        )
                        .then(
                            Commands.literal("origin")
                                .requires(cs -> PermissionHolder.hasPermission(cs,0))
                                .executes(CommandExecute::executeCommandLicense_Origin)
                        )
                        .requires(cs -> PermissionHolder.hasPermission(cs,0))
                        .executes(CommandExecute::executeCommandLicense)
                )
                // Final execute father command.
                .executes(CommandExecute::executeCommandBare)
        );

        dispatcher.register(
            Commands.literal("chx-a")
                // Subcommands.
                .then(
                    Commands.literal("help")
                        .requires(cs -> PermissionHolder.hasPermission(cs,2))
                        .executes(CommandExecute::executeAdminCommandHelp)
                )
                .then(
                    Commands.literal("detail")
                        .requires(cs -> PermissionHolder.hasPermission(cs,2))
                        .executes(CommandExecute::executeCommandDetail)
                )
                .then(
                    Commands.literal("license")
                        .then(
                            Commands.literal("agree")
                                .requires(cs -> PermissionHolder.hasPermission(cs,2))
                                .executes(CommandExecute::executeCommandLicense_Agree)
                        )
                        .then(
                            Commands.literal("origin")
                                .requires(cs -> PermissionHolder.hasPermission(cs,2))
                                .executes(CommandExecute::executeCommandLicense_Origin)
                        )
                        .requires(cs -> PermissionHolder.hasPermission(cs,2))
                        .executes(CommandExecute::executeCommandLicense)
                )
                .then(
                    Commands.literal("timer")
                        .then(
                            Commands.literal("help")
                                    .executes(CommandExecute::executeAdminCommandTimer_Help)
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
                                                                    Commands.argument("end_behavior", StringArgumentType.word())
                                                                        .then(
                                                                            Commands.argument("behavior_content", StringArgumentType.word())
                                                                                .executes(CommandExecute::executeAdminCommandTimer_Template_Create)
                                                                        )
                                                                        .suggests(CommandSuggest::suggestEndBehaviorCategory)
                                                                        .executes(CommandExecute::executeAdminCommandTimer_Template_Create)
                                                                )
                                                                .suggests(CommandSuggest::suggestUnit)
                                                                .executes(CommandExecute::executeAdminCommandTimer_Template_Create)
                                                        )
                                                        .executes(CommandExecute::executeAdminCommandTimer_Template_Create)
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
                                                                        .suggests(CommandSuggest::suggestUnit)
                                                                        .executes(CommandExecute::executeAdminCommandTimer_Template_CreateRange)
                                                                )
                                                                .executes(CommandExecute::executeAdminCommandTimer_Template_CreateRange)
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
                                                                .executes(CommandExecute::executeAdminCommandTimer_Template_Read)
                                                        )
                                                        .suggests(CommandSuggest::suggestReadCategory)
                                                        .executes(CommandExecute::executeAdminCommandTimer_Template_Read)
                                                )
                                                .suggests(CommandSuggest::suggestTemplateTimer)
                                        )
                                )
                                .then(
                                    Commands.literal("delete")
                                        .then(
                                            Commands.argument("timer_id", StringArgumentType.word())
                                                .suggests(CommandSuggest::suggestTemplateTimer)
                                                .executes(CommandExecute::executeAdminCommandTimer_Template_Delete)
                                        )
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
                                                        .executes(CommandExecute::executeAdminCommandTimer_Instance_Apply)
                                                )
                                                .suggests(CommandSuggest::suggestTemplateTimer)
                                        )
                                )
                                .then(
                                    Commands.literal("start")
                                )
                                .then(
                                    Commands.literal("stop")
                                )
                                .then(
                                    Commands.literal("reset")
                                )
                                .then(
                                    Commands.literal("read")
                                )
                                .then(
                                    Commands.literal("delete")
                                )
                        )
                        .requires(cs -> PermissionHolder.hasPermission(cs,2))
                        .executes(CommandExecute::executeAdminCommandTimer)
                )
                .requires(cs -> PermissionHolder.hasPermission(cs,0))
                .executes(CommandExecute::executeAdminCommandBare)
        );
    }
}
