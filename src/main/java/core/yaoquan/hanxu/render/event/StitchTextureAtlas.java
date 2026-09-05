package core.yaoquan.hanxu.render.event;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.render.data.CacheRainSplash;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;

@EventBusSubscriber(modid = CoreHanXu.MOD_ID, value = Dist.CLIENT)
public class StitchTextureAtlas {
    private static final ResourceLocation PARTICLE_ATLAS = ResourceLocation.withDefaultNamespace("textures/atlas/particles.png");

    @SubscribeEvent
    public static void onTextureAtlasStitched(TextureAtlasStitchedEvent event) {
        if (!event.getAtlas().location().equals(PARTICLE_ATLAS)) {
            return;
        }

        CacheRainSplash.cacheSprites(event.getAtlas());
    }
}
