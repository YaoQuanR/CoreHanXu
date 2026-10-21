package core.yaoquan.hanxu.render.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.render.data.WeatherClient;
import core.yaoquan.hanxu.util.tool.ColorHSV;
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

        if (fog != null && fog.phase() == WeatherHolder.WeatherPhase.ACTIVE) {
            applyToFog(fog, cir, level);
            return;
        }

        if (coloredRain != null && coloredRain.phase() == WeatherHolder.WeatherPhase.ACTIVE) {
            applyToColoredRain(camera, renderDistance, isFoggy, deltaTracker, level, coloredRain, cir);
        }
    }

    @Unique
    private void applyToFog(WeatherClient.FogInfo fog, CallbackInfoReturnable<Vector4f> cir, ClientLevel level) {
        int color = fog.color();

        // Determines the darkness of fog color:
        float dayBrightness = Math.max(1.0f - ((level.getSkyDarken() - 3.0f) / (11.0f - 3.0f)), 0.5f);
        int modifiedColor = ColorHSV.adjustBrightness(color, dayBrightness);

        float colorR = ((modifiedColor >> 16) & 0xFF) / 255.0f;
        float colorG = ((modifiedColor >> 8) & 0xFF) / 255.0f;
        float colorB = (modifiedColor & 0xFF) / 255.0f;
        Vector4f fogColor = new Vector4f(colorR, colorG, colorB, 1.0f);
        Vector4f currentFog = cir.getReturnValue();
        if (currentFog == null) {
            return;
        }

        currentFog.x = colorR;
        currentFog.y = colorG;
        currentFog.z = colorB;
        currentFog.w = 1.0f;

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
        Vector4f currentFog = cir.getReturnValue();
        if (currentFog == null) {
            return;
        }

        // Get the current vanilla color, then modify with day brightness for color fixing, and transition with the process:
        int currentFogColor = ((int) (currentFog.x * 255) << 16) | ((int) (currentFog.y * 255) << 8) | (int) (currentFog.z * 255);
        float dayBrightness = Math.max(1.0f - ((level.getSkyDarken() - 3.0f) / (11.0f - 3.0f)), 0.2f);
        int transitionedColor = ColorHSV.adjustBrightness(coloredRain.fogColor(), dayBrightness);
        transitionedColor = ColorHSV.interpolate(currentFogColor, transitionedColor, coloredRain.progress());

        float colorR = ((transitionedColor >> 16) & 0xFF) / 255.0f;
        float colorG = ((transitionedColor >> 8) & 0xFF) / 255.0f;
        float colorB = (transitionedColor & 0xFF) / 255.0f;
        Vector4f fogVector = new Vector4f(colorR, colorG, colorB, 1.0f);

        currentFog.x = colorR;
        currentFog.y = colorG;
        currentFog.z = colorB;
        currentFog.w = 1.0f;

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
                    fogVector,
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
