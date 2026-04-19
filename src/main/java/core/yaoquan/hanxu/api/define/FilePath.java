package core.yaoquan.hanxu.api.define;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FilePath {
    private static final String BASE_PATH = "core_hanxu";

    public static Path getModDataPath(ServerLevel level) {
        return level.getServer().getWorldPath(LevelResource.ROOT).resolve("data").resolve("core_hanxu.dat");
    }

    // Get global path for YAML.
    public static Path getGlobalPath() {
        return Paths.get("config", BASE_PATH);
    }

    // Get current save (world) path for YAML.
    public static Path getWorldPath() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }

        return server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(BASE_PATH);
    }

    // Get defined Yaml subpath.
    public static final List<String> SUB_DIRS = List.of(
            "scene",
            "galaxy"
    );
}
