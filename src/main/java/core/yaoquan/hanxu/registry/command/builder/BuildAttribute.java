package core.yaoquan.hanxu.registry.command.builder;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import core.yaoquan.hanxu.api.PermissionHolder;
import core.yaoquan.hanxu.registry.command.CommandSuggest;
import core.yaoquan.hanxu.registry.command.execute.*;
import core.yaoquan.hanxu.registry.config.PermissionConfig;
import core.yaoquan.hanxu.test.TestHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class BuildAttribute {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("attribute")
            .then(
                Commands.literal("help")
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.guide.attribute.get()))
                    .executes(ExecuteGuide::executeAttribute_Help)
            )
            .then(
                Commands.literal("list")
                    .then(
                        Commands.literal("api")
                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs,PermissionConfig.VALUE.information.attributeApi.get()))
                            .executes(ExecuteInformation::executeAdvancedAttribute_List)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs,PermissionConfig.VALUE.information.attribute.get()))
                    .executes(ExecuteInformation::executeAttribute_List)
            )
            .then(
                Commands.literal("read")
                    .then(
                        Commands.argument("attribute_id", StringArgumentType.string())
                            .then(
                                Commands.literal("threshold")
                                    .then(
                                        Commands.literal("all")
                                            .executes(cs -> ExecuteInformation.executeAttribute_Read(cs, "threshold_all"))
                                    )
                                    .then(
                                        Commands.literal("specific")
                                            .then(
                                                Commands.argument("threshold_value", FloatArgumentType.floatArg())
                                                    .executes(cs -> ExecuteInformation.executeAttribute_Read(cs, "threshold_specific"))
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("zero")
                                    .executes(cs -> ExecuteInformation.executeAttribute_Read(cs, "zero"))
                            )
                            .then(
                                Commands.literal("recovery")
                                    .executes(cs -> ExecuteInformation.executeAttribute_Read(cs, "recovery"))
                            )
                            .then(
                                Commands.literal("maximum")
                                    .executes(cs -> ExecuteInformation.executeAttribute_Read(cs, "maximum"))
                            )
                            .then(
                                Commands.literal("recovery_interval")
                                    .executes(cs -> ExecuteInformation.executeAttribute_Read(cs, "recovery_interval"))
                            )
                            .then(
                                Commands.literal("value")
                                    .then(
                                        Commands.argument("player_id", StringArgumentType.string())
                                            .suggests(CommandSuggest::suggestPlayer)
                                            .executes(cs -> ExecuteInformation.executeAttribute_Read(cs, "value"))
                                    )
                                    .executes(cs -> ExecuteInformation.executeAttribute_Read(cs, "value"))
                            )
                            .then(
                                Commands.literal("group")
                                    .executes(cs -> ExecuteInformation.executeAttribute_Read(cs, "group"))
                            )
                            .suggests(CommandSuggest::suggestYamlAttribute)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.information.attribute.get()))
            )
            .then(
                Commands.literal("create")
                    .then(
                        Commands.argument("attribute_id", StringArgumentType.word())
                            .then(
                                Commands.argument("to_path", StringArgumentType.string())
                                    .then(
                                        Commands.argument("maximum", FloatArgumentType.floatArg())
                                            .then(
                                                Commands.argument("default_value", FloatArgumentType.floatArg())
                                                    .executes(ExecuteCreate::executeAttribute_Create)
                                            )
                                            .executes(ExecuteCreate::executeAttribute_Create)
                                    )
                                    .suggests(CommandSuggest::suggestSavePath)
                                    .executes(ExecuteCreate::executeAttribute_Create)
                            )
                            .suggests(CommandSuggest::suggestYamlAttribute)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.create.attribute.get()))
            )
            .then(
                Commands.literal("define")
                    .then(
                        Commands.argument("attribute_id", StringArgumentType.string())
                            .then(
                                Commands.argument("threshold", FloatArgumentType.floatArg())
                                    .then(
                                        Commands.literal("remind")
                                            .then(
                                                Commands.argument("content", StringArgumentType.greedyString())
                                                    .executes(cs -> ExecuteModification.executeAttribute_Define(cs, "remind"))
                                            )
                                    )
                                    .then(
                                        Commands.literal("execute")
                                            .then(
                                                Commands.argument("content", StringArgumentType.greedyString())
                                                    .executes(cs -> ExecuteModification.executeAttribute_Define(cs, "execute"))
                                            )
                                    )
                                    .then(
                                        Commands.literal("api")
                                            .then(
                                                Commands.argument("callback_id", StringArgumentType.string())
                                                    .executes(cs -> ExecuteModification.executeAttribute_Define(cs, "api"))
                                            )
                                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs,PermissionConfig.VALUE.modification.attributeDefineApi.get()))
                                    )
                            )
                            .suggests(CommandSuggest::suggestYamlAttribute)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.modification.attributeDefine.get()))
            )
            .then(
                Commands.literal("delete")
                    .then(
                        Commands.argument("attribute_id", StringArgumentType.string())
                            .then(
                                Commands.literal("world")
                                    .executes(cs -> ExecuteDelete.executeAttribute_Delete(cs, "world"))
                            )
                            .then(
                                Commands.literal("global")
                                    .executes(cs -> ExecuteDelete.executeAttribute_Delete(cs, "global"))
                            )
                            .suggests(CommandSuggest::suggestYamlAttribute)
                            .executes(cs -> ExecuteDelete.executeAttribute_Delete(cs, "try"))
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs,PermissionConfig.VALUE.delete.attribute.get()))
            )
            .then(
                Commands.literal("modify")
                    .then(
                        Commands.argument("attribute_id", StringArgumentType.string())
                            .then(
                                Commands.argument("player_id", StringArgumentType.string())
                                    .then(
                                        Commands.literal("set")
                                            .then(
                                                Commands.argument("value", FloatArgumentType.floatArg())
                                                    .then(
                                                        Commands.argument("direction", StringArgumentType.word())
                                                            .suggests(CommandSuggest::suggestAttributeDirection)
                                                            .executes(cs -> ExecuteModification.executeAttribute_Modify(cs, "set"))
                                                    )
                                                    .executes(cs -> ExecuteModification.executeAttribute_Modify(cs, "set"))
                                            )
                                    )
                                    .then(
                                        Commands.literal("add")
                                            .then(
                                                Commands.argument("value", FloatArgumentType.floatArg())
                                                    .then(
                                                        Commands.argument("direction", StringArgumentType.word())
                                                            .suggests(CommandSuggest::suggestAttributeDirection)
                                                            .executes(cs -> ExecuteModification.executeAttribute_Modify(cs, "add"))
                                                    )
                                                    .executes(cs -> ExecuteModification.executeAttribute_Modify(cs, "add"))
                                            )
                                    )
                                    .then(
                                        Commands.literal("reduce")
                                            .then(
                                                Commands.argument("value", FloatArgumentType.floatArg())
                                                    .then(
                                                        Commands.argument("direction", StringArgumentType.word())
                                                            .suggests(CommandSuggest::suggestAttributeDirection)
                                                            .executes(cs -> ExecuteModification.executeAttribute_Modify(cs, "reduce"))
                                                    )
                                                    .executes(cs -> ExecuteModification.executeAttribute_Modify(cs, "reduce"))
                                            )
                                    )
                                    .suggests(CommandSuggest::suggestPlayer)
                            )
                            .suggests(CommandSuggest::suggestYamlAttribute)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.modification.attributeModify.get()))
            )
            .then(
                Commands.literal("recovery")
                    .then(
                        Commands.argument("attribute_id", StringArgumentType.string())
                            .then(
                                Commands.literal("simple")
                                    .then(
                                        Commands.argument("interval", IntegerArgumentType.integer())
                                            .then(
                                                Commands.argument("interval_unit", StringArgumentType.word())
                                                    .then(
                                                        Commands.argument("value", FloatArgumentType.floatArg())
                                                            .then(
                                                                Commands.argument("direction", StringArgumentType.word())
                                                                    .suggests(CommandSuggest::suggestAttributeDirection)
                                                                    .executes(cs -> ExecuteModification.executeAttribute_Recovery(cs, "simple"))
                                                            )
                                                            .executes(cs -> ExecuteModification.executeAttribute_Recovery(cs, "simple"))
                                                    )
                                                    .suggests(CommandSuggest::suggestUnit)
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("api")
                                    .then(
                                        Commands.argument("callback_id", StringArgumentType.string())
                                            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.modification.attributeRecoveryApi.get()))
                                            .executes(cs -> ExecuteModification.executeAttribute_Recovery(cs, "api"))
                                    )
                            )
                            .suggests(CommandSuggest::suggestYamlAttribute)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.modification.attributeRecovery.get()))
            )
            .then(
                Commands.literal("display")
                    .then(
                        Commands.argument("attribute_id", StringArgumentType.string())
                            .then(
                                Commands.argument("master_id", StringArgumentType.string())
                                    .then(
                                        Commands.literal("true")
                                            .executes(cs -> ExecuteDisplay.executeAttribute_Display(cs, true))
                                    )
                                    .then(
                                        Commands.literal("false")
                                            .executes(cs -> ExecuteDisplay.executeAttribute_Display(cs, false))
                                    )
                                    .suggests(CommandSuggest::suggestUUIDOwner)
                                    .executes(cs -> ExecuteDisplay.executeAttribute_Display(cs, true))
                            )
                            .suggests(CommandSuggest::suggestAllAttribute)
                    )
                    .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.display.attribute.get()))
            )
            .then(
                Commands.literal("test")
                    .then(
                        Commands.literal("example")
                            .then(
                                Commands.argument("test_id", IntegerArgumentType.integer())
                                    .then(
                                        Commands.argument("extra", StringArgumentType.string())
                                            .executes(TestHolder::executeTest_Attribute)
                                    )
                                    .executes(TestHolder::executeTest_Attribute)
                            )
                    )
                    .then(
                        Commands.literal("yaml_display")
                            .executes(TestHolder::executeTest_AttributeYamlDisplay)
                    )
                    .requires(TestHolder::hasPrivateTestPermission)
            )
            .requires(cs -> PermissionHolder.Verify.hasPermission(cs, PermissionConfig.VALUE.guide.attribute.get()))
            .executes(ExecuteGuide::executeAttribute);
    }
}
