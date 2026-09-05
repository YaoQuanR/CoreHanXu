package core.yaoquan.hanxu.render.data;

import core.yaoquan.hanxu.CoreHanXu;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class CacheRainSplash {
    private static final ResourceLocation[] GREY_SPLASHES = {
            ResourceLocation.fromNamespaceAndPath(CoreHanXu.MOD_ID, "splash_0"),
            ResourceLocation.fromNamespaceAndPath(CoreHanXu.MOD_ID, "splash_1"),
            ResourceLocation.fromNamespaceAndPath(CoreHanXu.MOD_ID, "splash_2"),
            ResourceLocation.fromNamespaceAndPath(CoreHanXu.MOD_ID, "splash_3"),
    };

    private static final ResourceLocation MISSING = MissingTextureAtlasSprite.getLocation();

    private static List<TextureAtlasSprite> cachedAtlasSprite = null;

    public static void cacheSprites(TextureAtlas atlas) {
        List<TextureAtlasSprite> sprites = new ArrayList<>();
        TextureAtlasSprite missingSprite = atlas.getSprite(MISSING);

        for (ResourceLocation location : GREY_SPLASHES) {
            TextureAtlasSprite sprite = atlas.getSprite(location);
            if (sprite != missingSprite) {
                sprites.add(sprite);
                CoreHanXu.LOGGER.info("[HX] Cached grey splash for mixin colored rain: {}", location);
            }

        }

        if (!sprites.isEmpty()) {
            cachedAtlasSprite = sprites;
            return;
        }

        CoreHanXu.LOGGER.warn("[HX] Grey splash not found for mixin colored rain!");
    }

    public static List<TextureAtlasSprite> getCachedAtlasSprite() {
        return cachedAtlasSprite;
    }
}
