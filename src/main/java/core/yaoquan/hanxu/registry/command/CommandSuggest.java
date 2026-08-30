package core.yaoquan.hanxu.registry.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import core.yaoquan.hanxu.api.*;
import core.yaoquan.hanxu.util.Resolver;
import core.yaoquan.hanxu.util.YamlReader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.Objective;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

class CommandSuggest {
    // Add unit suggestion.
    static CompletableFuture<Suggestions> suggestUnit(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("tick");
        builder.suggest("second");
        builder.suggest("minute");
        builder.suggest("hour");

        return builder.buildFuture();
    }

    // Add UUID suggestion.
    static CompletableFuture<Suggestions> suggestUUIDOwner(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("-global");
        builder.suggest("-temporary");
        builder.suggest("-me");
        builder.suggest("-random");
        builder.suggest("-nearest");

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
    static CompletableFuture<Suggestions> suggestPlayer(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("-me");
        builder.suggest("-random");
        builder.suggest("-nearest");

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
    static CompletableFuture<Suggestions> suggestReadCategory(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("remaining_time");
        builder.suggest("initial_time");
        builder.suggest("state");
        builder.suggest("end_behavior");

        return builder.buildFuture();
    }

    // Add template timer suggestion.
    static CompletableFuture<Suggestions> suggestTemplateTimer(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (String id : TimeHolder.getAllTemplateIds()) {
            builder.suggest(id);
        }

        return builder.buildFuture();
    }

    // Add instance timer suggestion.
    static CompletableFuture<Suggestions> suggestInstanceTimer(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
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
    static CompletableFuture<Suggestions> suggestScene(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<Path> sceneFiles = YamlReader.listOut("scene");
        for (Path path : sceneFiles) {
            String fileName = path.getFileName().toString().replace(".yaml", "");
            builder.suggest(fileName);
        }

        return builder.buildFuture();
    }

    // For scene create to save path suggestion.
    static CompletableFuture<Suggestions> suggestSavePath(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("world");
        builder.suggest("global");

        return builder.buildFuture();
    }

    // For YAML attribute suggestion.
    static CompletableFuture<Suggestions> suggestYamlAttribute(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<Path> attributeFiles = YamlReader.listOut("attribute");
        for (Path path : attributeFiles) {
            String fileName = path.getFileName().toString().replace(".yaml", "");
            builder.suggest(fileName);
        }

        return builder.buildFuture();
    }

    // For all attribute suggestion.
    static CompletableFuture<Suggestions> suggestAllAttribute(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (String id : AttributeHolder.getApiAttributes().keySet()) {
            builder.suggest("\"" + id + "\"");
        }

        for (String id : AttributeHolder.getCommandAttributes().keySet()) {
            builder.suggest(id);
        }

        return builder.buildFuture();
    }

    // For attribute direction detection suggestion.
    static CompletableFuture<Suggestions> suggestAttributeDirection(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("up");
        builder.suggest("down");
        builder.suggest("flex");
        builder.suggest("point");

        return builder.buildFuture();
    }

    // For variable suggestion.
    static CompletableFuture<Suggestions> suggestAllVariable(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (String name : VariableHolder.getAllRegisteredVariables()) {
            builder.suggest(name);
        }

        return builder.buildFuture();
    }

    // For variable suggestion with "-all" operation.
    static CompletableFuture<Suggestions> suggestAllVariableWithAll(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (String name : VariableHolder.getAllRegisteredVariables()) {
            builder.suggest(name);
        }

        builder.suggest("-all");

        return builder.buildFuture();
    }

    // For variable suggestion with "-self" operation.
    static CompletableFuture<Suggestions> suggestAllVariableWithSelf(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (String name : VariableHolder.getAllRegisteredVariables()) {
            builder.suggest(name);
        }

        builder.suggest("-self");

        return builder.buildFuture();
    }

    // For variable type suggestion.
    static CompletableFuture<Suggestions> suggestVariableType(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("string");
        builder.suggest("integer");
        builder.suggest("boolean");
        builder.suggest("float");
        builder.suggest("double");
        builder.suggest("long");

        return builder.buildFuture();
    }

    // For variable compare sign suggestion.
    static CompletableFuture<Suggestions> suggestVariableCompareSign(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("\"=\"");
        builder.suggest("\"==\"");
        builder.suggest("\"!=\"");
        builder.suggest("\"≠\"");
        builder.suggest("\"<\"");
        builder.suggest("\"<=\"");
        builder.suggest("\"≤\"");
        builder.suggest("\">\"");
        builder.suggest("\">=\"");
        builder.suggest("\"≥\"");
        builder.suggest("instanceof");
        builder.suggest("contains");
        builder.suggest("length");
        builder.suggest("starts_with");
        builder.suggest("ends_with");

        return builder.buildFuture();
    }

    // For variable if/scoreif/margin_equals action suggestion.
    static CompletableFuture<Suggestions> suggestVariableIfThenAction(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("set");
        builder.suggest("add");
        builder.suggest("reduce");
        builder.suggest("copy_from");
        builder.suggest("copy_to");
        builder.suggest("same");

        return builder.buildFuture();
    }

    // For variable modify suggestion.
    static CompletableFuture<Suggestions> suggestVariableModifyAction(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("set");
        builder.suggest("add");
        builder.suggest("reduce");
        builder.suggest("same");

        return builder.buildFuture();
    }

    // For any scoreboard name suggestion.
    static CompletableFuture<Suggestions> suggestAllScoreboardName(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        MinecraftServer server = context.getSource().getServer();
        ServerScoreboard scoreboard = server.getScoreboard();

        for (Objective objective : scoreboard.getObjectives()) {
            builder.suggest(objective.getName());
        }

        return builder.buildFuture();
    }

    // For file loot table suggestion.
    static CompletableFuture<Suggestions> suggestAllFileLootTable(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Set<String> tableIds = LootHolder.getRegisteredTableIds();

        for (String tableId : tableIds) {
            builder.suggest(tableId);
        }

        return builder.buildFuture();
    }

    // For block position suggestion:
    static CompletableFuture<Suggestions> suggestHitBlockPosition(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return buildHitBlockPositionSuggestion(context, builder, "x");
    }

    static CompletableFuture<Suggestions> suggestHitBlockPositionInYZ(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return buildHitBlockPositionSuggestion(context, builder, "y");
    }

    static CompletableFuture<Suggestions> suggestHitBlockPositionInZ(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return buildHitBlockPositionSuggestion(context, builder, "z");
    }

    private static CompletableFuture<Suggestions> buildHitBlockPositionSuggestion(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder, String currentStage) {
        Player player = context.getSource().getPlayer();
        if (player != null) {
            HitResult hitResult = player.pick(30.0, 0f, false);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockPos position = blockHitResult.getBlockPos();
                switch (currentStage) {
                    case "x" -> {
                        builder.suggest(position.getX());
                        builder.suggest(position.getX() + " " + position.getY() + " " + position.getZ());
                    }
                    case "y" -> {
                        builder.suggest(position.getY());
                        builder.suggest(position.getY() + " " + position.getZ());
                    }
                    case "z" -> builder.suggest(position.getZ());
                }
            }
        }

        return builder.buildFuture();
    }

    // For weather suggestion.
    static CompletableFuture<Suggestions> suggestWeather(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (String id : WeatherHolder.getApiWeatherDefinitions().keySet()) {
            builder.suggest(id);
        }

        for (String id : WeatherHolder.getCommandWeatherDefinitions().keySet()) {
            builder.suggest(id);
        }

        return builder.buildFuture();
    }

    // For dimension suggestion.
    static CompletableFuture<Suggestions> suggestDimension(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        MinecraftServer server = context.getSource().getServer();
        for (ServerLevel level : server.getAllLevels()) {
            builder.suggest("\"" + level.dimension().location() + "\"");
        }

        return builder.buildFuture();
    }

    // For weather type suggestion.
    static CompletableFuture<Suggestions> suggestWeatherType(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (WeatherHolder.WeatherType type : WeatherHolder.WeatherType.values()) {
            builder.suggest(type.name().toLowerCase());
        }

        return builder.buildFuture();
    }
}
