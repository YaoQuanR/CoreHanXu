package core.yaoquan.hanxu.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.PermissionHolder;
import core.yaoquan.hanxu.api.TimeHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ModCommand {
    // Register core command chx to here:
    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("chx")
                        // Subcommands.
                        .then(
                                Commands.literal("help")
                                        .requires(cs -> PermissionHolder.hasPermission(cs,0))
                                        .executes(ModCommand::executeCommandHelp)
                        )
                        .then(
                                Commands.literal("detail")
                                        .requires(cs -> PermissionHolder.hasPermission(cs,0))
                                        .executes(ModCommand::executeCommandDetail)
                        )
                        .then(
                                Commands.literal("license")
                                        .then(
                                                Commands.literal("agree")
                                                        .requires(cs -> PermissionHolder.hasPermission(cs,0))
                                                        .executes(ModCommand::executeCommandLicense_Agree)
                                        )
                                        .then(
                                                Commands.literal("origin")
                                                        .requires(cs -> PermissionHolder.hasPermission(cs,0))
                                                        .executes(ModCommand::executeCommandLicense_Origin)
                                        )
                                        .requires(cs -> PermissionHolder.hasPermission(cs,0))
                                        .executes(ModCommand::executeCommandLicense)
                        )
                        .then(
                                Commands.literal("timer")
                                        .then(
                                                Commands.literal("help")
                                                        .requires(cs -> PermissionHolder.hasPermission(cs,1))
                                                        .executes(ModCommand::executeCommandTimer_Help)
                                        )
                                        .requires(cs -> PermissionHolder.hasPermission(cs,1))
                                        .executes(ModCommand::executeCommandTimer)
                        )
                        // Final execute father command.
                        .executes(ModCommand::executeCommandBare)
        );

        dispatcher.register(
                Commands.literal("chx-a")
                        // Subcommands.
                        .then(
                                Commands.literal("help")
                                        .requires(cs -> PermissionHolder.hasPermission(cs,2))
                                        .executes(ModCommand::executeAdminCommandHelp)
                        )
                        .then(
                                Commands.literal("detail")
                                        .requires(cs -> PermissionHolder.hasPermission(cs,2))
                                        .executes(ModCommand::executeCommandDetail)
                        )
                        .then(
                                Commands.literal("license")
                                        .then(
                                                Commands.literal("agree")
                                                        .requires(cs -> PermissionHolder.hasPermission(cs,2))
                                                        .executes(ModCommand::executeCommandLicense_Agree)
                                        )
                                        .then(
                                                Commands.literal("origin")
                                                        .requires(cs -> PermissionHolder.hasPermission(cs,2))
                                                        .executes(ModCommand::executeCommandLicense_Origin)
                                        )
                                        .requires(cs -> PermissionHolder.hasPermission(cs,2))
                                        .executes(ModCommand::executeCommandLicense)
                        )
                        .then(
                                Commands.literal("timer")
                                        .then(
                                                Commands.literal("help")
                                                        .requires(cs -> PermissionHolder.hasPermission(cs,2))
                                                        .executes(ModCommand::executeAdminCommandTimer_Help)
                                        )
                                        .then(
                                                Commands.literal("create")
                                                        .then(
                                                                Commands.argument("timer_id", StringArgumentType.word())
                                                                        .then(
                                                                                Commands.argument("time_amount", IntegerArgumentType.integer(1))
                                                                                        .then(
                                                                                                Commands.argument("time_unit", StringArgumentType.word())
                                                                                                        .executes(ModCommand::executeAdminCommandTimer_Create)
                                                                                        )
                                                                                        .executes(ModCommand::executeAdminCommandTimer_Create)
                                                                        )
                                                        )
                                                        .requires(cs -> PermissionHolder.hasPermission(cs,2))

                                        )
                                        .then(
                                                Commands.literal("read")
                                                        .then(
                                                                Commands.argument("timer_id", StringArgumentType.word())
                                                                        .then(
                                                                                Commands.argument("category", StringArgumentType.word())
                                                                                        .then(
                                                                                                Commands.argument("time_unit", StringArgumentType.word())
                                                                                                        .executes(ModCommand::executeAdminCommandTimer_Read)
                                                                                        )
                                                                                        .executes(ModCommand::executeAdminCommandTimer_Read)
                                                                        )
                                                        )
                                        )
                                        .requires(cs -> PermissionHolder.hasPermission(cs,2))
                                        .executes(ModCommand::executeCommandTimer)
                        )
                        .requires(cs -> PermissionHolder.hasPermission(cs,0))
                        .executes(ModCommand::executeAdminCommandBare)
        );
    }

    private static int executeCommandBare(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(
                Component.translatable("commands.chx.bare")
        );
        return 1;
    }

    private static int executeCommandHelp(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();

        if (player != null) {
            boolean agreedLicense = player.getPersistentData()
                    .getBoolean("core.yaoquan.hanxu.agreed_license")
                    .orElse(false);

            if (agreedLicense || PermissionHolder.hasPermission(context.getSource(), 2)) {
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_title"));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_page"));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.fixed.available_commands"));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_innertext1"));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_innertext2"));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_innertext3"));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_innertext4"));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_innertext5"));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_innertext6"));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_innertext7"));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_innertext8"));
            }
            else {
                PermissionHolder.sendMessageToNotAgreedLicense(context.getSource(), player);
            }
        }
        else {
            context.getSource().sendFailure(Component.translatable(("commands." +  CoreHanXu.MOD_ID +".not_player")));
        }

        return 1;
    }

    private static int executeCommandDetail(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.detail_title"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.detail_innertext1"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.detail_innertext2"));
        return 1;
    }

    private static int executeCommandLicense(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_title"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_innertext1"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_innertext2"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_innertext3"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_innertext4"));
        return 1;
    }

    private static int executeCommandLicense_Origin(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_title"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext1"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext2"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext3"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext4"));
        context.getSource().sendSystemMessage(Component.literal(""));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext5"));
        context.getSource().sendSystemMessage(Component.literal(""));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext6"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext7"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext8"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext9"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext10"));
        context.getSource().sendSystemMessage(Component.literal(""));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext11"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext12"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext13"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext14"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext15"));
        context.getSource().sendSystemMessage(Component.literal(""));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext16"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext17"));
        context.getSource().sendSystemMessage(Component.literal(""));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext18"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext19"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext20"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext21"));
        context.getSource().sendSystemMessage(Component.literal(""));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext22"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext23"));
        return 1;
    }

    private static int executeCommandLicense_Agree(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_agree"));
        if (context.getSource().getEntity() instanceof Player player) {
            player.getPersistentData()
                    .putBoolean("core.yaoquan.hanxu.agreed_license", true);
        }
        else {
            context.getSource().sendFailure(Component.translatable(("commands." +  CoreHanXu.MOD_ID +".not_player")));
        }
        return 1;
    }

    private static int executeCommandTimer(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.timer"));
        return 1;
    }

    private static int executeCommandTimer_Help(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.translatable(("commands." +  CoreHanXu.MOD_ID +".not_player")));
            return 0;
        }

        context.getSource().sendSystemMessage(Component.translatable("commands.chx.timer_help_title"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.fixed.available_commands"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.timer_help_innertext1"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.timer_help_innertext2"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.timer_help_read_argument1"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.timer_help_read_argument2"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.fixed.admin_reminder"));
        return 1;
    }

    private static int executeAdminCommandBare(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.bare1"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.bare2"));
        return 1;
    }

    private static int executeAdminCommandHelp(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.translatable(("commands." +  CoreHanXu.MOD_ID +".not_player")));
            return 0;
        }

        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_title"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_page"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.fixed.available_commands"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext1"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext2"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext3"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext4"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext5"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext6"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext7"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext8"));
        return 1;
    }

    private static int executeAdminCommandTimer_Help(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.translatable(("commands." +  CoreHanXu.MOD_ID +".not_player")));
            return 0;
        }

        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_title"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.fixed.available_commands"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext1"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext2"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_read_argument1"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_read_argument2"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext3"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_create_argument"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext4"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext5"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext6"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext7"));
        return 1;
    }

    private static int executeAdminCommandTimer_Create(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();

        // Receive arguments.
        String timerId = StringArgumentType.getString(context, "timer_id");
        int timeAmount = IntegerArgumentType.getInteger(context, "time_amount");
        String timeUnit;

        // If no unit, receive time data as ticks.
        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }


        // Check if player exist.
        if (player == null) {
            context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".not_player"));
            return 0;
        }
        // Check if timer exist.
        if (TimeHolder.returnRemainingTicks(timerId) != -1) {
            context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_already_exist"));
            return 0;
        }

        // Then register.
        switch (timeUnit) {
            case "t", "tick":
                TimeHolder.createTemplateTimer(timerId, timeAmount, callback -> callback.sendSystemMessage(
                        Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_time_out")
                                .append(Component.literal(" " + timerId))
                ));
                break;
            case "s", "second":
                TimeHolder.createTemplateTimerInSeconds(timerId, timeAmount, callback -> callback.sendSystemMessage(
                        Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_time_out")
                                .append(Component.literal(" " + timerId))
                ));
                break;
            case "m", "minute":
                TimeHolder.createTemplateTimerInMinutes(timerId, timeAmount, callback -> callback.sendSystemMessage(
                        Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_time_out")
                                .append(Component.literal(" " + timerId))
                ));
                break;
            case "h", "hour":
                TimeHolder.createTemplateTimerInHours(timerId, timeAmount, callback -> callback.sendSystemMessage(
                        Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_time_out")
                                .append(Component.literal(" " + timerId))
                ));
                break;
            default:
                context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".invalid_unit_argument"));
                return 0;
        }

        // Output message.
        context.getSource().sendSystemMessage(
                Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_created")
                        .append(Component.literal(" " + timerId + " -> " + timeAmount + " " + timeUnit))
                        .withColor(0x66FF66)
        );

        return 1;
    }

    private static int executeAdminCommandTimer_Read(CommandContext<CommandSourceStack> context) {
        // Receive arguments.
        String timerId = StringArgumentType.getString(context, "timer_id");
        String infoCategory = StringArgumentType.getString(context, "category");
        String timeUnit;

        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        switch (infoCategory) {
            case "remaining_time":
                int remainingTicks = TimeHolder.returnRemainingTicks(timerId);
                if (remainingTicks != -1) {
                    switch (timeUnit) {
                        case "t", "tick":
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_remaining_time")
                                            .append(Component.literal(" (" + timerId + "): " + remainingTicks))
                            );
                            break;
                        case "s", "second":
                            int remainingSeconds = TimeHolder.returnRemainingSeconds(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_remaining_time")
                                            .append(Component.literal(" (" + timerId + "): " + remainingSeconds))
                            );
                            break;
                        case "m", "minute":
                            int remainingMinutes = TimeHolder.returnRemainingMinutes(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_remaining_time")
                                            .append(Component.literal(" (" + timerId + "): " + remainingMinutes))
                            );
                            break;
                        case "h", "hour":
                            int remainingHours = TimeHolder.returnRemainingHours(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_remaining_time")
                                            .append(Component.literal(" (" + timerId + "): " + remainingHours))
                            );
                            break;
                        default:
                            context.getSource().sendFailure(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".invalid_unit_argument")
                            );
                            return 0;
                    }
                }
                else {
                    context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_not_exist"));
                    return 0;
                }
                break;
            case "initial_time":
                int initialTicks = TimeHolder.returnInitialTicks(timerId);
                if (initialTicks != -1) {
                    switch (timeUnit) {
                        case "t", "tick":
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_initial_time")
                                            .append(Component.literal(" (" + timerId + "): " + initialTicks))
                            );
                            break;
                        case "s", "second":
                            int initialSeconds = TimeHolder.returnInitialSeconds(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_initial_time")
                                            .append(Component.literal(" (" + timerId + "): " + initialSeconds))
                            );
                            break;
                        case "m", "minute":
                            int initialMinutes = TimeHolder.returnInitialMinutes(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_initial_time")
                                            .append(Component.literal(" (" + timerId + "): " + initialMinutes))
                            );
                            break;
                        case "h", "hour":
                            int initialHours = TimeHolder.returnInitialHours(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_initial_time")
                                            .append(Component.literal(" (" + timerId + "): " + initialHours))
                            );
                            break;
                        default:
                            context.getSource().sendFailure(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".invalid_unit_argument")
                            );
                            return 0;
                    }
                }
                else {
                    context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_not_exist"));
                    return 0;
                }
                break;
            case "state":
                boolean isCounting = TimeHolder.isItCounting(timerId);

                if (TimeHolder.returnRemainingTicks(timerId) != -1) {
                    context.getSource().sendSystemMessage(
                            Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_state")
                                    .append(Component.literal(" (" + timerId + "): " + (isCounting? "Counting" : "Stopping")))
                    );
                }
                else {
                    context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_not_exist"));
                    return 0;
                }
                break;
        }
        return 1;
    }
}
