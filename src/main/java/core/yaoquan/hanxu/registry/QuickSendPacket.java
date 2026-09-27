package core.yaoquan.hanxu.registry;

import core.yaoquan.hanxu.api.AttributeHolder;
import core.yaoquan.hanxu.api.TimeHolder;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.registry.event.payload.GeneralPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class QuickSendPacket {
    public static void sendRegisteredTermPacket(ServerPlayer player) {
        int timerCount = TimeHolder.getTotalInstanceCount();
        int attributeCount = AttributeHolder.getApiAttributes().size() + AttributeHolder.getCommandAttributes().size();
        int weatherCount = WeatherHolder.getApiWeatherDefinitions().size() + WeatherHolder.getCommandWeatherDefinitions().size();

        PacketDistributor.sendToPlayer(player, new GeneralPayload.RegisteredTermPacket(timerCount, attributeCount, weatherCount));
    }
}
