package core.yaoquan.hanxu.registry.event.payload;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.registry.event.ModNetwork;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GeneralPayload {
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

    public record AttributeF4Packet(UUID masterId, String attributeId, boolean isVisible, float value, boolean isApiAttribute, String masterName)
            implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<AttributeF4Packet> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CoreHanXu.MOD_ID, "attribute_f4_packet"));

        public static final StreamCodec<ByteBuf, AttributeF4Packet> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), AttributeF4Packet::masterId,
                ByteBufCodecs.STRING_UTF8, AttributeF4Packet::attributeId,
                ByteBufCodecs.BOOL, AttributeF4Packet::isVisible,
                ByteBufCodecs.FLOAT, AttributeF4Packet::value,
                ByteBufCodecs.BOOL, AttributeF4Packet::isApiAttribute,
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

    public record WeatherF4Packet(String weatherId, WeatherHolder.WeatherType weatherType, WeatherHolder.WeatherPhase phase, int remainingTicks, int initialTicks, int durationTicks, int stillnessTicks, boolean isVisible, String dimension)
            implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<WeatherF4Packet> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CoreHanXu.MOD_ID, "weather_f4_packet"));

        public static final StreamCodec<ByteBuf, WeatherF4Packet> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, WeatherF4Packet::weatherId,
                ByteBufCodecs.STRING_UTF8.map(str -> {
                            try {
                                return WeatherHolder.WeatherType.valueOf(str.toUpperCase());
                            }
                            catch (IllegalArgumentException e) {
                                return WeatherHolder.WeatherType.NULL;
                            }
                        }, wt ->
                                wt.name().toLowerCase()
                ), WeatherF4Packet::weatherType,
                ByteBufCodecs.STRING_UTF8.map(str -> {
                            try {
                                return WeatherHolder.WeatherPhase.valueOf(str.toUpperCase());
                            }
                            catch (IllegalArgumentException e) {
                                return WeatherHolder.WeatherPhase.IDLE;
                            }
                        }, wp ->
                                wp.name().toLowerCase()
                ), WeatherF4Packet::phase,
                ByteBufCodecs.INT, WeatherF4Packet::remainingTicks,
                ByteBufCodecs.INT, WeatherF4Packet::initialTicks,
                ByteBufCodecs.INT, WeatherF4Packet::durationTicks,
                ByteBufCodecs.INT, WeatherF4Packet::stillnessTicks,
                ByteBufCodecs.BOOL, WeatherF4Packet::isVisible,
                ByteBufCodecs.STRING_UTF8, WeatherF4Packet::dimension,
                WeatherF4Packet::new
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handleClient(WeatherF4Packet packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (packet.isVisible()) {
                    ModNetwork.WeatherF4Client.add(packet);
                }
                else {
                    ModNetwork.WeatherF4Client.remove(packet.dimension(), packet.weatherId());
                }
            }).exceptionally(e -> {
                CoreHanXu.LOGGER.error("[HX] Failed to update F4 list to weather system: ", e);
                return null;
            });
        }
    }
}
