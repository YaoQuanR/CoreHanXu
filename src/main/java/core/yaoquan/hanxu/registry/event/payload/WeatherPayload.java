package core.yaoquan.hanxu.registry.event.payload;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.api.weather.ColoredRain;
import core.yaoquan.hanxu.render.data.WeatherClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class WeatherPayload {
    public record FogPacket(
            WeatherHolder.WeatherPhase phase,
            String dimension,
            int color,
            float currentDistance
    ) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<FogPacket> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CoreHanXu.MOD_ID, "fog_packet"));

        public static final StreamCodec<ByteBuf, FogPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.map(str -> {
                            try {
                                return WeatherHolder.WeatherPhase.valueOf(str.toUpperCase());
                            }
                            catch (IllegalArgumentException e) {
                                return WeatherHolder.WeatherPhase.IDLE;
                            }
                        }, wp ->
                                wp.name().toLowerCase()
                ), FogPacket::phase,
                ByteBufCodecs.STRING_UTF8, FogPacket::dimension,
                ByteBufCodecs.INT, FogPacket::color,
                ByteBufCodecs.FLOAT, FogPacket::currentDistance,
                FogPacket::new
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handleClient(FogPacket packet, IPayloadContext context) {
            context.enqueueWork(
                    () -> WeatherClient.updateFog(packet)
            ).exceptionally(e -> {
                CoreHanXu.LOGGER.error("[HX] Failed to handle fog packet: ", e);
                return null;
            });
        }
    }

    public record ColoredRainPacket(
            WeatherHolder.WeatherPhase phase,
            String dimension,
            int skyColor,
            int rainColor,
            int snowColor,
            int fogColor,
            float intensity,
            ColoredRain.RainTypeData rainTypeData
    ) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ColoredRainPacket> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CoreHanXu.MOD_ID, "colored_rain_packet"));

        public static final StreamCodec<ByteBuf, ColoredRainPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.map(str -> {
                            try {
                                return WeatherHolder.WeatherPhase.valueOf(str.toUpperCase());
                            }
                            catch (IllegalArgumentException e) {
                                return WeatherHolder.WeatherPhase.IDLE;
                            }
                        }, wp ->
                                wp.name().toLowerCase()
                ), ColoredRainPacket::phase,
                ByteBufCodecs.STRING_UTF8, ColoredRainPacket::dimension,
                ByteBufCodecs.INT, ColoredRainPacket::skyColor,
                ByteBufCodecs.INT, ColoredRainPacket::rainColor,
                ByteBufCodecs.INT, ColoredRainPacket::snowColor,
                ByteBufCodecs.INT, ColoredRainPacket::fogColor,
                ByteBufCodecs.FLOAT, ColoredRainPacket::intensity,
                ByteBufCodecs.STRING_UTF8.map(
                        ColoredRain.RainTypeData::decode,
                        ColoredRain.RainTypeData::encode
                ), ColoredRainPacket::rainTypeData,
                ColoredRainPacket::new
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handleClient(ColoredRainPacket packet, IPayloadContext context) {
            context.enqueueWork(
                    () -> WeatherClient.updateColoredRain(packet)
            ).exceptionally(e -> {
                CoreHanXu.LOGGER.error("[HX] Failed to handle colored rain packet: ", e);
                return null;
            });
        }
    }

    public record WindPacket(
            WeatherHolder.WeatherPhase phase,
            String dimension,
            double vectorX,
            double vectorY,
            double vectorZ,
            float intensity,
            boolean affectRain
    ) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<WindPacket> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CoreHanXu.MOD_ID, "wind_packet"));

        public static final StreamCodec<ByteBuf, WindPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.map(str -> {
                            try {
                                return WeatherHolder.WeatherPhase.valueOf(str.toUpperCase());
                            }
                            catch (IllegalArgumentException e) {
                                return WeatherHolder.WeatherPhase.IDLE;
                            }
                        }, wp ->
                                wp.name().toLowerCase()
                ), WindPacket::phase,
                ByteBufCodecs.STRING_UTF8, WindPacket::dimension,
                ByteBufCodecs.DOUBLE, WindPacket::vectorX,
                ByteBufCodecs.DOUBLE, WindPacket::vectorY,
                ByteBufCodecs.DOUBLE, WindPacket::vectorZ,
                ByteBufCodecs.FLOAT, WindPacket::intensity,
                ByteBufCodecs.BOOL, WindPacket::affectRain,
                WindPacket::new
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handleClient(WindPacket packet, IPayloadContext context) {
            // TODO
        }
    }
}
