package core.yaoquan.hanxu.api.define;

import net.minecraft.network.chat.Component;

public class Error {
    public enum TimerError {
        alreadyExist,
        notExist,
        notExistOrAlreadyInstantiated,
        unableToStart,
        unableToStop,
        unableToReset,
        unableToDeleteInstance,
    }

    public enum GeneralError {
        licenseAlreadyAgreed,
        notPlayer,
        notYetAgreed,
        invalidUnitArgument,
        invalidSelectorUsed,
        selectorToNearestUsed,
        targetNotExist,
        undefinedOperationCategory,
        undefinedOperationId,
    }

    public static Component returnTimerError(TimerError timerError) {
        return switch (timerError) {
            case alreadyExist -> Component.translatable("commands.chx-a.timer_already_exist");
            case notExist -> Component.translatable("commands.chx-a.timer_not_exist");
            case notExistOrAlreadyInstantiated -> Component.translatable("commands.chx-a.timer_not_exist_or_already_instantiated");
            case unableToStart -> Component.translatable("commands.chx-a.timer_unable_to_start");
            case unableToStop -> Component.translatable("commands.chx-a.timer_unable_to_stop");
            case unableToReset -> Component.translatable("commands.chx-a.timer_unable_to_reset");
            case unableToDeleteInstance -> Component.translatable("commands.chx-a.timer_unable_to_delete_instance");
            default -> Component.translatable("commands.core_hanxu.undefined_error_type");
        };
    }

    public static Component returnGeneralError(GeneralError generalError) {
        return switch (generalError) {
            case licenseAlreadyAgreed -> Component.translatable("commands.core_hanxu.license_already_agreed");
            case notPlayer -> Component.translatable("commands.core_hanxu.not_player");
            case notYetAgreed -> Component.translatable("commands.core_hanxu.not_yet_agreed");
            case invalidUnitArgument -> Component.translatable("commands.core_hanxu.invalid_unit_argument");
            case invalidSelectorUsed -> Component.translatable("commands.core_hanxu.invalid_selector_used");
            case selectorToNearestUsed -> Component.translatable("commands.core_hanxu.selector_to_nearest_used");
            case targetNotExist -> Component.translatable("commands.core_hanxu.target_not_exist");
            case undefinedOperationCategory -> Component.translatable("commands.core_hanxu.undefined_operation_category");
            case undefinedOperationId -> Component.translatable("commands.core_hanxu.undefined_operation_id");
            default -> Component.translatable("commands.core_hanxu.undefined_error_type");
        };
    }
}
