package core.yaoquan.hanxu.api.solution;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.VariableHolder;
import core.yaoquan.hanxu.api.WeatherHolder;
import core.yaoquan.hanxu.api.define.FilePath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;

import java.io.IOException;
import java.nio.file.Path;

public class SaveDat {
    public static void saveToWorld(ServerLevel level) {
        CompoundTag root = new CompoundTag();

        String variableHeadKey = "core.yaoquan.hanxu.variables";
        String weatherHeadKey = "core.yaoquan.hanxu.weathers";


        CoreHanXu.LOGGER.info("[HX] Save Procedure: Weather States");

        Path file = FilePath.getModDataPath(level);
        try {
            NbtIo.writeCompressed(root, file.toFile().toPath());
        }
        catch (IOException e) {
            CoreHanXu.LOGGER.warn("[HX] Failed to save world dat ", e);
        }
    }
}
