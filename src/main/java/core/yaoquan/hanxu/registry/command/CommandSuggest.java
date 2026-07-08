package core.yaoquan.hanxu.registry.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import core.yaoquan.hanxu.api.AttributeHolder;
import core.yaoquan.hanxu.api.TimeHolder;
import core.yaoquan.hanxu.util.Resolver;
import core.yaoquan.hanxu.util.YamlReader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
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
        builder.suggest("-global");
        builder.suggest("-temporary");
        builder.suggest("-me");

        // Suggest player id.
        CommandSourceStack source = context.getSource();
        if (source.getEntity() instanceof ServerPlayer) {
            for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
                builder.suggest(player.getName().getString());
            }
        }

        return builder.buildFuture();
    }

    // Add bare player id suggestion.
    static <S> CompletableFuture<Suggestions> suggestPlayer(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("-me");

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
        builder.suggest("end_behavior");

        return builder.buildFuture();
    }

    // Add template timer suggestion.
    static <S> CompletableFuture<Suggestions> suggestTemplateTimer(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (String id : TimeHolder.getAllTemplateIds()) {
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

    // Add instance timer suggestion.
    static <S> CompletableFuture<Suggestions> suggestInstanceTimer(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String masterString;
        try {
            masterString = StringArgumentType.getString(context, "master_id");
        }
        catch (IllegalArgumentException e) {
            return builder.buildFuture();
        }

        UUID masterId = Resolver.resolveTargetUUID(context, masterString);

        if (masterId == null) {
            return builder.buildFuture();
        }

        for (String id : TimeHolder.getAllInstanceIds(masterId)) {
            builder.suggest(id);
        }
        return builder.buildFuture();
    }

    // For scene suggestion.
    static <S> CompletableFuture<Suggestions> suggestScene(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<Path> sceneFiles = YamlReader.listOut("scene");
        for (Path path : sceneFiles) {
            String fileName = path.getFileName().toString().replace(".yaml", "");
            builder.suggest(fileName);
        }

        return builder.buildFuture();
    }

    // For scene create to save path suggestion.
    static <S> CompletableFuture<Suggestions> suggestSavePath(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("world");
        builder.suggest("global");

        return builder.buildFuture();
    }

    // For YAML attribute suggestion.
    static <S> CompletableFuture<Suggestions> suggestYamlAttribute(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<Path> attributeFiles = YamlReader.listOut("attribute");
        for (Path path : attributeFiles) {
            String fileName = path.getFileName().toString().replace(".yaml", "");
            builder.suggest(fileName);
        }

        return builder.buildFuture();
    }

    // For all attribute suggestion.
    static <S> CompletableFuture<Suggestions> suggestAllAttribute(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (String id : AttributeHolder.getApiAttributes().keySet()) {
            builder.suggest("\"" + id + "\"");
        }

        for (String id : AttributeHolder.getCommandAttributes().keySet()) {
            builder.suggest(id);
        }

        return builder.buildFuture();
    }

    // For attribute direction detection suggestion.
    static <S> CompletableFuture<Suggestions> suggestAttributeDirection(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("up");
        builder.suggest("down");
        builder.suggest("flex");
        builder.suggest("point");

        return builder.buildFuture();
    }
}
