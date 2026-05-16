package core.yaoquan.hanxu.registry.event;

import core.yaoquan.hanxu.CoreHanXu;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModNetwork {
    public static class ClientF4Display {
        private static final Map<String, TimerInfo> localInfoDisplayTimer = new ConcurrentHashMap<>();

        public static void add(ModPayload.F4DisplayPacket packet) {
            UUID masterId = packet.masterId();
            String timerId = packet.timerId();
            String key = masterId.toString() + ":" + timerId;
            localInfoDisplayTimer.put(key, new TimerInfo(packet.remainingTicks(), packet.isCounting(), packet.masterName()));
        }

        public static void remove(UUID masterId, String timerId) {
            String key = masterId.toString() + ":" + timerId;
            localInfoDisplayTimer.remove(key);
        }

        public static void clear() {
            localInfoDisplayTimer.clear();
        }

        public static Set<String> getAllInfoKeys() {
            return localInfoDisplayTimer.keySet();
        }

        public static TimerInfo getTimerInfo(String key) {
            return localInfoDisplayTimer.get(key);
        }

        public static boolean isInfoDisplay(UUID masterId, String timerId) {
            String masterIdString = masterId.toString();
            String key = masterIdString + ":" + timerId;

            Map<String, TimerInfo> compareMap = new ConcurrentHashMap<>();
            compareMap.put(key, getTimerInfo(key));

            return localInfoDisplayTimer.equals(compareMap);
        }

        public record TimerInfo(int remainingTicks, boolean isCounting, String masterName) {}

        public static void tick() {
            for (Map.Entry<String, TimerInfo> entry : localInfoDisplayTimer.entrySet()) {
                TimerInfo timerInfo = entry.getValue();
                if (timerInfo.isCounting() && timerInfo.remainingTicks > 0) {
                    localInfoDisplayTimer.put(
                            entry.getKey(),
                            new TimerInfo(timerInfo.remainingTicks - 1, true, timerInfo.masterName())
                    );
                }
            }
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
