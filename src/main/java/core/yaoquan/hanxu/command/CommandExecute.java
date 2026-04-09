package core.yaoquan.hanxu.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.PermissionHolder;
import core.yaoquan.hanxu.api.TimeHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

class CommandExecute {
    static int executeCommandBare(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(
                Component.translatable("commands.chx.bare")
                    .withColor(0xFFD700)
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
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_title").withColor(0xFFD700));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_page").withColor(0xFFD700));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.fixed.available_commands").withColor(0xFFD700));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_innertext1").withColor(0xFFD700));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_innertext2").withColor(0xFFD700));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_innertext3").withColor(0xFFD700));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_innertext4").withColor(0xFFD700));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_innertext5").withColor(0xFFD700));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_innertext6").withColor(0xFFD700));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_innertext7").withColor(0xFFD700));
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.help_innertext8").withColor(0xFFD700));
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
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.detail_title").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.detail_innertext1").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.detail_innertext2").withColor(0xFFD700));
        return 1;
    }

    static int executeCommandLicense(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_title").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_innertext1").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_innertext2").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_innertext3").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_innertext4").withColor(0xFFD700));
        return 1;
    }

    static int executeCommandLicense_Origin(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_title").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext1").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext2").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext3").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext4").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.literal("").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext5").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.literal("").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext6").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext7").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext8").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext9").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext10").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.literal("").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext11").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext12").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext13").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext14").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext15").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.literal("").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext16").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext17").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.literal("").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext18").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext19").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext20").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext21").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.literal("").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext22").withColor(0xFFFACD));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_origin_innertext23").withColor(0xFFFACD));
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
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer").withColor(0xFFD700));
        return 1;
    }

    static int executeAdminCommandBare(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.bare1").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.bare2").withColor(0xFFD700));
        return 1;
    }

    static int executeAdminCommandHelp(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.translatable(("commands." +  CoreHanXu.MOD_ID +".not_player")));
            return 0;
        }

        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_title").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_page").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.fixed.available_commands").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext1").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext2").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext3").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext4").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext5").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext6").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext7").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext8").withColor(0xFFD700));
        return 1;
    }

    static int executeAdminCommandTimer_Help(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.translatable(("commands." +  CoreHanXu.MOD_ID +".not_player")));
            return 0;
        }

        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_title").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.fixed.available_commands").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext1").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext2").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_read_argument1").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_read_argument2").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext3").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_create_argument").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext4").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext5").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext6").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext7").withColor(0xFFD700));
        return 1;
    }

    static int executeAdminCommandTimer_Template_Create(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();

        // Receive arguments.
        String timerId = StringArgumentType.getString(context, "timer_id");
        int timeAmount = IntegerArgumentType.getInteger(context, "time_amount");
        String timeUnit;
        String endBehavior = StringArgumentType.getString(context, "end_behavior");
        String behaviorContent = StringArgumentType.getString(context, "behavior_content");

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

        return forCreateTemplateTimer(context, timerId, timeUnit, timeAmount, endBehavior, behaviorContent);
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
                                            .withColor(0xFFD700)
                            );
                            break;
                        case "s", "second":
                            int remainingSeconds = TimeHolder.returnRemainingSeconds(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_remaining_time")
                                            .append(Component.literal(" (" + timerId + "): " + remainingSeconds + " " + timeUnit))
                                            .withColor(0xFFD700)
                            );
                            break;
                        case "m", "minute":
                            int remainingMinutes = TimeHolder.returnRemainingMinutes(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_remaining_time")
                                            .append(Component.literal(" (" + timerId + "): " + remainingMinutes + " " + timeUnit))
                                            .withColor(0xFFD700)
                            );
                            break;
                        case "h", "hour":
                            int remainingHours = TimeHolder.returnRemainingHours(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_remaining_time")
                                            .append(Component.literal(" (" + timerId + "): " + remainingHours + " " + timeUnit))
                                            .withColor(0xFFD700)
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
                                            .withColor(0xFFD700)
                            );
                            break;
                        case "s", "second":
                            int initialSeconds = TimeHolder.returnInitialSeconds(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_initial_time")
                                            .append(Component.literal(" (" + timerId + "): " + initialSeconds + " " + timeUnit))
                                            .withColor(0xFFD700)
                            );
                            break;
                        case "m", "minute":
                            int initialMinutes = TimeHolder.returnInitialMinutes(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_initial_time")
                                            .append(Component.literal(" (" + timerId + "): " + initialMinutes + " " + timeUnit))
                                            .withColor(0xFFD700)
                            );
                            break;
                        case "h", "hour":
                            int initialHours = TimeHolder.returnInitialHours(timerId);
                            context.getSource().sendSystemMessage(
                                    Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_read_initial_time")
                                            .append(Component.literal(" (" + timerId + "): " + initialHours + " " + timeUnit))
                                            .withColor(0xFFD700)
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
                                    .withColor(0xFFD700)
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
                            .withColor(0xFFD700)
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
        String endBehavior = StringArgumentType.getString(context, "end_behavior");
        String behaviorContent = StringArgumentType.getString(context, "behavior_content");

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

        return forCreateTemplateTimer(context,  timerId, timeUnit, selectedTimeAmount, endBehavior, behaviorContent);
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
                            .withColor(0xFFD700)
            );
        }
        else {
            context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_not_exist_or_already_instantiated"));
            return 0;
        }

        return 1;
    }

    private static int forCreateTemplateTimer(CommandContext<CommandSourceStack> context,
                                              String timerId, String timeUnit, int timeAmount,
                                              String endBehavior, String behaviorContent) {
        // Build callback according to endBehavior;
        // ?(You are advised to use API "createTemplateTimer" to build advanced timer behavior).
        Consumer<ServerPlayer> callback;
        switch (endBehavior) {
            case "e", "execute":
                // Pass create only if selector used @r/a/e.
                if (behaviorContent.contains("@s") || behaviorContent.contains("@p")) {
                    context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".invalid_selector_used"));
                    return 0;
                }

                // Then register command execution into source stack;
                // !(If NO online player exist, selector which used @r/a will lose their effect on command execution).
                callback = player -> {
                    @SuppressWarnings("resource")
                    MinecraftServer server = player.level().getServer();
                    server.getCommands().performPrefixedCommand(
                            // Execute command on behalf of control panel.
                            server.createCommandSourceStack(), behaviorContent
                    );
                };
                break;
            case "r", "remind":
                // Send message when time out:
                // Modified information.
                if (behaviorContent != null) {
                    callback = player -> player.sendSystemMessage(
                            Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_time_out")
                                    .append(Component.literal(" " + timerId))
                                    .withColor(0xFFD700)
                    );
                }
                // Or default information.
                else {
                    callback = player -> player.sendSystemMessage(
                            Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_time_out")
                                    .append(Component.literal(" " + timerId))
                                    .withColor(0xFFD700)
                    );
                    context.getSource().sendSystemMessage(Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_default_end_behavior").withColor(0xFFD700));
                }
                break;
            case "n", "null":
                // Nothing to do, same as default.
            default:
                callback = player -> {};
                break;
        }

        // Then register.
        switch (timeUnit) {
            case "t", "tick":
                TimeHolder.createTemplateTimer(timerId, timeAmount, callback);
                break;
            case "s", "second":
                TimeHolder.createTemplateTimerInSeconds(timerId, timeAmount, callback);
                break;
            case "m", "minute":
                TimeHolder.createTemplateTimerInMinutes(timerId, timeAmount, callback);
                break;
            case "h", "hour":
                TimeHolder.createTemplateTimerInHours(timerId, timeAmount, callback);
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
