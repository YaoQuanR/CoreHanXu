package core.yaoquan.hanxu.registry.event;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.registry.event.payload.GeneralPayload;
import core.yaoquan.hanxu.registry.event.payload.WeatherPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModNetwork {
    public static class TimerF4Client {
        private static final Map<String, TimerInfo> localInfoDisplayTimer = new ConcurrentHashMap<>();

        public static void add(GeneralPayload.TimerF4Packet packet) {
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

    public static class AttributeF4Client {
        private static final Map<String, AttributeInfo> localInfoDisplayAttribute = new ConcurrentHashMap<>();

        public static void add(GeneralPayload.AttributeF4Packet packet) {
            UUID masterId = packet.masterId();
            String attributeId = packet.attributeId();
            String key = masterId.toString() + ":" + attributeId;

            localInfoDisplayAttribute.put(key, new AttributeInfo(packet.value(), packet.isApiAttribute(), packet.masterName()));
        }

        public static void remove(UUID masterId, String attributeId) {
            String key = masterId.toString() + ":" + attributeId;
            localInfoDisplayAttribute.remove(key);
        }

        public static  void clear() {
            localInfoDisplayAttribute.clear();
        }

        public static Set<String> getAllInfoKeys() {
            return localInfoDisplayAttribute.keySet();
        }

        public static AttributeInfo getAttributeInfo(String key) {
            return localInfoDisplayAttribute.get(key);
        }

        public static boolean isInfoDisplay(UUID masterId, String attributeId) {
            String key = masterId.toString() + ":" + attributeId;

            Map<String, AttributeInfo> compareMap = new ConcurrentHashMap<>();
            compareMap.put(key, getAttributeInfo(key));

            return localInfoDisplayAttribute.equals(compareMap);
        }

        public record AttributeInfo(float value, boolean fromApi, String masterName) {}
    }

    public static class WeatherF4Client {
        private static final Map<String, WeatherInfo> localInfoDisplayWeather = new ConcurrentHashMap<>();

        public static void add(GeneralPayload.WeatherF4Packet packet) {
            String key = packet.dimension() + ":" + packet.weatherId();
            localInfoDisplayWeather.put(key, new WeatherInfo(
                    packet.weatherType(),
                    packet.phase(),
                    packet.remainingTicks(),
                    packet.initialTicks(),
                    packet.durationTicks(),
                    packet.stillnessTicks()
            ));
        }

        public static void remove(String dimension, String weatherId) {
            String key = dimension + ":" + weatherId;
            localInfoDisplayWeather.remove(key);
        }

        public static void clear() {
            localInfoDisplayWeather.clear();
        }

        public static Set<String> getAllInfoKeys() {
            return localInfoDisplayWeather.keySet();
        }

        public static WeatherInfo getWeatherInfo(String key) {
            return localInfoDisplayWeather.get(key);
        }

        public record WeatherInfo(
                WeatherHolder.WeatherType type,
                WeatherHolder.WeatherPhase phase,
                int remainingTicks,
                int initialTicks,
                int durationTicks,
                int stillnessTicks
        ) {}
    }

    @EventBusSubscriber(modid = CoreHanXu.MOD_ID)
    public static class RegisterNetworking {
        @SubscribeEvent
        public static void register(final RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");

            registrar.playToClient(
                    GeneralPayload.TimerF4Packet.TYPE,
                    GeneralPayload.TimerF4Packet.STREAM_CODEC,
                    GeneralPayload.TimerF4Packet::handleClient
            );

            registrar.playToClient(
                    GeneralPayload.AttributeF4Packet.TYPE,
                    GeneralPayload.AttributeF4Packet.STREAM_CODEC,
                    GeneralPayload.AttributeF4Packet::handleClient
            );

            registrar.playToClient(
                    GeneralPayload.WeatherF4Packet.TYPE,
                    GeneralPayload.WeatherF4Packet.STREAM_CODEC,
                    GeneralPayload.WeatherF4Packet::handleClient
            );

            registrar.playToClient(
                    WeatherPayload.FogPacket.TYPE,
                    WeatherPayload.FogPacket.STREAM_CODEC,
                    WeatherPayload.FogPacket::handleClient
            );

            registrar.playToClient(
                    WeatherPayload.ColoredRainPacket.TYPE,
                    WeatherPayload.ColoredRainPacket.STREAM_CODEC,
                    WeatherPayload.ColoredRainPacket::handleClient
            );

            registrar.playToClient(
                    WeatherPayload.WindPacket.TYPE,
                    WeatherPayload.WindPacket.STREAM_CODEC,
                    WeatherPayload.WindPacket::handleClient
            );
        }
    }
}
