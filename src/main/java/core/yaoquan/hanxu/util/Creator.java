package core.yaoquan.hanxu.util;

import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.AttributeHolder;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.api.custom.BehaviorRegistry;
import core.yaoquan.hanxu.api.define.Error;
import core.yaoquan.hanxu.api.weather.ColoredRain;
import core.yaoquan.hanxu.api.weather.Fog;
import core.yaoquan.hanxu.api.weather.Wind;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import static core.yaoquan.hanxu.api.define.Error.errorComponent;

public class Creator {
    /**
     * Use this method to create callback behavior, it is same to command timer creation.
     * @param context           CommandSourceStack from command builder {@link com.mojang.brigadier.context}.
     * @param timerId           Unique title of timer.
     * @param titleParameter    If you are using command callback generator,
     *                          remind/execute/null is required to fill in for recreate callback.
     * @param contentParameter  Also required when using command callback,
     *                          remind: display information context; execute: command execution; null: nothing.
     * @return                  Generated callback: Consumer<\ServerPlayer>.
     */
    public static Consumer<ServerPlayer> createCallback(CommandContext<CommandSourceStack> context, String timerId, String titleParameter, String contentParameter) {
        // Build callback according to titleParameter from command;
        // ?(You are advised to use API "createTemplateTimer"/"createInstanceTimer" to build advanced timer behavior).
        Consumer<ServerPlayer> callback;
        switch (titleParameter) {
            case "e", "execute":
                // Pass create only if selector used @r/a/e, @p will replace by player id.
                if (contentParameter.contains("@s")) {
                    MessagePublisher.sendFailureMessage(context, errorComponent(Error.GeneralError.invalidSelectorUsed));
                    return null;
                }
                else if (contentParameter.contains("@p")) {
                    if (context != null) {
                        MessagePublisher.sendFailureMessage(context, errorComponent(Error.GeneralError.selectorToNearestUsed));
                    }
                }

                // Then register command execution into source stack;
                // !(If NO online player exist, selector which used @r/a/p will lose their effect on command execution).
                callback = player -> {
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    if (server != null) {
                        String callbackCommand = contentParameter.startsWith("/")? contentParameter : ("/" + contentParameter);
                        if (callbackCommand.contains("@p") && player != null) {
                            List<ServerPlayer> players = server.getPlayerList().getPlayers();
                            ServerPlayer nearest = players.stream().min(Comparator.comparing(p -> p.distanceTo(player))).orElse(player);
                            callbackCommand = callbackCommand.replace("@p", nearest.getName().getString());
                        }
                        else if (callbackCommand.contains("@p") && player == null) {
                            callbackCommand = callbackCommand.replace("@p", "@a");
                        }

                        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), callbackCommand);
                    }
                };
                break;
            case "r", "remind":
                // Send message when time out:
                // Modified information.
                if (contentParameter != null) {
                    callback = player -> {
                        if (player != null) {
                            player.sendSystemMessage(Component.literal(contentParameter));
                        }
                        // Else broadcast to everyone.
                        else {
                            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                            if (server != null) {
                                server.getPlayerList().getPlayers().forEach(p -> {
                                    p.sendSystemMessage(Component.literal(contentParameter));
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
                                Component.translatable("commands.chx.timer_time_out")
                                    .append(Component.literal(" " + timerId))
                                    .withColor(0xFFD700)
                            );
                        }
                        // Else broadcast to everyone.
                        else {
                            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                            if (server != null) {
                                server.getPlayerList().getPlayers().forEach(p -> {
                                    p.sendSystemMessage(Component.translatable("commands.chx.timer_time_out")
                                        .append(Component.literal(" " + timerId))
                                        .withColor(0xFFD700));
                                });
                            }
                        }
                    };
                    MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_default_remind").withColor(0xFFD700));
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

    /// Register callback for commands/YAML's behaviors.
    public static void registerCallback(String callbackId, String callbackBehavior, String callbackContent) {
        if (callbackId != null && !callbackId.isEmpty()) {
            String fullCallbackId;
            if (callbackId.split(":").length == 3) {
                fullCallbackId = callbackId;
            }
            else {
                fullCallbackId = "attribute:core_hanxu-command:" + callbackId;
            }
            if (callbackContent != null && !callbackContent.isEmpty()) {
                switch (callbackBehavior) {
                    case "e", "execute":
                        BehaviorRegistry.register(
                            fullCallbackId, (player, parameters) -> {
                                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                                if (server != null && player != null) {
                                    String content = callbackContent;
                                    if (content.contains("@s") || content.contains("@p")) {
                                        content = content.replace("@s", player.getName().getString());
                                    }
                                    if (!content.startsWith("/")) {
                                        content = "/" + content;
                                    }
                                    server.getCommands().performPrefixedCommand(
                                        player.createCommandSourceStack(), content
                                    );
                                }
                            }
                        );
                        return;
                    case "r", "remind":
                        BehaviorRegistry.register(
                            fullCallbackId, (player, parameters) -> {
                                if (player != null) {
                                    player.sendSystemMessage(Component.literal(callbackContent));
                                }
                            }
                        );
                        return;
                    case "recovery":
                        String[] recoveryData = callbackContent.split(":", 3);
                        String attributeId = recoveryData[0];
                        float value = Float.parseFloat(recoveryData[1]);

                        AttributeHolder.ThresholdDirection thresholdDirection = getThresholdDirection(recoveryData);

                        BehaviorRegistry.register(
                            fullCallbackId, (player, parameters) -> {
                                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                                if (server != null) {
                                    for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                                        if (serverPlayer != null) {
                                            AttributeHolder.addValue(serverPlayer.getUUID(), attributeId, value, false, thresholdDirection);
                                        }
                                    }
                                }
                            }
                        );
                        return;
                    case "n", "null":
                        return;
                    default:
                        break;
                }
            }
            else {
                switch (callbackBehavior) {
                    case "e", "execute", "r", "remind":
                        BehaviorRegistry.register(
                            fullCallbackId, (player, parameters) -> {
                                if (player != null) {
                                    player.sendSystemMessage(
                                        Component.translatable("commands.chx.attribute_reached_threshold")
                                                .append(Component.literal(" " + callbackId))
                                                .withColor(0xFFD700)
                                    );
                                }
                            }
                        );
                        return;
                    case "n", "null":
                        return;
                    default:
                        break;
                }
            }

            // Mod will try to load callback if existed.
            if (callbackBehavior.equals("a") || callbackBehavior.equals("api")) {
                BehaviorRegistry.BehaviorCallback callback = BehaviorRegistry.getCallback(callbackId);
                if (callback != null) {
                    BehaviorRegistry.register(
                        callbackId, BehaviorRegistry.getCallback(callbackId)
                    );
                }
                else {
                    CoreHanXu.LOGGER.warn("[HX] Failed to register behavior callback: {}", callbackId);
                }
            }
        }
    }

    /**
     * Create weather for command/YAML definitions.
     */
    public static WeatherHolder.WeatherDefinition createWeatherDefinition(
            String id, WeatherHolder.WeatherType type, Random random,
            Map<String, Object> parameters) {
        int minimumDuration = 1200;
        int maximumDuration = 6000;
        Object durationObject = parameters.get("duration");
        if (durationObject instanceof Map<?, ?> durationMap) {
            minimumDuration = Cast.toInteger(durationMap.get("min"), minimumDuration);
            maximumDuration = Cast.toInteger(durationMap.get("max"), maximumDuration);
        }

        int minimumStillness = 600;
        int maximumStillness = 2400;
        Object stillnessObject = parameters.get("stillness");
        if (stillnessObject instanceof Map<?, ?> stillnessMap) {
            minimumStillness = Cast.toInteger(stillnessMap.get("min"), minimumStillness);
            maximumStillness = Cast.toInteger(stillnessMap.get("max"), maximumStillness);
        }

        switch (type) {
            case FOG -> {
                int color = Cast.toInteger(parameters, "color", WeatherHolder.DefaultColor.FOG);
                float minimumDistance = 4f;
                float maximumDistance = 64f;

                Object distanceObject = parameters.get("distance");
                if (distanceObject instanceof Map<?, ?> distanceMap) {
                    minimumDistance = Cast.toFloat(distanceMap.get("min"), minimumDistance);
                    maximumDistance = Cast.toFloat(distanceMap.get("max"), maximumDistance);
                }

                Fog fog = new Fog(id, random)
                        .color(color)
                        .distance(minimumDistance, maximumDistance)
                        .duration(minimumDuration, maximumDuration)
                        .stillness(minimumStillness, maximumStillness);

                Object heightOffsets = parameters.get("height_offsets");
                if (!(heightOffsets instanceof Map)) {
                    return fog;
                }

                for (Map.Entry<?, ?> entry : ((Map<?, ?>) heightOffsets).entrySet()) {
                    if (entry.getKey() instanceof Number && entry.getValue() instanceof Number) {
                        fog.heightOffset(
                                ((Number) entry.getKey()).floatValue(),
                                ((Number) entry.getValue()).floatValue()
                        );
                    }
                }

                return fog;
            }
            case COLORED_RAIN -> {
                int skyColor = Cast.toInteger(parameters, "sky_color", WeatherHolder.DefaultColor.RAINY_SKY);
                int rainColor = Cast.toInteger(parameters, "rain_color", WeatherHolder.DefaultColor.RAIN);
                int snowColor = Cast.toInteger(parameters, "snow_color", WeatherHolder.DefaultColor.SNOW);

                ColoredRain coloredRain = new ColoredRain(id, random)
                        .skyColor(skyColor)
                        .rainColor(rainColor)
                        .snowColor(snowColor)
                        .duration(minimumDuration, maximumDuration)
                        .stillness(minimumStillness, maximumStillness);

                coloredRain
                        .rainBiomes(getRainType(parameters, "rain_biomes"))
                        .snowBiomes(getRainType(parameters, "snow_biomes"))
                        .dryBiomes(getRainType(parameters, "dry_biomes"));

                return coloredRain;
            }
            case WIND -> {
                Wind wind = new Wind(id, random)
                        .duration(minimumDuration, maximumDuration)
                        .stillness(minimumStillness, maximumStillness);

                String windType = Cast.toString(parameters, "wind_type");

                if (windType == null) {
                    CoreHanXu.LOGGER.warn("[HX] Received null wind type for: {}", id);
                    return null;
                }

                windType = windType.toLowerCase();

                Object vectorObject = parameters.get("vector");

                if (!(vectorObject instanceof Map)) {
                    CoreHanXu.LOGGER.warn("[HX] Received null vector for: {}", id);
                    return null;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> vector = (Map<String, Object>) vectorObject;
                switch (windType) {
                    case "static" -> wind.staticVector(
                            Cast.toDouble(vector, "x", 0),
                            Cast.toDouble(vector, "y", 0),
                            Cast.toDouble(vector, "z", 0)
                    );
                    case "static_range" -> wind.staticVector(
                            Cast.toDouble(vector, "minimum_x", 0),
                            Cast.toDouble(vector, "maximum_x", 0),
                            Cast.toDouble(vector, "minimum_y", 0),
                            Cast.toDouble(vector, "maximum_y", 0),
                            Cast.toDouble(vector, "minimum_z", 0),
                            Cast.toDouble(vector, "maximum_z", 0)
                    );
                    case "dynamic" -> wind.dynamicVector(
                            Cast.toDouble(vector, "minimum_x", 0),
                            Cast.toDouble(vector, "maximum_x", 0),
                            Cast.toDouble(vector, "minimum_y", 0),
                            Cast.toDouble(vector, "maximum_y", 0),
                            Cast.toDouble(vector, "minimum_z", 0),
                            Cast.toDouble(vector, "maximum_z", 0)
                    );
                    default -> CoreHanXu.LOGGER.warn("[HX] Received unknown wind type for: {}", id);
                }

                float minimumSpeedReduction = Cast.toFloat(parameters, "minimum_speed_reduction", 0);
                float maximumSpeedReduction = Cast.toFloat(parameters, "maximum_speed_reduction", 0);
                wind.speedReduction(minimumSpeedReduction, maximumSpeedReduction);

                float minimumDriftDistance = Cast.toFloat(parameters, "minimum_drift_distance", 0);
                float maximumDriftDistance = Cast.toFloat(parameters, "maximum_drift_distance", 0);
                wind.driftDistance(minimumDriftDistance, maximumDriftDistance);

                boolean affectRain = Cast.toBoolean(parameters, "affect_rain", true);
                float dynamicSpeedChange = Cast.toFloat(parameters, "dynamic_speed_change", 0);
                wind.affectRain(affectRain).dynamicSpeedChange(dynamicSpeedChange);

                return wind;
            }
            // Wait for more definitions.
            case null, default -> {
                return null;
            }
        }
    }

    private static AttributeHolder.ThresholdDirection getThresholdDirection(String[] recoveryData) {
        String direction = recoveryData[2];

        AttributeHolder.ThresholdDirection thresholdDirection;
        switch (direction) {
            case "up" -> thresholdDirection = AttributeHolder.ThresholdDirection.UP;
            case "down" -> thresholdDirection = AttributeHolder.ThresholdDirection.DOWN;
            case "flex" -> thresholdDirection = AttributeHolder.ThresholdDirection.FLEX;
            default -> thresholdDirection = AttributeHolder.ThresholdDirection.POINT;
        }
        return thresholdDirection;
    }

    private static ColoredRain.RainType getRainType(Map<String, Object> parameters, String key) {
        if (parameters.isEmpty() || !parameters.containsKey(key)) {
            return ColoredRain.RainType.DEFAULT;
        }

        if (!(parameters.get(key) instanceof String)) {
            return ColoredRain.RainType.DEFAULT;
        }

        String rainType = parameters.get(key).toString().toLowerCase();

        return switch (rainType) {
            case "rain" -> ColoredRain.RainType.RAIN;
            case "snow" -> ColoredRain.RainType.SNOW;
            case "dry" -> ColoredRain.RainType.DRY;
            default -> ColoredRain.RainType.DEFAULT;
        };
    }
}
