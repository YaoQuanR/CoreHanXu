package core.yaoquan.hanxu.api.define;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

public class FilePath {
    public static Path getModDataPath(ServerLevel level) {
        return level.getServer().getWorldPath(LevelResource.ROOT).resolve("data").resolve("core_hanxu.dat");
    }
}
