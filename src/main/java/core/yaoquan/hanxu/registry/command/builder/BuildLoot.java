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

class BuildLoot {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("loot")
            .then(
                Commands.literal("help")
                    .then(
                        Commands.literal("functions")
                            .executes(ExecuteGuide::executeLoot_Help_Functions)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.guide.loot.get()))
                    .executes(ExecuteGuide::executeLoot_Help)
            )
            .then(
                Commands.literal("list")
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.information.loot.get()))
                    .executes(ExecuteInformation::executeLoot_List)
            )
            .then(
                Commands.literal("read")
                    .then(
                        Commands.argument("table_id", StringArgumentType.string())
                            .suggests(CommandSuggest::suggestAllFileLootTable)
                            .executes(ExecuteInformation::executeLoot_Read)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.information.loot.get()))
            )
            .then(
                Commands.literal("give")
                    .then(
                        Commands.argument("player_id", StringArgumentType.string())
                            .then(
                                Commands.argument("table_id", StringArgumentType.string())
                                    .then(
                                        Commands.literal("-first_item")
                                            .executes(cs -> ExecuteRun.executeLoot_Give(cs, "first_item"))
                                    )
                                    .then(
                                        Commands.literal("-with_condition")
                                            .executes(cs -> ExecuteRun.executeLoot_Give(cs, "with_condition"))
                                    )
                                    .then(
                                        Commands.literal("-ignore")
                                            .then(
                                                Commands.argument("ignore_item", StringArgumentType.greedyString())
                                                    .executes(cs -> ExecuteRun.executeLoot_Give(cs, "ignore"))
                                            )
                                    )
                                    .then(
                                        Commands.literal("-guaranteed")
                                            .executes(cs -> ExecuteRun.executeLoot_Give(cs, "guaranteed"))
                                    )
                                    .suggests(CommandSuggest::suggestAllFileLootTable)
                                    .executes(cs -> ExecuteRun.executeLoot_Give(cs, "default"))
                            )
                            .suggests(CommandSuggest::suggestPlayer)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.run.lootGive.get()))
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
                                                                                    .executes(cs -> ExecuteRun.executeLoot_Fill(cs, true, "sorted-ignore"))
                                                                            )
                                                                    )
                                                                    .executes(cs -> ExecuteRun.executeLoot_Fill(cs, true, "sorted"))
                                                            )
                                                            .then(
                                                                Commands.literal("-ignore")
                                                                    .then(
                                                                        Commands.argument("ignore_item", StringArgumentType.greedyString())
                                                                            .executes(cs -> ExecuteRun.executeLoot_Fill(cs, true, "ignore"))
                                                                    )
                                                            )
                                                            .then(
                                                                Commands.literal("-guaranteed")
                                                                    .executes(cs -> ExecuteRun.executeLoot_Fill(cs, true, "guaranteed"))
                                                            )
                                                            .executes(cs -> ExecuteRun.executeLoot_Fill(cs, true, "default"))
                                                    )
                                                    .then(
                                                        Commands.literal("with_condition")
                                                            .then(
                                                                Commands.literal("-sorted")
                                                                    .then(
                                                                        Commands.literal("-ignore")
                                                                            .then(
                                                                                Commands.argument("ignore_item", StringArgumentType.greedyString())
                                                                                    .executes(cs -> ExecuteRun.executeLoot_Fill(cs, false, "sorted-ignore"))
                                                                            )
                                                                    )
                                                                    .executes(cs -> ExecuteRun.executeLoot_Fill(cs, false, "sorted"))
                                                            )
                                                            .then(
                                                                Commands.literal("-ignore")
                                                                    .then(
                                                                        Commands.argument("ignore_item", StringArgumentType.greedyString())
                                                                            .executes(cs -> ExecuteRun.executeLoot_Fill(cs, false, "ignore"))
                                                                    )
                                                            )
                                                            .then(
                                                                Commands.literal("-guaranteed")
                                                                    .executes(cs -> ExecuteRun.executeLoot_Fill(cs, false, "guaranteed"))
                                                            )
                                                            .executes(cs -> ExecuteRun.executeLoot_Fill(cs, false, "default"))
                                                    )
                                                    .suggests(CommandSuggest::suggestAllFileLootTable)
                                            )
                                            .suggests(CommandSuggest::suggestHitBlockPositionInZ)
                                    )
                                    .suggests(CommandSuggest::suggestHitBlockPositionInYZ)
                            )
                            .suggests(CommandSuggest::suggestHitBlockPosition)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.run.lootFill.get()))
            )
            .then(
                Commands.literal("create")
                    .then(
                        Commands.argument("to_path", StringArgumentType.word())
                            .suggests(CommandSuggest::suggestSavePath)
                            .executes(ExecuteCreate::executeLoot_Create)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.create.loot.get()))
            )
            .then(
                Commands.literal("delete")
                    .then(
                        Commands.argument("table_id", StringArgumentType.string())
                            .then(
                                Commands.literal("world")
                                    .executes(cs -> ExecuteDelete.executeLoot_Delete(cs, "world"))
                            )
                            .then(
                                Commands.literal("global")
                                    .executes(cs -> ExecuteDelete.executeLoot_Delete(cs, "global"))
                            )
                            .suggests(CommandSuggest::suggestAllFileLootTable)
                            .executes(cs -> ExecuteDelete.executeLoot_Delete(cs, "try"))
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.delete.loot.get()))
            )
            .then(
                Commands.literal("template")
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.template.loot.get()))
                    .executes(ExecuteTemplate::executeLoot_Template)
            )
            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.guide.loot.get()))
            .executes(ExecuteGuide::executeLoot);
    }
}
