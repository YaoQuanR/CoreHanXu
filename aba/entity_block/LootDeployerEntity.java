package core.yaoquan.hanxu.registry.entity_block;

import core.yaoquan.hanxu.registry.ModConfig;
import core.yaoquan.hanxu.registry.ModRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LootDeployerEntity extends BlockEntity implements MenuProvider {
    private String lootPath = "";
    private String containerId = "minecraft:chest";
    private String containerName = "";

    private static final String LOOT_TABLE = "LootTable";
    private static final String CONTAINER_ID = "ContainerId";
    private static final String CONTAINER_NAME = "CustomName";

    public LootDeployerEntity(BlockPos pos, BlockState state) {
        super(ModRegister.LOOT_DEPLOYER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.putString(LOOT_TABLE, lootPath);
        valueOutput.putString(CONTAINER_ID, containerId);
        valueOutput.putString(CONTAINER_NAME, containerName);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput valueInput) {
        super.loadAdditional(valueInput);
        lootPath = valueInput.getStringOr(LOOT_TABLE, "");
        containerId = valueInput.getStringOr(CONTAINER_ID, "");
        containerName = valueInput.getStringOr(CONTAINER_NAME, "");
    }

    public boolean isReady() {
        return !lootPath.isEmpty() && !containerId.isEmpty();
    }

    public void deploy() {
        if (level == null || level.isClientSide()) {
            return;
        }

        Direction facing = level.getBlockState(worldPosition).getValue(LootDeployer.FACING);

        // Get target container, reject until it is non-air block.
        ResourceLocation containerLocation = ResourceLocation.tryParse(this.containerId);
        if (containerLocation == null) {
            return;
        }
        var newBlock = BuiltInRegistries.BLOCK.get(containerLocation);
        if (newBlock.isEmpty()) {
            return;
        }
        Block newBlockValue = newBlock.get().value();
        if (newBlockValue == Blocks.AIR) {
            return;
        }

        // Set new block state.
        BlockState newBlockState = newBlockValue.defaultBlockState();
        if (newBlockState.hasProperty(BlockStateProperties.FACING)) {
            newBlockState = newBlockState.setValue(BlockStateProperties.FACING, facing);
        }

        // Then set block.
        level.setBlock(worldPosition, newBlockState, Block.UPDATE_ALL);

        // Play sound.
        if (ModConfig.SET_ENABLED_DEPLOY_SOUND.getAsBoolean()) {
            level.playSound(
                    null,
                    worldPosition,
                    SoundEvents.FIREWORK_ROCKET_BLAST,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
        }

        // Get replaced block's entity.
        BlockEntity newBlockEntity = level.getBlockEntity(worldPosition);

        // Modify:
        // End if this container without modifiable features.
        if (newBlockEntity == null) {
            return;
        }

        // Edit custom name.
        if (!containerName.isEmpty() && newBlockEntity instanceof Nameable nameable) {
            if (!containerName.startsWith("{") && !containerName.endsWith("}")) {
                containerName = "{\"text\": \"" + containerName + "\"}";
            }
            CompoundTag nameTag = new CompoundTag();
            nameTag.putString("CustomName", containerName);

            ValueInput valueInput = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), nameTag);
            newBlockEntity.loadWithComponents(valueInput);
        }

        // Edit loot.
        if (!lootPath.isEmpty() && newBlockEntity instanceof RandomizableContainerBlockEntity container) {
            ResourceLocation lootTableLocation = ResourceLocation.tryParse(lootPath);
            if (lootTableLocation != null) {
                ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTableLocation);
                container.setLootTable(lootTableKey, level.getRandom().nextLong());
            }
        }
    }
    public String getLootPath() {
        return lootPath;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setLootPath(String lootPath) {
        this.lootPath = lootPath;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, @Nullable Inventory inventory, @NotNull Player player) {
        return new LootDeployerMenu(containerId, inventory, worldPosition);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.loot_deployer");
    }
}
