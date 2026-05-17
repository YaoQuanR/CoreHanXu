package core.yaoquan.hanxu.registry.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.PermissionHolder;
import core.yaoquan.hanxu.api.SceneHolder;
import core.yaoquan.hanxu.api.TimeHolder;
import core.yaoquan.hanxu.api.define.FilePath;
import core.yaoquan.hanxu.api.define.Version;
import core.yaoquan.hanxu.registry.ModConfig;
import core.yaoquan.hanxu.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.level.GameRules;
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
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.detail_title").withColor(0xFFD700));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.detail_innertext1").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context,
            Component.translatable("commands.chx.detail_innertext2")
                    .append(Component.literal(" " + Version.getCoreVersion()))
                    .withColor(0xFFFACD)
        );
        return 1;
    }

    static int executeLicense(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_title").withColor(0xFFD700));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_innertext1").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_innertext2").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_innertext3").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_innertext4").withColor(0xFFFACD));
        return 1;
    }

    static int executeLicense_Origin(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_title").withColor(0xFFD700));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext1").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext2").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext3").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext4").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext5").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext6").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext7").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext8").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext9").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext10").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext11").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext12").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext13").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext14").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext15").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext16").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext17").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext18").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext19").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext20").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext21").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext22").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext23").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext24").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext25").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext26").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext27").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext28").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext29").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext30").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext31").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext32").withColor(0xFFFACD));
        return 1;
    }

    static int executeLicense_Agree(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof Player player) {
            if (!PermissionHolder.getLicenseState(player)) {
                MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_agree").withColor(0xFFFACD));
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
            boolean state = PermissionHolder.getLicenseState(player);
            MessagePublisher.sendSystemMessage(context, Component.literal(String.valueOf(state)).withColor(0xFFD700));
            return 1;
        }
        return 0;
    }

    static int executeScene_List(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        List<Component> displayList = new ArrayList<>();

        // Scan global path.
        Path globalPath = FilePath.getGlobalPath().resolve("scene");
        if (Files.isDirectory(globalPath)) {
            try (Stream<Path> stream = Files.list(globalPath)) {
                stream.filter(p -> p.toString().endsWith(".yaml")).forEach(p -> {
                    displayList.add(Component.literal("(global): " + p.getFileName().toString()).withColor(0xFFFACD));
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
                        displayList.add(Component.literal("(world): " + p.getFileName().toString()).withColor(0xFFFACD));
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
            CommandDisplay.displaySceneList(context, displayList);
        }

        return 1;
    }

    static int executeScene_Play(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
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
                        .withColor(0xFFFACD));
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
                            .withColor(0xFFFACD));
            SceneHolder.playSceneToEveryone(context.getSource().getServer(), sceneName);
        }
        catch (Exception e) {
            MessagePublisher.sendFailureMessage(context, returnSceneError(SceneError.playFailed));
            return 0;
        }

        return 1;
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
            boolean state = PermissionHolder.getLicenseState(player);
            MessagePublisher.sendSystemMessage(context, Component.literal(String.valueOf(state)).withColor(0xFFD700));
            return 1;
        }
    }

    static int executeTimer(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer").withColor(0xFFD700));
        return 1;
    }

    static int executeBare(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.bare1").withColor(0xFFD700));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.bare2").withColor(0xFFD700));
        return 1;
    }

    static int executeHelp(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();
        if (player == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.notPlayer));
            return 0;
        }

        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_title").withColor(0xFFD700));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_page").withColor(0xFFD700));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.fixed.available_commands").withColor(0xFFD700));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext1").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext2").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext3").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext4").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext5").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext6").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext7").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext8").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext9").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext10").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext11").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext12").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext13").withColor(0xFFFACD));
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

                    int permissionLevel = PermissionHolder.getPlayerPermissionLevel(player);
                    MessagePublisher.sendSystemMessage(context, Component.literal(String.valueOf(permissionLevel)).withColor(0xFFD700));
                }
            }
            case "server" -> {
                int commandblockPermissionLevel = context.getSource().getLevel().getGameRules().getInt(PermissionHolder.OVERRIDE_COMMAND_BLOCK_PERMISSION);
                MessagePublisher.sendSystemMessage(context, Component.literal(String.valueOf(commandblockPermissionLevel)).withColor(0xFFD700));
            }
            case "player_override" -> {
                int overridePlayerPermissionLevel = PermissionHolder.getPlayerOverridePermissionLevel();
                MessagePublisher.sendSystemMessage(context, Component.literal(String.valueOf(overridePlayerPermissionLevel)).withColor(0xFFD700));
            }
            case "player_first_grant" -> {
                int autoAuthorizedPermissionLevel = ModConfig.SET_AUTO_AUTHORIZED_PERMISSION_LEVEL.getAsInt();
                MessagePublisher.sendSystemMessage(context, Component.literal(String.valueOf(autoAuthorizedPermissionLevel)).withColor(0xFFD700));
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
        if (playerId.equals("-me") || playerId.equals("-m")) {
            if (context.getSource().getPlayer() != null) {
                player = context.getSource().getPlayer();
            }
            else {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.notPlayer));
                return 0;
            }
        }
        else {
            player = server.getPlayerList().getPlayerByName(playerId);
            if (player == null) {
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
                return 0;
            }
        }

        int newLevel = IntegerArgumentType.getInteger(context, "level");

        boolean editable = ModConfig.SET_PLAYER_PERMISSION_EDITABLE.getAsBoolean();

        if (!editable) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.uneditablePlayerPermission));
            return 0;
        }

        PermissionHolder.setPlayerPermissionLevel(player, newLevel);

        MessagePublisher.sendSystemMessage(context, Component.literal("✔ -> " + newLevel).withColor(0xFFD700));
        return 1;
    }

    static int executeTimer_Help(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();
        if (player == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.notPlayer));
            return 0;
        }

        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_title").withColor(0xFFD700));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_introduction").withColor(0xFFD700));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.fixed.available_commands").withColor(0xFFD700));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext1").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_create_argument1").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext2").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_create-range_argument1").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext3").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext4").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext5").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_create_argument2").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext6").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_create-range_argument2").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext7").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext8").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext9").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext10").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext11").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext12").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext13").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_modify_argument").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext14").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_read_argument1").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_read_argument2").withColor(0xFFFACD));
        return 1;
    }

    static int executeTimer_Template_Create(CommandContext<CommandSourceStack> context, String endBehavior) {
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
            behaviorContent = null;
        }

        // Check if timer exist.
        if (TimeHolder.getRemainingTimeFromTemplate(timerId, timeUnit) != -1) {
            MessagePublisher.sendFailureMessage(context, Component.translatable("commands.chx.timer_already_exist"));
            return 0;
        }

        return commandCreateTemplateTimer(context, timerId, timeUnit, timeAmount, endBehavior, behaviorContent);
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

        return CommandDisplay.displayCommandTimerRead(context, timerId, "", timeUnit, infoCategory, "template");
    }

    static int executeTimer_Template_Delete(CommandContext<CommandSourceStack> context) {
        // Receive argument.
        String timerId = StringArgumentType.getString(context, "timer_id");

        boolean isDeleted = TimeHolder.deleteTemplateTimer(timerId);
        if (isDeleted) {
            MessagePublisher.sendSystemMessage(context,
                    Component.translatable("commands.chx.timer_deleted")
                            .append(Component.literal(" (" + timerId + ")"))
                            .withColor(0xFFD700)
            );
        } else {
            MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExist));
        }

        return 1;
    }

    static int executeTimer_Template_CreateRange(CommandContext<CommandSourceStack> context, String endBehavior) {
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
            behaviorContent = null;
        }

        int selectedTimeAmount = Converter.convertFromRangeToRandom(timeFirstRange, timeSecondRange);

        // Check if timer exist.
        if (TimeHolder.getRemainingTimeFromTemplate(timerId, timeUnit) != -1) {
            MessagePublisher.sendFailureMessage(context, Component.translatable("commands.chx.timer_already_exist"));
            return 0;
        }

        return commandCreateTemplateTimer(context, timerId, timeUnit, selectedTimeAmount, endBehavior, behaviorContent);
    }

    static int executeTimer_Template_List(CommandContext<CommandSourceStack> context) {
        String[] templateIds = TimeHolder.getAllTemplateIds();

        return CommandDisplay.displayIdList(context, templateIds);
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
                            .withColor(0x66FF66)
            );
        }
        else {
            MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExistOrAlreadyInstantiated));
            return 0;
        }

        return 1;
    }

    static int executeTimer_Instance_Create(CommandContext<CommandSourceStack> context, String endBehavior) {
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
            behaviorContent = null;
        }

        int returnValue = commandCreateInstanceTimer(context, timerId, masterString, timeUnit, timeAmount, endBehavior, behaviorContent);

        if (returnValue == 1) {
            CommandDisplay.displayCreateMessage(context, timerId, timeAmount, timeUnit, endBehavior, behaviorContent);
            return 1;
        }
        else {
            return 0;
        }
    }

    static int executeTimer_Instance_CreateRange(CommandContext<CommandSourceStack> context, String endBehavior) {
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
            behaviorContent = null;
        }

        int selectedTimeAmount = Converter.convertFromRangeToRandom(timeFirstRange, timeSecondRange);

        int returnValue = commandCreateInstanceTimer(context, timerId, masterString, timeUnit, selectedTimeAmount, endBehavior, behaviorContent);

        if (returnValue == 1) {
            CommandDisplay.displayCreateMessage(context, timerId, selectedTimeAmount, timeUnit, endBehavior, behaviorContent);
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
                if (TimeHolder.modifyInstanceTimer(masterId, timerId, timeAmount, timeUnit, category)) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.timer_success_modification")
                                    .append(Component.literal(" (" + timerId + " -> " + masterString + "): " + category + " " + timeAmount + " " + timeUnit))
                                    .withColor(0xFFD700)
                    );
                    return 1;
                }
                else {
                    MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.notExist));
                    return 0;
                }
            default:
                MessagePublisher.sendFailureMessage(context, Component.translatable("commands." + CoreHanXu.MOD_ID + ".invalid_unit_argument"));
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

        return CommandDisplay.displayCommandTimerRead(context, timerId, masterString, timeUnit, infoCategory, "instance");
    }

    static int executeTimer_Instance_List(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");

        UUID masterId = Resolver.resolveTargetUUID(context, masterString);

        if (masterId == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.targetNotExist));
            return 0;
        }

        String[] instanceIds = TimeHolder.getAllInstanceIds(masterId);

        return CommandDisplay.displayIdList(context, instanceIds);
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

        TimeHolder.displayToInfoPage(player, masterId, timerId, state, false);
        MessagePublisher.sendSystemMessage(context,
                Component.literal("[HX] " + timerId + " ")
                        .append(Component.translatable("commands." + CoreHanXu.MOD_ID + ".has_changed_to"))
                        .append(Component.literal(" " + state))
                        .withColor(0x66FF66)
        );

        return 1;
    }

    static int executeScene(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene").withColor(0xFFD700));
        return 1;
    }

    static int executeScene_Help(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_title").withColor(0xFFD700));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_introduction").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext1").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext2").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext3").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext4").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext5").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext6").withColor(0xFFFACD));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext7").withColor(0xFFFACD));
        return 1;
    }

    static int executeScene_Delete(CommandContext<CommandSourceStack> context, String specifiedPath) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.notPlayer));
            return 0;
        }

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
                                    .withColor(0xFFFACD));
                    break;
                }
                MessagePublisher.sendFailureMessage(context, returnSceneError(SceneError.failedToDelete));
                return 0;
            case "global":
                if (SceneHolder.deleteScene(sceneName, YamlReader.TargetPath.TO_GLOBAL)) {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.scene_deleted")
                                    .withColor(0xFFFACD));
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

        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_get_template").withColor(0xFFFACD));
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
            YamlReader.save("scene", sceneId + ".yaml", yamlMap, targetPath);
            MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_created").withColor(0xFFFACD));
        }
        catch (Exception e) {
            MessagePublisher.sendFailureMessage(context, returnSceneError(SceneError.failedToSave));
            return 0;
        }

        return 1;
    }

    private static int commandCreateTemplateTimer(CommandContext<CommandSourceStack> context,
                                                  String timerId, String timeUnit, int timeAmount,
                                                  String endBehavior, String behaviorContent) {
        // Create callback.
        Consumer<ServerPlayer> callback = Creator.createCallback(context, timerId, endBehavior, behaviorContent);

        // Then register.
        switch (timeUnit) {
            case "t", "tick", "s", "second", "m", "minute", "h", "hour":
                TimeHolder.createTemplateTimer(timerId, timeAmount, timeUnit, callback, endBehavior, behaviorContent, "core_hanxu-command");
                break;
            default:
                MessagePublisher.sendFailureMessage(context, Component.translatable("commands." + CoreHanXu.MOD_ID + ".invalid_unit_argument"));
                return 0;
        }

        return 1;
    }

    private static int commandCreateInstanceTimer(CommandContext<CommandSourceStack> context,
                                                  String timerId, String masterString,
                                                  String timeUnit, int timeAmount,
                                                  String endBehavior, String behaviorContent) {
        // Analysis UUID.
        UUID masterId = Resolver.resolveTargetUUID(context, masterString);

        // Create callback.
        Consumer<ServerPlayer> callback = Creator.createCallback(context, timerId, endBehavior, behaviorContent);
        if (callback == null) {
            return 0;
        }


        // Then register if time not yet created.
        switch (timeUnit) {
            case "t", "tick", "s", "second", "m", "minute", "h", "hour":
                // Then register if timer not yet created.
                if (!TimeHolder.createInstanceTimer(masterId, timerId, timeAmount, timeUnit, callback, endBehavior, behaviorContent, "core_hanxu-command")) {
                    MessagePublisher.sendFailureMessage(context,
                            returnTimerError(TimerError.alreadyExist)
                    );
                    return 0;
                }
                else {
                    return 1;
                }
            default:
                MessagePublisher.sendFailureMessage(context, Component.translatable("commands." + CoreHanXu.MOD_ID + ".invalid_unit_argument"));
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
            case "start":
                // Then start.
                if (!TimeHolder.startInstanceTimer(masterId, timerId)) {
                    MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.unableToStart));
                    return 0;
                }

                // Send success message.
                int startingTime = TimeHolder.getRemainingTimeFromInstance(masterId, timerId, "tick");
                String endBehavior = TimeHolder.getInstanceEndBehavior(masterId, timerId);
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_started").withColor(0x66FF66)
                );
                MessagePublisher.sendSystemMessage(context,
                        Component.literal(" (" + timerId + " -> " + masterString + "): " + startingTime + " tick(s) ->> " + endBehavior)
                                .withColor(0x66FF66)
                );

                break;
            case "stop":
                // Then stop.
                if (!TimeHolder.stopInstanceTimer(masterId, timerId)) {
                    MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.unableToStop));
                    return 0;
                }

                // Send success message.
                int remainingTime = TimeHolder.getRemainingTimeFromInstance(masterId, timerId, "tick");
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_stopped").withColor(0x66FF66)
                );
                MessagePublisher.sendSystemMessage(context,
                        Component.literal(" (" + timerId + " -> " + masterString + "): " + remainingTime + " tick(s)")
                                .withColor(0x66FF66)
                );

                break;
            case "reset":
                // Then reset.
                if (!TimeHolder.resetInstanceTimer(masterId, timerId)) {
                    MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.unableToReset));
                    return 0;
                }

                // Send success message.
                int initialTime = TimeHolder.getInitialTimeFromInstance(masterId, timerId, "tick");
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_reset").withColor(0x66FF66)
                );
                MessagePublisher.sendSystemMessage(context,
                        Component.literal(" (" + timerId + " -> " + masterString + "): " + initialTime + " tick(s)")
                                .withColor(0x66FF66)
                );

                break;
            case "delete":
                // Then delete.
                if (!TimeHolder.deleteInstanceTimer(masterId, timerId)) {
                    MessagePublisher.sendFailureMessage(context, returnTimerError(TimerError.unableToDeleteInstance));
                }

                // Send success message.
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_deleted")
                                .withColor(0x66FF66)
                );
                MessagePublisher.sendSystemMessage(context,
                        Component.literal(" (" + timerId + " -> " + masterString + ")")
                                .withColor(0x66FF66)
                );

                break;
            default:
                MessagePublisher.sendFailureMessage(context, returnGeneralError(GeneralError.undefinedOperationCategory));
                return 0;
        }

        // Refresh state of F4 display.
        if (context.getSource().getPlayer() != null) {
            TimeHolder.checkAndRefreshDisplay(context.getSource().getPlayer(), masterId, timerId);
        }

        return 1;
    }
}
