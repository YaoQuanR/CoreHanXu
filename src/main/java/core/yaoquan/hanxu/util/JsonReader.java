package core.yaoquan.hanxu.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import core.yaoquan.hanxu.api.define.FilePath;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static core.yaoquan.hanxu.api.define.Error.*;

public class JsonReader {
    private static final Gson GSON = new Gson();

    /**
     * Enum the target path for operation JSON documents: TO_WORLD / TO_GLOBAL.
     */
    public enum TargetPath {
        TO_WORLD,
        TO_GLOBAL,
    }

    /**
     * Read file from defined path.
     * @param path              Path is the definition to locate file for execution.
     *                          You are advised to check for the method {@link java.nio.file.Path}.
     * @return                  Return a map that storage JSON information: Map
     * @throws IOException      Throw exception when read failed.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> read(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonElement element = JsonParser.parseReader(reader);
            return GSON.fromJson(element, Map.class);
        }
        // throw IOException.
        catch (JsonSyntaxException e) {
            throw new IOException(returnCodeError(CodeError.jsonFileNotFound) + path.getFileName().toString(), e);
        }
    }

    /**
     * Read file at saved document (world & global).
     * @param subPath           Subpath is defined to two path under the folder of "core_hanxu".
     *                          Read path information from here's source code.
     * @param fileName          Define the YAML file id that required for reading.
     *                          Storage as file_name.yaml.
     * @return                  Return a mao that storage YAML information: Map
     * @throws IOException      Throw exception when nothing found.
     */
    public static Map<String, Object> read(String subPath, String fileName) throws IOException {
        // Read world (save) JSON:
        // .minecraft\saves\[save]\data\core_hanxu\...
        Path worldPath = FilePath.getWorldPath();
        // If existed, read.
        if (worldPath != null) {
            // .minecraft\saves\[save]\data\core_hanxu\[subPath]\[fileName].json
            Path worldSpecificFile = worldPath.resolve(subPath).resolve(fileName + ".json");
            if (Files.exists(worldSpecificFile)) {
                return read(worldSpecificFile);
            }
        }

        // Then read global JSON:
        // .minecraft\config\core_hanxu\[subPath]\[fileName].json
        Path globalPath = FilePath.getGlobalPath().resolve(subPath).resolve(fileName + ".json");
        if (Files.exists(globalPath)) {
            return read(globalPath);
        }

        // Else just throw nothing.
        throw new FileNotFoundException(returnCodeError(CodeError.jsonFileNotFound) + subPath + "\\" + fileName);
    }

    /// Check if file existed at specific path.
    public static boolean doesFileExist(TargetPath targetPath, String subPath, String fileName) {
        if (targetPath == TargetPath.TO_WORLD) {
            Path worldPath = FilePath.getWorldPath();
            if (worldPath != null) {
                Path worldSpecificFile = worldPath.resolve(subPath).resolve(fileName + ".json");
                return Files.exists(worldSpecificFile);
            }
        }
        else if (targetPath == TargetPath.TO_GLOBAL) {
            Path globalPath = FilePath.getGlobalPath().resolve(subPath).resolve(fileName + ".json");
            return Files.exists(globalPath);
        }

        return false;
    }

    /// List out all JSON files.
    public static List<Path> listOut(String subPath) {
        List<Path> returnList = new ArrayList<>();

        Path worldRootPath = FilePath.getWorldPath();
        if (worldRootPath != null) {
            Path worldDirectory = worldRootPath.resolve(subPath);
            if (Files.isDirectory(worldDirectory)) {
                try (Stream<Path> stream = Files.list(worldDirectory)) {
                    stream.filter(p -> p.toString().endsWith(".json")).forEach(returnList::add);
                }
                catch (IOException ignored) {}
            }
        }

        Path globalDirectory = FilePath.getGlobalPath().resolve(subPath);
        if (Files.isDirectory(globalDirectory)) {
            try (Stream<Path> stream = Files.list(globalDirectory)) {
                stream.filter(p -> p.toString().endsWith(".json")).forEach(returnList::add);
            }
            catch (IOException ignored) {}
        }

        return returnList;
    }
}
