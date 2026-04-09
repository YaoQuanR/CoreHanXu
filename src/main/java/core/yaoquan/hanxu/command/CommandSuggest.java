package core.yaoquan.hanxu.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import core.yaoquan.hanxu.api.TimeHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;

class CommandSuggest {
    // Add unit suggestion.
    static <S> CompletableFuture<Suggestions> suggestUnit(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("tick");
        builder.suggest("second");
        builder.suggest("minute");
        builder.suggest("hour");

        return builder.buildFuture();
    }

    // Add UUID suggestion.
    static <S> CompletableFuture<Suggestions> suggestUUIDOwner(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("0");
        builder.suggest("1");

        // Suggest player id.
        CommandSourceStack source = context.getSource();
        if (source.getEntity() instanceof ServerPlayer) {
            for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
                builder.suggest(player.getName().getString());
            }
        }

        return builder.buildFuture();
    }

    // Add read category suggestion.
    static <S> CompletableFuture<Suggestions> suggestReadCategory(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("remaining_time");
        builder.suggest("initial_time");
        builder.suggest("state");

        return builder.buildFuture();
    }

    // Add template timer suggestion.
    static <S> CompletableFuture<Suggestions> suggestTemplateTimer(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (String id : TimeHolder.returnAllTemplateIds()) {
            builder.suggest(id);
        }

        return builder.buildFuture();
    }

    // Add end behavior suggestion.
    static <S> CompletableFuture<Suggestions> suggestEndBehaviorCategory(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("remind");
        builder.suggest("execute");
        builder.suggest("null");

        return builder.buildFuture();
    }
}
