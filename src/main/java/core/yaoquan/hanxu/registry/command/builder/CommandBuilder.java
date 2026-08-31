package core.yaoquan.hanxu.registry.command.builder;

import com.mojang.brigadier.CommandDispatcher;
import core.yaoquan.hanxu.api.PermissionHolder;
import core.yaoquan.hanxu.registry.command.execute.*;
import core.yaoquan.hanxu.registry.config.PermissionConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandBuilder {
    // Register core command chx to here:
    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("chx")
                // Subcommands.
                .then(
                    BuildGeneral.help()
                )
                .then(
                    BuildGeneral.detail()
                )
                .then(
                    BuildGeneral.license()
                )
                .then(
                    BuildGeneral.permission()
                )
                .then(
                    BuildTimer.build()
                )
                .then(
                    BuildScene.build()
                )
                .then(
                    BuildAttribute.build()
                )
                .then(
                    BuildVariable.build()
                )
                .then(
                    BuildLoot.build()
                )
                .then(
                    BuildWeather.build()
                )
                .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.guide.general.get()))
                .executes(ExecuteGuide::executeBare)
        );
    }
}
