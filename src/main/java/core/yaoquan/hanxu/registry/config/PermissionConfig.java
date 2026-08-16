package core.yaoquan.hanxu.registry.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class PermissionConfig {
    private static final ModConfigSpec.Builder CONFIG = new ModConfigSpec.Builder();

    private static final String comment_h = " Permission define '/chx help' requirement.";
    private static final String comment_d = " Permission define '/chx detail' requirement.";
    private static final String comment_l = " Permission define '/chx license state [player id]' requirement.";
    private static final String comment_p = " Permission define '/chx permission ...' requirement.";
    private static final String comment_th = " Permission define '/chx timer help' requirement.";
    private static final String comment_tt = " Permission define '/chx timer template ...' requirement.";
    private static final String comment_tic = " Permission define '/chx timer instance' at create requirement.";
    private static final String comment_tis = " Permission define '/chx timer instance' at start/stop requirement.";
    private static final String comment_tio = " Permission define '/chx timer instance' at other modification requirement.";
    private static final String comment_tid = " Permission define '/chx timer instance display' requirement.";
    private static final String comment_sh = " Permission define '/chx scene help' requirement.";
    private static final String comment_spb = " Permission define '/chx scene' at play/broadcast requirement.";
    private static final String comment_so = " Permission define '/chx scene' at other modification requirement.";
    private static final String comment_ah = " Permission define '/chx attribute help' requirement.";
    private static final String comment_ac = " Permission define '/chx attribute' at create/define requirement.";
    private static final String comment_ao = " Permission define '/chx attribute' at other modification requirement.";
    private static final String comment_ad = " Permission define '/chx attribute display' requirement.";
    private static final String comment_v = " Permission define '/chx variable' entire system requirement.";
    private static final String comment_lt = " Permission define '/chx loot' entire system requirement.";

    // Permission set list config:
    public static final ModConfigSpec.IntValue setPermissionHelp = CONFIG
            .comment(comment_h)
            .defineInRange("permission_help", 0, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionDetail = CONFIG
            .comment(comment_d)
            .defineInRange("permission_detail", 0, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionLicenseAdvancedState = CONFIG
            .comment(comment_l)
            .defineInRange("permission_license_advanced_state", 1, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionPermissionLevel = CONFIG
            .comment(comment_p)
            .defineInRange("permission_level", 10, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionTimerHelp = CONFIG
            .comment(comment_th)
            .defineInRange("permission_timer_help", 1, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionTimerTemplate = CONFIG
            .comment(comment_tt)
            .defineInRange("permission_timer_template", 1, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionTimerInstanceCreate = CONFIG
            .comment(comment_tic)
            .defineInRange("permission_timer_instance_create", 2, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionTimerInstanceRun = CONFIG
            .comment(comment_tis)
            .defineInRange("permission_timer_instance_run", 2, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionTimerInstanceOthers = CONFIG
            .comment(comment_tio)
            .defineInRange("permission_timer_instance_others", 2, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionTimerF4 = CONFIG
            .comment(comment_tid)
            .defineInRange("permission_timer_instance_f4", 3, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionSceneHelp = CONFIG
            .comment(comment_sh)
            .defineInRange("permission_scene_help", 1, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionScenePlay = CONFIG
            .comment(comment_spb)
            .defineInRange("permission_scene_play", 1, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionSceneOthers = CONFIG
            .comment(comment_so)
            .defineInRange("permission_scene_others", 2, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionAttributeHelp = CONFIG
            .comment(comment_ah)
            .defineInRange("permission_attribute_help", 1, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionAttributeCreate = CONFIG
            .comment(comment_ac)
            .defineInRange("permission_attribute_create", 2, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionAttributeOthers = CONFIG
            .comment(comment_ao)
            .defineInRange("permission_attribute_others", 2, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionAttributeF4 = CONFIG
            .comment(comment_ad)
            .defineInRange("permission_attribute_f4", 3, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionVariableOperations = CONFIG
            .comment(comment_v)
            .defineInRange("permission_variable_operations", 2, 0, 10);

    public static final ModConfigSpec.IntValue setPermissionLootOperations = CONFIG
            .comment(comment_lt)
            .defineInRange("permission_loot_operations", 2, 0, 10);

    public static final ModConfigSpec SPEC_PERMISSION = CONFIG.build();
}
