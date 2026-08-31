package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.AttributeHolder;
import core.yaoquan.hanxu.api.TimeHolder;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.api.define.Error;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.MessagePublisher;
import core.yaoquan.hanxu.util.NullableValue;
import core.yaoquan.hanxu.util.Resolver;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class ExecuteDisplay {
    public static int executeTimer_Instance_Display(CommandContext<CommandSourceStack> context, boolean state) {
        String masterString = StringArgumentType.getString(context, "master_id");
        String timerId = StringArgumentType.getString(context, "timer_id");

        UUID masterId = Resolver.resolveTargetUUID(context, masterString);
        if (masterId == null) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.targetNotExist));
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

    public static int executeAttribute_Display(CommandContext<CommandSourceStack> context, boolean state) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.notPlayer));
            return 0;
        }

        String attributeId = StringArgumentType.getString(context, "attribute_id");
        String masterId = StringArgumentType.getString(context, "master_id");

        if (!AttributeHolder.doesAttributeExist(attributeId)) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.AttributeError.notFound));
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

    public static int executeWeather_Display(CommandContext<CommandSourceStack> context, boolean state) {
        String weatherId = StringArgumentType.getString(context, "weather_id");

        NullableValue<ServerLevel> nullableLevel = CommandUtils.findServerLevel(context);
        if (nullableLevel.isNull()) {
            return 0;
        }
        ServerLevel level = nullableLevel.get();

        ServerPlayer player = context.getSource().getPlayer();

        if (player == null) {
            MessagePublisher.sendFailureMessage(context, Error.errorComponent(Error.GeneralError.notPlayer));
            return 0;
        }

        WeatherHolder.displayToInfoPage(player, level, weatherId, state);
        MessagePublisher.sendSystemMessage(context,
                Component.literal("[HX] " + level + " -> " + weatherId + " ")
                        .append(Component.translatable("commands.core_hanxu.has_changed_to"))
                        .append(Component.literal(" " + state))
                        .withColor(General.Color.SUCCESS)
        );

        return 1;
    }
}
