package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.MessagePublisher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ExecuteGuide {
    public static int executeDetail(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.detail_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.detail_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context,
            Component.translatable("commands.chx.detail_innertext2")
                    .append(Component.literal(" " + General.Version.getCoreVersion()))
                    .withColor(General.Color.CONTENT)
        );
        return 1;
    }

    public static int executeLicense(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_innertext2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_innertext3").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_innertext4").withColor(General.Color.CONTENT));
        return 1;
    }

    public static int executeLicense_Origin(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext3").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext4").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext5").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext6").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext7").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext8").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext9").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext10").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext11").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext12").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext13").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext14").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext15").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext16").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext17").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext18").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext19").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext20").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext21").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext22").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext23").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext24").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext25").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext26").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext27").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext28").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext29").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext30").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.literal("").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext31").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.license_origin_innertext32").withColor(General.Color.CONTENT));
        return 1;
    }

    public static int executeBare(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.bare1").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.bare2").withColor(General.Color.TITLE));
        return 1;
    }

    public static int executeHelp(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.fixed.available_commands").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext3").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext4").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext5").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext6").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext7").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext8").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext9").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext10").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext11").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext12").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext13").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.help_innertext14").withColor(General.Color.CONTENT));
        return 1;
    }

    public static int executeTimer(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer").withColor(General.Color.TITLE));
        return 1;
    }

    public static int executeScene(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene").withColor(General.Color.TITLE));
        return 1;
    }

    public static int executeAttribute(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute").withColor(General.Color.TITLE));
        return 1;
    }

    public static int executeVariable(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable").withColor(General.Color.TITLE));

        return 1;
    }

    public static int executeLoot(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot").withColor(General.Color.TITLE));
        return 1;
    }

    public static int executeWeather(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather").withColor(General.Color.TITLE));
        return 1;
    }

    public static int executeTimer_Help(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_introduction").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.fixed.available_commands").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_create_argument1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_create-range_argument1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext3").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext4").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext5").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_create_argument2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext6").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_create-range_argument2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext7").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext8").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext9").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext10").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext11").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext12").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext13").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext14").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_modify_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_innertext15").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_read_argument1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.timer_help_read_argument2").withColor(General.Color.CONTENT));
        return 1;
    }

    public static int executeScene_Help(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_introduction").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.fixed.available_commands").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext3").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext4").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext5").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext6").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_help_innertext7").withColor(General.Color.CONTENT));
        return 1;
    }

    public static int executeAttribute_Help(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_introduction").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.fixed.available_commands").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext3").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext4").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext5").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_create_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext6").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_define_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext7").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext8").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext9").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext10").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext11").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_modify_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext12").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_recovery_argument1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_recovery_argument2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.attribute_help_innertext13").withColor(General.Color.CONTENT));
        return 1;
    }

    public static int executeVariable_Help(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_introduction").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.fixed.available_commands").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext3").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext4").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext5").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_create_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext6").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext7").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_copy_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext8").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_if_value_argument1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_if_value_argument2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext9").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_if_score_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext10").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_if_margin_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext11").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.variable_help_innertext12").withColor(General.Color.CONTENT));

        return 1;
    }

    public static int executeLoot_Help(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_introduction").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.fixed.available_commands").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext1").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext2").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext3").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext4").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext5").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_give_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext6").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_fill_argument").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext7").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext8").withColor(General.Color.CONTENT));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_innertext9").withColor(General.Color.CONTENT));
        return 1;
    }

    public static int executeLoot_Help_Functions(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_functions_title").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_help_functions_introduction").withColor(General.Color.TITLE));
        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.fixed.available_functions").withColor(General.Color.TITLE));

        String[] lines = {
                "set_count: (Number/Map) count",
                "set_damage: (Float:0.0~1.0) damage",
                "set_exactly_damage: (Integer) damage",
                "set_name: (String/Json) name",
                "set_lore: (List) lore",
                "set_custom_model_data: (List/Float/Boolean/String/Integer) floats, flags, strings, colors",
                "enchant_randomly: (null)",
                "set_enchantments: (Map) enchantments",
                "enchant_with_levels: (Number/Map) levels",
                "looting_enchant: (null)",
                "furnace_smelt: (null)",
                "explosion_decay: (Float:0.0~1.0) chance",
                "limit_count: (Integer) limit",
                "set_potion: (String) id",
                "set_attributes: (List) attributes",
                "set_glint_override: (Boolean) glint",
                "set_repair_cost: (Integer) cost",
                "set_food: (Map) food",
                "unbreakable: (null)",
                "set_can_break: (List) blocks",
                "set_can_place_on: (List) blocks",
                "set_consumable: (Map) consume_seconds, animation, sound, effects",
                "set_equippable: (String) slot",
                "set_trim: (String) material, pattern",
                "set_firework: (Map) flight_duration, explosions",
                "set_fire_resistant: (null)"
        };

        for (String line : lines) {
            MessagePublisher.sendSystemMessage(context,
                    Component.literal(line).withColor(General.Color.CONTENT));
        }

        return 1;
    }

    public static int executeWeather_Help(CommandContext<CommandSourceStack> context) {
        MessagePublisher.sendFailureMessage(context, Component.literal("[HX] This function is not yet finished!"));
        return 0;
    }
}
