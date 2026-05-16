package core.yaoquan.hanxu.registry.event;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.TimeHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = CoreHanXu.MOD_ID, value = Dist.CLIENT)
public class ModInfoOverlay {
    private static boolean isShownInfo = false;

    // Listen to the key pressed:
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        // Check if exist player.
        if (mc.player == null) return;

        // Switch stage when pressed key.
        while (ModKey.INFO_KEY.get().consumeClick()) {
            isShownInfo = !isShownInfo;
        }
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
        displayLines.add("Loaded.");

        int displayedTimer = 0;
        displayLines.add("");
        displayLines.add("-> Timer");
        for (String key : ModNetwork.ClientF4Display.getAllInfoKeys()) {
            if (displayedTimer < 10) {
                String[] parts = key.split(":", 2);
                UUID masterId = UUID.fromString(parts[0]);
                String timerId = parts[1];

                int remainingTime = TimeHolder.getRemainingTimeFromInstance(masterId, timerId, "tick");
                String masterName = returnMasterName(masterId);

                if (remainingTime == -1) {
                    displayLines.add("(" + timerId + " -> " + masterName + ") REMOVED");
                }
                else {
                    int remainingTicks = remainingTime % (20);
                    int remainingSeconds = TimeHolder.getRemainingTimeFromInstance(masterId, timerId, "second") % 60;
                    int remainingMinutes = TimeHolder.getRemainingTimeFromInstance(masterId, timerId, "minute") % 60;
                    int remainingHours = TimeHolder.getRemainingTimeFromInstance(masterId, timerId, "hour");
                    boolean isItCounting = TimeHolder.isInstanceTimerCounting(masterId, timerId);
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

    private static String returnMasterName(UUID masterId) {
        if (TimeHolder.GLOBAL_UUID.equals(masterId)) {
            return "-global";
        }
        else if (TimeHolder.TEMPORARY_UUID.equals(masterId)) {
            return "-temporary";
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ServerPlayer player = server.getPlayerList().getPlayer(masterId);
            if (player != null) {
                return player.getName().getString();
            }
        }

        // If no pair target exist, return this.
        return "-not_found";
    }
}
