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
        unableToRestart,
        unableToDeleteInstance,
    }

    public enum SceneError {
        notFound,
        playFailed,
        failedToDelete,
        alreadyExist,
        sameNameFound,
        failedToSave,
    }

    public enum AttributeError {
        tryToModifyApiTarget,
        notFound,
        sameNameFound,
        failedToDelete,
        noThreshold,
        noSpecificThreshold,
        noZero,
        noIdFieldProvidedByNonPlayer,
        noRecovery,
        tryToOverrideApiRecovery,
        tryToRegisterUnExistApiRecovery,
    }

    public enum VariableError {
        alreadyExist,
        invalidType,
        emptyVariable,
        notExist,
        selfFieldInScoreIf,
    }

    public enum GeneralError {
        licenseAlreadyAgreed,
        uneditablePlayerPermission,
        exceedMaximumPermissionLevel,
        notPlayer,
        notYetAgreed,
        invalidUnitArgument,
        invalidSelectorUsed,
        selectorToNearestUsed,
        targetNotExist,
        undefinedOperationCategory,
        undefinedOperationId,
        undefinedSavePath,
        mainHandItemNotTarget,
        noContentFound,
        missingIdField,
        uncompletedContent,
        invalidMeFieldUsed,
        invalidFieldForName,
        unexpected,
    }

    public enum CodeError {
        yamlFileNotFound,
        unexceptedTarget,
        unavailableTargetPath,
        mismatchFileElement,
    }

    public static Component returnTimerError(TimerError timerError) {
        return switch (timerError) {
            case alreadyExist -> Component.translatable("commands.chx.timer_already_exist");
            case notExist -> Component.translatable("commands.chx.timer_not_exist");
            case notExistOrAlreadyInstantiated -> Component.translatable("commands.chx.timer_not_exist_or_already_instantiated");
            case unableToStart -> Component.translatable("commands.chx.timer_unable_to_start");
            case unableToStop -> Component.translatable("commands.chx.timer_unable_to_stop");
            case unableToReset -> Component.translatable("commands.chx.timer_unable_to_reset");
            case unableToRestart -> Component.translatable("commands.chx.timer_unable_to_restart");
            case unableToDeleteInstance -> Component.translatable("commands.chx.timer_unable_to_delete_instance");
            default -> Component.translatable("commands.core_hanxu.undefined_error_type");
        };
    }

    public static Component returnSceneError(SceneError sceneError) {
        return switch (sceneError) {
            case notFound -> Component.translatable("commands.chx.scene_not_found");
            case playFailed -> Component.translatable("commands.chx.scene_play_failed");
            case failedToDelete -> Component.translatable("commands.chx.scene_failed_to_delete");
            case alreadyExist -> Component.translatable("commands.chx.scene_already_exist");
            case sameNameFound -> Component.translatable("commands.chx.scene_same_name_found");
            case failedToSave ->  Component.translatable("commands.chx.scene_failed_to_save");
            default -> Component.translatable("commands.core_hanxu.undefined_error_type");
        };
    }

    public static Component returnAttributeError(AttributeError attributeError) {
        return switch (attributeError) {
            case tryToModifyApiTarget -> Component.translatable("commands.chx.attribute_try_to_modify_api_target");
            case notFound -> Component.translatable("commands.chx.attribute_not_found");
            case sameNameFound -> Component.translatable("commands.chx.attribute_same_name_found");
            case failedToDelete -> Component.translatable("commands.chx.attribute_failed_to_delete");
            case noThreshold -> Component.translatable("commands.chx.attribute_no_threshold");
            case noSpecificThreshold -> Component.translatable("commands.chx.attribute_no_specific_threshold");
            case noZero -> Component.translatable("commands.chx.attribute_no_zero");
            case noIdFieldProvidedByNonPlayer -> Component.translatable("commands.chx.attribute_no_id_with_non_player");
            case noRecovery -> Component.translatable("commands.chx.attribute_no_recovery");
            case tryToOverrideApiRecovery -> Component.translatable("commands.chx.attribute_try_to_override_api_recovery");
            case tryToRegisterUnExistApiRecovery -> Component.translatable("commands.chx.attribute_try_to_register_un_exist_api_recovery");
            default -> Component.translatable("commands.core_hanxu.undefined_error_type");
        };
    }

    public static Component returnVariableError(VariableError variableError) {
        return switch (variableError) {
            case alreadyExist -> Component.translatable("commands.chx.variable_already_exist");
            case invalidType -> Component.translatable("commands.chx.variable_invalid_type");
            case emptyVariable -> Component.translatable("commands.chx.variable_empty_variable");
            case notExist -> Component.translatable("commands.chx.variable_not_exist");
            case selfFieldInScoreIf -> Component.translatable("commands.chx.variable_self_field_in_score_if");
            default -> Component.translatable("commands.core_hanxu.undefined_error_type");
        };
    }

    public static Component returnGeneralError(GeneralError generalError) {
        return switch (generalError) {
            case licenseAlreadyAgreed -> Component.translatable("commands.core_hanxu.license_already_agreed");
            case uneditablePlayerPermission -> Component.translatable("commands.core_hanxu.uneditable_player_permission");
            case exceedMaximumPermissionLevel -> Component.translatable("commands.core_hanxu.exceed_maximum_permission_level");
            case notPlayer -> Component.translatable("commands.core_hanxu.not_player");
            case notYetAgreed -> Component.translatable("commands.core_hanxu.not_yet_agreed");
            case invalidUnitArgument -> Component.translatable("commands.core_hanxu.invalid_unit_argument");
            case invalidSelectorUsed -> Component.translatable("commands.core_hanxu.invalid_selector_used");
            case selectorToNearestUsed -> Component.translatable("commands.core_hanxu.selector_to_nearest_used");
            case targetNotExist -> Component.translatable("commands.core_hanxu.target_not_exist");
            case undefinedOperationCategory -> Component.translatable("commands.core_hanxu.undefined_operation_category");
            case undefinedOperationId -> Component.translatable("commands.core_hanxu.undefined_operation_id");
            case undefinedSavePath -> Component.translatable("commands.core_hanxu.undefined_save_path");
            case mainHandItemNotTarget -> Component.translatable("commands.core_hanxu.main_hand_item_not_target");
            case noContentFound ->  Component.translatable("commands.core_hanxu.no_content_found");
            case missingIdField -> Component.translatable("commands.core_hanxu.missing_id_field");
            case uncompletedContent -> Component.translatable("commands.core_hanxu.uncompleted_content");
            case invalidMeFieldUsed -> Component.translatable("commands.core_hanxu.invalid_me_field_used");
            case invalidFieldForName -> Component.translatable("commands.core_hanxu.invalid_field_for_name");
            case unexpected -> Component.translatable("commands.core_hanxu.unexpected");
            default -> Component.translatable("commands.core_hanxu.undefined_error_type");
        };
    }

    public static String returnCodeError(CodeError codeError) {
        return switch (codeError) {
            case yamlFileNotFound -> "[HX] Yaml file not found: ";
            case unexceptedTarget -> "[HX] Unexcepted target: ";
            case unavailableTargetPath ->  "[HX] Unavailable target path.";
            case mismatchFileElement ->  "[HX] Mismatch to the file element: ";
            default -> "[HX] Undefined error type: " + codeError.toString();
        };
    }
}
