package core.yaoquan.hanxu.registry;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.registry.object.ModBlock;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ModDataGenerator {
    public static class ModModelProvider extends ModelProvider {
        public ModModelProvider(PackOutput packOutput) {
            super(packOutput, CoreHanXu.MOD_ID);
        }

        @Override
        protected void registerModels(@NotNull BlockModelGenerators blockModels, @NotNull ItemModelGenerators itemModels) {
            // Block models.
            final List<Block> orientableBlocks = new ArrayList<>();
            orientableBlocks.add(ModBlock.LOOT_DEPLOYER.get());

            for (Block block : orientableBlocks) {
                TextureMapping orientableBlockMapping = new TextureMapping()
                        .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side"))
                        .put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "_front"))
                        .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "_bottom"))
                        .put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top"));

                var modelLocation = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(block, orientableBlockMapping, blockModels.modelOutput);
                var baseVariant = BlockModelGenerators.plainVariant(modelLocation);

                var horizontalRotation = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                        .select(Direction.NORTH, BlockModelGenerators.NOP)
                        .select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
                        .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
                        .select(Direction.WEST, BlockModelGenerators.Y_ROT_270);

                blockModels.blockStateOutput.accept(
                        MultiVariantGenerator.dispatch(block, baseVariant).with(horizontalRotation)
                );

                blockModels.itemModelOutput.accept(
                        block.asItem(),
                        ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(block))
                );
            }
        }
    }
}
