package core.yaoquan.hanxu.registry.command;

import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.TimeHolder;
import core.yaoquan.hanxu.api.define.Error;
import core.yaoquan.hanxu.util.MessagePublisher;
import core.yaoquan.hanxu.util.Resolver;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

import static core.yaoquan.hanxu.api.define.Error.returnGeneralError;
import static core.yaoquan.hanxu.api.define.Error.returnTimerError;

public class CommandDisplay {
    static int displayCommandTimerRead(CommandContext<CommandSourceStack> context,
                                       String timerId, String masterString, String timeUnit,
                                       String infoCategory, String timerCategory) {
        if (timerCategory.equals("template")) {
            switch (infoCategory) {
                case "remaining_time":
                    int remainingTime = TimeHolder.getRemainingTimeFromTemplate(timerId, timeUnit);
                    if (remainingTime != -1) {
                        MessagePublisher.sendSystemMessage(context,
                                Component.translatable("commands.chx.timer_read_remaining_time")
                                        .append(Component.literal(" (" + timerId + "): " + remainingTime + " " + timeUnit))
                                        .withColor(0xFFD700)
                        );
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, returnTimerError(Error.TimerError.notExist));
                        return 0;
                    }
                    break;
                case "initial_time":
                    int initialTime = TimeHolder.getInitialTimeFromTemplate(timerId, timeUnit);
                    if (initialTime != -1) {
                        MessagePublisher.sendSystemMessage(context,
                                Component.translatable("commands.chx.timer_read_initial_time")
                                        .append(Component.literal(" (" + timerId + "): " + initialTime + " " + timeUnit))
                                        .withColor(0xFFD700)
                        );
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, returnTimerError(Error.TimerError.notExist));
                        return 0;
                    }
                    break;
                case "state":
                    boolean isCounting = TimeHolder.isTemplateTimerCounting(timerId);

                    if (TimeHolder.getRemainingTimeFromTemplate(timerId, timeUnit) != -1) {
                        MessagePublisher.sendSystemMessage(context,
                                Component.translatable("commands.chx.timer_state")
                                        .append(Component.literal(" (" + timerId + "): " + (isCounting? "Counting" : "Stopping")))
                                        .withColor(0xFFD700)
                        );
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, returnTimerError(Error.TimerError.notExist));
                        return 0;
                    }
                    break;
                case "end_behavior":
                    String endBehavior = TimeHolder.getTemplateEndBehavior(timerId);
                    String behaviorContent = TimeHolder.getTemplateBehaviorContent(timerId);

                    if (TimeHolder.getRemainingTimeFromTemplate(timerId, timeUnit) != -1) {
                        if (endBehavior == null) {
                            endBehavior = "null";
                        }

                        MessagePublisher.sendSystemMessage(context,
                                Component.literal("(" + timerId + ") <<- ")
                                        .append(endBehavior)
                                        .append(Component.literal(behaviorContent == null? "" : (" : " + behaviorContent)))
                                        .withColor(0xFFD700)
                        );
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, returnTimerError(Error.TimerError.notExist));
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
                    int remainingTime = TimeHolder.getRemainingTimeFromInstance(masterId, timerId, timeUnit);
                    if (remainingTime != -1) {
                        MessagePublisher.sendSystemMessage(context,
                                Component.translatable("commands.chx.timer_read_remaining_time")
                                        .append(Component.literal(" (" + timerId + " -> " + masterString + "): " + remainingTime + " " + timeUnit))
                                        .withColor(0xFFD700)
                        );
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, returnTimerError(Error.TimerError.notExist));
                        return 0;
                    }
                    break;
                case "initial_time":
                    int initialTime = TimeHolder.getInitialTimeFromInstance(masterId, timerId, timeUnit);
                    if (initialTime != -1) {
                        MessagePublisher.sendSystemMessage(context,
                                Component.translatable("commands.chx.timer_read_initial_time")
                                        .append(Component.literal(" (" + timerId + "->" + masterString + "): " + initialTime + " " + timeUnit))
                                        .withColor(0xFFD700)
                        );
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, returnTimerError(Error.TimerError.notExist));
                        return 0;
                    }
                    break;
                case "state":
                    boolean isCounting = TimeHolder.isInstanceTimerCounting(masterId, timerId);
                    if (TimeHolder.getRemainingTimeFromInstance(masterId, timerId, timeUnit) != -1) {
                        MessagePublisher.sendSystemMessage(context,
                                Component.translatable("commands.chx.timer_state")
                                        .append(Component.literal(" (" + timerId + "): " + (isCounting? "Counting" : "Stopping")))
                                        .withColor(0xFFD700)
                        );
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, returnTimerError(Error.TimerError.notExist));
                        return 0;
                    }
                    break;
                case "end_behavior":
                    String endBehavior = TimeHolder.getInstanceEndBehavior(masterId, timerId);
                    String behaviorContent = TimeHolder.getInstanceBehaviorContent(masterId, timerId);

                    if (TimeHolder.getRemainingTimeFromInstance(masterId, timerId, timeUnit) != -1) {
                        if (endBehavior == null) {
                            endBehavior = "null";
                        }

                        MessagePublisher.sendSystemMessage(context,
                                Component.literal("(" + timerId + " -> " + masterString + ") <<- ")
                                        .append(endBehavior)
                                        .append(Component.literal(behaviorContent == null? "" : (" : " + behaviorContent)))
                                        .withColor(0xFFD700)
                        );
                    }
                    else {
                        MessagePublisher.sendFailureMessage(context, returnTimerError(Error.TimerError.notExist));
                        return 0;
                    }
                    break;
                default:
                    return 0;
            }
            return 1;
        }
        else {
            MessagePublisher.sendFailureMessage(context, returnGeneralError(Error.GeneralError.undefinedOperationCategory));
            return 0;
        }
    }

    static int displayTimerIdList(CommandContext<CommandSourceStack> context, String[] idList) {
        if (idList.length == 0) {
            MessagePublisher.sendFailureMessage(context, Component.translatable("commands.chx.fixed.empty"));
            return 0;
        }

        MessagePublisher.sendSystemMessage(context,
                Component.translatable("commands.chx.timer_list_title").withColor(0xFFD700)
        );
        for (String id : idList) {
            MessagePublisher.sendSystemMessage(context, Component.literal(id).withColor(0xFFFACD));
        }

        return 1;
    }

    static void displayTimerCreateMessage(CommandContext<CommandSourceStack> context, String timerId, int timeAmount, String timeUnit, String endBehavior, String behaviorContent) {
        // Output message.
        MessagePublisher.sendSystemMessage(context,
                Component.translatable("commands.chx.timer_created")
                        .append(Component.literal(" " + timerId + " -> " + timeAmount + " " + timeUnit))
                        .withColor(0x66FF66)
        );
        switch (endBehavior) {
            case "e", "execute":
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_with_execute_behavior")
                                .append(Component.literal(": " + behaviorContent))
                                .withColor(0x66FF66)
                );
                break;
            case "r", "remind":
                MessagePublisher.sendSystemMessage(context,
                        Component.translatable("commands.chx.timer_with_remind_behavior")
                                .append(Component.literal(": " + behaviorContent))
                                .withColor(0x66FF66)
                );
                break;
            default:
                break;
        }
    }

    static void displaySceneIdList(CommandContext<CommandSourceStack> context, List<Component> displayList) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_list_title").withColor(0xFFD700));
        for (Component line : displayList) {
            MessagePublisher.sendSystemMessage(context, line);
        }
    }

    static int displayAttributeIdList(CommandContext<CommandSourceStack> context, String[] attributeArrayList, boolean fromApi) {
        MessagePublisher.sendSystemMessage(
            context,
            fromApi?
                Component.translatable("commands.chx.attribute_api_list_title").withColor(0xFFD700) :
                Component.translatable("commands.chx.attribute_yaml_list_title").withColor(0xFFD700)
            );

        if (attributeArrayList.length == 0) {
            MessagePublisher.sendFailureMessage(
                context, Component.translatable("commands.chx.fixed.empty")
            );
            return 0;
        }

        for (String attribute : attributeArrayList) {
            MessagePublisher.sendSystemMessage(context, Component.literal(attribute).withColor(0xFFFACD));
        }

        return 1;
    }

    static void displayAttributeCreateMessage(CommandContext<CommandSourceStack> context, String attributeId, float maximum, float defaultValue, boolean registered) {
        if (registered) {
            MessagePublisher.sendSystemMessage(
                context,
                Component.translatable("commands.chx.attribute_created")
                        .append(Component.literal(" " + attributeId + " -> " + maximum + " _ " + defaultValue))
                        .withColor(0x66FF66)
            );
        }
        else {
            MessagePublisher.sendFailureMessage(
                    context,
                    Component.translatable("commands.chx.attribute_not_created")
                            .withColor(0x66FF66)
            );
        }
    }
}
