package core.yaoquan.hanxu.render.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.render.data.WeatherClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;
import java.util.List;

@Mixin(FogRenderer.class)
public class FogRender {
    static {
        CoreHanXu.LOGGER.info("[HX] Mixin: Fog Render.");
    }

    @Shadow(remap = false)
    private MappableRingBuffer regularBuffer;

    @Shadow(remap = false)
    protected void updateBuffer(
            ByteBuffer buffer,
            int position,
            Vector4f fogColor,
            float environmentalStart,
            float environmentalEnd,
            float renderDistanceStart,
            float renderDistanceEnd,
            float skyEnd,
            float cloudEnd
    ) {}

    @Shadow(remap = false)
    private static List<FogEnvironment> FOG_ENVIRONMENTS;

    @Inject(
            method = "setupFog",
            at = @At("RETURN"),
            remap = false
    )
    private void onSetupFog(
            Camera camera,
            int renderDistance,
            boolean isFoggy,
            DeltaTracker deltaTracker,
            float darkenWorldAmount,
            ClientLevel level,
            CallbackInfoReturnable<Vector4f> cir) {
        String dimension = level.dimension().location().toString();
        WeatherClient.FogInfo fog = WeatherClient.getFog(dimension);
        WeatherClient.ColoredRainInfo coloredRain = WeatherClient.getColoredRain(dimension);

        if (fog != null && fog.phase() != WeatherHolder.WeatherPhase.STILLNESS) {
            applyToFog(fog, cir);
            return;
        }

        if (coloredRain != null && coloredRain.phase() != WeatherHolder.WeatherPhase.STILLNESS) {
            applyToColoredRain(camera, renderDistance, isFoggy, deltaTracker, level, coloredRain, cir);
        }
    }

    @Unique
    private void applyToFog(WeatherClient.FogInfo fog, CallbackInfoReturnable<Vector4f> cir) {
        int color = fog.color();
        float colorR = ((color >> 16) & 0xFF) / 255.0f;
        float colorG = ((color >> 8) & 0xFF) / 255.0f;
        float colorB = (color & 0xFF) / 255.0f;
        Vector4f fogColor = new Vector4f(colorR, colorG, colorB, 1.0f);
        Vector4f returnColor = cir.getReturnValue();
        if (returnColor != null) {
            returnColor.x = colorR;
            returnColor.y = colorG;
            returnColor.z = colorB;
            returnColor.w = 1.0f;
        }

        float currentDistance = fog.currentDistance();
        float start = currentDistance * 0.15f;

        GpuBuffer currentBuffer = this.regularBuffer.currentBuffer();
        try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(currentBuffer, false, true)) {
            this.updateBuffer(
                    mappedView.data(),
                    0,
                    fogColor,
                    start,
                    currentDistance,
                    start,
                    currentDistance,
                    currentDistance,
                    currentDistance
            );
        }
        catch (Exception e) {
            CoreHanXu.LOGGER.warn("[HX] Failed to mixin fog.", e);
        }
    }

    @Unique
    private void applyToColoredRain(Camera camera, int renderDistance, boolean isFoggy, DeltaTracker deltaTracker, ClientLevel level, WeatherClient.ColoredRainInfo coloredRain, CallbackInfoReturnable<Vector4f> cir) {
        int color = coloredRain.skyColor();
        float colorR = ((color >> 16) & 0xFF) / 255.0f;
        float colorG = ((color >> 8) & 0xFF) / 255.0f;
        float colorB = (color & 0xFF) / 255.0f;
        Vector4f fogColor = new Vector4f(colorR, colorG, colorB, 1.0f);
        Vector4f returnColor = cir.getReturnValue();
        if (returnColor != null) {
            returnColor.x = colorR;
            returnColor.y = colorG;
            returnColor.z = colorB;
            returnColor.w = 1.0f;
        }

        float renderDistanceBlocks = (float) (renderDistance * 16);
        FogType fogType = isFoggy? FogType.DIMENSION_OR_BOSS : FogType.ATMOSPHERIC;
        Entity entity = camera.getEntity();
        FogData fogData = new FogData();

        for (FogEnvironment environment : FOG_ENVIRONMENTS) {
            if (environment.isApplicable(fogType, entity)) {
                environment.setupFog(fogData, entity, camera.getBlockPosition(), level, renderDistanceBlocks, deltaTracker);
                break;
            }
        }

        float f2 = Mth.clamp(renderDistanceBlocks / 10.0f, 4.0f, 64.0f);
        fogData.renderDistanceStart = renderDistanceBlocks - f2;
        fogData.renderDistanceEnd = renderDistanceBlocks;

        GpuBuffer currentBuffer = this.regularBuffer.currentBuffer();
        try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(currentBuffer, false, true)) {
            this.updateBuffer(
                    mappedView.data(),
                    0,
                    fogColor,
                    fogData.environmentalStart,
                    fogData.environmentalEnd,
                    fogData.renderDistanceStart,
                    fogData.renderDistanceEnd,
                    fogData.skyEnd,
                    fogData.cloudEnd
            );
        }
        catch (Exception e) {
            CoreHanXu.LOGGER.warn("[HX] Failed to mixin fog for colored rain.", e);
        }
    }
}
