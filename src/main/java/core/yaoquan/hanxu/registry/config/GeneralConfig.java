package core.yaoquan.hanxu.registry.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class GeneralConfig {
    // Field of mod config:
    private static final ModConfigSpec.Builder CONFIG = new ModConfigSpec.Builder();

    private static final String comment1 = " Define the permission level for new player when server start.";
    // Generate the list of config:
    public static final ModConfigSpec.IntValue autoAuthorizePermissionLevel = CONFIG
            .comment(comment1)
            .translation("config.core_hanxu.auto_authorize_permission_level")
            .defineInRange("auto_level", 2, 0, 10);

    private static final String comment2 = " Determine the allowance on player edit permission level in game.";
    public static final ModConfigSpec.BooleanValue editablePlayerPermission = CONFIG
            .comment(comment2)
            .translation("config.core_hanxu.editable_player_permission")
            .define("player_permission_editable", true);

    private static final String comment3 = " Determine does *Loot Deployer* play sound after deploy.";
    public static final ModConfigSpec.BooleanValue enableDeployerSound = CONFIG
            .comment(comment3)
            .translation("config.core_hanxu.enable_deployer_sound")
            .define("enabled_deploy_sound", true);

    private static final String comment4 = " Define the percentage of fog gradient between active phase:";
    private static final String comment4_1 = " In active phase: [ [transition: ratio] [static: min. distance (1-ratio*2)] [transition: ratio] ]";
    public static final ModConfigSpec.DoubleValue fogTransitionRatio = CONFIG
            .comment(comment4)
            .comment(comment4_1)
            .translation("config.core_hanxu.fog_transition_ratio")
            .defineInRange("fog_transition_ratio", 0.1, 0.01, 0.5);

    private static final String comment5 = " Define the percentage of colored rain gradient between active phase:";
    private static final String comment5_1 = " Instance change when set to 0.";
    private static final String comment5_2 = " Otherwise, in active phase [ [transition: ratio] [static: defined color (1-ratio*2)] [transition: ratio] ]";
    public static final ModConfigSpec.DoubleValue coloredRainTransitionRatio = CONFIG
            .comment(comment5)
            .comment(comment5_1)
            .comment(comment5_2)
            .translation("config.core_hanxu.colored_rain_transition_ratio")
            .defineInRange("colored_rain_transition_ratio", 0.1, 0, 0.5);

    private static final String comment6 = " Define the brightness factor of environment fog at activated colored rain:";
    private static final String comment6_1 = " In brightness: [1.0 sky brightness] | [x environment brightness]";
    public static final ModConfigSpec.DoubleValue coloredRainEnvironmentFogBrightness = CONFIG
            .comment(comment6)
            .comment(comment6_1)
            .translation("config.core_hanxu.colored_rain_environment_fog_brightness")
            .defineInRange("colored_rain_environment_fog_brightness", 0.85, 0.1, 2.0);

    public static final ModConfigSpec SPEC_GENERAL = CONFIG.build();
}