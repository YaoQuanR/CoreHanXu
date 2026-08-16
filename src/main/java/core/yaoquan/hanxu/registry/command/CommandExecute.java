package core.yaoquan.hanxu.registry.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.*;
import core.yaoquan.hanxu.api.custom.BehaviorRegistry;
import core.yaoquan.hanxu.api.define.FilePath;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.NullableValue;
import core.yaoquan.hanxu.registry.config.GeneralConfig;
import core.yaoquan.hanxu.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static core.yaoquan.hanxu.api.define.Error.*;

class CommandExecute {
    static int executeDetail(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.detail_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.detail_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context,
            Component.translatable("commands.chx.detail_innertext2")
                    .append(Component.literal(" " + General.Version.getCoreVersion()))
                    .withColor(General.Color.CONTENT)
        );
        return 1;
    }

    static int executeLicense(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_innertext2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_innertext3").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_innertext4").withColor(General.Color.CONTENT));
        return 1;
    }

    static int executeLicense_Origin(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext3").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext4").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext5").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext6").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext7").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext8").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext9").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext10").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext11").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext12").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext13").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext14").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext15").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext16").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext17").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext18").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext19").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext20").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext21").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext22").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext23").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext24").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext25").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext26").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext27").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext28").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext29").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext30").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext31").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext32").withColor(General.Color.CONTENT));
        return 1;
    }

    static int executeLicense_Agree(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof Player player) {
            if (!PermissionHolder.Storage.getLicenseState(player)) {
                MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_agree").withColor(General.Color.CONTENT));
                player.getPersistentData()
                    .putBoolean("core.yaoquan.hanxu.agreed_license", true);
            }
            else {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.licenseAlreadyAgreed));
            }
        }
        else {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.notPlayer));
        }
        return 1;
    }

    static int executeLicense_State(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof Player player) {
            boolean state = PermissionHolder.Storage.getLicenseState(player);
            MessagePublisher.sendSystemMessage(context, Component.literal("[HX] " + state).withColor(General.Color.TITLE));
            return 1;
        }
        return 0;
    }

    static int executeAdvancedLicense_State(CommandContext<CommandSourceStack> context) {
        String playerId = StringArgumentType.getString(context, "player_id");
        UUID playerUUID = Resolver.resolveTargetUUID(context, playerId);
        if (playerUUID == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
            return 0;
        }
        else {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) {
                return 0;
            }
            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
            if (player == null) {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
                return 0;
            }
            boolean state = PermissionHolder.Storage.getLicenseState(player);
            MessagePublisher.sendSystemMessage(context, Component.literal("[HX] " + state).withColor(General.Color.TITLE));
            return 1;
        }
    }

    static int executeTimer(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer").withColor(General.Color.TITLE));
        return 1;
    }

    static int executeBare(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.bare1").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.bare2").withColor(General.Color.TITLE));
        return 1;
    }

    static int executeHelp(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.fixed.available_commands").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext3").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext4").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext5").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext6").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext7").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext8").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext9").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext10").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext11").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext12").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext13").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext14").withColor(General.Color.CONTENT));
        return 1;
    }

    static int executePermission_Check(CommandContext<CommandSourceStack> context, String target) {
        String playerId;
        try {
            playerId = StringArgumentType.getString(context, "player_id");
        }
        catch (IllegalArgumentException e) {
            if (context.getSource().getPlayer() != null) {
                playerId = context.getSource().getPlayer().getName().getString();
            }
            else {
                playerId = null;
            }
        }

        switch (target) {
            case "player" -> {
                UUID playerUUID = Resolver.resolveTargetUUID(context, playerId);
                if (playerUUID == null) {
                    MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
                    return 0;
                }
                else {
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    if (server == null) {
                        return 0;
                    }
                    ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
                    if (player == null) {
                        MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
                        return 0;
                    }

                    int permissionLevel = PermissionHolder.Storage.getPlayerPermissionLevel(player);
                    MessagePublisher.sendSystemMessage(context, Component.literal("[HX] " + permissionLevel).withColor(General.Color.TITLE));
                }
            }
            case "server" -> {
                int commandblockPermissionLevel = context.getSource().getLevel().getGameRules().getInt(PermissionHolder.Storage.nonPlayerSourcePermissionLevel);
                MessagePublisher.sendSystemMessage(context, Component.literal("[HX] " + commandblockPermissionLevel).withColor(General.Color.TITLE));
            }
            case "player_first_grant" -> {
                int autoAuthorizedPermissionLevel = GeneralConfig.SET_AUTO_AUTHORIZED_PERMISSION_LEVEL.getAsInt();
                MessagePublisher.sendSystemMessage(context, Component.literal("[HX] " + autoAuthorizedPermissionLevel).withColor(General.Color.TITLE));
            }
            default -> {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.undefinedOperationCategory));
                return 0;
            }
        }
        return 1;
    }

    static int executePermission_Set(CommandContext<CommandSourceStack> context) {
        String playerId = StringArgumentType.getString(context, "player_id");

        MinecraftServer server = context.getSource().getServer();
        ServerPlayer player;
        if (playerId.equals("-me") || playerId.equals("-m") || playerId.equals("-nearest") || playerId.equals("-n")) {
            if (context.getSource().getPlayer() != null) {
                player = context.getSource().getPlayer();
            }
            else {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.notPlayer));
                return 0;
            }
        }
        else if (playerId.equals("-random") || playerId.equals("-r")) {
            player = server.getPlayerList().getPlayers().get(new Random().nextInt(server.getPlayerList().getPlayers().size()));
        }
        else {
            player = server.getPlayerList().getPlayerByName(playerId);
            if (player == null) {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
                return 0;
            }
        }

        int newLevel = IntegerArgumentType.getInteger(context, "level");

        if (newLevel > 10) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.exceedMaximumPermissionLevel));
            return 0;
        }

        boolean editable = GeneralConfig.SET_PLAYER_PERMISSION_EDITABLE.getAsBoolean();

        if (!editable) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.uneditablePlayerPermission));
            return 0;
        }

        PermissionHolder.Storage.setPlayerPermissionLevel(player, newLevel);

        MessagePublisher.sendSystemMessage(context, Component.literal("[HX] ✔ -> " + newLevel).withColor(General.Color.TITLE));
        return 1;
    }

    static int executeTimer_Help(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_introduction").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.fixed.available_commands").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_create_argument1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_create-range_argument1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext3").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext4").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext5").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_create_argument2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext6").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_create-range_argument2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext7").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext8").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext9").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext10").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext11").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext12").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext13").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext14").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_modify_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext15").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_read_argument1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_read_argument2").withColor(General.Color.CONTENT));
        return 1;
    }

    static int executeTimer_Template_Create(CommandContext<CommandSourceStack> context, String titleParameter) {
        // Receive arguments.
        String timerId = StringArgumentType.getString(context, "timer_id");
        int timeAmount = IntegerArgumentType.getInteger(context, "time_amount");
        String timeUnit;
        String contentParameter;

        // If no unit, receive time data as ticks.
        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        try {
            contentParameter = StringArgumentType.getString(context, "behavior_content");
        }
        catch (IllegalArgumentException e) {
            contentParameter = null;
        }

        // Check if timer exist.
        if (TimeHolder.getRemainingTimeFromTemplate(timerId, timeUnit).isPresent()) {
            MessagePublisher.sendFailureMessage(context, Component.translatable("commands.chx.timer_already_exist"));
            return 0;
        }

        return commandCreateTemplateTimer(context, timerId, timeUnit, timeAmount, titleParameter, contentParameter);
    }

    static int executeTimer_Template_Read(CommandContext<CommandSourceStack> context) {
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

        return displayCommandTimerRead(context, timerId, "", timeUnit, infoCategory, "template");
    }

    static int executeTimer_Template_Delete(CommandContext<CommandSourceStack> context) {
        // Receive argument.
        String timerId = StringArgumentType.getString(context, "timer_id");

        boolean isDeleted = TimeHolder.deleteTemplateTimer(timerId);
        if (isDeleted) {
            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.timer_deleted")
                            .append(Component.literal(" (" + timerId + ")"))
                            .withColor(General.Color.TITLE)
            );
        } else {
            MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExist));
        }

        return 1;
    }

    static int executeTimer_Template_CreateRange(CommandContext<CommandSourceStack> context, String titleParameter) {
        // Receive arguments.
        String timerId = StringArgumentType.getString(context, "timer_id");
        int timeFirstRange = IntegerArgumentType.getInteger(context, "time_first_range");
        int timeSecondRange = IntegerArgumentType.getInteger(context, "time_second_range");
        String timeUnit;
        String contentParameter;

        // If no unit, receive time data as ticks.
        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        try {
            contentParameter = StringArgumentType.getString(context, "behavior_content");
        }
        catch (IllegalArgumentException e) {
            contentParameter = null;
        }

        int selectedTimeAmount = Converter.convertFromRangeToRandom(timeFirstRange, timeSecondRange);

        // Check if timer exist.
        if (TimeHolder.getRemainingTimeFromTemplate(timerId, timeUnit).isPresent()) {
            MessagePublisher.sendFailureMessage(context, Component.translatable("commands.chx.timer_already_exist"));
            return 0;
        }

        return commandCreateTemplateTimer(context, timerId, timeUnit, selectedTimeAmount, titleParameter, contentParameter);
    }

    static int executeTimer_Template_List(CommandContext<CommandSourceStack> context) {
        String[] templateIds = TimeHolder.getAllTemplateIds();

        return displayTimerIdList(context, templateIds);
    }

    static int executeTimer_Instance_Apply(CommandContext<CommandSourceStack> context) {
        // Receive arguments.
        String templateTimerId = StringArgumentType.getString(context, "template_timer_id");
        String applyTarget = StringArgumentType.getString(context, "apply_target");

        // Analysis to UUID.
        UUID targetUUID = Resolver.resolveTargetUUID(context, applyTarget);

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
            MessagePublisher.sendFailureMessage(context,
                    returnGeneralError(GeneralError.targetNotExist)
            );
            return 0;
        }

        // Determine if template timer exist and if instance timer exist, then register (Copy).
        if (TimeHolder.createInstanceFromTemplate(targetUUID, templateTimerId)) {
            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.timer_instantiated")
                            .append(Component.literal(" " + templateTimerId + " -> " + displayTarget))
                            .withColor(General.Color.SUCCESS)
            );
        }
        else {
            MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExistOrAlreadyInstantiated));
            return 0;
        }

        return 1;
    }

    static int executeTimer_Instance_Create(CommandContext<CommandSourceStack> context, String titleParameter) {
        // Receive arguments.
        String timerId = StringArgumentType.getString(context, "timer_id");
        String masterString = StringArgumentType.getString(context, "master_id");
        int timeAmount = IntegerArgumentType.getInteger(context, "time_amount");
        String timeUnit;
        String contentParameter;

        // Receive optional arguments.
        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        try {
            contentParameter = StringArgumentType.getString(context, "behavior_content");
        }
        catch (IllegalArgumentException e) {
            contentParameter = null;
        }

        int returnValue = commandCreateInstanceTimer(context, timerId, masterString, timeUnit, timeAmount, titleParameter, contentParameter);

        if (returnValue == 1) {
            displayTimerCreateMessage(context, timerId, timeAmount, timeUnit, titleParameter, contentParameter);
            return 1;
        }
        else {
            return 0;
        }
    }

    static int executeTimer_Instance_CreateRange(CommandContext<CommandSourceStack> context, String titleParameter) {
        // Receive arguments.
        String timerId = StringArgumentType.getString(context, "timer_id");
        String masterString = StringArgumentType.getString(context, "master_id");
        int timeFirstRange = IntegerArgumentType.getInteger(context, "time_first_range");
        int timeSecondRange = IntegerArgumentType.getInteger(context, "time_second_range");
        String timeUnit;
        String contentParameter;

        // If no unit, receive time data as ticks.
        try {
            timeUnit = StringArgumentType.getString(context, "time_unit");
        }
        catch (IllegalArgumentException e) {
            timeUnit = "t";
        }

        try {
            contentParameter = StringArgumentType.getString(context, "behavior_content");
        }
        catch (IllegalArgumentException e) {
            contentParameter = null;
        }

        int selectedTimeAmount = Converter.convertFromRangeToRandom(timeFirstRange, timeSecondRange);

        int returnValue = commandCreateInstanceTimer(context, timerId, masterString, timeUnit, selectedTimeAmount, titleParameter, contentParameter);

        if (returnValue == 1) {
            displayTimerCreateMessage(context, timerId, selectedTimeAmount, timeUnit, titleParameter, contentParameter);
            return 1;
        }
        else {
            return 0;
        }
    }

    static int executeTimer_Instance_Start(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        return commandOperateInstanceTimer(context, timerId, masterString, "start");
    }

    static int executeTimer_Instance_Stop(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        return commandOperateInstanceTimer(context, timerId, masterString, "stop");
    }

    static int executeTimer_Instance_Reset(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        return commandOperateInstanceTimer(context, timerId, masterString, "reset");
    }

    static int executeTimer_Instance_Restart(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        return commandOperateInstanceTimer(context, timerId, masterString, "restart");
    }

    static int executeTimer_Instance_Delete(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        return commandOperateInstanceTimer(context, timerId, masterString, "delete");
    }

    static int executeTimer_Instance_Modify(CommandContext<CommandSourceStack> context, String category) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        UUID masterId = Resolver.resolveTargetUUID(context, masterString);

        if (masterId == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
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

        switch (timeUnit) {
            case "t", "tick", "s", "second", "m", "minute", "h", "hour":
                TimeHolder.ModifyCategory modifyCategory = category.equals("initial_time")? TimeHolder.ModifyCategory.INITIAL_TIME : TimeHolder.ModifyCategory.REMAINING_TIME;

                if (TimeHolder.modifyInstanceTimer(masterId, timerId, timeAmount, timeUnit, modifyCategory)) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.timer_success_modification")
                                    .append(Component.literal(" (" + timerId + " -> " + masterString + "): " + category + " " + timeAmount + " " + timeUnit))
                                    .withColor(General.Color.TITLE)
                    );
                    return 1;
                }
                else {
                    MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExist));
                    return 0;
                }
            default:
                MessagePublisher.sendFailureMessage(context, Component.translatable("commands.core_hanxu.invalid_unit_argument"));
                return 0;
        }
    }

    static int executeTimer_Instance_Read(CommandContext<CommandSourceStack> context) {
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

        return displayCommandTimerRead(context, timerId, masterString, timeUnit, infoCategory, "instance");
    }

    static int executeTimer_Instance_List(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");

        UUID masterId = Resolver.resolveTargetUUID(context, masterString);

        if (masterId == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
            return 0;
        }

        String[] instanceIds = TimeHolder.getAllInstanceIds(masterId);

        return displayTimerIdList(context, instanceIds);
    }

    static int executeTimer_Instance_Display(CommandContext<CommandSourceStack> context, boolean state) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        UUID masterId = Resolver.resolveTargetUUID(context, masterString);
        if (masterId == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
            return 0;
        }

        ServerPlayer player = context.getSource().getPlayer();

        TimeHolder.displayToInfoPage(player, masterId, timerId, state);
        MessagePublisher.sendSystemMessage(context,
                Component.literal("[HX] " + timerId + " ")
                        .append(Component.translatable("commands.core_hanxu.has_changed_to"))
                        .append(Component.literal(" " + state))
                        .withColor(General.Color.SUCCESS)
        );

        return 1;
    }

    static int executeScene(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene").withColor(General.Color.TITLE));
        return 1;
    }

    static int executeScene_Help(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_introduction").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.fixed.available_commands").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext3").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext4").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext5").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext6").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext7").withColor(General.Color.CONTENT));
        return 1;
    }

    static int executeScene_List(CommandContext<CommandSourceStack> context) {
        List<Component> displayList = new ArrayList<>();

        // Scan global path.
        Path globalPath = FilePath.getGlobalPath().resolve("scene");
        if (Files.isDirectory(globalPath)) {
            try (Stream<Path> stream = Files.list(globalPath)) {
                stream.filter(p -> p.toString().endsWith(".yaml")).forEach(p -> {
                    displayList.add(Component.literal("(global): " + p.getFileName().toString()).withColor(General.Color.CONTENT));
                });
            }
            catch (IOException ignored) {}
        }

        // Scan world path.
        Path worldPath = FilePath.getWorldPath();
        if (worldPath != null) {
            Path worldScenePath = worldPath.resolve("scene");
            if (Files.isDirectory(worldScenePath)) {
                try (Stream<Path> stream = Files.list(worldScenePath)) {
                    stream.filter(p -> p.toString().endsWith(".yaml")).forEach(p -> {
                        displayList.add(Component.literal("(world): " + p.getFileName().toString()).withColor(General.Color.CONTENT));
                    });
                }
                catch (IOException ignored) {}
            }
        }

        // Then list out.
        if (displayList.isEmpty()) {
            MessagePublisher.sendFailureMessage(context, returnSceneError(SceneError.notFound));
        }
        else {
            displaySceneIdList(context, displayList);
        }

        return 1;
    }

    static int executeScene_Play(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();

        try {
            String playerId = StringArgumentType.getString(context, "player_id");
            UUID playerUUID = Resolver.resolveTargetUUID(context, playerId);
            player = Resolver.resolveTargetPlayer(playerUUID);
        }
        catch (IllegalArgumentException ignored) {}

        if (player == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.notPlayer));
            return 0;
        }

        String sceneName = StringArgumentType.getString(context, "scene_name");

        // Check if existed.
        if (!SceneHolder.doesSceneExist(sceneName)) {
            MessagePublisher.sendFailureMessage(context, returnSceneError(SceneError.notFound));
            return 0;
        }

        try {
            MessagePublisher.sendSystemMessage(context,
                Component.translatable("commands.chx.scene_now_playing")
                    .append(Component.literal(": " + sceneName))
                    .withColor(General.Color.CONTENT));
            SceneHolder.playScene(player, sceneName);
        }
        catch (Exception e) {
            MessagePublisher.sendFailureMessage(context, returnSceneError(SceneError.playFailed));
            return 0;
        }

        return 1;
    }

    static int executeScene_Broadcast(CommandContext<CommandSourceStack> context) {
        String sceneName = StringArgumentType.getString(context, "scene_name");

        // Check if existed.
        if (!SceneHolder.doesSceneExist(sceneName)) {
            MessagePublisher.sendFailureMessage(context, returnSceneError(SceneError.notFound));
            return 0;
        }

        try {
            MessagePublisher.sendSystemMessage(context,
                Component.translatable("commands.chx.scene_now_playing_to_everyone")
                    .append(Component.literal(": " + sceneName))
                    .withColor(General.Color.CONTENT));
            SceneHolder.playSceneToEveryone(context.getSource().getServer(), sceneName);
        }
        catch (Exception e) {
            MessagePublisher.sendFailureMessage(context, returnSceneError(SceneError.playFailed));
            return 0;
        }

        return 1;
    }

    static int executeScene_Delete(CommandContext<CommandSourceStack> context, String specifiedPath) {
        String sceneName = StringArgumentType.getString(context, "scene_name");

        // Check and delete.
        if (specifiedPath.equals("try")) {
            boolean worldSceneExist = SceneHolder.doesSceneExist(sceneName, YamlReader.TargetPath.TO_WORLD);
            boolean globalSceneExist = SceneHolder.doesSceneExist(sceneName, YamlReader.TargetPath.TO_GLOBAL);
            if (worldSceneExist && globalSceneExist) {
                MessagePublisher.sendFailureMessage(context, returnSceneError(SceneError.sameNameFound));
                return 0;
            }
            else if (worldSceneExist) {
                specifiedPath = "world";
            }
            else if (globalSceneExist) {
                specifiedPath = "global";
            }
            else {
                MessagePublisher.sendFailureMessage(context, returnSceneError(SceneError.notFound));
                return 0;
            }
        }
        switch (specifiedPath) {
            case "world":
                if (SceneHolder.deleteScene(sceneName, YamlReader.TargetPath.TO_WORLD)) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.scene_deleted")
                                    .withColor(General.Color.CONTENT));
                    break;
                }
                MessagePublisher.sendFailureMessage(context, returnSceneError(SceneError.failedToDelete));
                return 0;
            case "global":
                if (SceneHolder.deleteScene(sceneName, YamlReader.TargetPath.TO_GLOBAL)) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.scene_deleted")
                                    .withColor(General.Color.CONTENT));
                    break;
                }
                MessagePublisher.sendFailureMessage(context, returnSceneError(SceneError.failedToDelete));
                return 0;
        }

        return 1;
    }

    static int executeScene_Template(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.notPlayer));
            return 0;
        }

        // Create a template book.
        ItemStack book = new ItemStack(Items.WRITABLE_BOOK);

        String bookTemplate = "id: \"FILE NAME?\"\ntype: \"simple\"\n\ndefault:\n  interval: 20\n\ndialogs:\n  - text: \"CONTENT HERE...\"";

        // Setup template.
        WritableBookContent content = new WritableBookContent(List.of(Filterable.passThrough(bookTemplate)));
        book.set(DataComponents.WRITABLE_BOOK_CONTENT, content);

        // Then give.
        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }

        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_get_template").withColor(General.Color.SUCCESS));
        return 1;
    }

    static int executeScene_Create(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.notPlayer));
            return 0;
        }

        String toPath = StringArgumentType.getString(context, "to_path");

        if (!toPath.equals("world") && !toPath.equals("global")) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.undefinedSavePath));
            return 0;
        }

        // Then read book from player's main hand.
        ItemStack book = player.getMainHandItem();
        if (book.isEmpty() || (!book.is(Items.WRITABLE_BOOK) && !book.is(Items.WRITTEN_BOOK))) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.mainHandItemNotTarget));
            return 0;
        }

        /*
        * Then verify if valid submission.
        * 1. Does content empty?
        * 2. Does field "id" existed?
        * 3. Does same name scene found?
        * 4. Does field "type" and "dialogs" existed?
        * Then try to submit.
        */

        String yamlContent = YamlReader.read(book);
        if (yamlContent == null || yamlContent.isEmpty()) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.noContentFound));
            return 0;
        }

        String sceneId = YamlReader.readSpecificField(yamlContent, "id");
        if (sceneId.isEmpty()) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.missingIdField));
            return 0;
        }

        YamlReader.TargetPath targetPath = toPath.equals("world")? YamlReader.TargetPath.TO_WORLD : YamlReader.TargetPath.TO_GLOBAL;

        if (SceneHolder.doesSceneExist(sceneId, targetPath)) {
            MessagePublisher.sendFailureMessage(context, returnSceneError(SceneError.alreadyExist));
            return 0;
        }

        if (!yamlContent.contains("type:") || !yamlContent.contains("dialogs:")) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.uncompletedContent));
            return 0;
        }

        try {
            // Prase data to map for storage.
            Map<String, Object> yamlMap = YamlReader.stringToMap(yamlContent);
            YamlReader.save("scene", sceneId, yamlMap, targetPath);
            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.scene_created")
                            .append(" " + sceneId + " -> " + targetPath)
                            .withColor(General.Color.SUCCESS)
            );
        }
        catch (Exception e) {
            MessagePublisher.sendFailureMessage(context, returnSceneError(SceneError.failedToSave));
            return 0;
        }

        return 1;
    }

    static int executeAttribute(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute").withColor(General.Color.TITLE));
        return 1;
    }

    static int executeAttribute_Help(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_introduction").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.fixed.available_commands").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext3").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext4").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext5").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_create_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext6").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_define_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext7").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext8").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext9").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext10").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext11").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_modify_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext12").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_recovery_argument1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_recovery_argument2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext13").withColor(General.Color.CONTENT));
        return 1;
    }

    static int executeAttribute_List(CommandContext<CommandSourceStack> context) {
        Map<String, AttributeHolder.CustomAttribute> commandAttributes = AttributeHolder.getCommandAttributes();
        String[] commandAttributeList = commandAttributes.keySet().toArray(new String[0]);

        return displayAttributeIdList(context, commandAttributeList, false);
    }

    static int executeAdvancedAttribute_List(CommandContext<CommandSourceStack> context) {
        Map<String, AttributeHolder.CustomAttribute> apiAttributes = AttributeHolder.getApiAttributes();
        Map<String, AttributeHolder.CustomAttribute> commandAttributes = AttributeHolder.getCommandAttributes();

        String[] commandAttributeList = commandAttributes.keySet().toArray(new String[0]);
        String[] apiAttributeList = apiAttributes.keySet().toArray(new String[0]);

        int returnValue1 = displayAttributeIdList(context, commandAttributeList, false);
        int returnValue2 = displayAttributeIdList(context, apiAttributeList, true);

        if (returnValue1 == 1 || returnValue2 == 1) {
            return 1;
        }
        else {
            return 0;
        }
    }

    static int executeAttribute_Create(CommandContext<CommandSourceStack> context) {
        String attributeId = StringArgumentType.getString(context, "attribute_id");
        String toPath = StringArgumentType.getString(context, "to_path");
        YamlReader.TargetPath targetPath;
        float maximum, defaultValue;
        try {
            maximum = FloatArgumentType.getFloat(context, "maximum");
        }
        catch (Exception e) {
            maximum = 100.0f;
        }
        try {
            defaultValue = FloatArgumentType.getFloat(context, "default_value");
        }
        catch (Exception e) {
            defaultValue = 0.0f;
        }

        targetPath = toPath.equals("global")? YamlReader.TargetPath.TO_GLOBAL : YamlReader.TargetPath.TO_WORLD;

        boolean registered = AttributeHolder.register(attributeId, maximum, defaultValue, targetPath);

        displayAttributeCreateMessage(context, attributeId, maximum, defaultValue, registered);

        if (registered) {
            AttributeHolder.registerAllYamlAttributes();
            return 1;
        }
        else {
            return 0;
        }
    }
    
    static int executeAttribute_Define(CommandContext<CommandSourceStack> context, String category) {
        String attributeId = StringArgumentType.getString(context, "attribute_id");
        float threshold = FloatArgumentType.getFloat(context, "threshold");
        String content = null, callbackId = null;
        String finalCallbackId;

        if (category.equals("remind") || category.equals("execute")) {
            try {
                content = StringArgumentType.getString(context, "content");
            }
            catch (IllegalArgumentException ignored) {}
        }
        else if (category.equals("api")) {
            try {
                callbackId = StringArgumentType.getString(context, "callback_id");
            }
            catch (IllegalArgumentException e) {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.missingIdField));
                return 0;
            }
        }

        NullableValue<AttributeHolder.CustomAttribute> nullableAttribute = AttributeHolder.getCommandAttribute(attributeId);
        if (nullableAttribute.isNull()) {
            MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.notFound));
            return 0;
        }

        AttributeHolder.CustomAttribute attribute = nullableAttribute.get();

        switch (category) {
            case "remind", "execute" -> {
                String fullCallbackId = "attribute:core_hanxu-command:" + attributeId + "-" + threshold;
                attribute.onThreshold(threshold, fullCallbackId);
                Creator.registerCallback(fullCallbackId, category, content);
                // Update YAML document.
                AttributeHolder.saveYamlAttribute(
                    attributeId,
                    AttributeHolder.UpdateCategory.normalThreshold,
                    threshold,
                    fullCallbackId,
                    category,
                    content
                );

                finalCallbackId = fullCallbackId;
            }
            case "api" -> {
                attribute.onThreshold(threshold, callbackId);
                Creator.registerCallback(callbackId, category, content);
                AttributeHolder.saveYamlAttribute(
                    attributeId,
                    AttributeHolder.UpdateCategory.normalThreshold,
                    threshold,
                    callbackId,
                    category,
                    content
                );

                finalCallbackId = callbackId;
            }
            default -> {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.undefinedOperationId));
                return 0;
            }
        }

        // Output message.
        MessagePublisher.sendSystemMessage(
                context, Component.translatable("commands.chx.attribute_threshold_modified")
                        .append(Component.literal(" " + attributeId + " -> " + finalCallbackId + " (" + threshold + ") -> " + category))
                        .withColor(General.Color.SUCCESS)
        );

        return 1;
    }

    static int executeAttribute_Delete(CommandContext<CommandSourceStack> context, String specifiedPath) {
        String attributeId = StringArgumentType.getString(context, "attribute_id");

        if (AttributeHolder.getApiAttributes().containsKey(attributeId)) {
            MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.tryToModifyApiTarget));
            return 0;
        }

        if (!AttributeHolder.doesYamlAttributeExist(attributeId)) {
            MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.notFound));
            return 0;
        }
        if (!AttributeHolder.doesAttributeExist(attributeId, "command")) {
            MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.notFound));
            return 0;
        }

        if (specifiedPath.equals("try")) {
            boolean worldAttributeExist = AttributeHolder.doesYamlAttributeExist(attributeId, YamlReader.TargetPath.TO_WORLD);
            boolean globalAttributeExist = AttributeHolder.doesYamlAttributeExist(attributeId, YamlReader.TargetPath.TO_GLOBAL);

            if (worldAttributeExist && globalAttributeExist) {
                MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.sameNameFound));
                return 0;
            }
            else if (worldAttributeExist) {
                specifiedPath = "world";
            }
            else if (globalAttributeExist) {
                specifiedPath = "global";
            }
        }

        switch (specifiedPath) {
            case "world":
                if (AttributeHolder.unregisterAndDelete(attributeId, YamlReader.TargetPath.TO_WORLD)) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.attribute_deleted")
                                    .withColor(General.Color.CONTENT)
                    );
                    break;
                }
                MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.failedToDelete));
                return 0;
            case "global":
                if (AttributeHolder.unregisterAndDelete(attributeId, YamlReader.TargetPath.TO_GLOBAL)) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.attribute_deleted")
                                    .withColor(General.Color.CONTENT)
                    );
                    break;
                }
                MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.failedToDelete));
                return 0;
        }

        return 1;
    }

    static int executeAttribute_Read(CommandContext<CommandSourceStack> context, String category) {
        String attributeId = StringArgumentType.getString(context, "attribute_id");

        if (!AttributeHolder.doesAttributeExist(attributeId)) {
            MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.notFound));
            return 0;
        }

        boolean isApiAttribute;
        AttributeHolder.CustomAttribute attribute;

        if (AttributeHolder.doesAttributeExist(attributeId, "command")) {
            isApiAttribute = false;
        }
        else if (AttributeHolder.doesAttributeExist(attributeId, "yaml")) {
            isApiAttribute = false;
        }
        else if (AttributeHolder.doesAttributeExist(attributeId, "api")) {
            isApiAttribute = true;
        }
        else {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.undefinedOperationId));
            return 0;
        }

        NullableValue<AttributeHolder.CustomAttribute> nullableAttribute;
        nullableAttribute = isApiAttribute?
                AttributeHolder.getApiAttribute(attributeId) :
                AttributeHolder.getCommandAttribute(attributeId);

        if (nullableAttribute.isNull()) {
            MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.notFound));
            return 0;
        }

        attribute = nullableAttribute.get();

        switch (category) {
            case "threshold_all" -> {
                Map<Float, String> thresholds = attribute.getThresholdCallbacks();

                if (thresholds.isEmpty()) {
                    MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.noThreshold));
                    return 0;
                }

                MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_threshold_found").withColor(General.Color.CONTENT));
                for (Map.Entry<Float, String> threshold : thresholds.entrySet()) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.literal(threshold.getKey().toString() + " -> " + threshold.getValue())
                                .withColor(General.Color.CONTENT)
                    );
                }
            }
            case "threshold_specific" -> {
                Map<Float, String> thresholds = attribute.getThresholdCallbacks();

                if (thresholds.isEmpty()) {
                    MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.noThreshold));
                    return 0;
                }

                float value = FloatArgumentType.getFloat(context, "threshold_value");

                if (!thresholds.containsKey(value)) {
                    MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.noSpecificThreshold));
                    return 0;
                }

                MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_threshold_found").withColor(General.Color.CONTENT));
                MessagePublisher.sendSystemMessage(context, Component.literal(value + " -> " + thresholds.get(value)));
            }
            case "zero" -> {
                String zeroId = attribute.getZeroCallbackId();
                if (zeroId == null) {
                    MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.noZero));
                    return 0;
                }

                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.attribute_zero")
                                .append(Component.literal(" " + zeroId))
                                .withColor(General.Color.CONTENT)
                );
            }
            case "recovery" -> {
                String recoveryId = attribute.getRecoveryCurveId();
                if (recoveryId == null) {
                    MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.noRecovery));
                    return 0;
                }

                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.attribute_recovery")
                                .append(Component.literal(" " + recoveryId))
                                .withColor(General.Color.CONTENT)
                );
            }
            case "maximum" -> {
                float maximum = attribute.getMaximum();
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.attribute_maximum")
                                .append(Component.literal(" " + maximum))
                                .withColor(General.Color.CONTENT)
                );
            }
            case "recovery_interval" -> {
                int interval = attribute.getRecoveryIntervalTicks();
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.attribute_recovery_interval")
                                .append(Component.literal(" " + interval))
                                .withColor(General.Color.CONTENT)
                );
            }
            case "value" -> {
                String playerId;
                try {
                    playerId = StringArgumentType.getString(context, "player_id");
                }
                catch (IllegalArgumentException e) {
                    if (context.getSource().getPlayer() != null) {
                        playerId = context.getSource().getPlayer().getName().toString();
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.noIdFieldProvidedByNonPlayer));
                        return 0;
                    }
                }

                if ((playerId.equals("-me") || playerId.equals("-m")) && context.getSource().getPlayer() == null) {
                    MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.invalidMeFieldUsed));
                    return 0;
                }

                UUID masterId = Resolver.resolveTargetUUID(context, playerId);

                float value = AttributeHolder.getValue(masterId, attributeId, isApiAttribute);
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.attribute_value")
                                .append(" " + value + " (" + playerId + ")")
                                .withColor(General.Color.CONTENT)
                );
            }
            case "group" -> {
                if (isApiAttribute) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.attribute_read_group")
                                    .append(" " + attributeId + " -> api")
                                    .withColor(General.Color.CONTENT)
                    );
                }
                else {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.attribute_read_group")
                                    .append(" " + attributeId + " -> command (yaml)")
                                    .withColor(General.Color.CONTENT)
                    );
                }
            }
            default -> {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.undefinedOperationId));
                return 0;
            }
        }

        return 1;
    }

    static int executeAttribute_Modify(CommandContext<CommandSourceStack> context, String category) {
        String attributeId = StringArgumentType.getString(context, "attribute_id");
        String playerId = StringArgumentType.getString(context, "player_id");
        float value = FloatArgumentType.getFloat(context, "value");
        String direction;

        try {
            direction = StringArgumentType.getString(context, "direction");
        }
        catch (IllegalArgumentException e) {
            direction = "point";
        }

        UUID masterId = Resolver.resolveTargetUUID(context, playerId);

        if (masterId == null && !(playerId.equals("-all") || playerId.equals("-a"))) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
            return 0;
        }

        ServerPlayer player = null;
        if (masterId != null) {
            player = context.getSource().getServer().getPlayerList().getPlayer(masterId);
        }

        if (player == null && !(playerId.equals("-all") || playerId.equals("-a"))) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
            return 0;
        }

        if (!AttributeHolder.doesAttributeExist(attributeId)) {
            MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.notFound));
            return 0;
        }

        boolean isApiAttribute = AttributeHolder.doesAttributeExist(attributeId, "api");

        AttributeHolder.ThresholdDirection thresholdDirection;
        boolean success = false;

        switch (direction) {
            case "point" -> thresholdDirection = AttributeHolder.ThresholdDirection.POINT;
            case "up" -> thresholdDirection = AttributeHolder.ThresholdDirection.UP;
            case "down" -> thresholdDirection = AttributeHolder.ThresholdDirection.DOWN;
            case "flex" -> thresholdDirection = AttributeHolder.ThresholdDirection.FLEX;
            default -> {
                MessagePublisher.sendFailureMessage(context,
                        Component.translatable("commands.chx.attribute_default_direction")
                );
                thresholdDirection = AttributeHolder.ThresholdDirection.POINT;
                direction = "point";
            }
        }

        switch (category) {
            case "set" -> {
                if (playerId.equals("-all") || playerId.equals("-a")) {
                    for (ServerPlayer serverPlayer : context.getSource().getServer().getPlayerList().getPlayers()) {
                        success = AttributeHolder.setValue(serverPlayer.getUUID(), attributeId, value, isApiAttribute, thresholdDirection);
                        if (!success) {
                            break;
                        }
                    }
                }
                else {
                    success = AttributeHolder.setValue(masterId, attributeId, value, isApiAttribute, thresholdDirection);
                }
            }
            case "add" -> {
                if (value == 0) {
                    break;
                }

                if (playerId.equals("-all") || playerId.equals("-a")) {
                    for (ServerPlayer serverPlayer : context.getSource().getServer().getPlayerList().getPlayers()) {
                        success = AttributeHolder.addValue(serverPlayer.getUUID(), attributeId, value, isApiAttribute, thresholdDirection);
                        if (!success) {
                            break;
                        }
                    }
                }
                else {
                    success = AttributeHolder.addValue(masterId, attributeId, value, isApiAttribute, thresholdDirection);
                }
            }
            case "reduce" -> {
                if (value == 0) {
                    break;
                }
                else if (value < 0) {
                    value = -value;
                }

                if (playerId.equals("-all")) {
                    for (ServerPlayer serverPlayer : context.getSource().getServer().getPlayerList().getPlayers()) {
                        success = AttributeHolder.reduceValue(serverPlayer.getUUID(), attributeId, value, isApiAttribute, thresholdDirection);
                        if (!success) {
                            break;
                        }
                    }
                }
                else {
                    success = AttributeHolder.reduceValue(masterId, attributeId, value, isApiAttribute, thresholdDirection);
                }
            }
        }

        if (value == 0) {
            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.attribute_no_changes")
                            .withColor(General.Color.CONTENT)
            );
            return 1;
        }

        if (success) {
            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.attribute_modified")
                            .withColor(General.Color.CONTENT)
            );
            MessagePublisher.sendSystemMessage(context,
                    Component.literal("(" + attributeId + " -> " + playerId + "): " + category + " " + value + " (" + direction + ")")
                            .withColor(General.Color.CONTENT)
            );
            return 1;
        }
        else {
            MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.notFound));
            return 0;
        }
    }

    static int executeAttribute_Recovery(CommandContext<CommandSourceStack> context, String category) {
        String attributeId = StringArgumentType.getString(context, "attribute_id");
        String finalCallbackId;

        if (!AttributeHolder.doesAttributeExist(attributeId)) {
            MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.notFound));
            return 0;
        }

        boolean isApiAttribute = AttributeHolder.doesAttributeExist(attributeId, "api");

        NullableValue<AttributeHolder.CustomAttribute> nullableAttribute = AttributeHolder.getAttributeDefinition(attributeId, isApiAttribute);
        if (nullableAttribute.isNull()) {
            MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.notFound));
            return 0;
        }

        AttributeHolder.CustomAttribute attribute = nullableAttribute.get();

        if (category.equals("simple")) {
            if (attribute.doesRecoveryRegistered() && isApiAttribute) {
                MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.tryToOverrideApiRecovery));
                return 0;
            }

            int interval = IntegerArgumentType.getInteger(context, "interval");
            String intervalUnit = StringArgumentType.getString(context, "interval_unit");
            float value = FloatArgumentType.getFloat(context, "value");
            String direction;
            try {
                direction = StringArgumentType.getString(context, "direction");
            }
            catch (IllegalArgumentException e) {
                direction = "point";
            }

            AttributeHolder.ThresholdDirection thresholdDirection;
            switch (direction) {
                case "up" -> thresholdDirection = AttributeHolder.ThresholdDirection.UP;
                case "down" -> thresholdDirection = AttributeHolder.ThresholdDirection.DOWN;
                case "flex" -> thresholdDirection = AttributeHolder.ThresholdDirection.FLEX;
                default -> thresholdDirection = AttributeHolder.ThresholdDirection.POINT;
            }

            String callbackId = "attribute:core_hanxu-command:" + attributeId + "-recovery";

            BehaviorRegistry.register(callbackId, (player, parameters) -> {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                if (server != null) {
                    for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                        if (serverPlayer != null) {
                            AttributeHolder.addValue(serverPlayer.getUUID(), attributeId, value, isApiAttribute, thresholdDirection);
                        }
                    }
                }
            });

            switch (intervalUnit) {
                case "t", "tick" -> {}
                case "s", "second" -> interval *= 20;
                case "m", "minute" -> interval *= (20 * 60);
                case "h", "hour" -> interval *= (20 * 3600);
                default -> {
                    MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.invalidUnitArgument));
                    return 0;
                }
            }

            attribute.setRecovery(callbackId).setRecoveryIntervalTicks(interval);

            if (!isApiAttribute) {
                AttributeHolder.saveYamlAttribute(
                    attributeId,
                    AttributeHolder.UpdateCategory.recovery,
                    0f,
                    callbackId,
                    "simple",
                    interval + ":" + value + ":" + direction
                );
            }

            finalCallbackId = callbackId;
        }
        else if (category.equals("api")) {
            String callbackId = StringArgumentType.getString(context, "callback_id");

            if (!BehaviorRegistry.isRegistered(callbackId)) {
                MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.tryToRegisterUnExistApiRecovery));
                return 0;
            }

            attribute.setRecovery(callbackId);

            if (!isApiAttribute) {
                AttributeHolder.saveYamlAttribute(
                    attributeId,
                    AttributeHolder.UpdateCategory.recovery,
                    0f,
                    callbackId,
                    "api",
                    null
                );
            }

            finalCallbackId = callbackId;
        }
        else {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.undefinedOperationCategory));
            return 0;
        }

        // Output message.
        MessagePublisher.sendSystemMessage(context,
                Component.translatable("commands.chx.attribute_recovery_registered")
                        .append(Component.literal(" " + attributeId + " <<- " + finalCallbackId))
                        .withColor(General.Color.CONTENT)
        );

        return 1;
    }

    static int executeAttribute_Display(CommandContext<CommandSourceStack> context, boolean state) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.notPlayer));
            return 0;
        }

        String attributeId = StringArgumentType.getString(context, "attribute_id");
        String masterId = StringArgumentType.getString(context, "master_id");

        if (!AttributeHolder.doesAttributeExist(attributeId)) {
            MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.notFound));
            return 0;
        }

        boolean isApiAttribute = AttributeHolder.doesAttributeExist(attributeId, "api");

        UUID masterUUID = Resolver.resolveTargetUUID(context, masterId);

        AttributeHolder.displayToInfoPage(player, masterUUID, attributeId, isApiAttribute, state);
        MessagePublisher.sendSystemMessage(context,
            Component.literal("[HX] " + attributeId + " ")
                    .append(Component.translatable("commands.core_hanxu.has_changed_to"))
                    .append(Component.literal(" " + state))
                    .withColor(General.Color.SUCCESS)
        );

        return 1;
    }

    static int executeVariable(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable").withColor(General.Color.TITLE));

        return 1;
    }

    static int executeVariable_Help(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_introduction").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.fixed.available_commands").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext3").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext4").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext5").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_create_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext6").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext7").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_copy_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext8").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_if_value_argument1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_if_value_argument2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext9").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_if_score_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext10").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_if_margin_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext11").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext12").withColor(General.Color.CONTENT));

        return 1;
    }

    static int executeVariable_List(CommandContext<CommandSourceStack> context, String category) {
        if (category.equals("variables")) {
            Set<String> variableNames = VariableHolder.getAllRegisteredVariables();

            if (variableNames.isEmpty()) {
                MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.emptyVariable));
                return 0;
            }

            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_list_title").withColor(General.Color.TITLE));
            for (String variableName : variableNames) {
                MessagePublisher.sendSystemMessage(context, Component.literal(variableName).withColor(General.Color.CONTENT));
            }
        }
        else if (category.equals("values")) {
            Map<String, String> allVariables = VariableHolder.getAllVariableAsString();
            if (allVariables.isEmpty()) {
                MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.emptyVariable));
                return 0;
            }

            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_list_title").withColor(General.Color.TITLE));
            for (String variableName : allVariables.keySet()) {
                MessagePublisher.sendSystemMessage(context, Component.literal(variableName + " -> " + allVariables.get(variableName)).withColor(General.Color.CONTENT));
            }
        }

        return 1;
    }

    static int executeVariable_Read(CommandContext<CommandSourceStack> context) {
        String variableName = StringArgumentType.getString(context, "variable_name");

        if (!VariableHolder.doesExists(variableName)) {
            MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.notExist));
            return 0;
        }

        String variableType = VariableHolder.getType(variableName).getOrElse("?");
        String variableValue = VariableHolder.getStringFrom(variableName).getOrElse("?");

        MessagePublisher.sendSystemMessage(context,
            Component.translatable("commands.chx.variable_read")
                .append(" " + variableType + " " + variableName + ": " + variableValue)
                .withColor(General.Color.CONTENT)
        );

        return 1;
    }

    static int executeVariable_Create(CommandContext<CommandSourceStack> context, boolean override) {
        String variableType = StringArgumentType.getString(context, "variable_type");
        String variableName = StringArgumentType.getString(context, "variable_name");
        String variableValue = StringArgumentType.getString(context, "variable_value");

        if (VariableHolder.doesExists(variableName) && !override) {
            MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.alreadyExist));
            return 0;
        }

        if (override) {
            NullableValue<String> nullableType = VariableHolder.getType(variableName);
            if (nullableType.isNull()) {
                MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.invalidType));
                return 0;
            }

            String actualType = nullableType.get();

            if (!actualType.equals(variableType)) {
                MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.invalidType));
                return 0;
            }
        }

        if (variableName.startsWith("-") || variableName.startsWith("@")) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.invalidFieldForName));
            return 0;
        }

        if (VariableHolder.createVariable(variableName, variableType, variableValue, override)) {
            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.variable_created")
                            .append(" " + variableType + " " + variableName + " <<- " + variableValue)
                            .withColor(General.Color.SUCCESS)
            );
        }
        else {
            MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.invalidType));
            return 0;
        }

        return 1;
    }

    static int executeVariable_Delete(CommandContext<CommandSourceStack> context) {
        String variableName = StringArgumentType.getString(context, "variable_name");

        if (variableName.equals("-all") || variableName.equals("-a")) {
            VariableHolder.deleteAllVariables();
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_all_deleted").withColor(General.Color.SUCCESS));
        }
        else {
            String variableType = VariableHolder.getType(variableName).getOrElse("?");
            if (VariableHolder.deleteVariable(variableName)) {
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.variable_deleted")
                                .append(" " + variableName + " (" +  variableType + ")")
                                .withColor(General.Color.SUCCESS)
                );
            }
            else {
                MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.notExist));
                return 0;
            }
        }

        return 1;
    }

    static int executeVariable_Copy(CommandContext<CommandSourceStack> context, boolean copyFromScore) {
        String variableName = StringArgumentType.getString(context, "variable_name");
        String playerId = StringArgumentType.getString(context, "player_id");
        String scoreName = StringArgumentType.getString(context, "score_name");

        if (!VariableHolder.doesExists(variableName)) {
            MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.notExist));
            return 0;
        }

        ServerPlayer player;

        if (playerId.equals("-me") || playerId.equals("-m")) {
            player = context.getSource().getPlayer();
        }
        else {
            player = context.getSource().getServer().getPlayerList().getPlayerByName(playerId);
        }

        if (player == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
            return 0;
        }

        if (copyFromScore) {
            try {
                VariableHolder.copyVariableFromScore(variableName, player.getScoreboardName(), scoreName);
            }
            catch (NumberFormatException e) {
                MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.invalidType));
                return 0;
            }
            catch (NullPointerException | IllegalArgumentException e) {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
                return 0;
            }
            catch (IllegalStateException e) {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.unexpected));
                return 0;
            }

            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.variable_copy_from_score")
                            .append(" " + variableName + " <- " + scoreName)
                            .withColor(General.Color.SUCCESS)
            );
        }
        else {
            try {
                VariableHolder.copyScoreFromVariable(variableName, player.getScoreboardName(), scoreName);
            }
            catch (NumberFormatException e) {
                MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.invalidType));
                return 0;
            }
            catch (NullPointerException | IllegalArgumentException e) {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
                return 0;
            }
            catch (IllegalStateException e) {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.unexpected));
                return 0;
            }

            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.variable_copy_to_score")
                            .append(" " + variableName + " -> " + scoreName)
                            .withColor(General.Color.SUCCESS)
            );
        }

        return 1;
    }

    static int executeVariable_If_Value(CommandContext<CommandSourceStack> context, String category) {
        String variableName = StringArgumentType.getString(context, "variable_name");
        String compareSign = StringArgumentType.getString(context, "compare_sign");
        String compareValue = StringArgumentType.getString(context, "compare_value");

        if (!VariableHolder.doesExists(variableName)) {
            MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.notExist));
            return 0;
        }

        boolean success;
        try {
            switch (compareSign) {
                // [Existing value (Variable)] {Sign} [Compare value] but method opposites: [Compare value] {Sign} [Existing value].
                case "=", "==" -> success = VariableHolder.doesEquals(variableName, compareValue);
                case "!=", "≠" -> success = VariableHolder.doesNotEquals(variableName, compareValue);
                case ">" -> success = VariableHolder.doesSmallerThanExisting(variableName, compareValue, false);
                case ">=", "≥" -> success = VariableHolder.doesSmallerOrEqualThanExisting(variableName, compareValue);
                case "<" -> success = VariableHolder.doesGreaterThanExisting(variableName, compareValue, false);
                case "<=", "≤" -> success = VariableHolder.doesGreaterOrEqualThanExisting(variableName, compareValue);
                case "instanceof" -> success = VariableHolder.doesInstanceof(variableName, compareValue);
                case "contains" -> success = VariableHolder.doesContains(variableName, compareValue);
                case "length" -> {
                    int length = Integer.parseInt(compareValue);
                    success = VariableHolder.doesLengthEquals(variableName, length);
                }
                case "starts_with" -> success = VariableHolder.doesStartsWith(variableName, compareValue);
                case "ends_with" -> success = VariableHolder.doesEndsWith(variableName, compareValue);
                default -> {
                    MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.undefinedOperationCategory));
                    return 0;
                }
            }
        }
        catch (NullPointerException e) {
            MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.notExist));
            return 0;
        } catch (NumberFormatException e) {
            MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.invalidType));
            return 0;
        }

        if (!success) {
            // Failed to pass comparison.
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_failed_comparison").withColor(General.Color.CONTENT));
            return 1;
        }

        return commandVariableExecution(context, variableName, category);
    }

    static int executeVariable_If_Score(CommandContext<CommandSourceStack> context, String category) {
        String playerId = StringArgumentType.getString(context, "player_id");
        String scoreName = StringArgumentType.getString(context, "score_name");
        String compareSign = StringArgumentType.getString(context, "compare_sign");
        int compareValue = IntegerArgumentType.getInteger(context, "compare_value");

        MinecraftServer server = context.getSource().getServer();
        ServerScoreboard scoreboard = server.getScoreboard();
        Objective objective = scoreboard.getObjective(scoreName);
        if (objective == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.unexpected));
            return 0;
        }

        // Resolve special cases.
        playerId = Resolver.resolveTargetPlayerName(context, playerId);

        int scoreValue;

        ScoreHolder scoreHolder = ScoreHolder.forNameOnly(playerId);
        scoreValue = scoreboard.getOrCreatePlayerScore(scoreHolder, objective).get();

        boolean success;
        try {
            switch (compareSign) {
                case "=", "==" -> success = scoreValue == compareValue;
                case "!=", "≠" -> success = scoreValue != compareValue;
                case ">" -> success = scoreValue > compareValue;
                case ">=", "≥" -> success = scoreValue >= compareValue;
                case "<" -> success = scoreValue < compareValue;
                case "<=", "≤" -> success = scoreValue <= compareValue;
                case "instanceof" -> success = false;
                case "contains" -> success = String.valueOf(scoreValue).contains(String.valueOf(compareValue));
                case "length" -> success = String.valueOf(scoreValue).length() == compareValue;
                case "starts_with" -> success = String.valueOf(scoreValue).startsWith(String.valueOf(compareValue));
                case "ends_with" -> success = String.valueOf(scoreValue).endsWith(String.valueOf(compareValue));
                default -> {
                    MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.undefinedOperationCategory));
                    return 0;
                }
            }
        }
        catch (NullPointerException e) {
            MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.notExist));
            return 0;
        } catch (NumberFormatException e) {
            MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.invalidType));
            return 0;
        }

        if (!success) {
            // Failed to pass comparison.
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_failed_comparison").withColor(General.Color.CONTENT));
            return 1;
        }

        return commandVariableExecution(context, null, category);
    }

    static int executeVariable_If_Margin(CommandContext<CommandSourceStack> context, String category) {
        String variableName = StringArgumentType.getString(context, "variable_name");
        String marginValue = StringArgumentType.getString(context, "margin_value");
        String compareValue = StringArgumentType.getString(context, "compare_value");

        if (!VariableHolder.doesExists(variableName)) {
            MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.notExist));
            return 0;
        }

        // This method only accept format: /chx variable margin_equals [variable_name] % [margin_value] {=/==} [compare_value] ...
        boolean success;
        try {
            success = VariableHolder.doesMarginEquals(variableName, marginValue, compareValue);
        }
        catch (NullPointerException e) {
            MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.notExist));
            return 0;
        }
        catch (NumberFormatException e) {
            MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.invalidType));
            return 0;
        }

        if (!success) {
            // Failed to pass comparison.
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_failed_comparison").withColor(General.Color.CONTENT));
            return 1;
        }

        return commandVariableExecution(context, variableName, category);
    }

    static int executeVariable_String(CommandContext<CommandSourceStack> context, String category) {
        String variableName = StringArgumentType.getString(context, "variable_name");

        try {
            switch (category) {
                case "to_lower" -> VariableHolder.stringToLowerCase(variableName);
                case "to_upper" -> VariableHolder.stringToUpperCase(variableName);
                default -> {
                    MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.undefinedOperationCategory));
                    return 0;
                }
            }
        }
        catch (NullPointerException e) {
            MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.notExist));
            return 0;
        }

        MessagePublisher.sendSystemMessage(context,
                Component.translatable("commands.chx.variable_case")
                        .append(Component.literal(" " + VariableHolder.getStringFrom(variableName).getOrElse("?")))
                        .withColor(General.Color.CONTENT)
        );

        return 1;
    }

    static int executeVariable_Modify(CommandContext<CommandSourceStack> context) {
        String variableName = StringArgumentType.getString(context, "variable_name");
        String category = StringArgumentType.getString(context, "category");
        String newValue = StringArgumentType.getString(context, "new_value");

        if (!VariableHolder.doesExists(variableName)) {
            MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.notExist));
            return 0;
        }

        NullableValue<String> nullableType = VariableHolder.getType(variableName);
        if (nullableType.isNull()) {
            MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.invalidType));
            return 0;
        }

        String variableType = nullableType.get();

        try {
            switch (category) {
                case "set" -> VariableHolder.modifyVariable(variableName, newValue);
                case "add" -> VariableHolder.addNumber(variableName, newValue);
                case "reduce" -> VariableHolder.reduceNumber(variableName, newValue);
                case "same" -> {
                    if (newValue.equals("-self") || newValue.equals("-s")) {
                        newValue = variableName;
                    }
                    VariableHolder.toSameValue(variableName, newValue);
                }
                default -> {
                    MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.undefinedOperationCategory));
                    return 0;
                }
            }
        }
        catch (NullPointerException e) {
            MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.notExist));
            return 0;
        }
        catch (NumberFormatException e) {
            MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.invalidType));
            return 0;
        }

        MessagePublisher.sendSystemMessage(context,
                Component.translatable("commands.chx.variable_modified")
                        .append(" " + variableName + " " + category + " " + newValue + " = " + VariableHolder.getStringFrom(variableName).getOrElse("?"))
                        .withColor(General.Color.CONTENT)
        );

        return 1;
    }

    static int executeLoot(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot").withColor(General.Color.CONTENT));
        return 1;
    }

    static int executeLoot_Help(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_introduction").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.fixed.available_commands").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext3").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext4").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext5").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_give_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext6").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_fill_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext7").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext8").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext9").withColor(General.Color.CONTENT));
        return 1;
    }

    static int executeLoot_Help_Functions(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_functions_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_functions_introduction").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.fixed.available_functions").withColor(General.Color.TITLE));

        String[] lines = {
                "set_count: (Number/Map) count",
                "set_damage: (Float:0.0~1.0) damage",
                "set_exactly_damage: (Integer) damage",
                "set_name: (String/Json) name",
                "set_lore: (List) lore",
                "set_custom_model_data: (List/Float/Boolean/String/Integer) floats, flags, strings, colors",
                "enchant_randomly: (null)",
                "set_enchantments: (Map) enchantments",
                "enchant_with_levels: (Number/Map) levels",
                "looting_enchant: (null)",
                "furnace_smelt: (null)",
                "explosion_decay: (Float:0.0~1.0) chance",
                "limit_count: (Integer) limit",
                "set_potion: (String) id",
                "set_attributes: (List) attributes",
                "set_glint_override: (Boolean) glint",
                "set_repair_cost: (Integer) cost",
                "set_food: (Map) food",
                "unbreakable: (null)",
                "set_can_break: (List) blocks",
                "set_can_place_on: (List) blocks",
                "set_consumable: (Map) consume_seconds, animation, sound, effects",
                "set_equippable: (String) slot",
                "set_trim: (String) material, pattern",
                "set_firework: (Map) flight_duration, explosions",
                "set_fire_resistant: (null)"
        };

        for (String line : lines) {
            MessagePublisher.sendSystemMessage(context,
                    Component.literal(line).withColor(General.Color.CONTENT));
        }

        return 1;
    }

    static int executeLoot_List(CommandContext<CommandSourceStack> context) {
        Set<String> tableIds = LootHolder.getRegisteredTableIds();

        if (tableIds.isEmpty()) {
            MessagePublisher.sendFailureMessage(context, returnLootError(LootError.emptyTable));
            return 0;
        }

        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_list_title").withColor(General.Color.TITLE));

        for (String tableId : tableIds) {
            MessagePublisher.sendSystemMessage(context, Component.literal(tableId).withColor(General.Color.CONTENT));
        }

        return 1;
    }

    static int executeLoot_Read(CommandContext<CommandSourceStack> context) {
        String tableId = StringArgumentType.getString(context, "table_id");

        List<Component> lines = LootHolder.readLootTable(tableId);

        if (lines.size() < 2) {
            MessagePublisher.sendSystemMessage(context, lines.getFirst());
            return 0;
        }

        for (Component line : lines) {
            MessagePublisher.sendSystemMessage(context, line);
        }

        return 1;
    }

    static int executeLoot_Give(CommandContext<CommandSourceStack> context, String category) {
        String playerId = StringArgumentType.getString(context, "player_id");
        String tableId = StringArgumentType.getString(context, "table_id");

        // Resolve special cases.
        playerId = Resolver.resolveTargetPlayerName(context, playerId);

        ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayerByName(playerId);

        if (player == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
            return 0;
        }

        boolean success;
        if (category.equals("ignore")) {
            String ignoreItem = StringArgumentType.getString(context, "ignore_item");
            success = LootHolder.sendItemToPlayerWithIgnoreItem(player, tableId, ignoreItem, false);
        } else {
            CoreHanXu.LOGGER.info("[HX] --> guaranteed: {}", category.equals("guaranteed"));
            success = LootHolder.sendItemToPlayer(player, tableId, !category.equals("with_condition"), category.equals("first_item"), category.equals("guaranteed"));
        }


        if (!success) {
            MessagePublisher.sendFailureMessage(context, returnLootError(LootError.tableNotExist));
            return 0;
        }

        MessagePublisher.sendSystemMessage(context,
                Component.translatable("commands.chx.loot_give")
                        .append(Component.literal(" " + tableId + " -> " + playerId))
                        .withColor(General.Color.SUCCESS));
        return 1;
    }

    static int executeLoot_Fill(CommandContext<CommandSourceStack> context, boolean ignoreCondition, String category) {
        int containerX = IntegerArgumentType.getInteger(context, "container_x");
        int containerY = IntegerArgumentType.getInteger(context, "container_y");
        int containerZ = IntegerArgumentType.getInteger(context, "container_z");
        String tableId = StringArgumentType.getString(context, "table_id");

        String ignoreItem = null;

        BlockPos position = new BlockPos(containerX, containerY, containerZ);
        ServerLevel level = context.getSource().getLevel();

        BlockEntity blockEntity = level.getBlockEntity(position);
        if (!(blockEntity instanceof Container)) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.notContainer));
            return 0;
        }

        boolean success;
        boolean isSorted = false, guaranteed = false;

        switch (category) {
            case "sorted":
                isSorted = true;
                break;
            case "ignore":
                ignoreItem = StringArgumentType.getString(context, "ignore_item");
                break;
            case "sorted-ignore":
                isSorted = true;
                ignoreItem = StringArgumentType.getString(context, "ignore_item");
                break;
            case "guaranteed":
                guaranteed = true;
                break;
        }

        success = LootHolder.sendItemToContainer(level, position, tableId, ignoreCondition, isSorted, ignoreItem, guaranteed);

        if (!success) {
            MessagePublisher.sendFailureMessage(context, returnLootError(LootError.tableNotExist));
            return 0;
        }

        MessagePublisher.sendSystemMessage(context,
                Component.translatable("commands.chx.loot_fill")
                        .append(Component.literal(" " + tableId + " -> " + "[" + containerX + ", " + containerY + ", " + containerZ + "]"))
                        .withColor(General.Color.SUCCESS)
        );

        return 1;
    }
    
    static int executeLoot_Template(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.notPlayer));
            return 0;
        }
        
        ItemStack book = new ItemStack(Items.WRITABLE_BOOK);
        
        String bookTemplate = lootTemplate();
        
        WritableBookContent content = new WritableBookContent(List.of(Filterable.passThrough(bookTemplate)));
        book.set(DataComponents.WRITABLE_BOOK_CONTENT, content);
        
        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }
        
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_template").withColor(General.Color.SUCCESS));
        
        return 1;
    }

    static int executeLoot_Create(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.notPlayer));
            return 0;
        }

        String toPath = StringArgumentType.getString(context, "to_path");
        if (!toPath.equals("world") && !toPath.equals("global")) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.undefinedSavePath));
            return 0;
        }

        ItemStack book = player.getMainHandItem();
        if (book.isEmpty() || (!book.is(Items.WRITABLE_BOOK) && !book.is(Items.WRITTEN_BOOK))) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.mainHandItemNotTarget));
            return 0;
        }

        String yamlContent = YamlReader.read(book);
        if (yamlContent == null || yamlContent.isEmpty()) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.noContentFound));
            return 0;
        }

        String tableId = YamlReader.readSpecificField(yamlContent, "id");
        if (tableId.isEmpty()) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.missingIdField));
            return 0;
        }

        YamlReader.TargetPath targetPath = toPath.equals("world")? YamlReader.TargetPath.TO_WORLD : YamlReader.TargetPath.TO_GLOBAL;

        if (LootHolder.doesFileLootTableExists(tableId, targetPath)) {
            MessagePublisher.sendFailureMessage(context, returnLootError(LootError.alreadyExist));
            return 0;
        }

        if (!yamlContent.contains("pools:")) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.uncompletedContent));
            return 0;
        }

        try {
            Map<String, Object> yamlMap = YamlReader.stringToMap(yamlContent);
            YamlReader.save("loot", tableId, yamlMap, targetPath);
            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.loot_created")
                            .append(" " + tableId + " -> " + targetPath)
                            .withColor(General.Color.SUCCESS)
            );
        }
        catch (Exception e) {
            MessagePublisher.sendFailureMessage(context, returnLootError(LootError.failedToSave));
            return 0;
        }

        return 1;
    }

    static int executeLoot_Delete(CommandContext<CommandSourceStack> context, String specifiedPath) {
        String tableId = StringArgumentType.getString(context, "table_id");

        boolean yamlFile = tableId.endsWith(".yaml");

        tableId = tableId.replace(".yaml", "").replace(".json", "");

        // Check and delete.
        if (specifiedPath.equals("try")) {
            boolean worldTableExist, globalTableExist;
            if (yamlFile) {
                worldTableExist = LootHolder.doesFileLootTableExists(tableId, YamlReader.TargetPath.TO_WORLD);
                globalTableExist = LootHolder.doesFileLootTableExists(tableId, YamlReader.TargetPath.TO_GLOBAL);

            }
            else {
                worldTableExist = LootHolder.doesFileLootTableExists(tableId, JsonReader.TargetPath.TO_WORLD);
                globalTableExist = LootHolder.doesFileLootTableExists(tableId, JsonReader.TargetPath.TO_GLOBAL);
            }

            if (worldTableExist && globalTableExist) {
                MessagePublisher.sendFailureMessage(context, returnLootError(LootError.sameNameFound));
                return 0;
            }
            else if (worldTableExist) {
                specifiedPath = "world";
            }
            else if (globalTableExist) {
                specifiedPath = "global";
            }
            else {
                MessagePublisher.sendFailureMessage(context, returnLootError(LootError.tableNotExist));
                return 0;
            }
        }
        switch (specifiedPath) {
            case "world":
                if (yamlFile? LootHolder.deleteFileLootTable(tableId, YamlReader.TargetPath.TO_WORLD) : LootHolder.deleteFileLootTable(tableId, JsonReader.TargetPath.TO_WORLD)) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.loot_deleted")
                                    .withColor(General.Color.CONTENT)
                    );
                    break;
                }

                MessagePublisher.sendFailureMessage(context, returnLootError(LootError.failedToDelete));
                return 0;
            case "global":
                if (yamlFile? LootHolder.deleteFileLootTable(tableId, YamlReader.TargetPath.TO_GLOBAL) : LootHolder.deleteFileLootTable(tableId, JsonReader.TargetPath.TO_GLOBAL)) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.loot_deleted")
                                    .withColor(General.Color.CONTENT)
                    );
                    break;
                }
                MessagePublisher.sendFailureMessage(context, returnLootError(LootError.failedToDelete));
                return 0;
        }

        return 1;
    }

    private static int commandCreateTemplateTimer(CommandContext<CommandSourceStack> context,
                                                  String timerId, String timeUnit, int timeAmount,
                                                  String titleParameter, String contentParameter) {
        // Create callback.
        Consumer<ServerPlayer> callback = Creator.createCallback(context, timerId, titleParameter, contentParameter);

        // Then register.
        switch (timeUnit) {
            case "t", "tick", "s", "second", "m", "minute", "h", "hour":
                TimeHolder.createTemplateTimer(timerId, timeAmount, timeUnit, callback, titleParameter, contentParameter, "core_hanxu-command");
                break;
            default:
                MessagePublisher.sendFailureMessage(context, Component.translatable("commands.core_hanxu.invalid_unit_argument"));
                return 0;
        }

        return 1;
    }

    private static int commandCreateInstanceTimer(CommandContext<CommandSourceStack> context,
                                                  String timerId, String masterString,
                                                  String timeUnit, int timeAmount,
                                                  String titleParameter, String contentParameter) {
        // Reject invalid "-me" field used by non player source.
        if ((masterString.equals("-me") || masterString.equals("-m")) && context.getSource().getPlayer() == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.invalidMeFieldUsed));
            return 0;
        }

        // Analysis UUID.
        UUID masterId = Resolver.resolveTargetUUID(context, masterString);

        // Create callback.
        Consumer<ServerPlayer> callback = Creator.createCallback(context, timerId, titleParameter, contentParameter);
        if (callback == null) {
            return 0;
        }


        // Then register if time not yet created.
        switch (timeUnit) {
            case "t", "tick", "s", "second", "m", "minute", "h", "hour":
                // Then register if timer not yet created.
                if (!TimeHolder.createInstanceTimer(masterId, timerId, timeAmount, timeUnit, callback, titleParameter, contentParameter, "core_hanxu-command")) {
                    MessagePublisher.sendFailureMessage(context,
                            returnTimerError(TimerError.alreadyExist)
                    );
                    return 0;
                }
                else {
                    return 1;
                }
            default:
                MessagePublisher.sendFailureMessage(context, Component.translatable("commands.core_hanxu.invalid_unit_argument"));
                return 0;
        }
    }

    private static int commandOperateInstanceTimer(CommandContext<CommandSourceStack> context,
                                           String timerId, String masterString,
                                           String categoryOfOperation) {
        // Get UUID.
        UUID masterId = Resolver.resolveTargetUUID(context, masterString);

        // Check if target master existed.
        if (masterId == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
            return 0;
        }

        switch (categoryOfOperation) {
            case "start" -> {
                // Then start.
                if (!TimeHolder.startInstanceTimer(masterId, timerId)) {
                    MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.unableToStart));
                    return 0;
                }

                // Send success message.
                NullableValue<Integer> nullableStarting = TimeHolder.getRemainingTimeFromInstance(masterId, timerId, "tick");
                NullableValue<String> nullableTitle = TimeHolder.getInstanceTitleParameter(masterId, timerId);
                if (nullableStarting.isNull() || nullableTitle.isNull()) {
                    MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExist));
                    return 0;
                }

                int startingTime = nullableStarting.get();
                String titleParameter = nullableTitle.get();

                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_started").withColor(General.Color.SUCCESS)
                );
                MessagePublisher.sendSystemMessage(context,
                        Component.literal(" (" + timerId + " -> " + masterString + "): " + startingTime + " tick(s) ->> " + titleParameter)
                                .withColor(General.Color.SUCCESS)
                );
            }
            case "stop" -> {
                // Then stop.
                if (!TimeHolder.stopInstanceTimer(masterId, timerId)) {
                    MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.unableToStop));
                    return 0;
                }

                // Send success message.
                NullableValue<Integer> nullableRemaining = TimeHolder.getRemainingTimeFromInstance(masterId, timerId, "tick");
                if (nullableRemaining.isNull()) {
                    MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExist));
                    return 0;
                }

                int remainingTime = nullableRemaining.get();
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_stopped").withColor(General.Color.SUCCESS)
                );
                MessagePublisher.sendSystemMessage(context,
                        Component.literal(" (" + timerId + " -> " + masterString + "): " + remainingTime + " tick(s)")
                                .withColor(General.Color.SUCCESS)
                );
            }
            case "reset" -> {
                // Then reset.
                if (!TimeHolder.resetInstanceTimer(masterId, timerId)) {
                    MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.unableToReset));
                    return 0;
                }

                // Send success message.
                NullableValue<Integer> nullableInitial = TimeHolder.getInitialTimeFromInstance(masterId, timerId, "tick");
                if (nullableInitial.isNull()) {
                    MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExist));
                    return 0;
                }

                int initialTime = nullableInitial.get();
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_reset").withColor(General.Color.SUCCESS)
                );
                MessagePublisher.sendSystemMessage(context,
                        Component.literal(" (" + timerId + " -> " + masterString + "): " + initialTime + " tick(s)")
                                .withColor(General.Color.SUCCESS)
                );
            }
            case "restart" -> {
                if (!TimeHolder.restartInstanceTimer(masterId, timerId)) {
                    MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.unableToRestart));
                    return 0;
                }

                // Send success message.
                NullableValue<Integer> nullableRestart = TimeHolder.getInitialTimeFromInstance(masterId, timerId, "tick");
                NullableValue<String> nullableTitle = TimeHolder.getInstanceTitleParameter(masterId, timerId);
                if (nullableRestart.isNull() || nullableTitle.isNull()) {
                    MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExist));
                    return 0;
                }

                int restartTime = nullableRestart.get();
                String restartTitleParameter = nullableTitle.get();

                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_restart").withColor(General.Color.SUCCESS)
                );
                MessagePublisher.sendSystemMessage(context,
                        Component.literal("(" + timerId + " -> " + masterString + "): " + restartTime + " tick(s) ->> " + restartTitleParameter)
                                .withColor(General.Color.SUCCESS)
                );
            }
            case "delete" -> {
                // Then delete.
                if (!TimeHolder.deleteInstanceTimer(masterId, timerId)) {
                    MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.unableToDeleteInstance));
                }

                // Send success message.
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_deleted")
                                .withColor(General.Color.SUCCESS)
                );
                MessagePublisher.sendSystemMessage(context,
                        Component.literal(" (" + timerId + " -> " + masterString + ")")
                                .withColor(General.Color.SUCCESS)
                );
            }
            default -> {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.undefinedOperationCategory));
                return 0;
            }
        }

        // Refresh state of F4 display.
        if (context.getSource().getPlayer() != null) {
            TimeHolder.checkAndRefreshDisplay(context.getSource().getPlayer(), masterId, timerId);
        }

        return 1;
    }

    private static int commandVariableExecution(CommandContext<CommandSourceStack> context, String variableName, String category) {
        // If passed.
        if (category.equals("execute")) {
            String command = StringArgumentType.getString(context, "command");
            if (!command.startsWith("/")) {
                command = "/" + command;
            }
            context.getSource().getServer().getCommands().performPrefixedCommand(context.getSource(), command);
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_executed").withColor(General.Color.CONTENT));
        }
        else if (category.equals("then")) {
            String targetVariableName = StringArgumentType.getString(context, "target_variable");
            String action = StringArgumentType.getString(context, "action");
            String target = StringArgumentType.getString(context, "target");
            String targetPlayerId = null;
            boolean getPlayerByContext = false;
            List<String> specialCases = List.of("-me", "-m", "-random", "-r", "-nearest", "-n");
            try {
                targetPlayerId = StringArgumentType.getString(context, "optional_player_id");

                if (specialCases.contains(targetPlayerId)) {
                    getPlayerByContext = true;
                }
            }
            catch (Exception e) {
                getPlayerByContext = true;
            }

            if (getPlayerByContext) {
                if (targetPlayerId == null) {
                    MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.missingIdField));
                    return 0;
                }
                targetPlayerId = Resolver.resolveTargetPlayerName(context, targetPlayerId);
                if (targetPlayerId == null) {
                    MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
                    return 0;
                }
            }

            // For special case.
            if (targetVariableName.equals("-self") || targetVariableName.equals("-s")) {
                if (variableName == null) {
                    MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.selfFieldInScoreIf));
                    return 0;
                }
                targetVariableName = variableName;
            }

            if (!VariableHolder.doesExists(targetVariableName)) {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
                return 0;
            }

            try {
                switch (action) {
                    case "set" -> VariableHolder.modifyVariable(targetVariableName, target);
                    case "add" -> VariableHolder.addNumber(targetVariableName, target);
                    case "reduce" -> VariableHolder.reduceNumber(targetVariableName, target);
                    case "copy_from" -> {
                        ServerPlayer player = context.getSource().getPlayer();
                        if (player != null) {
                            VariableHolder.copyVariableFromScore(targetVariableName, targetPlayerId, target);
                        }
                    }
                    case "copy_to" -> {
                        ServerPlayer player = context.getSource().getPlayer();
                        if (player != null) {
                            VariableHolder.copyScoreFromVariable(targetVariableName, targetPlayerId, target);
                        }
                    }
                    case "same" -> VariableHolder.toSameValue(targetVariableName, target);
                }
            }
            catch (NullPointerException e) {
                MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.notExist));
                return 0;
            }
            catch (NumberFormatException e) {
                MessagePublisher.sendFailureMessage(context, returnVariableError(VariableError.invalidType));
                return 0;
            }
            catch (IllegalStateException e) {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.unexpected));
                return 0;
            }
            catch (IllegalArgumentException e) {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
                return 0;
            }

            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_target_finished").withColor(General.Color.CONTENT));
        }

        return 1;
    }

    private static int displayCommandTimerRead(CommandContext<CommandSourceStack> context,
                                       String timerId, String masterString, String timeUnit,
                                       String infoCategory, String timerCategory) {
        if (timerCategory.equals("template")) {
            switch (infoCategory) {
                case "remaining_time":
                    NullableValue<Integer> nullableRemaining = TimeHolder.getRemainingTimeFromTemplate(timerId, timeUnit);
                    if (nullableRemaining.isNull()) {
                        MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExist));
                        return 0;
                    }

                    int remainingTime = nullableRemaining.get();
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.timer_read_remaining_time")
                                    .append(Component.literal(" (" + timerId + "): " + remainingTime + " " + timeUnit))
                                    .withColor(General.Color.TITLE)
                    );
                    break;
                case "initial_time":
                    NullableValue<Integer> nullableInitial = TimeHolder.getInitialTimeFromTemplate(timerId, timeUnit);
                    if (nullableInitial.isNull()) {
                        MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExist));
                        return 0;
                    }

                    int initialTime = nullableInitial.get();
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.timer_read_initial_time")
                                    .append(Component.literal(" (" + timerId + "): " + initialTime + " " + timeUnit))
                                    .withColor(General.Color.TITLE)
                    );
                    break;
                case "state":
                    boolean isCounting = TimeHolder.isTemplateTimerCounting(timerId);

                    if (TimeHolder.getRemainingTimeFromTemplate(timerId, timeUnit).isPresent()) {
                        MessagePublisher.sendSystemMessage(context,
                                Component.translatable("commands.chx.timer_state")
                                        .append(Component.literal(" (" + timerId + "): " + (isCounting? "Counting" : "Stopping")))
                                        .withColor(General.Color.TITLE)
                        );
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExist));
                        return 0;
                    }
                    break;
                case "end_behavior":
                    NullableValue<String> nullableTitle = TimeHolder.getTemplateTitleParameter(timerId);
                    NullableValue<String> nullableContent = TimeHolder.getTemplateContentParameter(timerId);

                    String titleParameter = nullableTitle.getOrElse("null");
                    String contentParameter = nullableContent.modify(str -> " : " + str).getOrElse("");

                    if (TimeHolder.getRemainingTimeFromTemplate(timerId, timeUnit).isPresent()) {
                        MessagePublisher.sendSystemMessage(context,
                                Component.translatable("commands.chx.timer_end_behavior")
                                        .append(Component.literal("[HX] (" + timerId + ") <<- "))
                                        .append(titleParameter)
                                        .append(contentParameter)
                                        .withColor(General.Color.TITLE)
                        );
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExist));
                        return 0;
                    }
                    break;
                default:
                    return 0;
            }
            return 1;
        }
        else if (timerCategory.equals("instance")) {
            UUID masterId = Resolver.resolveTargetUUID(context, masterString);

            switch (infoCategory) {
                case "remaining_time":
                    NullableValue<Integer> nullableRemaining = TimeHolder.getRemainingTimeFromInstance(masterId, timerId, "tick");
                    if (nullableRemaining.isNull()) {
                        MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExist));
                        return 0;
                    }

                    int remainingTime = nullableRemaining.get();
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.timer_read_remaining_time")
                                    .append(Component.literal(" (" + timerId + " -> " + masterString + "): " + remainingTime + " " + timeUnit))
                                    .withColor(General.Color.TITLE)
                    );

                    break;
                case "initial_time":
                    NullableValue<Integer> nullableInitial = TimeHolder.getInitialTimeFromInstance(masterId, timerId, timeUnit);
                    if (nullableInitial.isNull()) {
                        MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExist));
                        return 0;
                    }

                    int initialTime = nullableInitial.get();
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.timer_read_initial_time")
                                    .append(Component.literal(" (" + timerId + "->" + masterString + "): " + initialTime + " " + timeUnit))
                                    .withColor(General.Color.TITLE)
                    );
                    break;
                case "state":
                    boolean isCounting = TimeHolder.isInstanceTimerCounting(masterId, timerId);
                    if (TimeHolder.getRemainingTimeFromInstance(masterId, timerId, timeUnit).isPresent()) {
                        MessagePublisher.sendSystemMessage(context,
                                Component.translatable("commands.chx.timer_state")
                                        .append(Component.literal(" (" + timerId + "): " + (isCounting? "Counting" : "Stopping")))
                                        .withColor(General.Color.TITLE)
                        );
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExist));
                        return 0;
                    }
                    break;
                case "end_behavior":
                    NullableValue<String> nullableTitle = TimeHolder.getInstanceTitleParameter(masterId, timerId);
                    NullableValue<String> nullableContent = TimeHolder.getInstanceContentParameter(masterId, timerId);
                    if (nullableTitle.isNull() || nullableContent.isNull()) {
                        MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExist));
                        return 0;
                    }

                    String titleParameter = nullableTitle.getOrElse("null");
                    String contentParameter = nullableContent.modify(str -> " : " + str).getOrElse("");

                    if (TimeHolder.getRemainingTimeFromInstance(masterId, timerId, timeUnit).isPresent()) {
                        MessagePublisher.sendSystemMessage(context,
                                Component.translatable("commands.chx.timer_end_behavior")
                                        .append(Component.literal("[HX] (" + timerId + " -> " + masterString + ") <<- "))
                                        .append(titleParameter)
                                        .append(contentParameter)
                                        .withColor(General.Color.TITLE)
                        );
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExist));
                        return 0;
                    }
                    break;
                default:
                    return 0;
            }
            return 1;
        }
        else {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.undefinedOperationCategory));
            return 0;
        }
    }

    private static int displayTimerIdList(CommandContext<CommandSourceStack> context, String[] idList) {
        if (idList.length == 0) {
            MessagePublisher.sendFailureMessage(context, Component.translatable("commands.chx.fixed.empty"));
            return 0;
        }

        MessagePublisher.sendSystemMessage(context,
                Component.translatable("commands.chx.timer_list_title").withColor(General.Color.TITLE)
        );
        for (String id : idList) {
            MessagePublisher.sendSystemMessage(context, Component.literal(id).withColor(General.Color.CONTENT));
        }

        return 1;
    }

    private static void displayTimerCreateMessage(CommandContext<CommandSourceStack> context, String timerId, int timeAmount, String timeUnit, String titleParameter, String contentParameter) {
        // Output message.
        MessagePublisher.sendSystemMessage(context,
                Component.translatable("commands.chx.timer_created")
                        .append(Component.literal(" " + timerId + " -> " + timeAmount + " " + timeUnit))
                        .withColor(General.Color.SUCCESS)
        );
        switch (titleParameter) {
            case "e", "execute":
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_with_execute_behavior")
                                .append(Component.literal(": " + contentParameter))
                                .withColor(General.Color.SUCCESS)
                );
                break;
            case "r", "remind":
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_with_remind_behavior")
                                .append(Component.literal(": " + contentParameter))
                                .withColor(General.Color.SUCCESS)
                );
                break;
            default:
                break;
        }
    }

    static void displaySceneIdList(CommandContext<CommandSourceStack> context, List<Component> displayList) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_list_title").withColor(General.Color.TITLE));
        for (Component line : displayList) {
            MessagePublisher.sendSystemMessage(context, line);
        }
    }

    private static int displayAttributeIdList(CommandContext<CommandSourceStack> context, String[] attributeArrayList, boolean isApiAttribute) {
        MessagePublisher.sendSystemMessage(
            context,
            isApiAttribute?
                Component.translatable("commands.chx.attribute_api_list_title").withColor(General.Color.TITLE) :
                Component.translatable("commands.chx.attribute_yaml_list_title").withColor(General.Color.TITLE)
            );

        if (attributeArrayList.length == 0) {
            MessagePublisher.sendFailureMessage(
                context, Component.translatable("commands.chx.fixed.empty")
            );
            return 0;
        }

        for (String attribute : attributeArrayList) {
            MessagePublisher.sendSystemMessage(context, Component.literal(attribute).withColor(General.Color.CONTENT));
        }

        return 1;
    }

    private static void displayAttributeCreateMessage(CommandContext<CommandSourceStack> context, String attributeId, float maximum, float defaultValue, boolean registered) {
        if (registered) {
            MessagePublisher.sendSystemMessage(
                context,
                Component.translatable("commands.chx.attribute_created")
                        .append(Component.literal(" " + attributeId + " -> " + maximum + " _ " + defaultValue))
                        .withColor(General.Color.SUCCESS)
            );
        }
        else {
            MessagePublisher.sendFailureMessage(context, returnAttributeError(AttributeError.sameNameFound));
        }
    }

    private static String lootTemplate() {
        return """
            id: "FILE NAME?"
            
            pools:
              - rolls: 1
                entries:
                  - type: item
                    id: "minecraft:iron_ingot"
                    weight: 3
                    functions:
                      - function: set_count
                        count:
                          min: 1
                          max: 4
                  - type: item
                    id: "minecraft:gold_ingot"
                    weight: 1
                conditions:
                  - condition: random_chance
                    chance: 0.5
            """;
    }
}
