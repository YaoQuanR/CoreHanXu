package core.yaoquan.hanxu.render.mixin;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.render.data.WeatherClient;
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
        if (coloredRain == null || coloredRain.phase() == WeatherHolder.WeatherPhase.STILLNESS) {
            return;
        }

        cir.setReturnValue(coloredRain.skyColor());
    }
}
