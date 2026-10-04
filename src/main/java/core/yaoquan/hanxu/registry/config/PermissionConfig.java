package core.yaoquan.hanxu.registry.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class PermissionConfig {
    public static final class Value {
        /// Define command: Help, license, origin, bare...
        public static final class Guide {
            public final ModConfigSpec.IntValue general;
            public final ModConfigSpec.IntValue licenseAdvanced;
            public final ModConfigSpec.IntValue timer;
            public final ModConfigSpec.IntValue scene;
            public final ModConfigSpec.IntValue attribute;
            public final ModConfigSpec.IntValue variable;
            public final ModConfigSpec.IntValue loot;
            public final ModConfigSpec.IntValue weather;

            Guide(ModConfigSpec.Builder builder) {
                builder.translation("config.core_hanxu.permission.guide")
                        .comment(" Define command: Help, license, origin, bare...")
                        .push("guide");
                general = define(builder, "general", 0);
                licenseAdvanced = define(builder, "license_advanced", 2);
                timer = define(builder, "timer", 1);
                scene = define(builder, "scene", 1);
                attribute = define(builder, "attribute", 1);
                variable = define(builder, "variable", 1);
                loot = define(builder, "loot", 1);
                weather = define(builder, "weather", 1);
                builder.pop();
            }
        }

        /// Define command: List, read, state...
        public static final class Information {
            public final ModConfigSpec.IntValue general;
            public final ModConfigSpec.IntValue timer;
            public final ModConfigSpec.IntValue scene;
            public final ModConfigSpec.IntValue attribute;
            public final ModConfigSpec.IntValue attributeApi;
            public final ModConfigSpec.IntValue variable;
            public final ModConfigSpec.IntValue loot;
            public final ModConfigSpec.IntValue weather;

            Information(ModConfigSpec.Builder builder) {
                builder.translation("config.core_hanxu.permission.information")
                        .comment(" Define command: List, read, state...")
                        .push("information");
                general = define(builder, "general", 0);
                timer = define(builder, "timer", 1);
                scene = define(builder, "scene", 1);
                attribute = define(builder, "attribute", 1);
                attributeApi = define(builder, "attribute_api", 10);
                variable = define(builder, "variable", 1);
                loot = define(builder, "loot", 1);
                weather = define(builder, "weather", 1);
                builder.pop();
            }
        }

        /// Define command: agree.
        public static final class Confirmation {
            public final ModConfigSpec.IntValue license;

            Confirmation(ModConfigSpec.Builder builder) {
                builder.translation("config.core_hanxu.permission.confirmation")
                        .comment(" Define command: agree.")
                        .push("confirmation");
                license = define(builder, "license", 0);
                builder.pop();
            }
        }

        /// Define command: create.
        public static final class Create {
            public final ModConfigSpec.IntValue timerApply;
            public final ModConfigSpec.IntValue timerCreate;
            public final ModConfigSpec.IntValue scene;
            public final ModConfigSpec.IntValue attribute;
            public final ModConfigSpec.IntValue variable;
            public final ModConfigSpec.IntValue loot;
            public final ModConfigSpec.IntValue weather;

            Create(ModConfigSpec.Builder builder) {
                builder.translation("config.core_hanxu.permission.create")
                        .comment(" Define command: create.")
                        .push("create");
                timerApply = define(builder, "timer_apply", 2);
                timerCreate = define(builder, "timer_create", 2);
                scene = define(builder, "scene", 2);
                attribute = define(builder, "attribute", 2);
                variable = define(builder, "variable", 2);
                loot = define(builder, "loot", 2);
                weather = define(builder, "weather", 2);
                builder.pop();
            }
        }

        /// Define command: delete.
        public static final class Delete {
            public final ModConfigSpec.IntValue timer;
            public final ModConfigSpec.IntValue scene;
            public final ModConfigSpec.IntValue attribute;
            public final ModConfigSpec.IntValue variable;
            public final ModConfigSpec.IntValue loot;
            public final ModConfigSpec.IntValue weather;

            Delete(ModConfigSpec.Builder builder) {
                builder.translation("config.core_hanxu.permission.delete")
                        .comment(" Define command: delete.")
                        .push("delete");
                timer = define(builder, "timer", 2);
                scene = define(builder, "scene", 2);
                attribute = define(builder, "attribute", 2);
                variable = define(builder, "variable", 2);
                loot = define(builder, "loot", 2);
                weather = define(builder, "weather", 2);
                builder.pop();
            }
        }

        /// Define command: start, play, give, fill, broadcast, resume...
        public static final class Run {
            public final ModConfigSpec.IntValue timer;
            public final ModConfigSpec.IntValue scenePlay;
            public final ModConfigSpec.IntValue sceneBroadcast;
            public final ModConfigSpec.IntValue lootGive;
            public final ModConfigSpec.IntValue lootFill;
            public final ModConfigSpec.IntValue weatherStart;
            public final ModConfigSpec.IntValue weatherResume;

            Run(ModConfigSpec.Builder builder) {
                builder.translation("config.core_hanxu.permission.run")
                        .comment(" Define command: start, play, give, fill, broadcast, resume...")
                        .push("run");
                timer = define(builder, "timer", 2);
                scenePlay = define(builder, "scene_play", 1);
                sceneBroadcast = define(builder, "scene_broadcast", 2);
                lootGive = define(builder, "loot_give", 2);
                lootFill = define(builder, "loot_fill", 2);
                weatherStart = define(builder, "weather_start", 2);
                weatherResume = define(builder, "weather_resume", 2);
                builder.pop();
            }
        }

        /// Define command: stop, pause...
        public static final class Stop {
            public final ModConfigSpec.IntValue timer;
            public final ModConfigSpec.IntValue weather;

            Stop(ModConfigSpec.Builder builder) {
                builder.translation("config.core_hanxu.permission.stop")
                        .comment(" Define command: stop, pause...")
                        .push("stop");
                timer = define(builder, "timer", 2);
                weather = define(builder, "weather", 2);
                builder.pop();
            }
        }

        /// Define command: reset, restart, ready, kill...
        public static final class Status {
            public final ModConfigSpec.IntValue timerReset;
            public final ModConfigSpec.IntValue timerRestart;
            public final ModConfigSpec.IntValue weatherRestart;
            public final ModConfigSpec.IntValue weatherReady;
            public final ModConfigSpec.IntValue weatherKill;

            Status(ModConfigSpec.Builder builder) {
                builder.translation("config.core_hanxu.permission.status")
                        .comment(" Define command: reset, restart, ready, kill...")
                        .push("status");
                timerReset = define(builder, "timer_reset", 2);
                timerRestart = define(builder, "timer_restart", 2);
                weatherRestart = define(builder, "weather_restart", 2);
                weatherReady = define(builder, "weather_ready", 2);
                weatherKill = define(builder, "weather_kill", 2);
                builder.pop();
            }
        }

        /// Define command: display.
        public static final class Display {
            public final ModConfigSpec.IntValue timer;
            public final ModConfigSpec.IntValue attribute;
            public final ModConfigSpec.IntValue weather;

            Display(ModConfigSpec.Builder builder) {
                builder.translation("config.core_hanxu.permission.display")
                        .comment(" Define command: display.")
                        .push("display");
                timer = define(builder, "timer", 3);
                attribute = define(builder, "attribute", 3);
                weather = define(builder, "weather", 3);
                builder.pop();
            }
        }

        /// Define command: template.
        public static final class Template {
            public final ModConfigSpec.IntValue scene;
            public final ModConfigSpec.IntValue loot;
            public final ModConfigSpec.IntValue weather;

            Template(ModConfigSpec.Builder builder) {
                builder.translation("config.core_hanxu.permission.template")
                        .comment(" Define command: template.")
                        .push("template");
                scene = define(builder, "scene", 2);
                loot = define(builder, "loot", 2);
                weather = define(builder, "weather", 2);
                builder.pop();
            }
        }

        /// Define command: modify (set, add, reduce), string, recovery, copy...
        public static final class Modification {
            public final ModConfigSpec.IntValue timerModify;
            public final ModConfigSpec.IntValue attributeModify;
            public final ModConfigSpec.IntValue attributeRecovery;
            public final ModConfigSpec.IntValue attributeRecoveryApi;
            public final ModConfigSpec.IntValue attributeDefine;
            public final ModConfigSpec.IntValue attributeDefineApi;
            public final ModConfigSpec.IntValue variableModify;
            public final ModConfigSpec.IntValue variableString;
            public final ModConfigSpec.IntValue variableCopy;
            public final ModConfigSpec.IntValue weatherModify;

            Modification(ModConfigSpec.Builder builder) {
                builder.translation("config.core_hanxu.permission.modification")
                        .comment(" Define command: modify (set, add, reduce), string, recovery, copy...")
                        .push("modification");
                timerModify = define(builder, "timer_modify", 2);
                attributeModify = define(builder, "attribute_modify", 2);
                attributeRecovery = define(builder, "attribute_recovery", 2);
                attributeRecoveryApi = define(builder, "attribute_recovery_api", 10);
                attributeDefine = define(builder, "attribute_define", 2);
                attributeDefineApi = define(builder, "attribute_define_api", 10);
                variableModify = define(builder, "variable_modify", 2);
                variableString = define(builder, "variable_string", 2);
                variableCopy = define(builder, "variable_copy", 2);
                weatherModify = define(builder, "weather_modify", 2);
                builder.pop();
            }
        }

        /// Define command: if (value, margin, score).
        public static final class Condition {
            public final ModConfigSpec.IntValue variable;

            Condition(ModConfigSpec.Builder builder) {
                builder.translation("config.core_hanxu.permission.condition")
                        .comment(" Define command: if (value, margin, score).")
                        .push("condition");
                variable = define(builder, "variable", 2);
                builder.pop();
            }
        }

        /// Define command: reload.
        public static final class Reload {
            public final ModConfigSpec.IntValue weather;

            Reload(ModConfigSpec.Builder builder) {
                builder.translation("config.core_hanxu.permission.reload")
                        .comment(" Define command: reload.")
                        .push("reload");
                weather = define(builder, "weather", 2);
                builder.pop();
            }
        }

        /// Define command: Set of permissions.
        public static final class Permission {
            public final ModConfigSpec.IntValue general;

            Permission(ModConfigSpec.Builder builder) {
                builder.translation("config.core_hanxu.permission.permission")
                        .comment(" Define command: Set of permissions.")
                        .push("permission");
                general = define(builder, "general", 10);
                builder.pop();
            }
        }
    }

    public static final class Build {
        public final Value.Guide guide;
        public final Value.Information information;
        public final Value.Confirmation confirmation;
        public final Value.Create create;
        public final Value.Delete delete;
        public final Value.Run run;
        public final Value.Stop stop;
        public final Value.Status status;
        public final Value.Display display;
        public final Value.Template template;
        public final Value.Modification modification;
        public final Value.Condition condition;
        public final Value.Reload reload;
        public final Value.Permission permission;

        Build(ModConfigSpec.Builder builder) {
            guide = new Value.Guide(builder);
            information = new Value.Information(builder);
            confirmation = new Value.Confirmation(builder);
            create = new Value.Create(builder);
            delete = new Value.Delete(builder);
            run = new Value.Run(builder);
            stop = new Value.Stop(builder);
            status = new Value.Status(builder);
            display = new Value.Display(builder);
            template = new Value.Template(builder);
            modification = new Value.Modification(builder);
            condition = new Value.Condition(builder);
            reload = new Value.Reload(builder);
            permission = new Value.Permission(builder);
        }
    }

    public static final Build VALUE;

    public static final ModConfigSpec SPEC_PERMISSION;

    static {
        Pair<Build, ModConfigSpec> pair = new ModConfigSpec.Builder()
                .configure(Build::new);
        VALUE = pair.getLeft();
        SPEC_PERMISSION = pair.getRight();
    }

    private static ModConfigSpec.IntValue define(ModConfigSpec.Builder builder, String path, int defaultValue) {
        return builder.comment("-> Define permission level requirement: " + path + ".")
                .translation("config.core_hanxu.configuration." + path)
                .defineInRange(path, defaultValue, 0, 10);
    }
}
