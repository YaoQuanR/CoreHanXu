package core.yaoquan.hanxu.registry.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class PermissionConfig {
    private static final ModConfigSpec.Builder CONFIG = new ModConfigSpec.Builder();

    private static final String PERMISSION_SET_COMMENT_H = " Permission define '/chx help' requirement.";
    private static final String PERMISSION_SET_COMMENT_D = " Permission define '/chx detail' requirement.";
    private static final String PERMISSION_SET_COMMENT_L = " Permission define '/chx license state [player id]' requirement.";
    private static final String PERMISSION_SET_COMMENT_P = " Permission define '/chx permission ...' requirement.";
    private static final String PERMISSION_SET_COMMENT_TH = " Permission define '/chx timer help' requirement.";
    private static final String PERMISSION_SET_COMMENT_TT = " Permission define '/chx timer template ...' requirement.";
    private static final String PERMISSION_SET_COMMENT_TIC = " Permission define '/chx timer instance' at create requirement.";
    private static final String PERMISSION_SET_COMMENT_TIS = " Permission define '/chx timer instance' at start/stop requirement.";
    private static final String PERMISSION_SET_COMMENT_TIO = " Permission define '/chx timer instance' at other modification requirement.";
    private static final String PERMISSION_SET_COMMENT_TID = " Permission define '/chx timer instance display' requirement.";
    private static final String PERMISSION_SET_COMMENT_SH = " Permission define '/chx scene help' requirement.";
    private static final String PERMISSION_SET_COMMENT_SPB = " Permission define '/chx scene' at play/broadcast requirement.";
    private static final String PERMISSION_SET_COMMENT_SO = " Permission define '/chx scene' at other modification requirement.";
    private static final String PERMISSION_SET_COMMENT_AH = " Permission define '/chx attribute help' requirement.";
    private static final String PERMISSION_SET_COMMENT_AC = " Permission define '/chx attribute' at create/define requirement.";
    private static final String PERMISSION_SET_COMMENT_AO = " Permission define '/chx attribute' at other modification requirement.";
    private static final String PERMISSION_SET_COMMENT_AD = " Permission define '/chx attribute display' requirement.";

    // Permission set list config:
    public static final ModConfigSpec.IntValue SET_PERMISSION_HELP = CONFIG
            .comment(PERMISSION_SET_COMMENT_H)
            .defineInRange("permission_help", 0, 0, 10);

    public static final ModConfigSpec.IntValue SET_PERMISSION_DETAIL = CONFIG
            .comment(PERMISSION_SET_COMMENT_D)
            .defineInRange("permission_detail", 0, 0, 10);

    public static final ModConfigSpec.IntValue SET_PERMISSION_LICENSE_ADVANCED_STATE = CONFIG
            .comment(PERMISSION_SET_COMMENT_L)
            .defineInRange("permission_license_advanced_state", 1, 0, 10);

    public static final ModConfigSpec.IntValue SET_PERMISSION_PERMISSION_LEVEL = CONFIG
            .comment(PERMISSION_SET_COMMENT_P)
            .defineInRange("permission_level", 10, 0, 10);

    public static final ModConfigSpec.IntValue SET_PERMISSION_TIMER_HELP = CONFIG
            .comment(PERMISSION_SET_COMMENT_TH)
            .defineInRange("permission_timer_help", 1, 0, 10);

    public static final ModConfigSpec.IntValue SET_PERMISSION_TIMER_TEMPLATE = CONFIG
            .comment(PERMISSION_SET_COMMENT_TT)
            .defineInRange("permission_timer_template", 1, 0, 10);

    public static final ModConfigSpec.IntValue SET_PERMISSION_TIMER_INSTANCE_CREATE = CONFIG
            .comment(PERMISSION_SET_COMMENT_TIC)
            .defineInRange("permission_timer_instance_create", 2, 0, 10);

    public static final ModConfigSpec.IntValue SET_PERMISSION_TIMER_INSTANCE_RUN = CONFIG
            .comment(PERMISSION_SET_COMMENT_TIS)
            .defineInRange("permission_timer_instance_run", 2, 0, 10);

    public static final ModConfigSpec.IntValue SET_PERMISSION_TIMER_INSTANCE_OTHERS = CONFIG
            .comment(PERMISSION_SET_COMMENT_TIO)
            .defineInRange("permission_timer_instance_others", 2, 0, 10);

    public static final ModConfigSpec.IntValue SET_PERMISSION_TIMER_F4 = CONFIG
            .comment(PERMISSION_SET_COMMENT_TID)
            .defineInRange("permission_timer_instance_f4", 3, 0, 10);

    public static final ModConfigSpec.IntValue SET_PERMISSION_SCENE_HELP = CONFIG
            .comment(PERMISSION_SET_COMMENT_SH)
            .defineInRange("permission_scene_help", 1, 0, 10);

    public static final ModConfigSpec.IntValue SET_PERMISSION_SCENE_PLAY = CONFIG
            .comment(PERMISSION_SET_COMMENT_SPB)
            .defineInRange("permission_scene_play", 1, 0, 10);

    public static final ModConfigSpec.IntValue SET_PERMISSION_SCENE_OTHERS = CONFIG
            .comment(PERMISSION_SET_COMMENT_SO)
            .defineInRange("permission_scene_others", 2, 0, 10);

    public static final ModConfigSpec.IntValue SET_PERMISSION_ATTRIBUTE_HELP = CONFIG
            .comment(PERMISSION_SET_COMMENT_AH)
            .defineInRange("permission_attribute_help", 1, 0, 10);

    public static final ModConfigSpec.IntValue SET_PERMISSION_ATTRIBUTE_CREATE = CONFIG
            .comment(PERMISSION_SET_COMMENT_AC)
            .defineInRange("permission_attribute_create", 2, 0, 10);

    public static final ModConfigSpec.IntValue SET_PERMISSION_ATTRIBUTE_OTHERS = CONFIG
            .comment(PERMISSION_SET_COMMENT_AO)
            .defineInRange("permission_attribute_others", 2, 0, 10);

    public static final ModConfigSpec.IntValue SET_PERMISSION_ATTRIBUTE_F4 = CONFIG
            .comment(PERMISSION_SET_COMMENT_AD)
            .defineInRange("permission_attribute_f4", 3, 0, 10);

    public static final ModConfigSpec SPEC_PERMISSION = CONFIG.build();
}
