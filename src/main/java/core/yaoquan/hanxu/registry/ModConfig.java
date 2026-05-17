package core.yaoquan.hanxu.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {
    // Field of mod config:
    private static final ModConfigSpec.Builder MOD_CONFIG = new ModConfigSpec.Builder();

    private static final String COMMENT_1 = " Define the permission level for new player when server start.";
    private static final String COMMENT_2 = " Determine the allowance on player edit permission level in game.";
    private static final String COMMENT_3 = " Determine does *Loot Deployer* play sound after deploy.";

    // Generate the list of config:
    public static final ModConfigSpec.IntValue SET_AUTO_AUTHORIZED_PERMISSION_LEVEL = MOD_CONFIG
            .comment(COMMENT_1)
            .defineInRange("auto_level", 1, 0, 2);

    public static final ModConfigSpec.BooleanValue SET_PLAYER_PERMISSION_EDITABLE = MOD_CONFIG
            .comment(COMMENT_2)
            .define("player_permission_editable", true);

    public static final ModConfigSpec.BooleanValue SET_ENABLED_DEPLOY_SOUND = MOD_CONFIG
            .comment(COMMENT_3)
            .define("enabled_deploy_sound", true);

    public static final ModConfigSpec SPEC = MOD_CONFIG.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}