package core.yaoquan.hanxu.registry.object;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.registry.entity_block.LootDeployerEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntity {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CoreHanXu.MOD_ID);

    public static final Supplier<BlockEntityType<LootDeployerEntity>> LOOT_DEPLOYER_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "loot_deployer_entity",
                    () -> new BlockEntityType<>(
                            LootDeployerEntity::new,
                            false,
                            ModBlock.LOOT_DEPLOYER.get()
                    )
            );
}
