package core.yaoquan.hanxu.api.define;

import net.minecraft.network.chat.Component;

public final class Error {
    public enum TimerError {
        alreadyExist,
        notExist,
        notExistOrAlreadyInstantiated,
        masterNotExist,
        timerTimedOut,
    }

    public enum SceneError {
        notFound,
        playFailed,
        alreadyExist,
        sameNameFound,
        failedToSave,
    }

    public enum AttributeError {
        tryToModifyApiTarget,
        notFound,
        yamlNotFound,
        sameNameFound,
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
        invalidCasting,
        invalidScoreCasting,
        emptyVariable,
        notExist,
        selfFieldInScoreIf,
        duplicated,
        mismatchType,
    }

    public enum LootError {
        emptyTable,
        tableNotExist,
        alreadyExist,
        notBlock,
        notContainer,
        yamlNotFound,
        emptyList,
        notEnoughSpace,
        failedToSave,
        sameNameFound,
    }

    public enum WeatherError {
        emptyWeather,
        notFound,
        yamlNotFound,
        inUse,
        alreadyActivated,
        alreadyExist,
        notInitialized,
        failedToSave,
        tryToModifyApiTarget,
        sameNameFound,
        noUnclaims,
    }

    public enum GeneralError {
        serverOffline,
        licenseAlreadyAgreed,
        uneditablePlayerPermission,
        exceedMaximumPermissionLevel,
        notPlayer,
        playerOffline,
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
        unknownScoreObjective,
        notContainer,
        unknownDimension,
    }

    public enum CodeError {
        yamlFileNotFound,
        jsonFileNotFound,
        unexceptedTarget,
        unavailableTargetPath,
        mismatchFileElement,
        missingNecessaryField,
    }

    public static Component errorComponent(TimerError timerError) {
        return switch (timerError) {
            case alreadyExist -> Component.translatable("commands.chx.timer_already_exist");
            case notExist -> Component.translatable("commands.chx.timer_not_exist");
            case notExistOrAlreadyInstantiated -> Component.translatable("commands.chx.timer_not_exist_or_already_instantiated");
            case masterNotExist -> Component.translatable("commands.chx.timer_master_not_exist");
            case timerTimedOut -> Component.translatable("commands.chx.timer_timed_out");
            default -> Component.translatable("commands.core_hanxu.undefined_error_type");
        };
    }

    public static Component errorComponent(SceneError sceneError) {
        return switch (sceneError) {
            case notFound -> Component.translatable("commands.chx.scene_not_found");
            case playFailed -> Component.translatable("commands.chx.scene_play_failed");
            case alreadyExist -> Component.translatable("commands.chx.scene_already_exist");
            case sameNameFound -> Component.translatable("commands.chx.scene_same_name_found");
            case failedToSave ->  Component.translatable("commands.chx.scene_failed_to_save");
            default -> Component.translatable("commands.core_hanxu.undefined_error_type");
        };
    }

    public static Component errorComponent(AttributeError attributeError) {
        return switch (attributeError) {
            case tryToModifyApiTarget -> Component.translatable("commands.chx.attribute_try_to_modify_api_target");
            case notFound -> Component.translatable("commands.chx.attribute_not_found");
            case yamlNotFound -> Component.translatable("commands.chx.attribute_yaml_not_found");
            case sameNameFound -> Component.translatable("commands.chx.attribute_same_name_found");
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

    public static Component errorComponent(VariableError variableError) {
        return switch (variableError) {
            case alreadyExist -> Component.translatable("commands.chx.variable_already_exist");
            case invalidType -> Component.translatable("commands.chx.variable_invalid_type");
            case invalidCasting -> Component.translatable("commands.chx.variable_invalid_casting");
            case invalidScoreCasting -> Component.translatable("commands.chx.variable_invalid_score_casting");
            case emptyVariable -> Component.translatable("commands.chx.variable_empty_variable");
            case notExist -> Component.translatable("commands.chx.variable_not_exist");
            case selfFieldInScoreIf -> Component.translatable("commands.chx.variable_self_field_in_score_if");
            case duplicated -> Component.translatable("commands.chx.variable_duplicated");
            case mismatchType -> Component.translatable("commands.chx.variable_mismatch_type");
            default -> Component.translatable("commands.core_hanxu.undefined_error_type");
        };
    }

    public static Component errorComponent(LootError lootError) {
        return switch (lootError) {
            case emptyTable -> Component.translatable("commands.chx.loot_empty_table");
            case tableNotExist -> Component.translatable("commands.chx.loot_table_not_exist");
            case alreadyExist -> Component.translatable("commands.chx.loot_already_exist");
            case notBlock -> Component.translatable("commands.chx.loot_not_block");
            case notContainer -> Component.translatable("commands.chx.loot_not_container");
            case yamlNotFound -> Component.translatable("commands.chx.loot_yaml_not_found");
            case emptyList -> Component.translatable("commands.chx.loot_empty_list");
            case notEnoughSpace -> Component.translatable("commands.chx.loot_not_enough_space");
            case failedToSave -> Component.translatable("commands.chx.loot_failed_to_save");
            case sameNameFound -> Component.translatable("commands.chx.loot_same_name_found");
            default -> Component.translatable("commands.core_hanxu.undefined_error_type");
        };
    }

    public static Component errorComponent(WeatherError weatherError) {
        return switch (weatherError) {
            case emptyWeather -> Component.translatable("commands.chx.weather_empty_weather");
            case notFound -> Component.translatable("commands.chx.weather_not_found");
            case yamlNotFound -> Component.translatable("commands.chx.weather_yaml_not_found");
            case inUse -> Component.translatable("commands.chx.weather_in_use");
            case alreadyActivated -> Component.translatable("commands.chx.weather_already_activated");
            case alreadyExist -> Component.translatable("commands.chx.weather_already_exist");
            case notInitialized -> Component.translatable("commands.chx.weather_not_initialized");
            case failedToSave -> Component.translatable("commands.chx.weather_failed_to_save");
            case tryToModifyApiTarget -> Component.translatable("commands.chx.weather_try_to_modify_api_target");
            case sameNameFound -> Component.translatable("commands.chx.weather_same_name_found");
            case noUnclaims -> Component.translatable("commands.chx.weather_no_unclaims");
            default -> Component.translatable("commands.core_hanxu.undefined_error_type");
        };
    }

    public static Component errorComponent(GeneralError generalError) {
        return switch (generalError) {
            case serverOffline -> Component.translatable("commands.core_hanxu.server_offline");
            case licenseAlreadyAgreed -> Component.translatable("commands.core_hanxu.license_already_agreed");
            case uneditablePlayerPermission -> Component.translatable("commands.core_hanxu.uneditable_player_permission");
            case exceedMaximumPermissionLevel -> Component.translatable("commands.core_hanxu.exceed_maximum_permission_level");
            case notPlayer -> Component.translatable("commands.core_hanxu.not_player");
            case playerOffline -> Component.translatable("commands.core_hanxu.player_offline");
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
            case unknownScoreObjective -> Component.translatable("commands.core_hanxu.unknown_score_objective");
            case notContainer -> Component.translatable("commands.core_hanxu.not_container");
            case unknownDimension -> Component.translatable("commands.core_hanxu.unknown_dimension");
            default -> Component.translatable("commands.core_hanxu.undefined_error_type");
        };
    }

    public static String errorString(CodeError codeError) {
        return switch (codeError) {
            case yamlFileNotFound -> "[HX] Yaml file not found: ";
            case jsonFileNotFound -> "[HX] Json file not found: ";
            case unexceptedTarget -> "[HX] Unexcepted target: ";
            case unavailableTargetPath ->  "[HX] Unavailable target path.";
            case mismatchFileElement ->  "[HX] Mismatch to the file element: ";
            case missingNecessaryField -> "[HX] Missing necessary field: ";
            default -> "[HX] Undefined error type: " + codeError;
        };
    }
}
