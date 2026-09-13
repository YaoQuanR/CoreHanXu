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

        String bookTemplate = "id: \"FILE NAME?\"\ntype: \"simple\"\n\ndefault:\n  interval: 20\n\ndialogs:\n  - text: \"CONTENT HERE...\"";

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

        String bookTemplate = CommandMisc.lootTemplate();

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

        String bookTemplate = CommandMisc.weatherTemplate(templateType);

        WritableBookContent content = new WritableBookContent(List.of(Filterable.passThrough(bookTemplate)));
        book.set(DataComponents.WRITABLE_BOOK_CONTENT, content);

        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }

        MessagePublisher.sendSystemMessage(context, Component.translatable("commands.chx.weather_template").append(" " + templateType).withColor(General.Color.SUCCESS));

        return 1;
    }
}
