package core.yaoquan.hanxu.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.define.Error;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.util.Converter;
import core.yaoquan.hanxu.util.JsonReader;
import core.yaoquan.hanxu.util.YamlReader;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

    /// Gain loot table data from YAML/JSON file.
    public static LootTableData loadFromFile(String fileName) throws IOException {
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

    /// Gain loot table data from vanilla.
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

    /**
     * Generate a new item stack list from loot table data.
     * @param data                  Loot table data from {@link #loadFromFile(String)} or {@link #loadFromVanilla(ResourceLocation, LootTable)}.
     * @param random                Java random generator.
     * @param luck                  Luck value that affect chance of item.
     * @param ignoreCondition       Ignore "condition" fields or not.
     * @param guaranteed            Guaranteed all item will be generated (Skip pool logics).
     * @return                      Result of generation: List<\ItemStack>.
     */
    public static List<ItemStack> generateItemList(LootTableData data, Random random, float luck, boolean ignoreCondition, boolean guaranteed) {
        if (data == null || data.pools == null || data.pools.isEmpty()) {
            return Collections.emptyList();
        }

        List<ItemStack> items = new ArrayList<>();

        for (Pool pool : data.pools) {
            if (!doesConditionSatisfied(pool.lootConditions, random) && !ignoreCondition) {
                continue;
            }

            // For guaranteed mode: Always provides item.
            if (guaranteed) {
                for (LootEntry entry : pool.lootEntries) {
                    if (!doesConditionSatisfied(entry.lootConditions, random) && !ignoreCondition) {
                        continue;
                    }

                    ItemStack item = generateItemStack(entry, random, luck, ignoreCondition);
                    if (item == null) {
                        continue;
                    }

                    item = applyFunction(item, entry.lootFunctions, random, luck, ignoreCondition);

                    if (item != null && !item.isEmpty()) {
                        items.add(item);
                    }
                }

                // Skip pool logics.
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

    /**
     * Generate loot and send item to player.
     * @param player                Player that from {@link ServerPlayer}.
     * @param tableId               Loot table id from registered or file table.
     * @param ignoreCondition       Ignore "condition" or not.
     * @param sendFirstItem         Determine if first generated item will be sent.
     * @param guaranteed            Guaranteed all item will be generated (Skip pool logics).
     * @return                      Does the data completed for send to player: boolean.
     */
    public static boolean sendItemToPlayer(ServerPlayer player, String tableId, boolean ignoreCondition, boolean sendFirstItem, boolean guaranteed) {
        LootTableData data = returnLootTableData(tableId);

        if (data == null) {
            return false;
        }

        List<ItemStack> items = generateItemList(data, new Random(), player.getLuck(), ignoreCondition, guaranteed);

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

    /**
     * Generate loot and send item to player. Extra string received for ignoring matched item.
     * @param player                Player that from {@link ServerPlayer}.
     * @param tableId               Loot table id from registered or file table.
     * @param ignoreItemString      String that determine what item should be ignored to send for player.
     *                              Receive item id as "[item_id_n] [item_id_n+1]" which space is split sign.
     *                              If item id not contains "minecraft:", normally used "minecraft:" as prefix.
     * @param guaranteed            Guaranteed all item will be generated (Skip pool logics).
     * @return                      Does the data completed for send to player: boolean.
     */
    public static boolean sendItemToPlayerWithIgnoreItem(ServerPlayer player, String tableId, String ignoreItemString, boolean guaranteed) {
        LootTableData data = returnLootTableData(tableId);

        if (data == null) {
            return false;
        }

        List<ItemStack> items = generateItemList(data, new Random(), player.getLuck(), true, guaranteed);

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

    /**
     * Generate loot and send item to a container.
     * @param level                 Level that from {@link ServerLevel}.
     * @param blockPos              Block position that using format from {@link BlockPos}.
     * @param tableId               Loot table id from registered or file table.
     * @param ignoreCondition       Ignore "condition" or not.
     * @param isSorted              Determine if list is sorted when push item.
     * @param ignoreItemString      String that determine what item should be ignored to send for player.
     *                              Receive item id as "[item_id_n] [item_id_n+1]" which space is split sign.
     *                              If item id not contains "minecraft:", normally used "minecraft:" as prefix.
     * @param guaranteed            Guaranteed all item will be generated (Skip pool logics).
     * @return                      Does the data completed for send to container: boolean.
     */
    public static boolean sendItemToContainer(ServerLevel level, BlockPos blockPos, String tableId, boolean ignoreCondition, boolean isSorted, String ignoreItemString, boolean guaranteed) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof Container container)) {
            return false;
        }

        LootTableData data = returnLootTableData(tableId);

        if (data == null) {
            return false;
        }

        List<ItemStack> items = generateItemList(data, new Random(), 0, ignoreCondition, guaranteed);
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
            CoreHanXu.LOGGER.info("[HX] Container is full, no item replaced: {}", blockPos);
            return true;
        }

        if (!isSorted) {
            Collections.shuffle(emptySlots, new Random());
        }

        List<String> ignoreItems = null;

        if (ignoreItemString != null) {
            ignoreItems = Arrays.stream((ignoreItemString.trim().replace("\"", "").split("\\s+")))
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
                CoreHanXu.LOGGER.info("[HX] Ignored item from this send container: {}", ignoreItems);
            }
        }

        int itemIndex = 0;
        int slotIndex = 0;

        for (; itemIndex < items.size() && slotIndex < emptySlots.size(); itemIndex++, slotIndex++) {
            ItemStack item = items.get(itemIndex);
            int slot = emptySlots.get(slotIndex);

            if (ignoreItems != null && ignoreItems.contains(item.getItem().toString())) {
                slotIndex--;
                continue;
            }

            container.setItem(slot, item);
        }

        return itemIndex >= items.size();
    }

    /**
     * Generate loot and send item to a container. Normally considered condition,
     * disrupt item list, nothing to ignore, and not guaranteed all item to be generated.
     * @param level                 Level that from {@link ServerLevel}.
     * @param blockPos              Block position that using format from {@link BlockPos}.
     * @param tableId               Loot table id from registered or file table.
     * @return                      Does the data completed for send to container: boolean.
     */
    public static boolean sendItemToContainer(ServerLevel level, BlockPos blockPos, String tableId) {
        return sendItemToContainer(level, blockPos, tableId, false, false, null, false);
    }

    /**
     * Generate loot and send item to a container. Ignored ignore item list, and not guaranteed all item to be generated.
     * @param level                 Level that from {@link ServerLevel}.
     * @param blockPos              Block position that using format from {@link BlockPos}.
     * @param tableId               Loot table id from registered or file table.
     * @param ignoreCondition       Ignore "condition" or not.
     * @param isSorted              Determine if list is sorted when push item.
     * @return                      Does the data completed for send to container: boolean.
     */
    public static boolean sendItemToContainer(ServerLevel level, BlockPos blockPos, String tableId, boolean ignoreCondition, boolean isSorted) {
        return sendItemToContainer(level, blockPos, tableId, ignoreCondition, isSorted, null, false);
    }

    /// Determine if this file exists in any possible location.
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

    /**
     * Determine if this YAML file exists in specified save path.
     * @param tableId               Loot table id from registered or file table.
     * @param targetPath            Enum path: TO_GLOBAL or TO_WORLD.
     * @return                      Does this file exists: boolean.
     */
    public static boolean doesFileLootTableExists(String tableId, YamlReader.TargetPath targetPath) {
        return YamlReader.doesFileExist(targetPath, "loot", tableId);
    }

    /**
     * Determine if this JSON file exists in specified save path.
     * @param tableId               Loot table id from registered or file table.
     * @param targetPath            Enum path: TO_GLOBAL or TO_WORLD.
     * @return                      Does this file exists: boolean.
     */
    public static boolean doesFileLootTableExists(String tableId, JsonReader.TargetPath targetPath) {
        return JsonReader.doesFileExist(targetPath, "loot", tableId);
    }

    /**
     * Delete this YAML table file.
     * @param tableId               Loot table id from registered or file table.
     * @param targetPath            Enum path: TO_GLOBAL or TO_WORLD.
     * @return                      Does the delete success: boolean.
     */
    public static boolean deleteFileLootTable(String tableId, YamlReader.TargetPath targetPath) {
        try {
            YamlReader.delete("loot", tableId, targetPath);
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    /**
     * Delete this JSON table file.
     * @param tableId               Loot table id from registered or file table.
     * @param targetPath            Enum path: TO_GLOBAL or TO_WORLD.
     * @return                      Does the delete success: boolean.
     */
    public static boolean deleteFileLootTable(String tableId, JsonReader.TargetPath targetPath) {
        try {
            JsonReader.delete("loot", tableId, targetPath);
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    /**
     * Generate component content list of table details from table data.
     * @param tableId               Loot table id from registered or file table.
     * @return                      The component list of details: List<\Component>.
     */
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

                    if (entry.lootFunctions != null && !entry.lootFunctions.isEmpty()) {
                        lines.add(Component.translatable("api.core_hanxu.loot.function_size")
                                .append(Component.literal(" " + entry.lootFunctions.size()))
                                .withColor(General.Color.TITLE)
                        );

                        for (LootFunction function : entry.lootFunctions) {
                            lines.add(Component.translatable("api.core_hanxu.loot.function")
                                    .append(Component.literal(" " + function.function))
                                    .withColor(General.Color.CONTENT)
                            );
                        }
                    }
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

    /**
     * Generate string of table details. It will translate description when {@link Component} change into string.
     * @param tableId               Loot table id from registered or file table.
     * @return                      The string of details: String.
     */
    public static String readLootTableAsTranslatedString(String tableId) {
        List<Component> lines = readLootTable(tableId);
        StringBuilder stringPackage = new StringBuilder();

        for (Component line : lines) {
            String thisLine = (line.getString() + "\n");
            stringPackage.append(thisLine);
        }

        return stringPackage.toString();
    }

    /**
     * Generate string of table details. It will use fixed English description.
     * @param tableId               Loot table id from registered or file table.
     * @return                      The string of details: String.
     */
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

                    if (entry.lootFunctions != null && !entry.lootFunctions.isEmpty()) {
                        stringPackage.append("      Functions: ").append(entry.lootFunctions.size()).append("\n");

                        for (LootFunction function : entry.lootFunctions) {
                            stringPackage.append("      => Function: ").append(function.function).append("\n");
                        }
                    }
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
                    referenceTable = loadFromFile(entry.id);
                }
                catch (IOException e) {
                    referenceTable = null;
                }

                if (referenceTable != null) {
                    List<ItemStack> subItems = generateItemList(referenceTable, random, luck, ignoreCondition, false);
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
            return item;
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
                case "minecraft:set_potion", "set_potion" -> {
                    Object potionId = parameters != null? parameters.get("id") : null;
                    if (potionId == null) {
                        continue;
                    }

                    String potionIdString = potionId.toString();

                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    if (server == null) {
                        continue;
                    }

                    RegistryAccess registryAccess = server.registryAccess();
                    Registry<Potion> potionRegistry = registryAccess.lookupOrThrow(Registries.POTION);

                    ResourceLocation potionLocation = ResourceLocation.tryParse(potionIdString);
                    if (potionLocation == null) {
                        continue;
                    }

                    Optional<Holder.Reference<Potion>> reference = potionRegistry.get(potionLocation);

                    if (reference.isEmpty()) {
                        continue;
                    }

                    item.set(DataComponents.POTION_CONTENTS, new PotionContents(reference.get()));
                }
                case "minecraft:set_attributes", "set_attributes" -> {
                    Object attributes = parameters != null? parameters.get("attributes") : null;
                    if (!(attributes instanceof List<?> rawList)) {
                        continue;
                    }

                    ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                    
                    if (ServerLifecycleHooks.getCurrentServer() == null) {
                        continue;
                    }
                    
                    RegistryAccess registryAccess = ServerLifecycleHooks.getCurrentServer().registryAccess();
                    Registry<Attribute> registry = registryAccess.lookupOrThrow(Registries.ATTRIBUTE);

                    for (Object rawEntry : rawList) {
                        if (!(rawEntry instanceof Map)) {
                            continue;
                        }

                        @SuppressWarnings("unchecked")
                        Map<String, Object> entry = (Map<String, Object>) rawEntry;

                        String attributeId = String.valueOf(entry.get("id"));
                        String slot = String.valueOf(entry.getOrDefault("slot", "mainhand"));
                        String operation = String.valueOf(entry.getOrDefault("operation", "add"));
                        Object amountObject = entry.get("amount");

                        if (attributeId == null || amountObject == null) {
                            continue;
                        }

                        ResourceLocation attributeLocation = ResourceLocation.tryParse(attributeId);
                        if (attributeLocation == null) {
                            continue;
                        }

                        double amount = ((Number) amountObject).doubleValue();
                        AttributeModifier.Operation modifierOperation;
                        switch (operation) {
                            case "multiply_base" -> modifierOperation = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                            case "multiply_total" -> modifierOperation = AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
                            default -> modifierOperation = AttributeModifier.Operation.ADD_VALUE;
                        }

                        EquipmentSlotGroup slotGroup;
                        switch (slot) {
                            case "offhand" -> slotGroup = EquipmentSlotGroup.OFFHAND;
                            case "head" -> slotGroup = EquipmentSlotGroup.HEAD;
                            case "chest" -> slotGroup = EquipmentSlotGroup.CHEST;
                            case "legs" -> slotGroup = EquipmentSlotGroup.LEGS;
                            case "feet" -> slotGroup = EquipmentSlotGroup.FEET;
                            default -> slotGroup = EquipmentSlotGroup.MAINHAND;
                        }

                        ResourceKey<Attribute> resourceKey = ResourceKey.create(Registries.ATTRIBUTE, attributeLocation);
                        Optional<Holder.Reference<Attribute>> reference = registry.get(resourceKey);
                        
                        if (reference.isEmpty()) {
                            continue;
                        }

                        AttributeModifier modifier = new AttributeModifier(attributeLocation, amount, modifierOperation);
                        builder.add(reference.get(), modifier, slotGroup);
                    }

                    ItemAttributeModifiers modifiers = builder.build();

                    item.remove(DataComponents.ATTRIBUTE_MODIFIERS);
                    item.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
                }
                case "minecraft:set_glint_override", "set_glint_override" -> {
                    Object glintOverride = parameters != null? parameters.get("glint") : null;
                    if (glintOverride instanceof Boolean) {
                        item.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, (Boolean) glintOverride);
                    }
                }
                case "minecraft:set_repair_cost", "set_repair_cost" -> {
                    Object cost = parameters != null? parameters.get("cost") : null;
                    if (cost instanceof Number) {
                        item.set(DataComponents.REPAIR_COST, ((Number) cost).intValue());
                    }
                }
                case "minecraft:set_food", "set_food" -> {
                    Object foodObject = parameters != null? parameters.get("food") : null;
                    if (!(foodObject instanceof Map)) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    Map<String, Object> foodMap = (Map<String, Object>) foodObject;

                    int nutrition = ((Number) foodMap.getOrDefault("nutrition", 4)).intValue();
                    float saturation = ((Number) foodMap.getOrDefault("saturation", 0.6f)).floatValue();
                    boolean canAlwaysEat = (Boolean) foodMap.getOrDefault("can_always_eat", false);

                    FoodProperties foodProperties = new FoodProperties(
                            nutrition, saturation, canAlwaysEat
                    );
                    item.set(DataComponents.FOOD, foodProperties);
                }
                case "minecraft:unbreakable", "unbreakable" -> {
                    item.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
                }
                case "minecraft:set_can_break", "set_can_break" -> {
                    setCanBreakOrPlace(parameters, item, "break");
                }
                case "minecraft:set_can_place_on", "set_can_place_on" -> {
                    setCanBreakOrPlace(parameters, item, "place");
                }
                case "minecraft:set_consumable", "set_consumable" -> {
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    if (server == null) {
                        continue;
                    }

                    float consumeSecond = parameters != null?
                            ((Number) parameters.getOrDefault("consume_seconds", Consumable.DEFAULT_CONSUME_SECONDS)).floatValue() :
                            Consumable.DEFAULT_CONSUME_SECONDS;

                    String animationString = parameters != null? String.valueOf(parameters.getOrDefault("animation", "eat")) : "eat";
                    ItemUseAnimation animation = animationString.equals("drink")? ItemUseAnimation.DRINK : ItemUseAnimation.EAT;

                    String soundId = parameters != null? String.valueOf(parameters.get("sound")) : null;
                    Holder<SoundEvent> soundHolder = null;
                    if (soundId != null) {
                        ResourceLocation soundLocation = ResourceLocation.tryParse(soundId);
                        if (soundLocation != null) {
                            soundHolder = BuiltInRegistries.SOUND_EVENT.get(soundLocation).orElse(null);
                        }
                    }

                    List<ConsumeEffect> effects = new ArrayList<>();
                    Object effectsObject = parameters != null? parameters.get("effects") : null;
                    if (!(effectsObject instanceof List)) {
                        continue;
                    }

                    RegistryAccess registryAccess = server.registryAccess();
                    Registry<MobEffect> effectRegistry = registryAccess.lookupOrThrow(Registries.MOB_EFFECT);

                    for (Object rawEffect : (List<?>) effectsObject) {
                        if (!(rawEffect instanceof Map)) {
                            continue;
                        }

                        @SuppressWarnings("unchecked")
                        Map<String, Object> effectMap = (Map<String, Object>) rawEffect;

                        String effectId = String.valueOf(effectMap.get("id"));
                        int duration = ((Number) effectMap.getOrDefault("duration", 100)).intValue();
                        int amplifier = ((Number) effectMap.getOrDefault("amplifier", 0)).intValue();
                        float probability = ((Number) effectMap.getOrDefault("probability", 1.0f)).floatValue();

                        if (effectId == null) {
                            continue;
                        }

                        ResourceLocation location = ResourceLocation.tryParse(effectId);
                        if (location == null) {
                            continue;
                        }

                        ResourceKey<MobEffect> resourceKey = ResourceKey.create(Registries.MOB_EFFECT, location);
                        Optional<Holder.Reference<MobEffect>> reference = effectRegistry.get(resourceKey);
                        if (reference.isEmpty()) {
                            continue;
                        }

                        MobEffectInstance instance = new MobEffectInstance(reference.get(), duration, amplifier);
                        effects.add(new ApplyStatusEffectsConsumeEffect(List.of(instance), probability));
                    }

                    if (soundHolder == null) {
                        soundHolder = SoundEvents.GENERIC_EAT;
                    }

                    Consumable consumable = new Consumable(
                            consumeSecond,
                            animation,
                            soundHolder,
                            true,
                            effects
                    );

                    item.set(DataComponents.CONSUMABLE, consumable);
                }
                case "minecraft:set_equippable", "set_equippable" -> {
                    Object slotObject = parameters != null? parameters.get("slot") : null;
                    if (slotObject == null) {
                        continue;
                    }

                    EquipmentSlot slot;
                    switch (String.valueOf(slotObject)) {
                        case "offhand" -> slot = EquipmentSlot.OFFHAND;
                        case "head" -> slot = EquipmentSlot.HEAD;
                        case "chest" -> slot = EquipmentSlot.CHEST;
                        case "legs" -> slot = EquipmentSlot.LEGS;
                        case "feet" -> slot = EquipmentSlot.FEET;
                        default -> slot = EquipmentSlot.MAINHAND;
                    }

                    Equippable equippable = Equippable.builder(slot).build();
                    item.set(DataComponents.EQUIPPABLE, equippable);
                }
                case "minecraft:set_trim", "set_trim" -> {
                    String material = parameters != null? String.valueOf(parameters.get("material")) : null;
                    String pattern = parameters != null? String.valueOf(parameters.get("pattern")) : null;
                    if (material == null || pattern == null) {
                        continue;
                    }

                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    if (server == null) {
                        continue;
                    }

                    RegistryAccess registryAccess = server.registryAccess();
                    Registry<TrimMaterial> materialRegistry = registryAccess.lookupOrThrow(Registries.TRIM_MATERIAL);
                    Registry<TrimPattern> patternRegistry = registryAccess.lookupOrThrow(Registries.TRIM_PATTERN);

                    ResourceLocation materialLocation = ResourceLocation.tryParse(material);
                    ResourceLocation patternLocation = ResourceLocation.tryParse(pattern);
                    if (materialLocation == null || patternLocation == null) {
                        continue;
                    }

                    ResourceKey<TrimMaterial> materialResourceKey = ResourceKey.create(Registries.TRIM_MATERIAL, materialLocation);
                    ResourceKey<TrimPattern> patternResourceKey = ResourceKey.create(Registries.TRIM_PATTERN, patternLocation);

                    Optional<Holder.Reference<TrimMaterial>> materialReference = materialRegistry.get(materialResourceKey);
                    Optional<Holder.Reference<TrimPattern>> patternReference = patternRegistry.get(patternResourceKey);
                    if (materialReference.isEmpty() || patternReference.isEmpty()) {
                        continue;
                    }

                    item.set(DataComponents.TRIM, new ArmorTrim(materialReference.get(), patternReference.get()));
                }
                case "minecraft:set_firework", "set_firework" -> {
                    Object explosionObject = parameters != null? parameters.get("explosions") : null;
                    List<FireworkExplosion> explosions = new ArrayList<>();

                    if (!(explosionObject instanceof List)) {
                        continue;
                    }

                    for (Object rawExplosion : (List<?>) explosionObject) {
                        if (!(rawExplosion instanceof Map)) {
                            continue;
                        }

                        @SuppressWarnings("unchecked")
                        Map<String, Object> explosionMap = (Map<String, Object>) rawExplosion;

                        String shapeString = String.valueOf(explosionMap.getOrDefault("shape", "small_ball"));
                        FireworkExplosion.Shape shape;
                        switch (shapeString) {
                            case "large_ball" -> shape = FireworkExplosion.Shape.LARGE_BALL;
                            case "star" -> shape = FireworkExplosion.Shape.STAR;
                            case "creeper" -> shape = FireworkExplosion.Shape.CREEPER;
                            case "burst" -> shape = FireworkExplosion.Shape.BURST;
                            default -> shape = FireworkExplosion.Shape.SMALL_BALL;
                        }

                        IntList colors = new IntArrayList();
                        Object colorObject = explosionMap.getOrDefault("colors", null);
                        if (colorObject instanceof List) {
                            for (Object color : (List<?>) colorObject) {
                                if (color instanceof Number) {
                                    colors.add(((Number) color).intValue());
                                }
                            }
                        }

                        IntList fadeColors = new IntArrayList();
                        Object fadeColorObject = explosionMap.getOrDefault("fade_colors", null);
                        if (fadeColorObject instanceof List) {
                            for (Object color : (List<?>) fadeColorObject) {
                                if (color instanceof Number) {
                                    fadeColors.add(((Number) color).intValue());
                                }
                            }
                        }

                        boolean hasTrail = (boolean) explosionMap.getOrDefault("trail", false);
                        boolean hasTwinkle = (boolean) explosionMap.getOrDefault("twinkle", false);

                        explosions.add(new FireworkExplosion(shape, colors, fadeColors, hasTrail, hasTwinkle));
                    }

                    int flightDuration = ((Number) parameters.getOrDefault("flight_duration", 1)).intValue();

                    if (item.is(Items.FIREWORK_ROCKET)) {
                        item.set(DataComponents.FIREWORKS, new Fireworks(flightDuration, explosions));
                    }
                    else if (item.is(Items.FIREWORK_STAR)) {
                        if (explosions.isEmpty()) {
                            continue;
                        }
                        item.set(DataComponents.FIREWORK_EXPLOSION, explosions.getFirst());
                    }
                }
                case "core_hanxu:set_exactly_damage", "set_exactly_damage" -> {
                    Object damage = parameters != null? parameters.get("damage") : null;
                    int damageValue;
                    if (damage instanceof Number) {
                        damageValue = ((Number) damage).intValue();
                    }
                    else {
                        continue;
                    }

                    int maximumDamage = item.getMaxDamage();
                    item.setDamageValue(Math.min(damageValue, maximumDamage - 1));
                }
                case "core_hanxu:set_fire_resistant", "set_fire_resistant" -> {
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    if (server == null) {
                        continue;
                    }

                    TagKey<DamageType> fireTag = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.withDefaultNamespace("is_fire"));

                    item.set(DataComponents.DAMAGE_RESISTANT, new DamageResistant(fireTag));
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
            data = loadFromFile(tableId);
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

    private static void setCanBreakOrPlace(Map<String, Object> parameters, ItemStack item, String category) {
        Object blocksObject = parameters != null? parameters.get("blocks") : null;
        if (!(blocksObject instanceof List)) {
            return;
        }

        List<Block> blocks = new ArrayList<>();
        for (Object rawBlock : (List<?>) blocksObject) {
            if (!(rawBlock instanceof String)) {
                return;
            }

            ResourceLocation blockLocation = ResourceLocation.tryParse(String.valueOf(rawBlock));
            if (blockLocation == null) {
                return;
            }

            Optional<Holder.Reference<Block>> reference = BuiltInRegistries.BLOCK.get(blockLocation);

            if (reference.isEmpty()) {
                return;
            }

            blocks.add(reference.get().value());
        }

        if (!blocks.isEmpty()) {
            if (ServerLifecycleHooks.getCurrentServer() == null) {
                return;
            }
            RegistryAccess registryAccess = ServerLifecycleHooks.getCurrentServer().registryAccess();
            HolderGetter<Block> blockGetter = registryAccess.lookupOrThrow(Registries.BLOCK);
            BlockPredicate predicate = BlockPredicate.Builder.block().of(blockGetter, blocks).build();

            if (category.equals("break")) {
                item.set(DataComponents.CAN_BREAK, new AdventureModePredicate(List.of(predicate)));
            }
            else if (category.equals("place")) {
                item.set(DataComponents.CAN_PLACE_ON, new AdventureModePredicate(List.of(predicate)));
            }
        }
    }
}
