package core.yaoquan.hanxu.registry.entity_block;

import core.yaoquan.hanxu.registry.object.ModBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;

public class LootDeployerEntity extends BlockEntity {
    public LootDeployerEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.LOOT_DEPLOYER_ENTITY.get(), pos, state);
    }

    public static class ExtendBlock extends Block implements EntityBlock {
        public ExtendBlock(BlockBehaviour.Properties properties) {
            super(properties);
            this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        }

        public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

        @Override
        public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
            return new LootDeployerEntity(blockPos, blockState);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(FACING);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            Direction direction = context.getHorizontalDirection().getOpposite();
            return this.defaultBlockState().setValue(FACING, direction);
        }
    }
}
