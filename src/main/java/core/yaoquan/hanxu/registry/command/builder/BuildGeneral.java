package core.yaoquan.hanxu.registry.command.builder;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import core.yaoquan.hanxu.api.PermissionHolder;
import core.yaoquan.hanxu.registry.command.CommandSuggest;
import core.yaoquan.hanxu.registry.command.execute.ExecuteConfirmation;
import core.yaoquan.hanxu.registry.command.execute.ExecuteGuide;
import core.yaoquan.hanxu.registry.command.execute.ExecuteInformation;
import core.yaoquan.hanxu.registry.command.execute.ExecutePermission;
import core.yaoquan.hanxu.registry.config.PermissionConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class BuildGeneral {
    public static LiteralArgumentBuilder<CommandSourceStack> help() {
        return Commands.literal("help")
            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.guide.general.get()))
            .executes(ExecuteGuide::executeHelp);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> detail() {
        return Commands.literal("detail")
            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.guide.general.get()))
            .executes(ExecuteGuide::executeDetail);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> license() {
        return Commands.literal("license")
            .then(
                Commands.literal("agree")
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs,0))
                    .executes(ExecuteConfirmation::executeLicense_Agree)
            )
            .then(
                Commands.literal("origin")
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs,0))
                    .executes(ExecuteGuide::executeLicense_Origin)
            )
            .then(
                Commands.literal("state")
                    .then(
                        Commands.argument("player_id", StringArgumentType.string())
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.guide.generalAdvanced.get()))
                            .suggests(CommandSuggest::suggestPlayer)
                            .executes(ExecuteInformation::executeAdvancedLicense_State)
                    )
                    .executes(ExecuteInformation::executeLicense_State)
            )
            .requires(cs -> PermissionHolder.Verify.hasPermission(cs,PermissionConfig.VALUE.guide.general.get()))
            .executes(ExecuteGuide::executeLicense);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> permission() {
        return Commands.literal("permission")
            .then(
                Commands.literal("player")
                    .then(
                        Commands.argument("player_id", StringArgumentType.string())
                            .then(
                                Commands.literal("set")
                                    .then(
                                        Commands.argument("level", IntegerArgumentType.integer())
                                            .executes(ExecutePermission::executePermission_Set)
                                    )
                            )
                            .suggests(CommandSuggest::suggestPlayer)
                            .executes(cs -> ExecutePermission.executePermission_Check(cs, "player"))
                    )
                    .executes(cs -> ExecutePermission.executePermission_Check(cs, "player"))
            )
            .then(
                Commands.literal("server")
                    .executes(cs -> ExecutePermission.executePermission_Check(cs, "server"))
            )
            .then(
                Commands.literal("player_first_grant")
                    .executes(cs -> ExecutePermission.executePermission_Check(cs, "player_first_grant"))
            )
            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.permission.general.get()));
    }
}
