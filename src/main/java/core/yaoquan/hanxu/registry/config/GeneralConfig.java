package core.yaoquan.hanxu.registry.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public class GeneralConfig {
    // Field of mod config:
    private static final ModConfigSpec.Builder CONFIG = new ModConfigSpec.Builder();

    private static final String GENERAL_SET_COMMENT1 = " Define the permission level for new player when server start.";
    private static final String GENERAL_SET_COMMENT2 = " Determine the allowance on player edit permission level in game.";
    private static final String GENERAL_SET_COMMENT3 = " Determine does *Loot Deployer* play sound after deploy.";

    // Generate the list of config:
    public static final ModConfigSpec.IntValue SET_AUTO_AUTHORIZED_PERMISSION_LEVEL = CONFIG
            .comment(GENERAL_SET_COMMENT1)
            .defineInRange("auto_level", 2, 0, 10);

    public static final ModConfigSpec.BooleanValue SET_PLAYER_PERMISSION_EDITABLE = CONFIG
            .comment(GENERAL_SET_COMMENT2)
            .define("player_permission_editable", true);

    public static final ModConfigSpec.BooleanValue SET_ENABLED_DEPLOY_SOUND = CONFIG
            .comment(GENERAL_SET_COMMENT3)
            .define("enabled_deploy_sound", true);

    public static final ModConfigSpec SPEC_GENERAL = CONFIG.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}