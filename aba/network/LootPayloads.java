package core.yaoquan.hanxu.registry.network;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.registry.gui.LootDeployerGUI;
import core.yaoquan.hanxu.util.Resolver;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LootPayloads {
    public record RequestPreview(String path, boolean readFromFile) implements CustomPacketPayload {
        public static final Type<RequestPreview> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(CoreHanXu.MOD_ID, "request_loot_preview")
        );

        public static final StreamCodec<FriendlyByteBuf, RequestPreview> STREAM_CODEC = StreamCodec.of(
                RequestPreview::encode, RequestPreview::decode
        );

        private static void encode(FriendlyByteBuf buf, RequestPreview packet) {
            buf.writeUtf(packet.path(), 32767);
            buf.writeBoolean(packet.readFromFile());
        }

        private static RequestPreview decode(FriendlyByteBuf buf) {
            String path = buf.readUtf(32767);
            boolean readFromFile = buf.readBoolean();
            return new RequestPreview(path, readFromFile);
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ResponsePreview(List<String> entries) implements CustomPacketPayload {
        public static final Type<ResponsePreview> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(CoreHanXu.MOD_ID, "response_loot_preview")
        );

        public static final StreamCodec<FriendlyByteBuf, ResponsePreview> STREAM_CODEC = StreamCodec.of(
                ResponsePreview::encode, ResponsePreview::decode
        );

        private static void encode(FriendlyByteBuf buf, ResponsePreview packet) {
            buf.writeInt(packet.entries().size());
            for (String entry : packet.entries()) {
                buf.writeUtf(entry, 32767);
            }
        }

        private static ResponsePreview decode(FriendlyByteBuf buf) {
            int size = buf.readInt();
            List<String> entries = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                entries.add(buf.readUtf(32767));
            }
            return new ResponsePreview(entries);
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @EventBusSubscriber(modid = CoreHanXu.MOD_ID)
    public class RegisterNetworking {
        @SubscribeEvent
        public static void register(final RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("1");

            // Request to server.
            registrar.playBidirectional(
                    RequestPreview.TYPE,
                    RequestPreview.STREAM_CODEC,
                    (payload, context) -> {
                        // Server will handle this.
                        if (context.flow().isServerbound()) {
                            context.enqueueWork(() -> {
                                ServerPlayer player = (ServerPlayer) context.player();
                                List<String> entries = new ArrayList<>();

                                String path = payload.path();
                                boolean isReadFromFile = payload.readFromFile();

                                List<String> wait = List.of("wait to finish...");
                                context.reply(new LootPayloads.ResponsePreview(wait));
                            });
                        }
                    }
            );

            // Response to client.
            registrar.playBidirectional(
                    ResponsePreview.TYPE,
                    ResponsePreview.STREAM_CODEC,
                    (payload, context) -> {
                        // Client will handle this.
                        if (context.flow().isClientbound()) {
                            context.enqueueWork(() -> {
                                if (Minecraft.getInstance().screen instanceof LootDeployerGUI gui) {

                                }
                            });
                        }
                    }
            );
        }
    }
}
