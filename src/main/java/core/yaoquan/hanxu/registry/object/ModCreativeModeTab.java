package core.yaoquan.hanxu.registry.object;

import core.yaoquan.hanxu.CoreHanXu;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CoreHanXu.MOD_ID);

    public static final Supplier<CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_tab.core_hanxu.main"))
                    .icon(() -> new ItemStack(ModItem.LOOT_DEPLOYER.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItem.LOOT_DEPLOYER);
                    })
                    .build()
    );
}
