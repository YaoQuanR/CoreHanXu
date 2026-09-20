package core.yaoquan.hanxu.render.mixin;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.render.data.CacheRainSplash;
import core.yaoquan.hanxu.render.data.WeatherClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(ParticleEngine.class)
public class ColoredRainSplash {
    static {
        CoreHanXu.LOGGER.info("[HX] Mixin: Colored Rain -> Splash.");
    }

    @Shadow(remap = false)
    @Nullable
    private ClientLevel level;

    @Inject(
            method = "makeParticle",
            at = @At("RETURN"),
            remap = false
    )
    private <T extends ParticleOptions> void onMakeParticle(
            T particleData,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            CallbackInfoReturnable<Particle> cir) {
        Particle particle = cir.getReturnValue();
        if (particle == null || this.level == null) {
            return;
        }

        ResourceLocation particleLocation = BuiltInRegistries.PARTICLE_TYPE.getKey(particleData.getType());
        if (particleLocation == null) {
            return;
        }

        String id = particleLocation.toString();
        if (!id.contains("minecraft:rain")) {
            return;
        }

        String dimension = level.dimension().location().toString();
        WeatherClient.ColoredRainInfo coloredRain = WeatherClient.getColoredRain(dimension);
        if (coloredRain == null || coloredRain.phase() != WeatherHolder.WeatherPhase.ACTIVE) {
            return;
        }

        int color = coloredRain.rainColor();
        float colorR = ((color >> 16) & 0xFF) / 255.0f;
        float colorG = ((color >> 8) & 0xFF) / 255.0f;
        float colorB = (color & 0xFF) / 255.0f;

        if (particle instanceof WaterDropParticle splashParticle) {
            List<TextureAtlasSprite> cachedAtlasSprite = CacheRainSplash.getCachedAtlasSprite();
            if (cachedAtlasSprite != null) {
                TextureAtlasSprite randomAtlasSprite = cachedAtlasSprite.get(ThreadLocalRandom.current().nextInt(cachedAtlasSprite.size()));
                ((SingleQuadParticleAccessor) splashParticle).setSprite(randomAtlasSprite);
            }
            splashParticle.setColor(colorR, colorG, colorB);
        }
    }
}
