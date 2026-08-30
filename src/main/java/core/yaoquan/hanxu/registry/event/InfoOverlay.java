package core.yaoquan.hanxu.registry.event;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.TimeHolder;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.api.define.General;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = CoreHanXu.MOD_ID, value = Dist.CLIENT)
public class InfoOverlay {
    private static boolean isShownInfo = false;

    // Listen to the key pressed:
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        // Check if exist player.
        if (mc.player == null) return;

        // Switch stage when pressed key.
        while (KeyBinding.INFO_KEY.get().consumeClick()) {
            isShownInfo = !isShownInfo;
        }

        NetworkData.TimerF4Client.tick();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!isShownInfo) return;

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics gui = event.getGuiGraphics();
        Font font = mc.font;

        // Information of ready to show:
        List<String> displayLines = new ArrayList<>();
        displayLines.add("-= Core HanXu Information =-");
        displayLines.add("HanXu (Core) Powered Engine, License under License.txt & Assistant.txt.");
        displayLines.add("Loaded version: " + General.Version.getCoreVersion());

        int maximumDisplay = 10;

        int displayedTimer = 0;
        int displayedAttribute = 0;
        int displayedWeather = 0;

        // Count of registered term.
        int timerCount = NetworkData.RegisteredTermClient.getTimerCount();
        int attributeCount = NetworkData.RegisteredTermClient.getAttributeCount();
        int weatherCount = NetworkData.RegisteredTermClient.getWeatherCount();

        displayLines.add("");
        displayLines.add("-> Timer (Registered: " + timerCount + ")");
        for (String key : NetworkData.TimerF4Client.getAllInfoKeys()) {
            if (displayedTimer < maximumDisplay) {
                String[] parts = key.split(":", 2);
                String timerId = parts[1];

                NetworkData.TimerF4Client.TimerInfo timerInfo = NetworkData.TimerF4Client.getTimerInfo(key);
                int remainingTime = timerInfo.remainingTicks();
                String masterName = timerInfo.masterName();

                if (remainingTime == -1) {
                    displayLines.add("(" + timerId + " -> " + masterName + ") REMOVED");
                }
                else {
                    int remainingTicks = returnUnitRemains(remainingTime, "tick");
                    int remainingSeconds = returnUnitRemains(remainingTime, "second");
                    int remainingMinutes = returnUnitRemains(remainingTime, "minute");
                    int remainingHours = returnUnitRemains(remainingTime, "hour");
                    boolean isItCounting = timerInfo.isCounting();
                    displayLines.add("(" + timerId + " -> " + masterName + ") " + remainingHours + ":" + remainingMinutes + ":" + remainingSeconds + ":" + remainingTicks + (isItCounting? " (-)" : " (#)"));
                }

                displayedTimer++;
            }
            else {
                displayLines.add("And more...");
                break;
            }
        }

        displayLines.add("");
        displayLines.add("-> Attribute (Registered: " + attributeCount + ")");
        for (String key : NetworkData.AttributeF4Client.getAllInfoKeys()) {
            if (displayedAttribute < maximumDisplay) {
                String[] parts = key.split(":", 2);
                String attributeId = parts[1];
                NetworkData.AttributeF4Client.AttributeInfo attributeInfo = NetworkData.AttributeF4Client.getAttributeInfo(key);

                String displayValue = String.format("%.2f", attributeInfo.value());

                if (attributeInfo.value() == -1.0f) {
                    displayLines.add("(" + attributeId + " -> " + attributeInfo.masterName() + ") REMOVED");
                }
                else {
                    displayLines.add("(" + attributeId + " -> " + attributeInfo.masterName() + ") " + displayValue);
                }
                displayedAttribute++;
            }
            else {
                displayLines.add("And more...");
                break;
            }
        }

        displayLines.add("");
        displayLines.add("-> Weather (Registered: " + weatherCount + ")");
        for (String key : NetworkData.WeatherF4Client.getAllInfoKeys()) {
            if (displayedWeather < maximumDisplay) {
                String[] parts = key.split(":", 3);
                String dimension = parts[0] + ":" + parts[1];
                String weatherId = parts[2];
                NetworkData.WeatherF4Client.WeatherInfo weatherInfo = NetworkData.WeatherF4Client.getWeatherInfo(key);

                if (weatherInfo != null && weatherInfo.type() != WeatherHolder.WeatherType.NULL) {
                    WeatherHolder.WeatherPhase phase = weatherInfo.phase();
                    int nextTicks = phase == WeatherHolder.WeatherPhase.ACTIVE? weatherInfo.stillnessTicks() : weatherInfo.durationTicks();

                    displayLines.add("(" + weatherId + " -> " + dimension + ") " + weatherInfo.phase().name().toLowerCase() + " <<< " + weatherInfo.remainingTicks() + " / " + weatherInfo.initialTicks() + " <... " + nextTicks + " (t)");
                }
                else {
                    displayLines.add("(" + weatherId + " -> " + dimension + ") LOST");
                }
                displayedWeather++;
            }
            else {
                displayLines.add("And more...");
                break;
            }
        }

        displayLines.add("");
        displayLines.add("[Press the key again to close]");

        int maxWidth = 0;
        for (String line : displayLines) {
            int width = font.width(line);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        int totalHeight = displayLines.size() * font.lineHeight;

        int textX = 3, textY = 3;
        int padding = 2;

        gui.fill(textX - padding, textY - padding,
                 textX + maxWidth + padding, textY + totalHeight + padding,
                 0x33000000);

        int currentY = textY;
        for (String line : displayLines) {
            gui.drawString(font, line, textX, currentY, 0xFFFFFFFF, false);
            currentY += font.lineHeight;
        }
    }

    private static int returnUnitRemains(int remainingTicks, String unit) {
        return switch (unit) {
            case "t", "tick" -> remainingTicks % TimeHolder.TICKS_PER_SECOND;
            case "s", "second" -> (remainingTicks / TimeHolder.TICKS_PER_SECOND) % 60;
            case "m", "minute" -> (remainingTicks / TimeHolder.TICKS_PER_MINUTE) % 60;
            case "h", "hour" -> remainingTicks / TimeHolder.TICKS_PER_HOUR;
            default -> -1;
        };
    }
}
