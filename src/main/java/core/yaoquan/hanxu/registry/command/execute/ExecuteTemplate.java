package core.yaoquan.hanxu.registry.command.execute;

import com.mojang.brigadier.context.CommandContext;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.tool.MessagePublisher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;

import java.util.List;

import static core.yaoquan.hanxu.api.define.Error.*;

public class ExecuteTemplate {
    public static int executeScene_Template(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            MessagePublisher.sendFailureMessage(context, errorComponent(GeneralError.notPlayer));
            return 0;
        }

        // Create a template book.
        ItemStack book = new ItemStack(Items.WRITABLE_BOOK);

        String bookTemplate = sceneTemplate();

        // Setup template.
        WritableBookContent content = new WritableBookContent(List.of(Filterable.passThrough(bookTemplate)));
        book.set(DataComponents.WRITABLE_BOOK_CONTENT, content);

        // Then give.
        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }

        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.scene_get_template").withColor(General.Color.SUCCESS));
        return 1;
    }

    public static int executeLoot_Template(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            MessagePublisher.sendFailureMessage(context, errorComponent(GeneralError.notPlayer));
            return 0;
        }

        ItemStack book = new ItemStack(Items.WRITABLE_BOOK);

        String bookTemplate = lootTemplate();

        WritableBookContent content = new WritableBookContent(List.of(Filterable.passThrough(bookTemplate)));
        book.set(DataComponents.WRITABLE_BOOK_CONTENT, content);

        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }

        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.loot_template").withColor(General.Color.SUCCESS));

        return 1;
    }

    public static int executeWeather_Template(CommandContext<CommandSourceStack> context, String templateType) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            MessagePublisher.sendFailureMessage(context, errorComponent(GeneralError.notPlayer));
            return 0;
        }

        ItemStack book = new ItemStack(Items.WRITABLE_BOOK);

        String bookTemplate = weatherTemplate(templateType);

        WritableBookContent content = new WritableBookContent(List.of(Filterable.passThrough(bookTemplate)));
        book.set(DataComponents.WRITABLE_BOOK_CONTENT, content);

        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }

        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_template").append(" " + templateType).withColor(General.Color.SUCCESS));

        return 1;
    }

    static String sceneTemplate() {
        return """
            id: "FILE NAME"
            type: "simple"
            
            default:
              interval: 20
            
            dialogs:
              - text: "CONTENT HERE..."
            """;
    }

    static String lootTemplate() {
        return """
            id: "FILE NAME?"
            
            pools:
              - rolls: 1
                entries:
                  - type: item
                    id: "minecraft:iron_ingot"
                    weight: 3
                    functions:
                      - function: set_count
                        count:
                          min: 1
                          max: 4
                  - type: item
                    id: "minecraft:gold_ingot"
                    weight: 1
                conditions:
                  - condition: random_chance
                    chance: 0.5
            """;
    }

    static String weatherTemplate(String templateType) {
        return switch (templateType) {
            case "fog" -> """
                # The register name of a weather.
                id: "SIMPLE_FOG"
                # Base on the support of weather type.
                # Vanilla mod support: fog | colored_rain | wind | ...
                type: fog
                
                # General arguments.
                duration:
                  min: 200
                  max: 400
                stillness:
                  min: 200
                  max: 400
                
                # Special arguments for fog weather.
                color: 0xD95D5D
                distance:
                  min: 8
                  max: 128
                # This will increase the distance base on minimum distance
                # (non-positive will use min distance).
                height_offsets:
                  64: 2
                  128: 1
                  192: 0.5
                """;
            default -> "";
        };
    }
}
