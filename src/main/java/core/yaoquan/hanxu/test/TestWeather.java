package core.yaoquan.hanxu.test;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.api.weather.Fog;
import core.yaoquan.hanxu.util.type.MethodResult;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Random;

public final class TestWeather {
    public static void testFog() {
        Random random = new Random(30000L);

        Fog blueFog = new Fog("blue_fog", random)
                .color(0xCBE2F2)
                .distance(8, 128)
                .duration(200, 400)
                .stillness(200, 400)
                .heightOffset(64f, 2f)
                .heightOffset(128f, 1f)
                .heightOffset(192f, 0.5f);

        WeatherHolder.register(blueFog);
        WeatherHolder.pickupUnclaimedStates();

        CoreHanXu.LOGGER.info("[HX] Registered test fog: blue_fog.");
    }

    public static void displayTestFog() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        ServerPlayer player = server.getPlayerList().getPlayerByName("Dev");
        if (player == null) {
            return;
        }
        ServerLevel level = server.getLevel(ServerLevel.OVERWORLD);

        if (!WeatherHolder.doesWeatherStateExist(level, "blue_fog")) {
            MethodResult result = WeatherHolder.restartWeather(level, "blue_fog");
            CoreHanXu.LOGGER.info("[HX] Started test fog 'blue_fog' {}", result.isSuccess()? "successfully." : "failed.");
        }
        WeatherHolder.displayToInfoPage(player, level, "blue_fog", true);
    }
}
