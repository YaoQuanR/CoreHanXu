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
import net.neoforged.neoforge.server.ServerLifecycleHooks;

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
        if (context.getSource().getEntity() instanceof Player player) {
            if (!PermissionHolder.returnLicenseState(player)) {
                context.getSource().sendSystemMessage(Component.translatable("commands.chx.license_agree").withColor(0xFFFACD));
                player.getPersistentData()
                    .putBoolean("core.yaoquan.hanxu.agreed_license", true);
            }
            else {
                context.getSource().sendFailure(Component.translatable("commands.chx.license_already_agreed"));
            }
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
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext9").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.help_innertext10").withColor(0xFFD700));
        return 1;
    }

    static int executeAdminCommandTimer_Help(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.translatable(("commands." +  CoreHanXu.MOD_ID +".not_player")));
            return 0;
        }

        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_title").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_introduction").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx.fixed.available_commands").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext1").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_create_argument1").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext2").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_create-range_argument1").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext3").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext4").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext5").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_create_argument2").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext6").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_create-range_argument2").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext7").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext8").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext9").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext10").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext11").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext12").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext13").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_modify_argument").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_innertext14").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_read_argument1").withColor(0xFFD700));
        context.getSource().sendSystemMessage(Component.translatable("commands.chx-a.timer_help_read_argument2").withColor(0xFFD700));
        return 1;
    }

    static int executeAdminCommandPermissionCheck(CommandContext<CommandSourceStack> context, String target) {
        String playerId;
        try {
            playerId = StringArgumentType.getString(context, "player_id");
        }
        catch (IllegalArgumentException e) {
            playerId = "null";
        }

        switch (target) {
            case "player" -> {
                UUID playerUUID = forAnalysisTargetUUID(context, playerId);
                if (playerUUID == null) {
                    context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".target_not_exist"));
                    return 0;
                }
                else {
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    if (server == null) {
                        return 0;
                    }
                    ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
                    if (player == null) {
                        context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".target_not_exist"));
                        return 0;
                    }

                    int permissionLevel = PermissionHolder.returnPlayerPermissionLevel(player);
                    context.getSource().sendSystemMessage(Component.literal(String.valueOf(permissionLevel)).withColor(0xFFD700));
                }
            }
            case "server" -> {
                int commandblockPermissionLevel = PermissionHolder.returnCommandBlockOverridePermissionLevel();
                context.getSource().sendSystemMessage(Component.literal(String.valueOf(commandblockPermissionLevel)).withColor(0xFFD700));
            }
            case "player_override" -> {
                int overridePlayerPermissionLevel = PermissionHolder.returnPlayerOverridePermissionLevel();
                context.getSource().sendSystemMessage(Component.literal(String.valueOf(overridePlayerPermissionLevel)).withColor(0xFFD700));
            }
            default -> {
                context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".undefined_operation_category"));
                return 0;
            }
        }
        return 1;
    }

    static int executeAdminCommandLicense_State(CommandContext<CommandSourceStack> context) {
        String playerId = StringArgumentType.getString(context, "player_id");
        UUID playerUUID = forAnalysisTargetUUID(context, playerId);
        if (playerUUID == null) {
            context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".target_not_exist"));
            return 0;
        }
        else {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) {
                return 0;
            }
            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
            if (player == null) {
                context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".target_not_exist"));
                return 0;
            }
            boolean state = PermissionHolder.returnLicenseState(player);
            context.getSource().sendSystemMessage(Component.literal(String.valueOf(state)).withColor(0xFFD700));
            return 1;
        }
    }

    static int executeAdminCommandTimer_Template_Create(CommandContext<CommandSourceStack> context, String endBehavior) {
        // Receive arguments.
        String timerId = StringArgumentType.getString(context, "timer_id");
        int timeAmount = IntegerArgumentType.getInteger(context, "time_amount");
        String timeUnit;
        String behaviorContent;

        // If no unit, receive time data as ticks.
        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        try {
            behaviorContent = StringArgumentType.getString(context, "behavior_content");
        }
        catch (IllegalArgumentException e) {
            behaviorContent = ".";
        }

        // Check if timer exist.
        if (TimeHolder.returnRemainingTimeFromTemplate(timerId, timeUnit) != -1) {
            context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_already_exist"));
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

        return forTimerRead(context, timerId, "", timeUnit, infoCategory, "template");
    }

    static int executeAdminCommandTimer_Template_Delete(CommandContext<CommandSourceStack> context) {
        // Receive argument.
        String timerId = StringArgumentType.getString(context, "timer_id");

        boolean isDeleted = TimeHolder.deleteTemplateTimer(timerId);
        if (isDeleted) {
            context.getSource().sendSystemMessage(
                    Component.translatable("commands.chx-a.timer_deleted")
                            .append(Component.literal(" (" + timerId + ")"))
                            .withColor(0xFFD700)
            );
        } else {
            context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_not_exist"));
        }

        return 1;
    }

    static int executeAdminCommandTimer_Template_CreateRange(CommandContext<CommandSourceStack> context, String endBehavior) {
        // Receive arguments.
        String timerId = StringArgumentType.getString(context, "timer_id");
        int timeFirstRange = IntegerArgumentType.getInteger(context, "time_first_range");
        int timeSecondRange = IntegerArgumentType.getInteger(context, "time_second_range");
        String timeUnit;
        String behaviorContent;

        // If no unit, receive time data as ticks.
        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        try {
            behaviorContent = StringArgumentType.getString(context, "behavior_content");
        }
        catch (IllegalArgumentException e) {
            behaviorContent = ".";
        }

        int selectedTimeAmount = compareAndSelect(timeFirstRange, timeSecondRange);

        // Check if timer exist.
        if (TimeHolder.returnRemainingTimeFromTemplate(timerId, timeUnit) != -1) {
            context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_already_exist"));
            return 0;
        }

        return forCreateTemplateTimer(context, timerId, timeUnit, selectedTimeAmount, endBehavior, behaviorContent);
    }

    static int executeAdminCommandTimer_Template_List(CommandContext<CommandSourceStack> context) {
        String[] templateIds = TimeHolder.returnAllTemplateIds();

        return forDisplayIdList(context, templateIds);
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
                    Component.translatable("commands.chx-a.timer_instantiated")
                            .append(Component.literal(" " + templateTimerId + " -> " + displayTarget))
                            .withColor(0x66FF66)
            );
        }
        else {
            context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_not_exist_or_already_instantiated"));
            return 0;
        }

        return 1;
    }

    static int executeAdminCommandTimer_Instance_Create(CommandContext<CommandSourceStack> context, String endBehavior) {
        // Receive arguments.
        String timerId = StringArgumentType.getString(context, "timer_id");
        String masterString = StringArgumentType.getString(context, "master_id");
        int timeAmount = IntegerArgumentType.getInteger(context, "time_amount");
        String timeUnit;
        String behaviorContent;

        // Receive optional arguments.
        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        try {
            behaviorContent = StringArgumentType.getString(context, "behavior_content");
        }
        catch (IllegalArgumentException e) {
            behaviorContent = ".";
        }

        return forCreateInstanceTimer(context, timerId, masterString, timeUnit, timeAmount, endBehavior, behaviorContent);
    }

    static int executeAdminCommandTimer_Instance_CreateRange(CommandContext<CommandSourceStack> context, String endBehavior) {
        // Receive arguments.
        String timerId = StringArgumentType.getString(context, "timer_id");
        String masterString = StringArgumentType.getString(context, "master_id");
        int timeFirstRange = IntegerArgumentType.getInteger(context, "time_first_range");
        int timeSecondRange = IntegerArgumentType.getInteger(context, "time_second_range");
        String timeUnit;
        String behaviorContent;

        // If no unit, receive time data as ticks.
        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        try {
            behaviorContent = StringArgumentType.getString(context, "behavior_content");
        }
        catch (IllegalArgumentException e) {
            behaviorContent = ".";
        }

        int selectedTimeAmount = compareAndSelect(timeFirstRange, timeSecondRange);

        return forCreateInstanceTimer(context, timerId, masterString, timeUnit, selectedTimeAmount, endBehavior, behaviorContent);
    }
    
    static int executeAdminCommandTimer_Instance_Start(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        return forOperatingInstanceTimer(context, timerId, masterString, "start");
    }

    static int executeAdminCommandTimer_Instance_Stop(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        return forOperatingInstanceTimer(context, timerId, masterString, "stop");
    }

    static int executeAdminCommandTimer_Instance_Reset(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        return forOperatingInstanceTimer(context, timerId, masterString, "reset");
    }

    static int executeAdminCommandTimer_Instance_Delete(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        return forOperatingInstanceTimer(context, timerId, masterString, "delete");
    }

    static int executeAdminCommandTimer_Instance_Modify(CommandContext<CommandSourceStack> context, String category) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        UUID masterId = forAnalysisTargetUUID(context, masterString);

        if (masterId == null) {
            context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".target_not_exist"));
            return 0;
        }

        int timeAmount = IntegerArgumentType.getInteger(context, "time_amount");
        String timeUnit;

        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        if (TimeHolder.modifyInstanceTimer(masterId, timerId, timeAmount, timeUnit, category)) {
            context.getSource().sendSystemMessage(
                    Component.translatable("commands.chx-a.timer_success_modification")
                            .append(Component.literal(" (" + timerId + " -> " + masterString + "): " + category + " " + timeAmount + " " + timeUnit))
                            .withColor(0xFFD700)
            );
            return 1;
        }
        else {
            context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_not_exist"));
            return 0;
        }
    }

    static int executeAdminCommandTimer_Instance_Read(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");
        String infoCategory = StringArgumentType.getString(context, "category");
        String timeUnit;

        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        return forTimerRead(context, timerId, masterString, timeUnit, infoCategory, "instance");
    }

    static int executeAdminCommandTimer_Instance_List(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");

        UUID masterId = forAnalysisTargetUUID(context, masterString);

        if (masterId == null) {
            context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".target_not_exist"));
            return 0;
        }

        String[] instanceIds = TimeHolder.returnAllInstanceIds(masterId);

        return forDisplayIdList(context, instanceIds);
    }

    static int executeAdminCommandDisplay_Info_Timer(CommandContext<CommandSourceStack> context, boolean state) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        UUID masterId = forAnalysisTargetUUID(context, masterString);
        if (masterId == null) {
            context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".target_not_exist"));
            return 0;
        }

        TimeHolder.displayToInfoPage(masterId, timerId, state);
        context.getSource().sendSystemMessage(
                Component.literal("[HX] " + timerId + " ")
                        .append(Component.translatable("commands." + CoreHanXu.MOD_ID + ".has_changed_to"))
                        .append(Component.literal(" " + state))
                        .withColor(0x66FF66)
        );

        return 1;
    }

    static UUID forAnalysisTargetUUID(CommandContext<CommandSourceStack> context, String targetString) {
        if ("-global".equals(targetString) || "-g".equals(targetString)) {
            return TimeHolder.GLOBAL_UUID;
        }
        else if ("-temporary".equals(targetString) || "-t".equals(targetString)) {
            return TimeHolder.TEMPORARY_UUID;
        }
        else if ("-me".equals(targetString) || "-m".equals(targetString)) {
            if (context.getSource().getEntity() instanceof ServerPlayer player) {
                return player.getUUID();
            }
            else {
                return null;
            }
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

    private static int forCreateTemplateTimer(CommandContext<CommandSourceStack> context,
                                              String timerId, String timeUnit, int timeAmount,
                                              String endBehavior, String behaviorContent) {
        // Create callback.
        Consumer<ServerPlayer> callback = createCallback(context, timerId, endBehavior, behaviorContent);

        // Then register.
        switch (timeUnit) {
            case "t", "tick", "s", "second", "m", "minute", "h", "hour":
                TimeHolder.createTemplateTimer(timerId, timeAmount, timeUnit, callback, endBehavior, behaviorContent);
                break;
            default:
                context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".invalid_unit_argument"));
                return 0;
        }

        // Output message.
        context.getSource().sendSystemMessage(
                Component.translatable("commands.chx-a.timer_created")
                        .append(Component.literal(" " + timerId + " -> " + timeAmount + " " + timeUnit))
                        .withColor(0x66FF66)
        );
        switch (endBehavior) {
            case "e", "execute":
                context.getSource().sendSystemMessage(
                        Component.translatable("commands.chx-a.timer_with_execute_behavior")
                                .append(Component.literal(": " + behaviorContent))
                                .withColor(0x66FF66)
                );
                break;
            case "r", "remind":
                context.getSource().sendSystemMessage(
                        Component.translatable("commands.chx-a.timer_with_remind_behavior")
                                .append(Component.literal(": " + behaviorContent))
                                .withColor(0x66FF66)
                );
                break;
            default:
                break;
        }

        return 1;
    }

    private static int forOperatingInstanceTimer(CommandContext<CommandSourceStack> context,
                                                 String timerId, String masterString,
                                                 String categoryOfOperation) {
        // Get UUID.
        UUID masterId = forAnalysisTargetUUID(context, masterString);

        // Check if target master existed.
        if (masterId == null) {
            context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".target_not_exist"));
            return 0;
        }

        switch (categoryOfOperation) {
            case "start":
                // Then start.
                if (!TimeHolder.startInstanceTimer(masterId, timerId)) {
                    context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_unable_to_start"));
                    return 0;
                }

                // Send success message.
                int startingTime = TimeHolder.returnRemainingTimeFromInstance(masterId, timerId, "tick");
                String endBehavior = TimeHolder.returnInstanceEndBehavior(masterId, timerId);
                context.getSource().sendSystemMessage(
                        Component.translatable("commands.chx-a.timer_started").withColor(0x66FF66)
                );
                context.getSource().sendSystemMessage(
                        Component.literal(" (" + timerId + " -> " + masterString + "): " + startingTime + " tick(s) ->> " + endBehavior)
                                .withColor(0x66FF66)
                );

                return 1;
            case "stop":
                // Then stop.
                if (!TimeHolder.stopInstanceTimer(masterId, timerId)) {
                    context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_unable_to_stop"));
                    return 0;
                }

                // Send success message.
                int remainingTime = TimeHolder.returnRemainingTimeFromInstance(masterId, timerId, "tick");
                context.getSource().sendSystemMessage(
                        Component.translatable("commands.chx-a.timer_stopped").withColor(0x66FF66)
                );
                context.getSource().sendSystemMessage(
                        Component.literal(" (" + timerId + " -> " + masterString + "): " + remainingTime + " tick(s)")
                                .withColor(0x66FF66)
                );

                return 1;
            case "reset":
                // Then reset.
                if (!TimeHolder.resetInstanceTimer(masterId, timerId)) {
                    context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_unable_to_reset"));
                    return 0;
                }

                // Send success message.
                int initialTime = TimeHolder.returnInitialTimeFromInstance(masterId, timerId, "tick");
                context.getSource().sendSystemMessage(
                        Component.translatable("commands.chx-a.timer_reset").withColor(0x66FF66)
                );
                context.getSource().sendSystemMessage(
                        Component.literal(" (" + timerId + " -> " + masterString + "): " + initialTime + " tick(s)")
                                .withColor(0x66FF66)
                );

                return 1;
            case "delete":
                // Then delete.
                if (!TimeHolder.deleteInstanceTimer(masterId, timerId)) {
                    context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_unable_to_delete_instance"));
                }

                // Send success message.
                context.getSource().sendSystemMessage(
                        Component.translatable("commands.chx-a.timer_deleted")
                                .withColor(0x66FF66)
                );
                context.getSource().sendSystemMessage(
                        Component.literal(" (" + timerId + " -> " + masterString + ")")
                                .withColor(0x66FF66)
                );

                return 1;
            default:
                context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".undefined_operation_category"));
                return 0;
        }
    }

    private static int forTimerRead(CommandContext<CommandSourceStack> context,
                                    String timerId, String masterString, String timeUnit,
                                    String infoCategory, String timerCategory) {
        if (timerCategory.equals("template")) {
            switch (infoCategory) {
                case "remaining_time":
                    int remainingTime = TimeHolder.returnRemainingTimeFromTemplate(timerId, timeUnit);
                    if (remainingTime != -1) {
                        context.getSource().sendSystemMessage(
                                Component.translatable("commands.chx-a.timer_read_remaining_time")
                                        .append(Component.literal(" (" + timerId + "): " + remainingTime + " " + timeUnit))
                                        .withColor(0xFFD700)
                        );
                    }
                    else {
                        context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_not_exist"));
                        return 0;
                    }
                    break;
                case "initial_time":
                    int initialTime = TimeHolder.returnInitialTimeFromTemplate(timerId, timeUnit);
                    if (initialTime != -1) {
                        context.getSource().sendSystemMessage(
                                Component.translatable("commands.chx-a.timer_read_initial_time")
                                        .append(Component.literal(" (" + timerId + "): " + initialTime + " " + timeUnit))
                                        .withColor(0xFFD700)
                        );
                    }
                    else {
                        context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_not_exist"));
                        return 0;
                    }
                    break;
                case "state":
                    boolean isCounting = TimeHolder.isTemplateTimerCounting(timerId);

                    if (TimeHolder.returnRemainingTimeFromTemplate(timerId, timeUnit) != -1) {
                        context.getSource().sendSystemMessage(
                                Component.translatable("commands.chx-a.timer_state")
                                        .append(Component.literal(" (" + timerId + "): " + (isCounting? "Counting" : "Stopping")))
                                        .withColor(0xFFD700)
                        );
                    }
                    else {
                        context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_not_exist"));
                        return 0;
                    }
                    break;
                case "end_behavior":
                    String endBehavior = TimeHolder.returnTemplateEndBehavior(timerId);
                    String behaviorContent = TimeHolder.returnTemplateBehaviorContent(timerId);

                    if (TimeHolder.returnRemainingTimeFromTemplate(timerId, timeUnit) != -1) {
                        if (endBehavior == null) {
                            endBehavior = "null";
                        }

                        context.getSource().sendSystemMessage(
                                Component.literal("(" + timerId + ") <<- ")
                                        .append(endBehavior)
                                        .append(Component.literal(behaviorContent == null? "" : (" : " + behaviorContent)))
                                        .withColor(0xFFD700)
                        );
                    }
                    else {
                        context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_not_exist"));
                        return 0;
                    }
                    break;
                default:
                    return 0;
            }
            return 1;
        }
        else if (timerCategory.equals("instance")) {
            UUID masterId = forAnalysisTargetUUID(context, masterString);

            switch (infoCategory) {
                case "remaining_time":
                    int remainingTime = TimeHolder.returnRemainingTimeFromInstance(masterId, timerId, timeUnit);
                    if (remainingTime != -1) {
                        context.getSource().sendSystemMessage(
                                Component.translatable("commands.chx-a.timer_read_remaining_time")
                                        .append(Component.literal(" (" + timerId + " -> " + masterString + "): " + remainingTime + " " + timeUnit))
                                        .withColor(0xFFD700)
                        );
                    }
                    else {
                        context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_not_exist"));
                        return 0;
                    }
                    break;
                case "initial_time":
                    int initialTime = TimeHolder.returnInitialTimeFromInstance(masterId, timerId, timeUnit);
                    if (initialTime != -1) {
                        context.getSource().sendSystemMessage(
                                Component.translatable("commands.chx-a.timer_read_initial_time")
                                        .append(Component.literal(" (" + timerId + "->" + masterString + "): " + initialTime + " " + timeUnit))
                                        .withColor(0xFFD700)
                        );
                    }
                    else {
                        context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_not_exist"));
                        return 0;
                    }
                    break;
                case "state":
                    boolean isCounting = TimeHolder.isInstanceTimerCounting(masterId, timerId);
                    if (TimeHolder.returnRemainingTimeFromInstance(masterId, timerId, timeUnit) != -1) {
                        context.getSource().sendSystemMessage(
                                Component.translatable("commands.chx-a.timer_state")
                                        .append(Component.literal(" (" + timerId + "): " + (isCounting? "Counting" : "Stopping")))
                                        .withColor(0xFFD700)
                        );
                    }
                    else {
                        context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_not_exist"));
                        return 0;
                    }
                    break;
                case "end_behavior":
                    String endBehavior = TimeHolder.returnInstanceEndBehavior(masterId, timerId);
                    String behaviorContent = TimeHolder.returnInstanceBehaviorContent(masterId, timerId);
                    
                    if (TimeHolder.returnRemainingTimeFromInstance(masterId, timerId, timeUnit) != -1) {
                        if (endBehavior == null) {
                            endBehavior = "null";
                        }

                        context.getSource().sendSystemMessage(
                                Component.literal("(" + timerId + " -> " + masterString + ") <<- ")
                                        .append(endBehavior)
                                        .append(Component.literal(behaviorContent == null? "" : (" : " + behaviorContent)))
                                        .withColor(0xFFD700)
                        );
                    }
                    else {
                        context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_not_exist"));
                        return 0;
                    }
                    break;
                default:
                    return 0;
            }
            return 1;
        }
        else {
            context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".undefined_operation_category"));
            return 0;
        }
    }

    private static int forDisplayIdList(CommandContext<CommandSourceStack> context, String[] idList) {
        if (idList.length == 0) {
            context.getSource().sendFailure(Component.translatable("commands.chx-a.timer_not_exist"));
            return 0;
        }

        context.getSource().sendSystemMessage(
                Component.translatable("commands.chx-a.timer_list_title").withColor(0xFFD700)
        );
        for (String id : idList) {
            context.getSource().sendSystemMessage(Component.literal(id).withColor(0xFFD700));
        }

        return 1;
    }

    private static int forCreateInstanceTimer(CommandContext<CommandSourceStack> context,
                                              String timerId, String masterString,
                                              String timeUnit, int timeAmount,
                                              String endBehavior, String behaviorContent) {
        // Analysis UUID.
        UUID masterId = forAnalysisTargetUUID(context, masterString);

        // Create callback.
        Consumer<ServerPlayer> callback = createCallback(context, timerId, endBehavior, behaviorContent);
        if (callback == null) {
            return 0;
        }

        // Then register if timer not yet created.
        if (!TimeHolder.createInstanceTimer(masterId, timerId, timeAmount, timeUnit, callback, endBehavior, behaviorContent)) {
            context.getSource().sendSystemMessage(
                    Component.translatable("commands.chx-a.timer_already_exist").withColor(0xFFD700)
            );
            return 0;
        }

        // Output message.
        context.getSource().sendSystemMessage(
            Component.translatable("commands.chx-a.timer_created")
                .append(Component.literal(" " + timerId + " -> " + timeAmount + " " + timeUnit))
                .withColor(0x66FF66)
        );
        switch (endBehavior) {
            case "e", "execute":
                context.getSource().sendSystemMessage(
                    Component.translatable("commands.chx-a.timer_with_execute_behavior")
                        .append(Component.literal(": " + behaviorContent))
                        .withColor(0x66FF66)
                );
                break;
            case "r", "remind":
                context.getSource().sendSystemMessage(
                    Component.translatable("commands.chx-a.timer_with_remind_behavior")
                        .append(Component.literal(": " + behaviorContent))
                        .withColor(0x66FF66)
                );
                break;
            default:
                break;
        }

        return 1;
    }

    private static Consumer<ServerPlayer> createCallback(CommandContext<CommandSourceStack> context, String timerId, String endBehavior, String behaviorContent) {
        // Build callback according to endBehavior;
        // ?(You are advised to use API "createTemplateTimer" to build advanced timer behavior).
        Consumer<ServerPlayer> callback;
        switch (endBehavior) {
            case "e", "execute":
                // Pass create only if selector used @r/a/e.
                if (behaviorContent.contains("@s") || behaviorContent.contains("@p")) {
                    context.getSource().sendFailure(Component.translatable("commands." + CoreHanXu.MOD_ID + ".invalid_selector_used"));
                    return null;
                }

                // Then register command execution into source stack;
                // !(If NO online player exist, selector which used @r/a will lose their effect on command execution).
                callback = player -> {
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    if (server != null) {
                        String callbackCommand = behaviorContent.startsWith("/")? behaviorContent : ("/" + behaviorContent);
                        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), callbackCommand);
                    }
                };
                break;
            case "r", "remind":
                // Send message when time out:
                // Modified information.
                if (behaviorContent != null) {
                    callback = player -> {
                        if (player != null) {
                            player.sendSystemMessage(Component.literal(behaviorContent));
                        }
                        // Else broadcast to everyone.
                        else {
                            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                            if (server != null) {
                                server.getPlayerList().getPlayers().forEach(p -> {
                                    p.sendSystemMessage(Component.literal(behaviorContent));
                                });
                            }
                        }
                    };
                }
                // Or default information.
                else {
                    callback = player -> {
                        if (player != null) {
                            player.sendSystemMessage(
                                Component.translatable("commands.chx-a.timer_time_out")
                                    .append(Component.literal(" " + timerId))
                                    .withColor(0xFFD700)
                            );
                        }
                        // Else broadcast to everyone.
                        else {
                            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                            if (server != null) {
                                server.getPlayerList().getPlayers().forEach(p -> {
                                    p.sendSystemMessage(Component.translatable("commands.chx-a.timer_time_out")
                                        .append(Component.literal(" " + timerId))
                                        .withColor(0xFFD700));
                                });
                            }
                        }
                    };
                    context.getSource().sendSystemMessage(Component.translatable("commands." + CoreHanXu.MOD_ID + ".timer_default_end_behavior").withColor(0xFFD700));
                }
                break;
            case "n", "null":
                // Nothing to do, same as default.
            default:
                callback = player -> {};
                break;
        }

        return callback;
    }

    private static int compareAndSelect(int firstRange, int secondRange) {
        // Compare.
        int lowerRange = Math.min(firstRange, secondRange);
        int upperRange = Math.max(firstRange, secondRange);

        // Take one random number between the range.
        return ThreadLocalRandom.current().nextInt(lowerRange, upperRange + 1);
    }
}
