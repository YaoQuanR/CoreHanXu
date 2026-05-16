package core.yaoquan.hanxu.registry.event;

import core.yaoquan.hanxu.CoreHanXu;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModNetwork {
    public static class ClientF4Display {
        private static final Set<String> localInfoDisplayTimer = ConcurrentHashMap.newKeySet();

        public static void add(UUID masterId, String timerId) {
            String key = masterId.toString() + ":" + timerId;
            localInfoDisplayTimer.add(key);
        }

        public static void remove(UUID masterId, String timerId) {
            String key = masterId.toString() + ":" + timerId;
            localInfoDisplayTimer.remove(key);
        }

        public static void clear() {
            localInfoDisplayTimer.clear();
        }

        public static Set<String> getAllInfoKeys() {
            return localInfoDisplayTimer;
        }

        public static boolean isInfoDisplay(UUID masterId, String timerId) {
            String masterIdString = masterId.toString();
            String key = masterIdString + ":" + timerId;

            return localInfoDisplayTimer.contains(key);
        }
    }

    @EventBusSubscriber(modid = CoreHanXu.MOD_ID)
    public static class RegisterNetworking {
        @SubscribeEvent
        public static void register(final RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");

            registrar.playToClient(
                    ModPayload.F4DisplayPacket.TYPE,
                    ModPayload.F4DisplayPacket.STREAM_CODEC,
                    ModPayload.F4DisplayPacket::handleClient
            );
        }
    }
}
