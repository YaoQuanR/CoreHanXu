package core.yaoquan.hanxu.render.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.api.weather.ColoredRain;
import core.yaoquan.hanxu.render.data.WeatherClient;
import core.yaoquan.hanxu.util.type.NullableValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.state.WeatherRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(WeatherEffectRenderer.class)
public class ColoredRainWeatherEffect {
    static {
        CoreHanXu.LOGGER.info("[HX] Mixin: Colored Rain -> Weather Effect.");
    }

    @Shadow(remap = false)
    private float[] columnSizeX;

    @Shadow(remap = false)
    private float[] columnSizeZ;

    @Shadow(remap = false)
    private WeatherEffectRenderer.ColumnInstance createRainColumnInstance(
            RandomSource random, int ticks, int x, int bottomY, int topY, int z, int lightCoords, float partialTick) {
        return null;
    }

    @Shadow(remap = false)
    private WeatherEffectRenderer.ColumnInstance createSnowColumnInstance(
            RandomSource random, int ticks, int x, int bottomY, int topY, int z, int lightCoords, float partialTick) {
        return null;
    }

    @Unique private static final ResourceLocation RAIN_LOCATION = ResourceLocation.fromNamespaceAndPath(CoreHanXu.MOD_ID, "textures/environment/rain.png");
    @Unique private static final ResourceLocation SNOW_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/snow.png");

    @Unique private static final Map<Long, RainPrecipitation> rainPrecipitations = new HashMap<>();
    @Unique private static boolean overrideFrozen = false;

    @Unique private record RainPrecipitation(Biome.Precipitation original, Biome.Precipitation overridden) {}

    @Unique
    private static void clearPrecipitations() {
        rainPrecipitations.clear();
    }

    @Unique
    private static void recordPrecipitations(BlockPos blockPos, Biome.Precipitation original, Biome.Precipitation overridden) {
        if (overrideFrozen) {
            return;
        }
        long mapKey = ((long) blockPos.getX() << 32) | (blockPos.getZ() & 0xFFFFFFFFL);
        rainPrecipitations.put(mapKey, new RainPrecipitation(original, overridden));
    }

    @Unique
    private static @NotNull NullableValue<RainPrecipitation> getPrecipitation(int x, int z) {
        long mapKey = ((long) x << 32) | (z & 0xFFFFFFFFL);
        return NullableValue.ofNullable(rainPrecipitations.get(mapKey));
    }

    @Inject(
            method = "getPrecipitationAt",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void onGetPrecipitationAt(
            Level level,
            BlockPos blockPos,
            CallbackInfoReturnable<Biome.Precipitation> cir) {
        if (!(level instanceof ClientLevel clientLevel)) {
            return;
        }

        String dimension = clientLevel.dimension().location().toString();
        WeatherClient.ColoredRainInfo coloredRain = WeatherClient.getColoredRain(dimension);

        if (coloredRain == null || coloredRain.phase() != WeatherHolder.WeatherPhase.ACTIVE) {
            return;
        }

        // Determine the biome set:
        Biome.Precipitation original = cir.getReturnValue();
        if (original == null) {
            return;
        }

        // Get the biome type by precipitation.
        ColoredRain.RainType type = switch (original) {
            case RAIN -> coloredRain.rainBiomes();
            case SNOW -> coloredRain.snowBiomes();
            case NONE -> coloredRain.dryBiomes();
        };

        Biome.Precipitation overridden = switch (type) {
            case RAIN -> Biome.Precipitation.RAIN;
            case SNOW -> Biome.Precipitation.SNOW;
            case DRY -> Biome.Precipitation.NONE;
            case DEFAULT -> original;
        };

        recordPrecipitations(blockPos, original, overridden);
    }

    @Inject(
            method = "extractRenderState",
            at = @At("HEAD"),
            remap = false
    )
    private void onExtractRenderStateHead(Level level, int ticks, float partialTicks, Vec3 cameraPosition, WeatherRenderState renderState, CallbackInfo ci) {
        clearPrecipitations();
        overrideFrozen = false;
    }

    @Inject(
            method = "extractRenderState",
            at = @At("RETURN"),
            remap = false
    )
    private void onExtractRenderStateReturn(Level level, int ticks, float partialTicks, Vec3 cameraPosition, WeatherRenderState renderState, CallbackInfo ci) {
        overrideFrozen = true;

        if (!(level instanceof ClientLevel clientLevel)) {
            return;
        }

        String dimension = clientLevel.dimension().location().toString();
        WeatherClient.ColoredRainInfo coloredRain = WeatherClient.getColoredRain(dimension);

        if (coloredRain == null || coloredRain.phase() != WeatherHolder.WeatherPhase.ACTIVE) {
            return;
        }
        if (coloredRain.intensity() <= 0.0f) {
            return;
        }

        int cameraX = Mth.floor(cameraPosition.x());
        int cameraY = Mth.floor(cameraPosition.y());
        int cameraZ = Mth.floor(cameraPosition.z());
        int radius = renderState.radius;

        for (Map.Entry<Long, RainPrecipitation> entry : rainPrecipitations.entrySet()) {
            RainPrecipitation precipitation = entry.getValue();

            // Skip when they are same.
            if (precipitation.original() == precipitation.overridden()) {
                continue;
            }

            int x = (int) (entry.getKey() >> 32);
            int z = (int) (entry.getKey() & 0xFFFFFFFFL);

            // Skip rendering over radius rain.
            if (Math.abs(x - cameraX) > radius || Math.abs(z - cameraZ) > radius) {
                continue;
            }

            // Rain limits (boarder) of rendering.
            int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            int bottomY = Math.max(cameraY - radius, height);
            int topY = Math.max(cameraY + radius, height);
            if (topY - bottomY == 0) {
                continue;
            }

            BlockPos blockPos = new BlockPos(x, cameraY, z);
            int lightCoordinates = LevelRenderer.getLightColor(level, blockPos);
            // Magic seed from vanilla.
            int seed = (x * x * 3121 + x * 45238971) ^ (z * z * 418711 + z * 13761);
            RandomSource random = RandomSource.create(seed);

            // Any to rain columns.
            if (precipitation.original() != Biome.Precipitation.RAIN && precipitation.overridden() == Biome.Precipitation.RAIN) {
                renderState.rainColumns.add(createRainColumnInstance(random, ticks, x, bottomY, topY, z, lightCoordinates, partialTicks));
            }
            // Any to snow columns.
            else if (precipitation.original() != Biome.Precipitation.SNOW && precipitation.overridden() == Biome.Precipitation.SNOW) {
                renderState.snowColumns.add(createSnowColumnInstance(random, ticks, x, bottomY, topY, z, lightCoordinates, partialTicks));
            }
        }
    }

    @Redirect(
            method = "tickRainParticles",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/WeatherEffectRenderer;getPrecipitationAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"
            ),
            remap = false
    )
    private Biome.Precipitation onTickRainParticles(
            WeatherEffectRenderer renderer, Level level, BlockPos blockPos) {
        Biome.Precipitation original = getPrecipitationAt(level, blockPos);

        if (!(level instanceof ClientLevel clientLevel)) {
            return original;
        }

        String dimension = clientLevel.dimension().location().toString();
        WeatherClient.ColoredRainInfo coloredRain = WeatherClient.getColoredRain(dimension);

        if (coloredRain == null || coloredRain.phase() != WeatherHolder.WeatherPhase.ACTIVE) {
            return original;
        }

        ColoredRain.RainType type = switch (original) {
            case RAIN -> coloredRain.rainBiomes();
            case SNOW -> coloredRain.snowBiomes();
            case NONE -> coloredRain.dryBiomes();
        };

        return switch (type) {
            case RAIN -> Biome.Precipitation.RAIN;
            case SNOW -> Biome.Precipitation.SNOW;
            case DRY -> Biome.Precipitation.NONE;
            case DEFAULT -> original;
        };
    }

    @Unique
    private Biome.Precipitation getPrecipitationAt(Level level, BlockPos blockPos) {
        if (!level.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()))) {
            return Biome.Precipitation.NONE;
        } else {
            Biome biome = level.getBiome(blockPos).value();
            return biome.getPrecipitationAt(blockPos, level.getSeaLevel());
        }
    }

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

        float intensity = coloredRain.intensity();

        if (!renderState.rainColumns.isEmpty()) {
            renderColoredInstances(
                    bufferSource,
                    renderState.rainColumns,
                    cameraPosition,
                    1.0f,
                    renderState.radius,
                    renderState.intensity,
                    coloredRain,
                    intensity,
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
                    intensity,
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
            float intensity,
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

            // Changing render columns.
            NullableValue<RainPrecipitation> nullablePrecipitation = getPrecipitation(columnInstance.x(), columnInstance.z())
                    .passOrDrop(pre -> pre.original() != pre.overridden());
            f3 *= nullablePrecipitation.matchPresent(
                    pre -> {
                        if (isRain) {
                            if (pre.original() == Biome.Precipitation.RAIN) {
                                return 1.0f - intensity;
                            }
                            else if (pre.overridden() == Biome.Precipitation.RAIN) {
                                return intensity;
                            }
                        }
                        else {
                            if (pre.original() == Biome.Precipitation.SNOW) {
                                return 1.0f - intensity;
                            }
                            else if (pre.overridden() == Biome.Precipitation.SNOW) {
                                return intensity;
                            }
                        }

                        return 1.0f;
                    },
                    1.0f
            );
            boolean transitionRequired = nullablePrecipitation.isPresent();

            // Change this for coloring.
            float alpha = transitionRequired? f3 : 1.0f;
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
