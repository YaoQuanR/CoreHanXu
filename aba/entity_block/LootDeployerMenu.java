package core.yaoquan.hanxu.registry.entity_block;

import core.yaoquan.hanxu.registry.ModRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LootDeployerMenu extends AbstractContainerMenu {
    private final BlockPos blockPos;

    public LootDeployerMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        this(containerId, inventory, buf.readBlockPos());
    }

    public LootDeployerMenu(int containerId, Inventory inventory, BlockPos blockPos) {
        super(ModRegister.LOOT_DEPLOYER_MENU.get(), containerId);
        this.blockPos = blockPos;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}
