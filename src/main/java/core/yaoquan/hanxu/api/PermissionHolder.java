package core.yaoquan.hanxu.api;

import core.yaoquan.hanxu.registry.config.GeneralConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p><b>
 *     Permission system API
 * </b></p>
 * <p>
 *     Permission system is used to handle user's permission level.
 * </p>
 * @since 0.1ea (Internal Development)
 */
public class PermissionHolder {
    // Permission manager list:
    // Always level 10.
    private static final Set<String> adminList = ConcurrentHashMap.newKeySet();

    static {
        adminList.add("YaoQuanR");
        adminList.add("Dev");
    }

    // Join admin group by method.
    public static boolean addToAdminList(String playerId) {
        if (playerId == null) {
            return false;
        }
        else {
            adminList.add(playerId);
            return true;
        }
    }

    public static class Storage {
        public static boolean autoAuthorizePermission = true;

        public static final GameRules.Key<GameRules.BooleanValue> ignorePermissionLevel =
                GameRules.register(
                        "ignorePermissionLevel",
                        GameRules.Category.PLAYER,
                        GameRules.BooleanValue.create(false)
                );

        public static final GameRules.Key<GameRules.IntegerValue> nonPlayerSourcePermissionLevel =
                GameRules.register(
                        "nonPlayerSourcePermissionLevel",
                        GameRules.Category.PLAYER,
                        GameRules.IntegerValue.create(2)
                );

        // Shut down auto authorize permission.
        public static void stopAutoAuthorizePermission() {
            autoAuthorizePermission = false;
        }

        /**
         * Set player level by API.
         * This is the only way to modify player permission level.
         * @param player            The target player that you want to modify.
         * @param level             The integer level of your targeted permission level.
         */
        public static void setPlayerPermissionLevel(Player player, int level) {
            player.getPersistentData().putInt("core.yaoquan.hanxu.player_permission_level", level);
        }

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

        // Get license state.
        /**
         * Check the agree state of the license.
         *
         * @param player            The target player that you want to know about the state.
         * @return                  Does the specific player agreed the license: boolean.
         */
        public static boolean getLicenseState(Player player) {
            return player.getPersistentData()
                    .getBoolean("core.yaoquan.hanxu.agreed_license")
                    .orElse(false);
        }

        public static int getNonPlayerSourcePermissionLevel(CommandSourceStack cs) {
            return cs.getLevel().getGameRules().getInt(nonPlayerSourcePermissionLevel);
        }
    }

    public static class Verify {
        // Permission check.
        /**
         * Use it when registering command. Reject if player is not admin & not agreed license.
         * @param source            CommandSourceStack from command builder {@link net.minecraft.commands.CommandSourceStack}.
         * @param requiredLevel     Set the level that required player to get that level for execution.
         */
        public static boolean hasPermission(CommandSourceStack source, int requiredLevel) {
            // Skip if ignore permission level.
            boolean skip = source.getLevel().getGameRules().getBoolean(Storage.ignorePermissionLevel);
            if (skip) {
                return true;
            }

            if (source.getEntity() instanceof Player player) {
                // Always pass for developer.
                if (adminList.contains(player.getName().getString())) {
                    return true;
                }

                // Get player permission level.
                int currentPlayerLevel = player.getPersistentData()
                        .getInt("core.yaoquan.hanxu.player_permission_level")
                        .orElse(GeneralConfig.setAutoPermissionLevelAuthorize.getAsInt());

                // Non admin must agree license for command use.
                boolean agreedLicense = player.getPersistentData()
                        .getBoolean("core.yaoquan.hanxu.agreed_license")
                        .orElse(false);
                // Pass if admin.
                if (!agreedLicense && requiredLevel > 0) {
                    return currentPlayerLevel == 10;
                }

                return currentPlayerLevel >= requiredLevel;
            }

            if (source.getEntity() == null) {
                int nonPlayerSourcePermissionLevel = source.getLevel().getGameRules().getInt(Storage.nonPlayerSourcePermissionLevel);
                return nonPlayerSourcePermissionLevel >= requiredLevel;
            }

            // Unexcepted.
            return false;
        }
    }
}
