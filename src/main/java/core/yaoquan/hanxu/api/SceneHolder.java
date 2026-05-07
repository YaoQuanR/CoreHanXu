package core.yaoquan.hanxu.api;

import core.yaoquan.hanxu.util.Converter;
import core.yaoquan.hanxu.util.YamlReader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static core.yaoquan.hanxu.api.define.Error.*;

/**
 * Scene system API
 * @since 0.3.0
 */
public class SceneHolder {
    public static class Scene {
        public String id;
        public String type;
        public int defaultColor;
        public int defaultInterval;
        public boolean defaultBold;
        public boolean defaultItalic;
        public boolean defaultUnderlined;
        public boolean defaultStrikethrough;
        public boolean defaultObfuscated;
        public boolean enabledSpeaker;
        public boolean enabledJsonText;
        public List<DialogNode> dialogs;
    }

    public static class DialogNode {
        public String speaker;
        public String text;
        public Integer color;
        public Integer interval;
        public Boolean bold;
        public Boolean italic;
        public Boolean underlined;
        public Boolean strikethrough;
        public Boolean obfuscated;
        public String execute;
    }

    // Load scene data.
    /**
     * Get the scene data from sub path "scene" for all .yaml documents.
     * @param fileName            The file name of YAML.
     * @return                    New scene class data: Scene.
     */
    public static Scene loadScene(String fileName) throws IOException {
        Map<String, Object> sceneData = YamlReader.read("scene", fileName + ".yaml");

        // Check if the id equals to file name.
        Scene scene = parseSceneData(sceneData);
        String yamlFileName = scene.id;
        if (yamlFileName != null && !yamlFileName.equals(fileName)) {
            throw new IOException(returnCodeError(CodeError.mismatchFileElement) + fileName + " ≠ " + yamlFileName);
        }

        return parseSceneData(sceneData);
    }

    // Play scene.
    /**
     * Scene will load from YAML file, then start playing the scene.
     * @param player              The source player that targeted to execute scene.
     * @param sceneName           As same as file name.
     */
    public static void playScene(ServerPlayer player, String sceneName) {
        try {
            Scene scene = loadScene(sceneName);
            startScene(player, scene, 0);
        }
        catch (IOException ignored) {}
    }

    /**
     * Delete scene from selected target.
     * @param sceneName           As same as file name.
     * @param targetPath          Enum path: TO_GLOBAL or TO_WORLD.
     * @return                    Does the delete success: boolean.
     */
    public static boolean deleteScene(String sceneName, YamlReader.TargetPath targetPath) {
        try {
            YamlReader.delete("scene", sceneName, targetPath);
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    public static boolean doesSceneExist(String sceneName) {
        try {
            YamlReader.read("scene", sceneName + ".yaml");
            return true;
        }
        catch (FileNotFoundException e) {
            return false;
        }
        catch (IOException e) {
            return true;
        }
    }

    public static boolean doesSceneExist(String sceneName, YamlReader.TargetPath targetPath) {
        return YamlReader.doesFileExist(targetPath, "scene", sceneName + ".yaml");
    }

    @SuppressWarnings("unchecked")
    private static Scene parseSceneData(Map<String, Object> sceneData) {
        // Read general information.
        Scene scene = new Scene();
        scene.id = (String) sceneData.get("id");
        scene.type = (String) sceneData.get("type");

        // Read default settings.
        Map<String, Object> defaults = (Map<String, Object>) sceneData.get("default");
        if (defaults != null) {
            scene.defaultColor = (Integer) defaults.getOrDefault("color", 0xFFFFFF);
            scene.defaultInterval = (Integer) defaults.getOrDefault("interval", 1);
            scene.defaultBold = (Boolean) defaults.getOrDefault("bold", false);
            scene.defaultItalic = (Boolean) defaults.getOrDefault("italic", false);
            scene.defaultUnderlined = (Boolean) defaults.getOrDefault("underlined", false);
            scene.defaultStrikethrough = (Boolean) defaults.getOrDefault("strikethrough", false);
            scene.defaultObfuscated = (Boolean) defaults.getOrDefault("obfuscated", false);
            scene.enabledSpeaker = (Boolean) defaults.getOrDefault("speaker", true);
            scene.enabledJsonText = (Boolean) defaults.getOrDefault("json", false);
        }
        else {
            scene.defaultColor = 0xFFFFFF;
            scene.defaultInterval = 1;
            scene.defaultBold = false;
            scene.defaultItalic = false;
            scene.defaultUnderlined = false;
            scene.defaultStrikethrough = false;
            scene.defaultObfuscated = false;
            scene.enabledSpeaker = true;
            scene.enabledJsonText = false;
        }

        // Read dialogs information.
        List<Map<String, Object>> dialogs = (List<Map<String, Object>>) sceneData.get("dialogs");
        scene.dialogs = new ArrayList<>();
        for (Map<String, Object> dialog : dialogs) {
            DialogNode dialogNode = new DialogNode();
            dialogNode.speaker = (String) dialog.get("speaker");
            dialogNode.text = (String) dialog.get("text");
            dialogNode.color = (Integer) dialog.get("color");
            dialogNode.interval = (Integer) dialog.get("interval");
            dialogNode.bold = (Boolean) dialog.get("bold");
            dialogNode.italic = (Boolean) dialog.get("italic");
            dialogNode.underlined = (Boolean) dialog.get("underlined");
            dialogNode.strikethrough = (Boolean) dialog.get("strikethrough");
            dialogNode.obfuscated = (Boolean) dialog.get("obfuscated");
            dialogNode.execute = (String) dialog.get("execute");
            
            scene.dialogs.add(dialogNode);
        }

        return scene;
    }

    private static void startScene(ServerPlayer player, Scene scene, int progress) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (progress >= scene.dialogs.size()) {
            return;
        }

        // If scene type is simple, then execute:
        if (scene.type.equals("simple")) {
            // Reading dialogs.
            DialogNode dialogNode = scene.dialogs.get(progress);
            int color = dialogNode.color != null? dialogNode.color : scene.defaultColor;
            int interval = dialogNode.interval != null? dialogNode.interval : scene.defaultInterval;
            boolean bold = dialogNode.bold != null? dialogNode.bold : scene.defaultBold;
            boolean italic = dialogNode.italic != null? dialogNode.italic : scene.defaultItalic;
            boolean underlined = dialogNode.underlined != null? dialogNode.underlined : scene.defaultUnderlined;
            boolean strikethrough = dialogNode.strikethrough != null? dialogNode.strikethrough : scene.defaultStrikethrough;
            boolean obfuscated = dialogNode.obfuscated != null? dialogNode.obfuscated : scene.defaultObfuscated;
            // Splicing string to complete message.
            String message = "";
            boolean showMessage = true;
            if (interval < 1) {
                interval = 1;
            }

            Component finalMessage;
            // Determine if enabled to detect json text.
            if ((dialogNode.text.startsWith("{") && dialogNode.text.endsWith("}"))
                    || (dialogNode.text.startsWith("[") && dialogNode.text.endsWith("]"))
                    && scene.enabledJsonText) {
                finalMessage = Converter.convertFromJsonToComponent(dialogNode.text);
            }
            else {
                if (scene.enabledSpeaker) {
                    switch (dialogNode.speaker) {
                        case null:
                            message = dialogNode.text;
                            break;
                        case "@skip":
                            showMessage = false;
                            break;
                        case "@p", "@s":
                            message = player.getName().getString() + ": " + dialogNode.text;
                            break;
                        case "@r":
                            List<ServerPlayer> players;
                            if (server != null) {
                                players = server.getPlayerList().getPlayers();
                                if (players.isEmpty()) {
                                    message = dialogNode.text;
                                }
                                else {
                                    ServerPlayer randomPlayer = players.get(new Random().nextInt(players.size()));
                                    message = randomPlayer.getName().getString() + ": " + dialogNode.text;
                                }
                            }
                            break;
                        case "@a", "@e":
                            message = "ALL: " + dialogNode.text;
                            break;
                        default:
                            message = dialogNode.speaker + ": " + dialogNode.text;
                            break;
                    }
                }
                else {
                    message = dialogNode.text;
                }

                finalMessage = Component.literal(message)
                        .withColor(color)
                        .withStyle(style -> style
                                .withBold(bold)
                                .withItalic(italic)
                                .withUnderlined(underlined)
                                .withStrikethrough(strikethrough)
                                .withObfuscated(obfuscated)
                        );
            }

            // Then display dialog.
            if (player != null && showMessage) {
                player.sendSystemMessage(finalMessage);
            }

            // Then execute command if required.
            if (dialogNode.execute != null) {
                String command = dialogNode.execute.startsWith("/")? dialogNode.execute : ("/" + dialogNode.execute);
                if (server != null) {
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
                }
            }

            int nextProgress = progress + 1;
            if (nextProgress < scene.dialogs.size()) {
                // Then add a gap time, wait for next dialog display.
                if (player != null) {
                    if (TimeHolder.getInstanceId(player.getUUID(), "core_hanxu-scene:" + scene.id) != null) {
                        TimeHolder.deleteInstanceTimer(player.getUUID(), "core_hanxu-scene:" + scene.id);
                    }

                    // Scene will stop when rejoin the game (callback = null).
                    TimeHolder.createInstanceTimer(
                        player.getUUID(),
                        "core_hanxu-scene:" + scene.id,
                        interval,
                        "tick",
                        p -> startScene(player, scene, nextProgress),
                        "scene_behavior",
                        "interval",
                        "core_hanxu-scene"
                    );
                    // Then start.
                    TimeHolder.startInstanceTimer(player.getUUID(), "core_hanxu-scene:" + scene.id);
                }
            }
        }
        // In the future, other type of scene will be created here.
    }
}
