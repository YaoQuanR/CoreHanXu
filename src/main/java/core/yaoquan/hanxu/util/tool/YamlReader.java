package core.yaoquan.hanxu.util.tool;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import org.yaml.snakeyaml.Yaml;

import static core.yaoquan.hanxu.api.define.Error.*;

public final class YamlReader {
    private static final Yaml YAML = new Yaml();

    private static final Map<Path, Long> lastModified = new ConcurrentHashMap<>();

    private static ScheduledExecutorService executor = null;

    /**
     * Enum the target path for operation YAML documents: TO_WORLD / TO_GLOBAL.
     */
    public enum TargetPath {
        TO_WORLD,
        TO_GLOBAL,
    }

    // YAML reader.
    /**
     * Read file from defined path.
     * @param path              Path is the definition to locate file for execution.
     *                          You are advised to check for method {@link java.nio.file.Path}.
     * @return                  Return a map that storage YAML information: Map
     * @throws IOException      Throw exception when read failed.
     */
    public static Map<String, Object> read(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return YAML.load(reader);
        }
        // throw IOException.
    }

    // Read the files.
    /**
     * Read file at saved document (world & global).
     * @param subPath           Subpath is defined to two path under the folder of "core_hanxu".
     *                          Read path information from here's source code.
     * @param fileName          Define the YAML file name that required for reading.
     *                          Storage as file_name.yaml.
     * @return                  Return a mao that storage YAML information: Map
     * @throws IOException      Throw exception when nothing found.
     */
    public static Map<String, Object> read(String subPath, String fileName) throws IOException {
        // Read world (save) YAML:
        // .minecraft\saves\[save]\data\core_hanxu\...
        Path worldPath = FilePath.getWorldPath();
        // If existed, read.
        if (worldPath != null) {
            // .minecraft\saves\[save]\data\core_hanxu\[subPath]\[fileName].yaml
            Path worldSpecificFile = worldPath.resolve(subPath).resolve(fileName + ".yaml");
            if (Files.exists(worldSpecificFile)) {
                return read(worldSpecificFile);
            }
        }

        // Then read global YAML:
        // .minecraft\config\core_hanxu\[subPath]\[fileName].yaml
        Path globalPath = FilePath.getGlobalPath().resolve(subPath).resolve(fileName + ".yaml");
        if (Files.exists(globalPath)) {
            return read(globalPath);
        }

        // Else just throw nothing.
        throw new FileNotFoundException(errorString(CodeError.yamlFileNotFound) + subPath + "\\" + fileName);
    }

    // Read YAML from Minecraft book.
    /**
     * Read YAML from Minecraft book.
     * @param book              Receive a Minecraft book item.
     * @return                  Return the string that storage YAML information: String
     */
    public static String read(ItemStack book) {
        if (book.is(Items.WRITABLE_BOOK)) {
            WritableBookContent content = book.get(DataComponents.WRITABLE_BOOK_CONTENT);
            if (content != null) {
                return content.pages().stream().map(Filterable::raw).collect(Collectors.joining("\n"));
            }
        }
        else if (book.is(Items.WRITTEN_BOOK)) {
            WrittenBookContent content = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (content != null) {
                return content.pages().stream().map(Filterable::raw).map(Component::getString).collect(Collectors.joining("\n"));
            }
        }
        return "";
    }

    // Read YAML and find specific field from string.
    /// Read YAML and find specific field from string.
    public static String readSpecificField(String yaml, String fieldName) {
        for (String line : yaml.split("\n")) {
            if (line.startsWith(fieldName)) {
                String fieldContent = line.substring(fieldName.length() + 1).trim();
                if (fieldContent.startsWith("\"") && fieldContent.endsWith("\"")) {
                    fieldContent = fieldContent.substring(1, fieldContent.length() - 1);
                }
                return fieldContent;
            }
        }
        return "";
    }

    // Delete the file.
    /**
     * Delete the specific file by the given arguments.
     * @param subPath           Subpath is defined to two path under the folder of "core_hanxu".
     * @param fileName          Define the YAML file name that required for delete.
     *                          Storage as file_name.yaml.
     * @param targetPath        Specific subpath from enum: TO_WORLD / TO_GLOBAL.
     * @throws IOException      Throw exception when file not found.
     */
    public static void delete(String subPath, String fileName, TargetPath targetPath) throws IOException {
        Path targetRootPath = targetPath == TargetPath.TO_WORLD? FilePath.getWorldPath() : FilePath.getGlobalPath();
        if (targetRootPath == null) {
            throw new FileNotFoundException(errorString(CodeError.yamlFileNotFound) + subPath + "\\" + fileName);
        }

        Path file = targetRootPath.resolve(subPath).resolve(fileName + ".yaml");
        Files.deleteIfExists(file);
    }

    // Check if file existed at specific path.
    /// Check if file existed at specific path.
    public static boolean doesFileExist(TargetPath targetPath, String subPath, String fileName) {
        if (targetPath == TargetPath.TO_WORLD) {
            Path worldPath = FilePath.getWorldPath();
            if (worldPath != null) {
                Path worldSpecificFile = worldPath.resolve(subPath).resolve(fileName + ".yaml");
                return Files.exists(worldSpecificFile);
            }
        }
        else if (targetPath == TargetPath.TO_GLOBAL) {
            Path globalPath = FilePath.getGlobalPath().resolve(subPath).resolve(fileName + ".yaml");
            return Files.exists(globalPath);
        }

        return false;
    }

    // List out all YAML files.
    /// List out all YAML files.
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

    public static Map<String, Object> stringToMap(String yamlString) {
        return YAML.load(yamlString);
    }

    public static String mapToString(Map<String, Object> map) {
        return YAML.dump(map);
    }

    // Save YAML files.
    /**
     * Save YAML files from map data and defined path.
     * @param subPath           Subpath is defined to two path under the folder of "core_hanxu".
     * @param fileName          Define the YAML file name that required for save.
     *                          Storage as file_name.yaml.
     * @param data              Required map data that storage YAML information.
     * @param targetPath        Specific subpath from enum: TO_WORLD / TO_GLOBAL.
     * @throws IOException      Throw exception when using unexpected target path,
     *                          OR unavailable target path,
     *                          OR failed to save by YAML.
     */
    public static void save(String subPath, String fileName, Map<String, Object> data, TargetPath targetPath) throws IOException {
        Path targetRootPath;
        if (targetPath == TargetPath.TO_WORLD) {
            targetRootPath = FilePath.getWorldPath();
        }
        else if (targetPath == TargetPath.TO_GLOBAL) {
            targetRootPath = FilePath.getGlobalPath();
        }
        else {
            throw new IOException(errorString(CodeError.unexceptedTarget) + targetPath.toString());
        }

        if (targetRootPath == null) {
            throw new IOException(errorString(CodeError.unavailableTargetPath));
        }

        Path targetFile = targetRootPath.resolve(subPath).resolve(fileName + ".yaml");
        Files.createDirectories(targetFile.getParent());

        try (Writer writer = Files.newBufferedWriter(targetFile)) {
            YAML.dump(data, writer);
        }
    }

    // Watcher available user to hot reload changed file by each sub path.
    /// Watcher available user to hot reload changed file by each sub path.
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
