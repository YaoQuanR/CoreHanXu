package core.yaoquan.hanxu.registry.gui;

import core.yaoquan.hanxu.registry.entity_block.LootDeployerEntity;
import core.yaoquan.hanxu.registry.entity_block.LootDeployerMenu;
import core.yaoquan.hanxu.registry.network.LootPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LootDeployerGUI extends Screen {
    private final LootDeployerMenu menu;

    private Button buttonToggleLootSource;
    private Button buttonLoadLoot;
    private Button buttonDeploy;

    private EditBox writeLootSource;
    private EditBox writeContainerIdSource;
    private EditBox writeContainerCustomName;

    private LootPreviewList lootPreviewList;
    private boolean isReadFromFile = true;
    private List<String> previewEntries = new ArrayList<>();

    private List<String> allBlockIds = new ArrayList<>();
    private SuggestionList suggestionList;

    public LootDeployerGUI(LootDeployerMenu menu, @Nullable Inventory inventory, Component title) {
        super(title);
        this.menu = menu;
    }

    @Override
    protected void init() {
        super.init();
        int screenCenterX = width / 2;
        int screenCenterY = height / 2;

        // Get registered block ids.
        allBlockIds = BuiltInRegistries.BLOCK.stream()
                .map(register -> BuiltInRegistries.BLOCK.getKey(register).toString())
                .collect(Collectors.toList());

        // Group 1: Switch, loot path, load loot table.
        buttonToggleLootSource = Button.builder(
                Component.translatable("button.gui.file"),
                button -> toggleNextSource()
        ).bounds(screenCenterX + 125, screenCenterY + 70, 45, 20).build();
        addRenderableWidget(buttonToggleLootSource);

        writeLootSource = new EditBox(
                font, screenCenterX - 15, screenCenterY - 80, 160, 20,
                Component.translatable("editbox.gui.write_loot_source")
        );
        writeLootSource.setValue("");
        addRenderableWidget(writeLootSource);

        buttonLoadLoot = Button.builder(
                Component.translatable("button.gui.load"),
                button -> requestPreview()
        ).bounds(screenCenterX + 150, screenCenterY - 80, 40, 20).build();
        addRenderableWidget(buttonLoadLoot);

        // Group 2: Preview loaded loot table information.
        lootPreviewList = new LootPreviewList(
                minecraft, 280, 100, screenCenterY - 30, screenCenterY + 50
        );
        lootPreviewList.setX(screenCenterX - 140);
        addRenderableWidget(lootPreviewList);

        // Group 3: Custom name, container id, deploy.
        writeContainerIdSource = new EditBox(
                font, screenCenterX - 130, screenCenterY + 70, 90, 20,
                Component.translatable("editbox.gui.write_container_source")
        );
        writeContainerIdSource.setValue("minecraft:chest");
        writeContainerIdSource.setResponder(this::updateContainerIdSuggestion);
        addRenderableWidget(writeContainerIdSource);

        writeContainerCustomName = new EditBox(
                font, screenCenterX - 30, screenCenterY + 70, 45, 20,
                Component.translatable("editbox.gui.write_object_name")
        );
        writeContainerCustomName.setValue("");
        addRenderableWidget(writeContainerCustomName);

        buttonDeploy = Button.builder(
                Component.translatable("button.gui.deploy"),
                button -> onDeploy()
        ).bounds(screenCenterX + 70, screenCenterY - 70, 45, 20).build();
        addRenderableWidget(buttonDeploy);

        suggestionList = new SuggestionList(screenCenterX - 130, screenCenterY + 92, 90);
        suggestionList.visible = false;
        addRenderableWidget(suggestionList);

        if (minecraft != null && minecraft.level != null) {
            var blockEntity = minecraft.level.getBlockEntity(menu.getBlockPos());
            if (blockEntity instanceof LootDeployerEntity lootDeployer) {
                writeLootSource.setValue(lootDeployer.getLootPath());
                writeContainerCustomName.setValue(lootDeployer.getContainerName());
                writeContainerIdSource.setValue(lootDeployer.getContainerId());
                if (!lootDeployer.getLootPath().isEmpty()) {
                    requestPreview();
                }
            }
        }
    }

    private void toggleNextSource() {
        isReadFromFile = !isReadFromFile;

        buttonToggleLootSource.setMessage(isReadFromFile?
                Component.translatable("button.gui.file") : Component.translatable("button.gui.registered_list")
                );

        previewEntries.clear();
        lootPreviewList.replaceEntries(previewEntries);

        writeLootSource.setValue("");
    }

    private void requestPreview() {
        String lootPath = writeLootSource.getValue();
        if (lootPath.isEmpty()) {
            return;
        }
        if (minecraft != null && minecraft.getConnection() != null) {
            minecraft.getConnection().send(
                    new ServerboundCustomPayloadPacket(
                            new LootPayloads.RequestPreview(lootPath, isReadFromFile)
                    )
            );
        }
    }

    private void updateContainerIdSuggestion(String inputText) {
        if (inputText.isEmpty()) {
            writeContainerIdSource.setSuggestion(null);
            return;
        }

        String matchedId = allBlockIds.stream()
                .filter(id -> id.startsWith(inputText))
                .findFirst()
                .orElse(null);
        if (matchedId != null && !matchedId.equals(inputText)) {
            String suggestion = matchedId.substring(inputText.length());
            writeContainerIdSource.setSuggestion(suggestion);
        }
        else {
            writeContainerIdSource.setSuggestion(null);
        }
    }

    private void closeContainerIdSuggestion() {
        suggestionList.visible = false;
        setFocused(writeContainerIdSource);
    }

    private void onReceivePreview(List<String> entries) {
        this.previewEntries = entries;
        this.lootPreviewList.replaceEntries(previewEntries);
    }

    private void onDeploy() {
        if (minecraft != null && minecraft.level != null) {
            var blockEntity = minecraft.level.getBlockEntity(menu.getBlockPos());
            if (blockEntity instanceof LootDeployerEntity lootDeployerEntity) {
                lootDeployerEntity.setLootPath(writeLootSource.getValue());
                lootDeployerEntity.setContainerName(writeContainerCustomName.getValue());
                lootDeployerEntity.setContainerId(writeContainerIdSource.getValue());
                if (lootDeployerEntity.isReady()) {
                    lootDeployerEntity.deploy();
                    onClose();
                }
            }
        }
    }

    private class LootPreviewList extends ObjectSelectionList<LootPreviewList.Entry> {
        private List<String> lines = new ArrayList<>();

        public LootPreviewList(Minecraft minecraft, int width, int height, int top, int bottom) {
            super(minecraft, width, height, top, bottom);
        }

        public void replaceEntries(List<String> lines) {
            this.lines = lines;
            clearEntries();

        }

        @Override
        public int getRowWidth() {
            return width - 10;
        }

        public List<String> getLines() {
            return lines;
        }

        private class Entry extends ObjectSelectionList.Entry<Entry> {
            private final String lineText;

            public Entry(String lineText) {
                this.lineText = lineText;
            }

            @Override
            public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
                int left = getX() + 5;
                int top = getY() + 2;
                guiGraphics.drawString(LootDeployerGUI.this.font, lineText, left, top, 0xFFFFFF);
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.literal(lineText);
            }
        }
    }

    private class SuggestionList extends AbstractContainerWidget {
        private List<String> matches = List.of();
        private int selectedIndex = 0;

        public SuggestionList(int x, int y, int width) {
            super(x, y, width, 0, Component.empty());
        }

        public void updateMatches(List<String> matches) {
            this.matches = matches;
            this.selectedIndex = 0;
            this.height = Math.min(matches.size(), 5) * 12;
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            if (!visible || matches.isEmpty()) {
                return;
            }

            guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0xCC000000);

            for (int i = 0; i < Math.min(matches.size(), 5); i++) {
                int yOffset = getY() + i * 12;
                if (i == selectedIndex) {
                    guiGraphics.fill(getX(), yOffset, getX() + width, yOffset, 0xFFAAAAAA);
                }
                String display = matches.get(i);
                guiGraphics.drawString(LootDeployerGUI.this.font, display, getX() + 2, yOffset + 2, 0xFFFFFF);
            }
        }

        public void next() {
            if (matches.isEmpty()) {
                return;
            }
            selectedIndex = (selectedIndex + 1) % matches.size();
        }

        public void previous() {
            if (matches.isEmpty()) {
                return;
            }
            selectedIndex = (selectedIndex - 1) % matches.size();
        }

        public void complete() {
            if (selectedIndex < matches.size()) {
                writeContainerIdSource.setValue(matches.get(selectedIndex));
                closeContainerIdSuggestion();
            }
        }

        @Override
        public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean p_435133_) {
            if (!visible || matches.isEmpty()) {
                return false;
            }
            double mouseX = event.x();
            double mouseY = event.y();
            if (mouseX >= getX() && mouseX <= getX() + width && mouseY >= getY() && mouseY <= getY() + height) {
                int relativeY = (int) (mouseY - getY());
                int clickedIndex = relativeY / 12;
                if (clickedIndex >= 0 && clickedIndex < matches.size()) {
                    selectedIndex = clickedIndex;
                    complete();
                    return true;
                }
            }
            return false;
        }

        @Override
        public int contentHeight() {
            return this.height;
        }

        @Override
        protected double scrollRate() {
            return 1.0;
        }

        @Override
        public void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
            if (!matches.isEmpty() && selectedIndex < matches.size()) {
                narrationElementOutput.add(NarratedElementType.TITLE, Component.literal(matches.get(selectedIndex)));
            }
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of();
        }
    }
}
