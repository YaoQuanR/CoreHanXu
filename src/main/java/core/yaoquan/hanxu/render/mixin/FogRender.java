package core.yaoquan.hanxu.render.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.render.data.WeatherClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;

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
            CallbackInfoReturnable<Vector4f> info4f) {
        String dimension = level.dimension().location().toString();
        WeatherClient.FogInfo fog = WeatherClient.getFog(dimension);

        if (fog == null || fog.currentDistance() < 0) {
            return;
        }

        int color = fog.color();
        float colorR = ((color >> 16) & 0xFF) / 255.0f;
        float colorG = ((color >> 8) & 0xFF) / 255.0f;
        float colorB = (color & 0xFF) / 255.0f;
        Vector4f fogColor = new Vector4f(colorR, colorG, colorB, 1.0f);
        Vector4f returnColor = info4f.getReturnValue();
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
}
