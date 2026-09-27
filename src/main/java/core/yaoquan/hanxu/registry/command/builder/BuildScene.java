package core.yaoquan.hanxu.registry.command.builder;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import core.yaoquan.hanxu.api.PermissionHolder;
import core.yaoquan.hanxu.registry.command.CommandSuggest;
import core.yaoquan.hanxu.registry.command.execute.*;
import core.yaoquan.hanxu.registry.config.PermissionConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

class BuildScene {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("scene")
            .then(
                Commands.literal("help")
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.guide.scene.get()))
                    .executes(ExecuteGuide::executeScene_Help)
            )
            .then(
                Commands.literal("list")
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.information.scene.get()))
                    .executes(ExecuteInformation::executeScene_List)
            )
            .then(
                Commands.literal("play")
                    .then(
                        Commands.argument("scene_name", StringArgumentType.string())
                            .then(
                                Commands.argument("player_id", StringArgumentType.word())
                                    .suggests(CommandSuggest::suggestPlayer)
                                    .executes(ExecuteRun::executeScene_Play)
                            )
                            .suggests(CommandSuggest::suggestScene)
                            .executes(ExecuteRun::executeScene_Play)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.run.scenePlay.get()))
            )
            .then(
                Commands.literal("broadcast")
                    .then(
                        Commands.argument("scene_name", StringArgumentType.string())
                            .suggests(CommandSuggest::suggestScene)
                            .executes(ExecuteRun::executeScene_Broadcast)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.run.sceneBroadcast.get()))
            )
            .then(
                Commands.literal("delete")
                    .then(
                        Commands.argument("scene_name", StringArgumentType.string())
                            .then(
                                Commands.literal("world")
                                    .executes(cs -> ExecuteDelete.executeScene_Delete(cs, "world"))
                            )
                            .then(
                                Commands.literal("global")
                                    .executes(cs -> ExecuteDelete.executeScene_Delete(cs, "global"))
                            )
                            .suggests(CommandSuggest::suggestScene)
                            .executes(cs -> ExecuteDelete.executeScene_Delete(cs, "try"))
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.delete.scene.get()))
            )
            .then(
                Commands.literal("template")
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.template.scene.get()))
                    .executes(ExecuteTemplate::executeScene_Template)
            )
            .then(
                Commands.literal("create")
                    .then(
                        Commands.argument("to_path", StringArgumentType.string())
                            .suggests(CommandSuggest::suggestSavePath)
                            .executes(ExecuteCreate::executeScene_Create)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.create.scene.get()))
            )
            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.guide.scene.get()))
            .executes(ExecuteGuide::executeScene);
    }
}
