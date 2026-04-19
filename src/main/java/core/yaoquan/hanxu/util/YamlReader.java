package core.yaoquan.hanxu.util;

import core.yaoquan.hanxu.api.define.FilePath;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.yaml.snakeyaml.Yaml;

import static core.yaoquan.hanxu.api.define.Error.*;

public class YamlReader {
    private static final Yaml YAML = new Yaml();

    private static final Map<Path, Long> lastModified = new ConcurrentHashMap<>();

    private static ScheduledExecutorService executor = null;

    public enum TargetPath {
        TO_WORLD,
        TO_GLOBAL,
    }

    // YAML reader.
    public static Map<String, Object> readSpecificFile(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return YAML.load(reader);
        }
        // throw IOException.
    }

    // Read the files.
    public static Map<String, Object> read(String subPath, String fileName) throws IOException {
        // Read world (save) YAML:
        // .minecraft\saves\[save]\data\core_hanxu\...
        Path worldPath = FilePath.getWorldPath();
        // If existed, read.
        if (worldPath != null) {
            // .minecraft\saves\[save]\data\core_hanxu\[subPath]\[fileName].yaml
            Path worldSpecificFile = worldPath.resolve(subPath).resolve(fileName);
            if (Files.exists(worldSpecificFile)) {
                return readSpecificFile(worldSpecificFile);
            }
        }

        // Then read global YAML:
        // .minecraft\config\core_hanxu\[subPath]\[fileName].yaml
        Path globalPath = FilePath.getGlobalPath().resolve(subPath).resolve(fileName);
        if (Files.exists(globalPath)) {
            return readSpecificFile(globalPath);
        }

        // Else just throw nothing.
        throw new FileNotFoundException(returnCodeError(CodeError.yamlFileNotFound) + subPath + "\\" + fileName);
    }

    // List out all YAML files.
    public static List<Path> listOut(String subPath) {
        List<Path> returnList = new ArrayList<>();

        Path worldRootPath = FilePath.getWorldPath();
        if (worldRootPath != null) {
            Path worldDirectory = worldRootPath.resolve(subPath);
            if (Files.isDirectory(worldDirectory)) {
                try (Stream<Path> stream = Files.list(worldDirectory)) {
                    stream.filter(p -> p.toString().endsWith(".yaml")).forEach(returnList::add);
                }
                catch (IOException ignored) {}
            }
        }

        Path globalDirectory = FilePath.getGlobalPath().resolve(subPath);
        if (Files.isDirectory(globalDirectory)) {
            try (Stream<Path> stream = Files.list(globalDirectory)) {
                stream.filter(p -> p.toString().endsWith(".yaml")).forEach(returnList::add);
            }
            catch (IOException ignored) {}
        }

        return returnList;
    }

    // Save YAML files.
    public static void save(String subPath, String fileName, Map<String, Object> data, TargetPath targetPath) throws IOException {
        Path targetRootPath;
        if (targetPath == TargetPath.TO_WORLD) {
            targetRootPath = FilePath.getWorldPath();
        }
        else if (targetPath == TargetPath.TO_GLOBAL) {
            targetRootPath = FilePath.getGlobalPath();
        }
        else {
            throw new IOException(returnCodeError(CodeError.unexceptedTarget) + targetPath.toString());
        }

        if (targetRootPath == null) {
            throw new IOException(returnCodeError(CodeError.unavailableTargetPath));
        }

        Path targetFile = targetRootPath.resolve(subPath).resolve(fileName);
        Files.createDirectories(targetFile.getParent());

        try (Writer writer = Files.newBufferedWriter(targetFile)) {
            YAML.dump(data, writer);
        }
    }

    // Watcher available user to hot reload changed file by each sub path.
    public static void startWatcher(Map<String, Runnable> callbacks) {
        if (executor != null && !executor.isShutdown()) {
            return;
        }

        executor = Executors.newSingleThreadScheduledExecutor();
        for (String subPath : FilePath.SUB_DIRS) {
            Runnable callback = callbacks.get(subPath);

            // Skip unknown callback.
            if (callback == null) {
                continue;
            }

            executor.scheduleAtFixedRate(() -> {
                hotReload(subPath, callback);
            }, 0, 1, TimeUnit.SECONDS);
        }
    }

    private static void hotReload(String subPath, Runnable callback) {
        List<Path> files = listOut(subPath);
        for (Path file : files) {
            try {
                long currentTime = Files.getLastModifiedTime(file).toMillis();
                long lastTime = lastModified.getOrDefault(file, 0L);
                if (currentTime > lastTime) {
                    lastModified.put(file, currentTime);
                    callback.run();
                }
            }
            catch (IOException ignored) {}
        }
    }

    // Stop watcher.
    public static void stopWatcher() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
