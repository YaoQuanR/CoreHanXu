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
    public record TimerF4Packet(UUID masterId, String timerId, boolean isVisible, int remainingTicks, boolean isCounting, String masterName)
            implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<TimerF4Packet> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CoreHanXu.MOD_ID, "timer_f4_packet"));

        public static final StreamCodec<ByteBuf, TimerF4Packet> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), TimerF4Packet::masterId,
                ByteBufCodecs.STRING_UTF8, TimerF4Packet::timerId,
                ByteBufCodecs.BOOL, TimerF4Packet::isVisible,
                ByteBufCodecs.INT, TimerF4Packet::remainingTicks,
                ByteBufCodecs.BOOL, TimerF4Packet::isCounting,
                ByteBufCodecs.STRING_UTF8, TimerF4Packet::masterName,
                TimerF4Packet::new
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handleClient(TimerF4Packet packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (packet.isVisible()) {
                    ModNetwork.TimerF4Client.add(packet);
                }
                else {
                    ModNetwork.TimerF4Client.remove(packet.masterId(), packet.timerId());
                }
            }).exceptionally(e -> {
                CoreHanXu.LOGGER.error("[HX] Failed to update F4 list to timer system: ", e);
                return null;
            });
        }
    }

    public record AttributeF4Packet(UUID masterId, String attributeId, boolean isVisible, float value, boolean fromApi, String masterName)
            implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<AttributeF4Packet> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CoreHanXu.MOD_ID, "attribute_f4_packet"));

        public static final StreamCodec<ByteBuf, AttributeF4Packet> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), AttributeF4Packet::masterId,
                ByteBufCodecs.STRING_UTF8, AttributeF4Packet::attributeId,
                ByteBufCodecs.BOOL, AttributeF4Packet::isVisible,
                ByteBufCodecs.FLOAT, AttributeF4Packet::value,
                ByteBufCodecs.BOOL, AttributeF4Packet::fromApi,
                ByteBufCodecs.STRING_UTF8, AttributeF4Packet::masterName,
                AttributeF4Packet::new
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handleClient(AttributeF4Packet packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (packet.isVisible()) {
                    ModNetwork.AttributeF4Client.add(packet);
                }
                else {
                    ModNetwork.AttributeF4Client.remove(packet.masterId(), packet.attributeId());
                }
            }).exceptionally(e -> {
                CoreHanXu.LOGGER.error("[HX] Failed to update F4 list to attribute system: ", e);
                return null;
            });
        }
    }
}
