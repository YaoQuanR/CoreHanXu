package core.yaoquan.hanxu;

import core.yaoquan.hanxu.api.TimeHolder;
import core.yaoquan.hanxu.registry.ModCommand;
import core.yaoquan.hanxu.api.PermissionHolder;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CoreHanXu.MOD_ID)
public class CoreHanXu {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "core_hanxu";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CoreHanXu(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("[HX] >>>>>> Begin register.");

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (CoreHanXu) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        LOGGER.info("[HX] >>>>>> End register.");
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommand.registerCommand(event.getDispatcher());
        LOGGER.info("[HX] Register Procedure: Commands.");
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        if (PermissionHolder.autoAuthorizePermission) {
            boolean hasPermissionLevel = player.getPersistentData()
                    .contains("core.yaoquan.hanxu.player_permission_level");
            int autoLevel = 1;

            if (!hasPermissionLevel) {
                player.getPersistentData()
                        .putInt("core.yaoquan.hanxu.player_permission_level", autoLevel);

                LOGGER.info("[HX] Auto authorize permission to new player. (Level: {})", autoLevel);
            }
        }
    }
}
