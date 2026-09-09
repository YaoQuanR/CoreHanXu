package core.yaoquan.hanxu.registry.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public class GeneralConfig {
    // Field of mod config:
    private static final ModConfigSpec.Builder CONFIG = new ModConfigSpec.Builder();

    private static final String comment1 = " Define the permission level for new player when server start.";
    // Generate the list of config:
    public static final ModConfigSpec.IntValue setAutoAuthorizePermissionLevel = CONFIG
            .comment(comment1)
            .translation("config.core_hanxu.set_auto_authorize_permission_level")
            .defineInRange("auto_level", 2, 0, 10);

    private static final String comment2 = " Determine the allowance on player edit permission level in game.";
    public static final ModConfigSpec.BooleanValue setEditablePlayerPermission = CONFIG
            .comment(comment2)
            .translation("config.core_hanxu.set_editable_player_permission")
            .define("player_permission_editable", true);

    private static final String comment3 = " Determine does *Loot Deployer* play sound after deploy.";
    public static final ModConfigSpec.BooleanValue setEnableDeployerSound = CONFIG
            .comment(comment3)
            .translation("config.core_hanxu.set_enable_deployer_sound")
            .define("enabled_deploy_sound", true);

    private static final String comment4 = " Define the percentage of fog gradient between active phase:";
    private static final String comment4_1 = " In active phase: [ [transition: ratio/2] [static: min. distance] [transition: ratio/2] ]";
    public static final ModConfigSpec.DoubleValue setFogTransitionRatio = CONFIG
            .comment(comment4)
            .comment(comment4_1)
            .translation("config.core_hanxu.set_fog_transition_ratio")
            .defineInRange("fog_transition_ratio", 0.1, 0.01, 0.5);

    private static final String comment5 = " Define the percentage of colored rain gradient between active phase:";
    public static final ModConfigSpec.DoubleValue setColoredRainTransitionRatio = CONFIG
            .comment(comment5)
            .comment(comment4_1)
            .translation("config.core_hanxu.set_colored_rain_transition_ratio")
            .defineInRange("colored_rain_transition_ratio", 0.1, 0.01, 0.5);

    public static final ModConfigSpec SPEC_GENERAL = CONFIG.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}