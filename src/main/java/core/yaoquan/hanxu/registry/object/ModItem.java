package core.yaoquan.hanxu.registry.object;

import core.yaoquan.hanxu.CoreHanXu;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItem {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CoreHanXu.MOD_ID);

    public static final DeferredItem<BlockItem> LOOT_DEPLOYER = ITEMS.register(
            "loot_deployer",
            registeredLocation -> new BlockItem(ModBlock.LOOT_DEPLOYER.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registeredLocation))
                    .stacksTo(64)
                    .rarity(Rarity.EPIC)
            )
    );
}
