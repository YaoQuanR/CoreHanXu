package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.LootHolder;
import core.yaoquan.hanxu.api.SceneHolder;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.tool.MessagePublisher;
import core.yaoquan.hanxu.util.type.MethodResult;
import core.yaoquan.hanxu.util.type.NullableValue;
import core.yaoquan.hanxu.util.tool.Resolver;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

import static core.yaoquan.hanxu.api.define.Error.*;

public class ExecuteRun {
    public static int executeTimer_Instance_Start(CommandContext<CommandSourceStack> context) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        return CommandMisc.commandOperateInstanceTimer(context, timerId, masterString, "start");
    }

    public static int executeScene_Play(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();

        try {
            String playerId = StringArgumentType.getString(context, "player_id");
            UUID playerUUID = Resolver.resolveTargetUUID(context, playerId);
            player = Resolver.resolveTargetPlayer(playerUUID);
        }
        catch (IllegalArgumentException ignored) {}

        if (player == null) {
            MessagePublisher.sendFailureMessage(context, core.yaoquan.hanxu.api.define.Error.errorComponent(core.yaoquan.hanxu.api.define.Error.GeneralError.notPlayer));
            return 0;
        }

        String sceneName = StringArgumentType.getString(context, "scene_name");

        // Check if existed.
        if (!SceneHolder.doesSceneExist(sceneName)) {
            MessagePublisher.sendFailureMessage(context, core.yaoquan.hanxu.api.define.Error.errorComponent(core.yaoquan.hanxu.api.define.Error.SceneError.notFound));
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
            MessagePublisher.sendFailureMessage(context, errorComponent(SceneError.playFailed));
            return 0;
        }

        return 1;
    }

    public static int executeScene_Broadcast(CommandContext<CommandSourceStack> context) {
        String sceneName = StringArgumentType.getString(context, "scene_name");

        // Check if existed.
        if (!SceneHolder.doesSceneExist(sceneName)) {
            MessagePublisher.sendFailureMessage(context, errorComponent(SceneError.notFound));
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
            MessagePublisher.sendFailureMessage(context, errorComponent(SceneError.playFailed));
            return 0;
        }

        return 1;
    }

    public static int executeLoot_Give(CommandContext<CommandSourceStack> context, String category) {
        String playerId = StringArgumentType.getString(context, "player_id");
        String tableId = StringArgumentType.getString(context, "table_id");

        // Resolve special cases.
        playerId = Resolver.resolveTargetPlayerName(context, playerId);

        ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayerByName(playerId);

        if (player == null) {
            MessagePublisher.sendFailureMessage(context, errorComponent(GeneralError.targetNotExist));
            return 0;
        }

        MethodResult result;
        if (category.equals("ignore")) {
            String ignoreItem = StringArgumentType.getString(context, "ignore_item");
            result = LootHolder.sendItemToPlayerWithIgnoreItem(player, tableId, ignoreItem, false);
        } else {
            CoreHanXu.LOGGER.info("[HX] --> guaranteed: {}", category.equals("guaranteed"));
            result = LootHolder.sendItemToPlayer(player, tableId, !category.equals("with_condition"), category.equals("first_item"), category.equals("guaranteed"));
        }

        String finalPlayerId = playerId;
        return result.matching(
                () -> {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.loot_give")
                                    .append(Component.literal(" " + tableId + " -> " + finalPlayerId))
                                    .withColor(General.Color.SUCCESS));
                    return 1;
                },
                (error, info) -> {
                    CommandError.displayLootErrorResult(context, error);
                    return 0;
                }
        );
    }

    public static int executeLoot_Fill(CommandContext<CommandSourceStack> context, boolean ignoreCondition, String category) {
        int containerX = IntegerArgumentType.getInteger(context, "container_x");
        int containerY = IntegerArgumentType.getInteger(context, "container_y");
        int containerZ = IntegerArgumentType.getInteger(context, "container_z");
        String tableId = StringArgumentType.getString(context, "table_id");

        String ignoreItem = null;

        BlockPos position = new BlockPos(containerX, containerY, containerZ);
        ServerLevel level = context.getSource().getLevel();

        BlockEntity blockEntity = level.getBlockEntity(position);
        if (!(blockEntity instanceof Container)) {
            MessagePublisher.sendFailureMessage(context, errorComponent(GeneralError.notContainer));
            return 0;
        }

        boolean isSorted = false, guaranteed = false;

        switch (category) {
            case "sorted" -> isSorted = true;
            case "ignore" -> ignoreItem = StringArgumentType.getString(context, "ignore_item");
            case "sorted-ignore" -> {
                isSorted = true;
                ignoreItem = StringArgumentType.getString(context, "ignore_item");
            }
            case "guaranteed" -> guaranteed = true;
        }

        MethodResult result = LootHolder.sendItemToContainer(level, position, tableId, ignoreCondition, isSorted, ignoreItem, guaranteed);

        return result.matching(
                () -> {
                    MessagePublisher.sendSystemMessage(context,
                            Component.translatable("commands.chx.loot_fill")
                                    .append(Component.literal(" " + tableId + " -> " + "[" + containerX + ", " + containerY + ", " + containerZ + "]"))
                                    .withColor(General.Color.SUCCESS)
                    );
                    return 1;
                },
                (error, info) -> {
                    CommandError.displayLootErrorResult(context, error);
                    return 0;
                }
        );
    }

    public static int executeWeather_Start(CommandContext<CommandSourceStack> context) {
        String weatherId = StringArgumentType.getString(context, "weather_id");

        NullableValue<ServerLevel> nullableLevel = Resolver.resolveServerLevel(context);
        if (nullableLevel.isNull()) {
            return 0;
        }
        ServerLevel level = nullableLevel.get();

        MethodResult result = WeatherHolder.startWeather(level, weatherId);

        return result.matching(
                () -> {
                    MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_started").withColor(General.Color.SUCCESS));
                    return 1;
                },
                (error, info) -> {
                    CommandError.displayWeatherErrorResult(context, error);
                    return 0;
                }
        );
    }

    public static int executeWeather_ResumeId(CommandContext<CommandSourceStack> context) {
        String weatherId = StringArgumentType.getString(context, "weather_id");

        NullableValue<ServerLevel> nullableLevel = Resolver.resolveServerLevel(context);
        if (nullableLevel.isNull()) {
            return 0;
        }
        ServerLevel level = nullableLevel.get();

        MethodResult result = WeatherHolder.resumeWeather(level, weatherId);

        return result.matching(
                () -> {
                    MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_resumed").withColor(General.Color.SUCCESS));
                    return 1;
                },
                (error, info) -> {
                    CommandError.displayWeatherErrorResult(context, error);
                    return 0;
                }
        );
    }

    public static int executeWeather_ResumeType(CommandContext<CommandSourceStack> context) {
        String weatherType = StringArgumentType.getString(context, "weather_type");

        NullableValue<ServerLevel> nullableLevel = Resolver.resolveServerLevel(context);
        if (nullableLevel.isNull()) {
            return 0;
        }
        ServerLevel level = nullableLevel.get();

        WeatherHolder.WeatherType type = WeatherHolder.parseType(weatherType);

        MethodResult result = WeatherHolder.resumeWeather(level, type);

        return result.matching(
                () -> {
                    MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_type_resumed").withColor(General.Color.SUCCESS));
                    return 1;
                },
                (error, info) -> {
                    CommandError.displayWeatherErrorResult(context, error);
                    return 0;
                }
        );
    }
}
