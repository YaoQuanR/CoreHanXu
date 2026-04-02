package core.yaoquan.hanxu.event;
import core.yaoquan.hanxu.CoreHanXu;
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
        displayLines.add("Waiting for more information...");

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
}
