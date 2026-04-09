package core.yaoquan.hanxu.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.PermissionHolder;
import core.yaoquan.hanxu.api.TimeHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

class CommandExecute {
    static int executeCommandBare(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(
                Component.translatable("commands.chx.bare")
        );
        return 1;
    }

    static int executeCommandHelp(CommandContext<CommandSourceStack> context) {
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

    static int executeCommandDetail(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.detail_title"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.detail_innertext1"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.detail_innertext2"));
        return 1;
    }

    static int executeCommandLicense(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_title"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_innertext1"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_innertext2"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_innertext3"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_innertext4"));
        return 1;
    }

    static int executeCommandLicense_Origin(CommandContext<CommandSourceStack> context) {
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

    static int executeCommandLicense_Agree(CommandContext<CommandSourceStack> context) {
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

    static int executeAdminCommandTimer(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer"));
        return 1;
    }

    static int executeAdminCommandBare(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.bare1"));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.bare2"));
        return 1;
    }

    static int executeAdminCommandHelp(CommandContext<CommandSourceStack> context) {
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

    static int executeAdminCommandTimer_Help(CommandContext<CommandSourceStack> context) {
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

    static int executeAdminCommandTimer_Template_Create(CommandContext<CommandSourceStack> context) {
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

        // Check if timer exist.
        if (TimeHolder.returnRemainingTicks(timerId) != -1) {
            context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_already_exist"));
            return 0;
        }

        return forCreateTemplateTimer(context, timerId, timeUnit, timeAmount);
    }

    static int executeAdminCommandTimer_Template_Read(CommandContext<CommandSourceStack> context) {
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
                                            .append(Component.literal(" (" + timerId + "): " + remainingTicks + " " + timeUnit))
                            );
                            break;
                        case "s", "second":
                            int remainingSeconds = TimeHolder.returnRemainingSeconds(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_remaining_time")
                                            .append(Component.literal(" (" + timerId + "): " + remainingSeconds + " " + timeUnit))
                            );
                            break;
                        case "m", "minute":
                            int remainingMinutes = TimeHolder.returnRemainingMinutes(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_remaining_time")
                                            .append(Component.literal(" (" + timerId + "): " + remainingMinutes + " " + timeUnit))
                            );
                            break;
                        case "h", "hour":
                            int remainingHours = TimeHolder.returnRemainingHours(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_remaining_time")
                                            .append(Component.literal(" (" + timerId + "): " + remainingHours + " " + timeUnit))
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
                                            .append(Component.literal(" (" + timerId + "): " + initialTicks + " " + timeUnit))
                            );
                            break;
                        case "s", "second":
                            int initialSeconds = TimeHolder.returnInitialSeconds(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_initial_time")
                                            .append(Component.literal(" (" + timerId + "): " + initialSeconds + " " + timeUnit))
                            );
                            break;
                        case "m", "minute":
                            int initialMinutes = TimeHolder.returnInitialMinutes(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_initial_time")
                                            .append(Component.literal(" (" + timerId + "): " + initialMinutes + " " + timeUnit))
                            );
                            break;
                        case "h", "hour":
                            int initialHours = TimeHolder.returnInitialHours(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_initial_time")
                                            .append(Component.literal(" (" + timerId + "): " + initialHours + " " + timeUnit))
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

    static int executeAdminCommandTimer_Template_Delete(CommandContext<CommandSourceStack> context) {
        // Receive argument.
        String timerId = StringArgumentType.getString(context, "timer_id");

        boolean isDeleted = TimeHolder.removeTemplateTimer(timerId);
        if (isDeleted) {
            context.getSource().sendSystemMessage(
                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_deleted")
                        .append(Component.literal(" (" + timerId + ")"))
            );
        }
        else {
            context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_not_exist"));
        }

        return 1;
    }

    static int executeAdminCommandTimer_Template_CreateRange(CommandContext<CommandSourceStack> context) {
        // Receive arguments.
        String timerId = StringArgumentType.getString(context, "timer_id");
        int timeFirstRange = IntegerArgumentType.getInteger(context, "time_first_range");
        int timeSecondRange = IntegerArgumentType.getInteger(context, "time_second_range");
        String timeUnit;

        // If no unit, receive time data as ticks.
        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        // Compare.
        int timeLowerRange = Math.min(timeFirstRange, timeSecondRange);
        int timeUpperRange = Math.max(timeFirstRange, timeSecondRange);

        // Take one random number between the range.
        int selectedTimeAmount = ThreadLocalRandom.current().nextInt(timeLowerRange, timeUpperRange + 1);

        // Check if timer exist.
        if (TimeHolder.returnRemainingTicks(timerId) != -1) {
            context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_already_exist"));
            return 0;
        }

        return forCreateTemplateTimer(context,  timerId, timeUnit, selectedTimeAmount);
    }

    static int executeAdminCommandTimer_Instance_Apply(CommandContext<CommandSourceStack> context) {
        // Receive arguments.
        String templateTimerId = StringArgumentType.getString(context, "template_timer_id");
        String applyTarget = StringArgumentType.getString(context, "apply_target");

        // Analysis to UUID.
        UUID targetUUID = forAnalysisTargetUUID(context, applyTarget);

        String displayTarget;
        if (Objects.equals(applyTarget, "0")) {
            displayTarget = "Global";
        }
        else if (Objects.equals(applyTarget, "1")) {
            displayTarget = "Temporary";
        }
        else {
            displayTarget = applyTarget;
        }

        // Determine if target exist.
        if (targetUUID == null) {
            context.getSource().sendFailure(
                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".target_not_exist")
                        .append(Component.literal(" (" + displayTarget + ")"))
            );
            return 0;
        }

        // Determine if template timer exist and if instance timer exist, then register (Copy).
        if (TimeHolder.registerToInstance(targetUUID, templateTimerId)) {
            context.getSource().sendSystemMessage(
                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_instantiated")
                        .append(Component.literal(" " + templateTimerId + " -> " + displayTarget))
            );
        }
        else {
            context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_not_exist_or_already_instantiated"));
            return 0;
        }

        return 1;
    }

    private static int forCreateTemplateTimer(CommandContext<CommandSourceStack> context, String timerId, String timeUnit, int timeAmount) {
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

    private static UUID forAnalysisTargetUUID(CommandContext<CommandSourceStack> context, String targetString) {
        if ("0".equals(targetString)) {
            return TimeHolder.GLOBAL_UUID;
        }
        else if ("1".equals(targetString)) {
            return TimeHolder.TEMPORARY_UUID;
        }

        CommandSourceStack source = context.getSource();
        if (source.getEntity() instanceof ServerPlayer) {
            for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
                if (player.getName().getString().equals(targetString)) {
                    return player.getUUID();
                }
            }
        }

        return null;
    }
}
