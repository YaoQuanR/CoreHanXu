package core.yaoquan.hanxu.registry;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.registry.entity_block.LootDeployer;
import core.yaoquan.hanxu.registry.entity_block.LootDeployerEntity;
import core.yaoquan.hanxu.registry.entity_block.LootDeployerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.function.Supplier;

public class ModRegister {
    private static final String MID = CoreHanXu.MOD_ID;

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MID);

    public static final Supplier<LootDeployer> LOOT_DEPLOYER = BLOCKS.register(
            "loot_deployer",
            () -> new LootDeployer(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(5F, 1200F)
                    .sound(SoundType.BAMBOO)
            )
    );

    public static final Supplier<BlockItem> LOOT_DEPLOYER_ITEM = ITEMS.register(
            "loot_deployer",
            () -> new BlockItem(LOOT_DEPLOYER.get(), new Item.Properties()
                    .stacksTo(16)
            )
    );

    public static final Supplier<BlockEntityType<LootDeployerEntity>> LOOT_DEPLOYER_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "loot_deployer",
            () -> new BlockEntityType<>(
                    LootDeployerEntity::new,
                    Set.of(LOOT_DEPLOYER.get())
            )
    );

    public static final Supplier<MenuType<LootDeployerMenu>> LOOT_DEPLOYER_MENU = MENUS.register(
            "loot_deployer",
            () -> IMenuTypeExtension.create(LootDeployerMenu::new)
    );
}
