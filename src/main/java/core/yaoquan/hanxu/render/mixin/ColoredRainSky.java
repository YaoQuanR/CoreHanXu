package core.yaoquan.hanxu.render.mixin;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.render.data.WeatherClient;
import core.yaoquan.hanxu.util.tool.ColorHSV;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ConstantConditions")
@Mixin(ClientLevel.class)
public class ColoredRainSky {
    static {
        CoreHanXu.LOGGER.info("[HX] Mixin: Colored Rain -> Sky.");
    }

    @Inject(
            method = "getSkyColor",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void onGetSkyColor(Vec3 cameraPosition, float particleTicks, CallbackInfoReturnable<Integer> cir) {
        ClientLevel level = (ClientLevel) (Object) this;

        String dimension = level.dimension().location().toString();
        WeatherClient.ColoredRainInfo coloredRain = WeatherClient.getColoredRain(dimension);
        if (coloredRain == null || coloredRain.phase() != WeatherHolder.WeatherPhase.ACTIVE) {
            return;
        }

        // Get the current vanilla color, then modify with day brightness for color fixing, and transition with the process:
        int currentSky = cir.getReturnValue();
        float dayBrightness = Math.max(1.0f - ((level.getSkyDarken() - 3.0f) / (11.0f - 3.0f)), 0.2f);
        int transitionedSky = ColorHSV.adjustBrightness(coloredRain.skyColor(), dayBrightness);
        transitionedSky = ColorHSV.interpolate(currentSky, transitionedSky, coloredRain.progress());

        cir.setReturnValue(transitionedSky);
    }
}
