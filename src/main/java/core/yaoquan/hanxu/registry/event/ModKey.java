package core.yaoquan.hanxu.registry.event;

import com.mojang.blaze3d.platform.InputConstants;
import core.yaoquan.hanxu.CoreHanXu;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = CoreHanXu.MOD_ID, value = Dist.CLIENT)
public class ModKey {
    // Register a key mapping
    public static final KeyMapping.Category KEY_CATEGORY = new KeyMapping.Category(
            ResourceLocation.fromNamespaceAndPath(CoreHanXu.MOD_ID, "category")
    );

    // Register key list:
    public static final Lazy<KeyMapping> INFO_KEY = Lazy.of(() ->
        new KeyMapping(
                ("key." + CoreHanXu.MOD_ID + ".info_key"),
                InputConstants.Type.KEYSYM, // Mapping of keyboard.
                GLFW.GLFW_KEY_F4, // Used key.
                KEY_CATEGORY // Mapping in misc category.
        )
    );

    // Then binding.
    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.registerCategory(KEY_CATEGORY);
        event.register(INFO_KEY.get());
    }
}
