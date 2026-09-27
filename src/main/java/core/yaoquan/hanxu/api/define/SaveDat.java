package core.yaoquan.hanxu.api.define;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.VariableHolder;
import core.yaoquan.hanxu.api.WeatherHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.nio.file.Path;

public final class SaveDat {
    public enum HeadKey {
        variables("core.yaoquan.hanxu.variables"),
        weathers("core.yaoquan.hanxu.weathers");

        private final String headKey;

        HeadKey(String headKey) {
            this.headKey = headKey;
        }

        public String get() {
            return headKey;
        }
    }

    /**
     * <p><b>
     *     Inner Method
     * </b></p>
     * <p>
     *     Pay for your own risk while using this function out of HanXu (Core) Powered Engine.
     * </p>
     */
    @ApiStatus.Internal
    public static void saveToWorld(ServerLevel level) {
        if (level == null) {
            return;
        }

        CompoundTag root = new CompoundTag();

        VariableHolder.packAllVariables()
                .ifPresent(tag -> {
                    root.put(HeadKey.variables.get(), tag);
                    CoreHanXu.LOGGER.info("[HX] Save Procedure: Variables in {}", level);
                });

        WeatherHolder.packAllLevelStates()
                .ifPresent(tag -> {
                    root.put(HeadKey.weathers.get(), tag);
                    CoreHanXu.LOGGER.info("[HX] Save Procedure: Weather States in {}", level);
                });

        if (root.keySet().isEmpty()) {
            CoreHanXu.LOGGER.info("[HX] No core_hanxu.dat data required to save.");
            return;
        }

        Path file = FilePath.getModDataPath(level);
        try {
            NbtIo.writeCompressed(root, file.toFile().toPath());
            CoreHanXu.LOGGER.info("[HX] Save Procedure: Saved core_hanxu.dat.");
        }
        catch (IOException e) {
            CoreHanXu.LOGGER.error("[HX] Failed to save core_hanxu.dat in file {}", file, e);
        }
    }
}
