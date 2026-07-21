package core.yaoquan.hanxu.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.CoreHanXuClient;
import core.yaoquan.hanxu.api.define.Error;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.Converter;
import core.yaoquan.hanxu.util.JsonReader;
import core.yaoquan.hanxu.util.YamlReader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static core.yaoquan.hanxu.api.define.Error.returnCodeError;

/**
 * Loot system API
 * @since 0.6.0 (Internal Development)
 */
public class LootHolder {
    public static class LootTableData {
        public String id;
        public List<Pool> pools;
    }

    public static class Pool {
        public Roll roll;
        public Integer bonusRoll = null;
        public List<LootEntry> lootEntries;
        public List<LootCondition> lootConditions;
    }

    public static class Roll {
        public int minimum;
        public int maximum;
        public boolean rangeMode;

        // Fixed value.
        public static Roll fixed(int value) {
            Roll roll = new Roll();
            roll.minimum = roll.maximum = value;
            roll.rangeMode = false;
            return roll;
        }

        // Range of chances.
        public static Roll range(int minimum, int maximum) {
            Roll roll = new Roll();
            roll.minimum = Math.min(minimum, maximum);
            roll.maximum = Math.max(minimum, maximum);
            roll.rangeMode = true;
            return roll;
        }
    }

    public static class LootEntry {
        public String type;                         // Vanilla feature: Define "item", "loot_table" or "empty"(Nothing).
        public String id;                           // Vanilla feature: Item id or loot table id.
        public int weight = 1;                      // Vanilla feature: Affects possibilities of choose.
        public List<LootFunction> lootFunctions;    // Vanilla feature: Post-processing of selected entry.
        public List<LootCondition> lootConditions;  // Vanilla feature: Conditions.
    }

    // Define when this poll available.
    public static class LootCondition {
        public String condition;
        public Map<String, Object> parameters;

        public LootItemCondition toVanillaCondition() {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(condition);
            if (condition == null) {
                return null;
            }

            switch (condition) {
                case "minecraft:random_chance", "random_chance" -> {
                    double chance = parameters != null?
                            ((Number) parameters.getOrDefault("chance", 1.0)).doubleValue() : 1.0;
                    return LootItemRandomChanceCondition.randomChance((float) chance).build();
                }
                case "minecraft:survives_explosion", "survives_explosion" -> {
                    return ExplosionCondition.survivesExplosion().build();
                }
                case "minecraft:killed_by_player", "killed_by_player" -> {
                    return LootItemKilledByPlayerCondition.killedByPlayer().build();
                }
                default -> {
                    return null;
                }
            }
        }
    }

    // Define the post-processing of selected entry.
    public static class LootFunction {
        public String function;
        public Map<String, Object> parameters;
        public List<LootCondition> lootConditions;
    }

    // Vanilla table cache.
    private static final Map<ResourceLocation, LootTableData> vanillaTableCache = new ConcurrentHashMap<>();

    public static LootTableData loadLootTable(String fileName) throws IOException {
        Map<String, Object> rawData = null;
        Exception lastException = null;

        if (fileName.contains(":")) {
            return null;
        }

        if (fileName.endsWith(".yaml")) {
            fileName = fileName.replace(".yaml", "");
            try {
                rawData = YamlReader.read("loot", fileName);
            }
            catch (IOException e) {
                lastException = e;
            }
        }
        else if (fileName.endsWith(".json")) {
            fileName = fileName.replace(".json", "");
            try {
                rawData = JsonReader.read("loot", fileName);
            }
            catch (IOException e) {
                lastException = e;
            }
        }
        else {
            fileName = fileName.split("\\.", 2)[0];
            try {
                rawData = YamlReader.read("loot", fileName);
            }
            catch (FileNotFoundException e) {
                lastException = e;
                try {
                    rawData = JsonReader.read("loot", fileName);
                }
                catch (FileNotFoundException e2) {
                    lastException = e2;
                }
            }
            catch (IOException e) {
                lastException = e;
            }
        }

        if (rawData == null) {
            throw new IOException("[HX] Loot table not found: " + fileName, lastException);
        }

        LootTableData data = parseLootTableData(rawData);

        if (data.id == null || !data.id.equals(fileName)) {
            throw new IOException(returnCodeError(Error.CodeError.mismatchFileElement) + fileName + " ≠ " + data.id);
        }

        CoreHanXu.LOGGER.info("[HX] Loot table loaded: {}", fileName);

        return data;
    }

    public static LootTableData loadFromVanilla(ResourceLocation id, LootTable vanillaTable) {
        if (vanillaTableCache.containsKey(id)) {
            return vanillaTableCache.get(id);
        }

        // Build new.
        LootTableData data = new LootTableData();
        data.id = id.toString();
        data.pools = new ArrayList<>();

        // Translation.
        try {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

            if (server == null) {
                throw new Exception();
            }

            RegistryAccess registryAccess = server.registryAccess();
            var ops = registryAccess.createSerializationContext(JsonOps.INSTANCE);

            DataResult<JsonElement> result = LootTable.DIRECT_CODEC.encodeStart(ops, vanillaTable);

            JsonElement jsonElement = result.getOrThrow();

            // Then storage as map element.
            Gson gson = new Gson();
            @SuppressWarnings("unchecked")
            Map<String, Object> rawData = gson.fromJson(jsonElement, Map.class);

            // Then parse.
            LootTableData parsedData = parseLootTableData(rawData);
            data.pools = parsedData.pools != null? parsedData.pools: Collections.emptyList();
        }
        catch (Exception e) {
            data.pools = Collections.emptyList();
        }

        vanillaTableCache.put(id, data);

        return data;
    }

    public static Set<String> getRegisteredTableIds() {
        Set<String> tableIds = new HashSet<>();

        for (var path : YamlReader.listOut("loot")) {
            String fileName = path.getFileName().toString();
            if (fileName.endsWith(".yaml")) {
                tableIds.add(path.getFileName().toString());
            }
        }

        for (var path : JsonReader.listOut("loot")) {
            String fileName = path.getFileName().toString();
            if (fileName.endsWith(".json")) {
                tableIds.add(path.getFileName().toString());
            }
        }

        return tableIds;
    }

    public static List<ItemStack> generateItemList(LootTableData data, Random random, float luck, boolean ignoreCondition) {
        if (data == null || data.pools == null || data.pools.isEmpty()) {
            return Collections.emptyList();
        }

        List<ItemStack> items = new ArrayList<>();

        for (Pool pool : data.pools) {
            if (!doesConditionSatisfied(pool.lootConditions, random) && !ignoreCondition) {
                continue;
            }

            int roll = 1;
            if (pool.roll != null) {
                if (pool.roll.rangeMode) {
                    roll = pool.roll.minimum + random.nextInt(pool.roll.maximum - pool.roll.minimum + 1);
                }
                else {
                    roll = pool.roll.minimum;
                }
            }

            if (pool.bonusRoll != null && pool.bonusRoll > 0) {
                int bonus = ((Number) (pool.bonusRoll * (1.0 + luck))).intValue();
                roll += Math.max(0, bonus);
            }

            for (int i = 0; i < roll; i++) {
                LootEntry entry = selectWeightedEntry(pool.lootEntries, random);
                if (entry == null) {
                    continue;
                }

                if (!doesConditionSatisfied(entry.lootConditions, random) && !ignoreCondition) {
                    continue;
                }

                ItemStack item = generateItemStack(entry, random, luck, ignoreCondition);
                if (item == null || item.isEmpty()) {
                    continue;
                }

                item = applyFunction(item, entry.lootFunctions, random, luck, ignoreCondition);

                if (item == null || item.isEmpty()) {
                    continue;
                }

                items.add(item);
            }
        }

        return items;
    }

    public static boolean sendItemToPlayer(ServerPlayer player, String tableId, boolean ignoreCondition, boolean sendFirstItem) {
        LootTableData data = returnLootTableData(tableId);

        if (data == null) {
            return false;
        }

        List<ItemStack> items = generateItemList(data, new Random(), player.getLuck(), ignoreCondition);

        if (sendFirstItem) {
            if (!player.addItem(items.getFirst())) {
                player.drop(items.getFirst(), false);
            }
            return true;
        }

        for (ItemStack item : items) {
            if (!player.addItem(item)) {
                player.drop(item, false);
            }
        }

        return true;
    }

    public static boolean sendItemToPlayerWithIgnoreItem(ServerPlayer player, String tableId, String ignoreItemString) {
        LootTableData data = returnLootTableData(tableId);

        if (data == null) {
            return false;
        }

        List<ItemStack> items = generateItemList(data, new Random(), player.getLuck(), true);

        List<String> ignoreItems = Arrays.stream((ignoreItemString.trim().replace("\"", "").split("\\s+")))
                .map(string -> {
                    if (string.contains(":")) {
                        return string;
                    }
                    else {
                        return "minecraft:" + string;
                    }
                })
                .toList();

        if (!ignoreItems.isEmpty()) {
            CoreHanXu.LOGGER.info("[HX] Ignored item from this send item: {}", ignoreItems);
        }

        for (ItemStack item : items) {
            if (ignoreItems.contains(item.getItem().toString())) {
                continue;
            }

            if (!player.addItem(item)) {
                player.drop(item, false);
            }
        }

        return true;
    }

    public static boolean sendItemToContainer(ServerLevel level, BlockPos blockPos, String tableId, boolean ignoredCondition, boolean isSorted) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof Container container)) {
            return false;
        }

        LootTableData data = returnLootTableData(tableId);

        if (data == null) {
            return false;
        }

        List<ItemStack> items = generateItemList(data, new Random(), 0, ignoredCondition);
        if (items.isEmpty()) {
            return true;
        }

        List<Integer> emptySlots = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (container.getItem(i).isEmpty()) {
                emptySlots.add(i);
            }
        }

        if (emptySlots.isEmpty()) {
            return false;
        }

        if (!isSorted) {
            Collections.shuffle(emptySlots, new Random());
        }

        int itemIndex = 0;
        int slotIndex = 0;

        for (; itemIndex < items.size() && slotIndex < emptySlots.size(); itemIndex++, slotIndex++) {
            ItemStack item = items.get(itemIndex);
            int slot = emptySlots.get(slotIndex);
            container.setItem(slot, item);
        }

        return itemIndex >= items.size();
    }

    public static boolean doesFileLootTableExists(String tableId) {
        try {
            YamlReader.read("loot", tableId);
            return true;
        }
        catch (FileNotFoundException e) {
            try {
                JsonReader.read("loot", tableId);
                return true;
            }
            catch (IOException ignored) {}
        }
        catch (IOException ignored) {}

        return false;
    }

    public static List<Component> readLootTable(String tableId) {
        List<Component> lines = new ArrayList<>();

        LootTableData data = returnLootTableData(tableId);

        if (data == null) {
            lines.add(Component.translatable("api.core_hanxu.loot.empty_table")
                    .append(Component.literal(" " + tableId))
                    .withColor(General.Color.FAILURE)
            );
            return lines;
        }

        // Else readable.
        lines.add(Component.translatable("api.core_hanxu.loot.table_title")
                .append(Component.literal(" " + tableId))
                .withColor(General.Color.TITLE)
        );

        // Pool.
        int poolSize = data.pools != null? data.pools.size(): 0;
        lines.add(Component.translatable("api.core_hanxu.loot.pool_size")
                .append(Component.literal(" " + poolSize))
                .withColor(General.Color.TITLE)
        );

        // Nothing inside pool.
        if (data.pools == null || data.pools.isEmpty()) {
            lines.add(Component.translatable("api.core_hanxu.loot.empty_pool")
                    .withColor(General.Color.CONTENT)
            );
            return lines;
        }

        // Else inside pool and add lines.
        for (int i = 0; i < data.pools.size(); i++) {
            Pool pool = data.pools.get(i);

            // Roll information.
            String rollInformation;
            if (pool.roll == null) {
                rollInformation = "?";
            }
            else {
                rollInformation = pool.roll.rangeMode? (pool.roll.minimum + " ~ " + pool.roll.maximum) : String.valueOf(pool.roll.minimum);
            }
            String bonusRollInformation = pool.bonusRoll != null && pool.bonusRoll > 0? (" (+" + pool.bonusRoll + ")") : "";

            lines.add(Component.translatable("api.core_hanxu.loot.pool")
                    .append(Component.literal(" " + (i + 1) + " "))
                    .append(Component.translatable("api.core_hanxu.loot.roll"))
                    .append(Component.literal(" " + rollInformation + bonusRollInformation))
                    .withColor(General.Color.TITLE)
            );

            // Entry information.
            if (pool.lootEntries != null && !pool.lootEntries.isEmpty()) {
                lines.add(Component.translatable("api.core_hanxu.loot.entry_size")
                        .append(Component.literal(" " + pool.lootEntries.size()))
                        .withColor(General.Color.TITLE)
                );

                for (LootEntry entry : pool.lootEntries) {
                    lines.add(Component.translatable("api.core_hanxu.loot.entry")
                            .append(Component.literal(" " + entry.id + " (" + entry.type + ") "))
                            .append(Component.translatable("api.core_hanxu.loot.weight"))
                            .append(Component.literal(" " + entry.weight))
                            .withColor(General.Color.CONTENT)
                    );
                }
            }

            // Condition information.
            if (pool.lootConditions != null && !pool.lootConditions.isEmpty()) {
                lines.add(Component.translatable("api.core_hanxu.loot.condition_size")
                        .append(Component.literal(" " + pool.lootConditions.size()))
                        .withColor(General.Color.TITLE)
                );

                for (LootCondition condition : pool.lootConditions) {
                    lines.add(Component.translatable("api.core_hanxu.loot.condition")
                            .append(Component.literal(" " + condition.condition + " : " + condition.parameters))
                            .withColor(General.Color.CONTENT)
                    );
                }
            }
        }

        return lines;
    }

    public static String readLootTableAsTranslatedString(String tableId) {
        List<Component> lines = readLootTable(tableId);
        StringBuilder stringPackage = new StringBuilder();

        for (Component line : lines) {
            String thisLine = (line.getString() + "\n");
            stringPackage.append(thisLine);
        }

        return stringPackage.toString();
    }

    public static String readLootTableAsString(String tableId) {
        StringBuilder stringPackage = new StringBuilder();

        LootTableData data = returnLootTableData(tableId);

        if (data == null) {
            stringPackage.append("[HX] Empty table: ").append(tableId);
            return stringPackage.toString();
        }

        // Else readable.
        stringPackage.append("[HX] Loot table found: ").append(tableId).append("\n");

        // Pool.
        int poolSize = data.pools != null? data.pools.size(): 0;
        stringPackage.append("  Pools: ").append(poolSize).append("\n");

        // Nothing inside pool.
        if (data.pools == null || data.pools.isEmpty()) {
            stringPackage.append("  -> Nothing...");
            return stringPackage.toString();
        }

        // Else inside pool and add lines.
        for (int i = 0; i < data.pools.size(); i++) {
            Pool pool = data.pools.get(i);

            // Roll information.
            String rollInformation;
            if (pool.roll == null) {
                rollInformation = "?";
            }
            else {
                rollInformation = pool.roll.rangeMode? (pool.roll.minimum + " ~ " + pool.roll.maximum) : String.valueOf(pool.roll.minimum);
            }
            String bonusRollInformation = pool.bonusRoll != null && pool.bonusRoll > 0? (" (+" + pool.bonusRoll + ")") : "";

            stringPackage.append("  -> Pool:").append(" ").append(i + 1).append(" , with roll: ").append(rollInformation).append(bonusRollInformation).append("\n");

            // Entry information.
            if (pool.lootEntries != null && !pool.lootEntries.isEmpty()) {
                stringPackage.append("    Entries: ").append(pool.lootEntries.size()).append("\n");

                for (LootEntry entry : pool.lootEntries) {
                    stringPackage.append("    -> Entry: ").append(entry.id).append(" (").append(entry.type).append(") , with weight: ").append(entry.weight).append("\n");
                }
            }

            // Condition information.
            if (pool.lootConditions != null && !pool.lootConditions.isEmpty()) {
                stringPackage.append("    Conditions: ").append(pool.lootConditions.size()).append("\n");

                for (LootCondition condition : pool.lootConditions) {
                    stringPackage.append("    => Condition: ").append(condition.condition).append(" : ").append(condition.parameters).append("\n");
                }
            }
        }

        return stringPackage.toString();
    }

    @SuppressWarnings("unchecked")
    private static LootTableData parseLootTableData(Map<String, Object> rawData) {
        LootTableData data = new LootTableData();
        data.id = (String) rawData.get("id");

        // Poll.
        List<Map<String, Object>> rawPools = (List<Map<String, Object>>) rawData.get("pools");
        if (rawPools != null) {
            data.pools = new ArrayList<>();

            for (Map<String, Object> rawPool : rawPools) {
                // Roll (Fixed value or range).
                Pool pool = new Pool();

                Object rollObject = rawPool.get("rolls");
                // Roll: Value.
                if (rollObject instanceof Number) {
                    pool.roll = Roll.fixed(((Number) rollObject).intValue());
                }
                // Roll: Range.
                else if (rollObject instanceof Map) {
                    Map<String, Number> range = (Map<String, Number>) rollObject;
                    pool.roll = Roll.range(range.getOrDefault("min", 1).intValue(), range.getOrDefault("max", 1).intValue());
                }

                // Roll bonus.
                if (rawPool.containsKey("bonus_roll")) {
                    pool.bonusRoll = ((Number) rawPool.get("bonus_roll")).intValue();
                }

                // Entries.
                List<Map<String, Object>> rawEntries = (List<Map<String, Object>>) rawPool.get("entries");
                if (rawEntries != null) {
                    pool.lootEntries = new ArrayList<>();

                    // Entry.
                    for (Map<String, Object> rawEntry : rawEntries) {
                        LootEntry entry = new LootEntry();

                        entry.type = (String) rawEntry.getOrDefault("type", "item");
                        entry.id = (String) rawEntry.get("id");
                        if (entry.id == null) {
                            entry.id = (String) rawEntry.get("name");
                        }
                        entry.weight = ((Number) rawEntry.getOrDefault("weight", 1)).intValue();

                        entry.lootFunctions = parseFunctions(rawEntry);
                        entry.lootConditions = parseConditions(rawEntry);

                        pool.lootEntries.add(entry);
                    }
                }

                pool.lootConditions = parseConditions(rawPool);

                data.pools.add(pool);
            }
        }

        return data;
    }

    @SuppressWarnings("unchecked")
    private static List<LootCondition> parseConditions(Map<String, Object> rawData) {
        List<Map<String, Object>> rawConditions = (List<Map<String, Object>>) rawData.get("conditions");

        if (rawConditions == null || rawConditions.isEmpty()) {
            return Collections.emptyList();
        }

        List<LootCondition> conditions = new ArrayList<>();

        for (Map<String, Object> rawCondition : rawConditions) {
            LootCondition condition = new LootCondition();
            condition.condition = (String) rawCondition.get("condition");
            // Collect remains for parameters.
            condition.parameters = new HashMap<>(rawCondition);
            // Remove condition id field.
            condition.parameters.remove("condition");

            conditions.add(condition);
        }

        return conditions;
    }

    @SuppressWarnings("unchecked")
    private static List<LootFunction> parseFunctions(Map<String, Object> rawData) {
        List<Map<String, Object>> rawFunctions = (List<Map<String, Object>>) rawData.get("functions");

        if (rawFunctions == null || rawFunctions.isEmpty()) {
            return Collections.emptyList();
        }

        List<LootFunction> functions = new ArrayList<>();

        for (Map<String, Object> rawFunction : rawFunctions) {
            LootFunction function = new LootFunction();
            function.function = (String) rawFunction.get("function");
            // Collect remains for parameters.
            function.parameters = new HashMap<>(rawFunction);
            // Remove non-parameter data.
            function.parameters.remove("function");
            function.parameters.remove("conditions");

            function.lootConditions = parseConditions(rawFunction);

            functions.add(function);
        }

        return functions;
    }

    private static ItemStack generateItemStack(LootEntry entry, Random random, float luck, boolean ignoreCondition) {
        switch (entry.type) {
            case "minecraft:item", "item" -> {
                ResourceLocation itemId = ResourceLocation.tryParse(entry.id);
                if (itemId != null) {
                    Optional<Holder.Reference<Item>> optionalItemReference = BuiltInRegistries.ITEM.get(itemId);
                    if (optionalItemReference.isPresent()) {
                        Item item = optionalItemReference.get().value();

                        return new ItemStack(item);
                    }
                }
            }
            case "minecraft:loot_table", "loot_table" -> {
                LootTableData referenceTable;
                try {
                    referenceTable = loadLootTable(entry.id);
                }
                catch (IOException e) {
                    referenceTable = null;
                }

                if (referenceTable != null) {
                    List<ItemStack> subItems = generateItemList(referenceTable, random, luck, ignoreCondition);
                    if (!subItems.isEmpty()) {
                        return subItems.getFirst();
                    }
                }
            }
        }

        // Else.
        return ItemStack.EMPTY;
    }

    private static boolean doesConditionSatisfied(List<LootCondition> conditions, Random random) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }

        for (LootCondition condition : conditions) {
            switch (condition.condition) {
                case "minecraft:random_chance", "random_chance" -> {
                    double chance = condition.parameters != null?
                            ((Number) condition.parameters.getOrDefault("chance", 1.0)).floatValue() : 1.0;
                    if (random.nextDouble() >= chance) {
                        return false;
                    }
                }
                // Default -> continue.
            }
        }

        return true;
    }

    private static LootEntry selectWeightedEntry(List<LootEntry> entries, Random random) {
        if (entries == null || entries.isEmpty()) {
            return null;
        }

        int totalWeight = 0;
        for (LootEntry entry : entries) {
            totalWeight += entry.weight;
        }

        if (totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        int rangeBound = 0;

        for (LootEntry entry : entries) {
            rangeBound += entry.weight;
            if (roll < rangeBound) {
                return entry;
            }
        }

        return entries.getLast();
    }

    private static ItemStack applyFunction(ItemStack item, List<LootFunction> functions, Random random, float luck, boolean ignoreCondition) {
        if (functions == null || functions.isEmpty()) {
            return null;
        }

        for (LootFunction function : functions) {
            if (!doesConditionSatisfied(function.lootConditions, random) && !ignoreCondition) {
                continue;
            }

            String functionName = function.function;
            Map<String, Object> parameters = function.parameters;

            switch (functionName) {
                case "minecraft:set_count", "set_count" -> {
                    Object count = parameters != null? parameters.get("count") : null;
                    if (count instanceof Number) {
                        item.setCount(((Number) count).intValue());
                    }
                    else if (count instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Number> range = (Map<String, Number>) count;
                        int minimum = range.getOrDefault("min", 1).intValue();
                        int maximum = range.getOrDefault("max", 1).intValue();
                        item.setCount(minimum + random.nextInt(maximum - minimum + 1));
                    }
                }
                case "minecraft:set_damage", "set_damage" -> {
                    Object damage = parameters != null? parameters.get("damage") : null;
                    double damageValue;
                    if (damage instanceof Number) {
                        damageValue = ((Number) damage).doubleValue();
                    }
                    else {
                        continue;
                    }

                    int maximumDamage = item.getMaxDamage();
                    int newDamage = ((Number) (maximumDamage * damageValue)).intValue();
                    item.setDamageValue(Math.min(newDamage, maximumDamage - 1));
                }
                case "minecraft:set_name", "set_name" -> {
                    String name = parameters != null? (String) parameters.get("name") : null;
                    if (name == null) {
                        continue;
                    }

                    Component component;
                    if ((name.startsWith("{")) && (name.endsWith("}")) || (name.startsWith("[")) && (name.endsWith("]"))) {
                        component = Converter.convertFromJsonToComponent(name);
                    }
                    else {
                        component = Component.literal(name);
                    }
                    item.set(DataComponents.CUSTOM_NAME, component);
                }
                case "minecraft:set_custom_model_data", "set_custom_model_data" -> {
                    @SuppressWarnings("unchecked")
                    List<Float> floats = parameters != null && parameters.get("floats") != null? (List<Float>) parameters.get("floats") : List.of();
                    @SuppressWarnings("unchecked")
                    List<Boolean> flags = parameters != null && parameters.get("flags") != null? (List<Boolean>) parameters.get("flags") : List.of();
                    @SuppressWarnings("unchecked")
                    List<String> strings = parameters != null && parameters.get("strings") != null? (List<String>) parameters.get("strings") : List.of();
                    @SuppressWarnings("unchecked")
                    List<Number> rawColors = parameters != null && parameters.get("colors") != null? (List<Number>) parameters.get("colors") : List.of();

                    List<Integer> colors = new ArrayList<>();
                    for (Number rawColor : rawColors) {
                        colors.add(rawColor.intValue());
                    }
                    
                    if (!floats.isEmpty() || !flags.isEmpty() || !strings.isEmpty() || !colors.isEmpty()) {
                        item.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(floats, flags, strings, colors));
                    }
                }
                case "minecraft:set_lore", "set_lore" -> {
                    Object lore = parameters != null? parameters.get("lore") : null;
                    if (lore instanceof List<?> rawList) {
                        List<Component> loreComponents = new ArrayList<>();

                        for (Object line : rawList) {
                            if (line instanceof String name) {
                                Component component;
                                if ((name.startsWith("{")) && (name.endsWith("}")) || (name.startsWith("[")) && (name.endsWith("]"))) {
                                    component = Converter.convertFromJsonToComponent(name);
                                }
                                else {
                                    component = Component.literal(name);
                                }
                                loreComponents.add(component);
                            }
                        }

                        if (!loreComponents.isEmpty()) {
                            item.set(DataComponents.LORE, new ItemLore(loreComponents));
                        }
                    }
                }
                case "minecraft:enchant_randomly", "enchant_randomly" -> {
                    if (!item.isEnchantable()) {
                        continue;
                    }

                    List<Holder.Reference<Enchantment>> applicableEnchantments = returnApplicableEnchantments(item);
                    if (applicableEnchantments.isEmpty()) {
                        continue;
                    }

                    int level = 1 + random.nextInt(30);
                    Holder.Reference<Enchantment> reference = applicableEnchantments.get(random.nextInt(applicableEnchantments.size()));
                    item.enchant(reference, level);
                }
                case "minecraft:set_enchantments", "set_enchantments" -> {
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    if (server == null) {
                        continue;
                    }

                    RegistryAccess registryAccess = server.registryAccess();
                    Registry<Enchantment> enchantments = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);

                    ItemEnchantments.Mutable mutableEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

                    Object field = parameters != null? parameters.get("enchantments") : null;
                    @SuppressWarnings("unchecked")
                    Map<String, Object> enchantmentField = field != null? (Map<String, Object>) field : null;

                    if (enchantmentField != null) {
                        for (Map.Entry<String, Object> entry : enchantmentField.entrySet()) {
                            String id = entry.getKey();
                            Object level = entry.getValue();
                            if (level == null) {
                                continue;
                            }

                            int intLevel = ((Number) level).intValue();

                            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, Objects.requireNonNull(ResourceLocation.tryParse(id)));
                            Optional<Holder.Reference<Enchantment>> reference = enchantments.get(key);
                            reference.ifPresent(enchantmentReference -> mutableEnchantments.set(enchantmentReference, intLevel));
                        }
                    }

                    ItemEnchantments finalEnchantments = mutableEnchantments.toImmutable();
                    if (!finalEnchantments.isEmpty()) {
                        item.set(DataComponents.ENCHANTMENTS, finalEnchantments);
                    }
                }
                case "minecraft:enchant_with_levels", "enchant_with_levels" -> {
                    if (!item.isEnchantable()) {
                        continue;
                    }

                    Object level = parameters != null? parameters.get("levels") : null;
                    int levelValue = 1;

                    if (level instanceof Number) {
                        levelValue = ((Number) level).intValue();
                    }
                    else if (level instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Number> range = (Map<String, Number>) level;
                        int minimum = range.getOrDefault("min", 1).intValue();
                        int maximum = range.getOrDefault("max", 30).intValue();

                        if (maximum < minimum) {
                            int temp = minimum;
                            minimum = maximum;
                            maximum = temp;
                        }

                        levelValue = minimum + random.nextInt(maximum - minimum + 1);
                    }

                    int enchantmentLevel = 1 + random.nextInt(Math.min(levelValue, 30));

                    List<Holder.Reference<Enchantment>> applicableEnchantments = returnApplicableEnchantments(item);
                    if (applicableEnchantments.isEmpty()) {
                        continue;
                    }

                    Holder.Reference<Enchantment> reference = applicableEnchantments.get(random.nextInt(applicableEnchantments.size()));
                    item.enchant(reference, enchantmentLevel);
                }
                case "minecraft:looting_enchant", "looting_enchant" -> {
                    if (luck > 0) {
                        int extraCount = ((Number) (luck * 0.5)).intValue();
                        if (extraCount > 0) {
                            item.setCount(item.getCount() + extraCount);
                        }
                    }
                }
                case "minecraft:furnace_smelt", "furnace_smelt" -> {
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    if (server == null) {
                        continue;
                    }

                    RecipeManager recipeManager = server.getRecipeManager();
                    SingleRecipeInput input = new SingleRecipeInput(item);
                    Level level = server.overworld();

                    var recipes = recipeManager.getRecipeFor(RecipeType.SMELTING, input, level);
                    if (recipes.isPresent()) {
                        var recipeHolder = recipes.get();
                        ItemStack result = recipeHolder.value().assemble(input, level.registryAccess());
                        if (!result.isEmpty()) {
                            int count = item.getCount();
                            item = new ItemStack(result.getItem(), count);
                        }
                    }
                }
                case "minecraft:explosion_decay", "explosion_decay" -> {
                    double decayChance = parameters != null?
                            ((Number) parameters.getOrDefault("chance", 0.5)).doubleValue() : 0.5;
                    if (random.nextDouble() < decayChance) {
                        item.setCount(0);
                    }
                }
                case "minecraft:limit_count", "limit_count" -> {
                    Object limit = parameters != null? parameters.get("limit") : null;
                    if (limit instanceof Number) {
                        int limitValue = ((Number) limit).intValue();
                        if (item.getCount() > limitValue) {
                            item.setCount(limitValue);
                        }
                    }
                }
                // Default -> continue.
            }
        }

        return item;
    }

    private static List<Holder.Reference<Enchantment>> returnApplicableEnchantments(ItemStack item) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return List.of();
        }

        RegistryAccess registryAccess = server.registryAccess();
        Registry<Enchantment> enchantments = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);

        List<Holder.Reference<Enchantment>> referenceEnchantment = new ArrayList<>();
        for (ResourceKey<Enchantment> key : enchantments.registryKeySet()) {
            Optional<Holder.Reference<Enchantment>> reference = enchantments.get(key);
            if (reference.isPresent()) {
                Enchantment enchantment = reference.get().value();
                if (enchantment.definition().supportedItems().contains(item.getItemHolder())) {
                    referenceEnchantment.add(reference.get());
                }
            }
        }

        return referenceEnchantment;
    }

    private static LootTableData returnLootTableData(String tableId) {
        // Read from file.
        LootTableData data;
        try {
            data = loadLootTable(tableId);
        }
        catch (IOException e) {
            data = null;
        }

        // Read from vanilla.
        if (data == null) {
            ResourceLocation idLocation = ResourceLocation.tryParse(tableId);
            if (idLocation == null) {
                return null;
            }

            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) {
                return null;
            }

            ResourceKey<LootTable> tableKey = ResourceKey.create(Registries.LOOT_TABLE, idLocation);
            LootTable table = server.reloadableRegistries().getLootTable(tableKey);

            if (table != LootTable.EMPTY) {
                data = loadFromVanilla(idLocation, table);
            }
        }

        return data;
    }
}
