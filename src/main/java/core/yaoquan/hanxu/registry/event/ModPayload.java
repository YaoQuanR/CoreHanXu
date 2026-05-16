package core.yaoquan.hanxu.registry.event;

import core.yaoquan.hanxu.CoreHanXu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ModPayload {
    public record F4DisplayPacket(UUID masterId, String timerId, boolean isVisible, int remainingTicks, boolean isCounting, String masterName)
            implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<F4DisplayPacket> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CoreHanXu.MOD_ID, "f4_display_packet"));

        public static final StreamCodec<ByteBuf, F4DisplayPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), F4DisplayPacket::masterId,
                ByteBufCodecs.STRING_UTF8, F4DisplayPacket::timerId,
                ByteBufCodecs.BOOL, F4DisplayPacket::isVisible,
                ByteBufCodecs.INT, F4DisplayPacket::remainingTicks,
                ByteBufCodecs.BOOL, F4DisplayPacket::isCounting,
                ByteBufCodecs.STRING_UTF8, F4DisplayPacket::masterName,
                F4DisplayPacket::new
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handleClient(F4DisplayPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (packet.isVisible()) {
                    ModNetwork.ClientF4Display.add(packet);
                }
                else {
                    ModNetwork.ClientF4Display.remove(packet.masterId(), packet.timerId());
                }
            }).exceptionally(e -> {
                CoreHanXu.LOGGER.error("[HX] Failed to update F4 list to timer system: ", e);
                return null;
            });
        }
    }
}
