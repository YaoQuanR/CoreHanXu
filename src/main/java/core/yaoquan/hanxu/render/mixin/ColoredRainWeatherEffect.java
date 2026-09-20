package core.yaoquan.hanxu.render.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.render.data.WeatherClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.state.WeatherRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(WeatherEffectRenderer.class)
public class ColoredRainWeatherEffect {
    static {
        CoreHanXu.LOGGER.info("[HX] Mixin: Colored Rain -> Weather Effect.");
    }

    @Shadow(remap = false)
    private float[] columnSizeX;

    @Shadow(remap = false)
    private float[] columnSizeZ;

    @Unique
    private static final ResourceLocation RAIN_LOCATION = ResourceLocation.fromNamespaceAndPath(CoreHanXu.MOD_ID, "textures/environment/rain.png");

    @Unique
    private static final ResourceLocation SNOW_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/snow.png");

    @Inject(
            method = "render(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/state/WeatherRenderState;Lnet/minecraft/client/renderer/state/LevelRenderState;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void onRender(
            MultiBufferSource bufferSource,
            Vec3 cameraPosition,
            WeatherRenderState renderState,
            @Nullable LevelRenderState levelRenderState,
            CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        String dimension = level.dimension().location().toString();
        WeatherClient.ColoredRainInfo coloredRain = WeatherClient.getColoredRain(dimension);
        if (coloredRain == null || coloredRain.phase() != WeatherHolder.WeatherPhase.ACTIVE) {
            return;
        }

        // Else use special source for rendering colored rain.

        if (!renderState.rainColumns.isEmpty()) {
            renderColoredInstances(
                    bufferSource,
                    renderState.rainColumns,
                    cameraPosition,
                    1.0f,
                    renderState.radius,
                    renderState.intensity,
                    coloredRain,
                    true
            );
        }

        if (!renderState.snowColumns.isEmpty()) {
            renderColoredInstances(
                    bufferSource,
                    renderState.snowColumns,
                    cameraPosition,
                    0.8f,
                    renderState.radius,
                    renderState.intensity,
                    coloredRain,
                    false
            );
        }

        ci.cancel();
    }

    @Unique
    private void renderColoredInstances(
            MultiBufferSource bufferSource,
            List<WeatherEffectRenderer.ColumnInstance> columnInstances,
            Vec3 cameraPosition,
            float amount,
            int radius,
            float rainLevel,
            WeatherClient.ColoredRainInfo coloredRain,
            boolean isRain) {
        ResourceLocation textureLocation = isRain? RAIN_LOCATION : SNOW_LOCATION;

        int color = isRain? coloredRain.rainColor() : coloredRain.snowColor();

        float colorR = ((color >> 16) & 0xFF) / 255.0f;
        float colorG = ((color >> 8) & 0xFF) / 255.0f;
        float colorB = (color & 0xFF) / 255.0f;

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.weather(textureLocation, Minecraft.useShaderTransparency()));

        for (WeatherEffectRenderer.ColumnInstance columnInstance : columnInstances) {
            float f = (float) ((double) columnInstance.x() + (double) 0.5F - cameraPosition.x);
            float f1 = (float) ((double) columnInstance.z() + (double) 0.5F - cameraPosition.z);
            float f2 = (float) Mth.lengthSquared(f, f1);
            float f3 = Mth.lerp(f2 / (float)(radius * radius), amount, 0.5F) * rainLevel;

            // Change this for coloring.
            float alpha = isRain? f3 : 1.0f;
            int i = ARGB.colorFromFloat(alpha, colorR, colorG, colorB);

            int j = (columnInstance.z() - Mth.floor(cameraPosition.z) + 16) * 32 + columnInstance.x() - Mth.floor(cameraPosition.x) + 16;
            float f4 = this.columnSizeX[j] / 2.0F;
            float f5 = this.columnSizeZ[j] / 2.0F;
            float f6 = f - f4;
            float f7 = f + f4;
            float f8 = (float) ((double) columnInstance.topY() - cameraPosition.y);
            float f9 = (float) ((double) columnInstance.bottomY() - cameraPosition.y);
            float f10 = f1 - f5;
            float f11 = f1 + f5;
            float f12 = columnInstance.uOffset() + 0.0F;
            float f13 = columnInstance.uOffset() + 1.0F;
            float f14 = (float) columnInstance.bottomY() * 0.25F + columnInstance.vOffset();
            float f15 = (float) columnInstance.topY() * 0.25F + columnInstance.vOffset();
            buffer.addVertex(f6, f8, f10).setUv(f12, f14).setColor(i).setLight(columnInstance.lightCoords());
            buffer.addVertex(f7, f8, f11).setUv(f13, f14).setColor(i).setLight(columnInstance.lightCoords());
            buffer.addVertex(f7, f9, f11).setUv(f13, f15).setColor(i).setLight(columnInstance.lightCoords());
            buffer.addVertex(f6, f9, f10).setUv(f12, f15).setColor(i).setLight(columnInstance.lightCoords());
        }
    }
}
