package core.yaoquan.hanxu.registry.object;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.registry.entity_block.LootDeployerEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlock {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CoreHanXu.MOD_ID);

    public static final DeferredBlock<LootDeployerEntity.ExtendBlock> LOOT_DEPLOYER =
            BLOCKS.register(
                    "loot_deployer",
                    registeredLocation -> new LootDeployerEntity.ExtendBlock(BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registeredLocation))
                            .strength(5.0F, 1200F)
                            .destroyTime(4F)
                            .mapColor(MapColor.WOOD)
                            .sound(SoundType.NETHER_WOOD)
                    )
            );
}
