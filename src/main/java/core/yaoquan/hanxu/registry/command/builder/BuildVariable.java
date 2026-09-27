package core.yaoquan.hanxu.registry.command.builder;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import core.yaoquan.hanxu.api.PermissionHolder;
import core.yaoquan.hanxu.registry.command.CommandSuggest;
import core.yaoquan.hanxu.registry.command.execute.*;
import core.yaoquan.hanxu.registry.config.PermissionConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

class BuildVariable {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("variable")
            .then(
                Commands.literal("help")
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.guide.variable.get()))
                    .executes(ExecuteGuide::executeVariable_Help)
            )
            .then(
                Commands.literal("list")
                    .then(
                        Commands.literal("variables")
                            .executes(cs -> ExecuteInformation.executeVariable_List(cs, "variables"))
                    )
                    .then(
                        Commands.literal("values")
                            .executes(cs -> ExecuteInformation.executeVariable_List(cs, "values"))
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.information.variable.get()))
            )
            .then(
                Commands.literal("read")
                    .then(
                        Commands.argument("variable_name", StringArgumentType.string())
                            .suggests(CommandSuggest::suggestAllVariable)
                            .executes(ExecuteInformation::executeVariable_Read)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.information.variable.get()))
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
                                                    .executes(cs -> ExecuteCreate.executeVariable_Create(cs, true))
                                            )
                                            .executes(cs -> ExecuteCreate.executeVariable_Create(cs, false))
                                    )
                                    .suggests(CommandSuggest::suggestAllVariable)
                            )
                            .suggests(CommandSuggest::suggestVariableType)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.create.variable.get()))
            )
            .then(
                Commands.literal("delete")
                    .then(
                        Commands.argument("variable_name", StringArgumentType.string())
                            .suggests(CommandSuggest::suggestAllVariableWithAll)
                            .executes(ExecuteDelete::executeVariable_Delete)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.delete.variable.get()))
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
                                                    .executes(cs -> ExecuteModification.executeVariable_Copy(cs, true))
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
                                                    .executes(cs -> ExecuteModification.executeVariable_Copy(cs, false))
                                            )
                                            .suggests(CommandSuggest::suggestPlayer)
                                    )
                            )
                            .suggests(CommandSuggest::suggestAllVariable)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.modification.variableCopy.get()))
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
                                                                    .executes(cs -> ExecuteCondition.executeVariable_If_Value(cs, "execute"))
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
                                                                                    .executes(cs -> ExecuteCondition.executeVariable_If_Value(cs, "then"))
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
                                                                            .executes(cs -> ExecuteCondition.executeVariable_If_Score(cs, "execute"))
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
                                                                                            .executes(cs -> ExecuteCondition.executeVariable_If_Score(cs, "then"))
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
                                                                                    .executes(cs -> ExecuteCondition.executeVariable_If_Margin(cs, "execute"))
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
                                                                                                    .executes(cs -> ExecuteCondition.executeVariable_If_Margin(cs, "then"))
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
                                                                                    .executes(cs -> ExecuteCondition.executeVariable_If_Margin(cs, "execute"))
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
                                                                                                    .executes(cs -> ExecuteCondition.executeVariable_If_Margin(cs, "then"))
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
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.condition.variable.get()))
            )
            .then(
                Commands.literal("string")
                    .then(
                        Commands.argument("variable_name", StringArgumentType.string())
                            .then(
                                Commands.literal("to_lower")
                                    .executes(cs -> ExecuteModification.executeVariable_String(cs, "to_lower"))
                            )
                            .then(
                                Commands.literal("to_upper")
                                    .executes(cs -> ExecuteModification.executeVariable_String(cs, "to_upper"))
                            )
                            .suggests(CommandSuggest::suggestAllVariable)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.modification.variableString.get()))
            )
            .then(
                Commands.literal("modify")
                    .then(
                        Commands.argument("variable_name", StringArgumentType.string())
                            .then(
                                Commands.argument("category", StringArgumentType.word())
                                    .then(
                                        Commands.argument("new_value", StringArgumentType.string())
                                            .executes(ExecuteModification::executeVariable_Modify)
                                    )
                                    .suggests(CommandSuggest::suggestVariableModifyAction)
                            )
                            .suggests(CommandSuggest::suggestAllVariable)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.modification.variableModify.get()))
            )
            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.guide.variable.get()))
            .executes(ExecuteGuide::executeVariable);
    }
}
