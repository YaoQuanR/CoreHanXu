package core.yaoquan.hanxu;

import core.yaoquan.hanxu.api.TimeHolder;
import core.yaoquan.hanxu.registry.ModConfig;
import core.yaoquan.hanxu.registry.ModDataGenerator;
import core.yaoquan.hanxu.registry.command.CommandBuilder;
import core.yaoquan.hanxu.api.PermissionHolder;
import core.yaoquan.hanxu.registry.object.ModBlock;
import core.yaoquan.hanxu.registry.object.ModBlockEntity;
import core.yaoquan.hanxu.registry.object.ModCreativeModeTab;
import core.yaoquan.hanxu.registry.object.ModItem;
import core.yaoquan.hanxu.test.TestCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
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

        // Register game rule (make sure it registered).
        registerGameRules();

        // Register all deferred register.
        registerAllDeferredRegister(modEventBus);

        // Add listener to data generator provider.
        modEventBus.addListener(CoreHanXu::gatherData);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, ModConfig.SPEC);

        LOGGER.info("[HX] >>>>>> End register.");
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("[HX] Server starting...");

        // Test methods.
        // Register.
        TimeHolder.registerCallback(new TestCallback());
        LOGGER.info("[HX] Test: Custom Timer Callback.");

        ServerLevel overworld = event.getServer().overworld();
        TimeHolder.loadInstanceTimerForGlobal(overworld);
        LOGGER.info("[HX] Rebuild Procedure: Timer - Global");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandBuilder.registerCommand(event.getDispatcher());
        LOGGER.info("[HX] Register Procedure: Commands.");
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        if (PermissionHolder.autoAuthorizePermission) {
            boolean hasPermissionLevel = player.getPersistentData()
                    .contains("core.yaoquan.hanxu.player_permission_level");

            // Changeable from config.
            int autoLevel = ModConfig.SET_AUTO_AUTHORIZED_PERMISSION_LEVEL.getAsInt();

            if (!hasPermissionLevel) {
                player.getPersistentData()
                        .putInt("core.yaoquan.hanxu.player_permission_level", autoLevel);

                LOGGER.info("[HX] Auto authorize permission to new player. (Level: {})", autoLevel);
            }
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            TimeHolder.loadInstanceTimerForPlayer(serverPlayer);
            LOGGER.info("[HX] Load Procedure: Timer - Player: {}", serverPlayer.getName().getString());
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TimeHolder.saveInstanceTimerForPlayer(player);
            LOGGER.info("[HX] Save Procedure: Timer - Player: {}", player.getName().getString());
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        MinecraftServer server = event.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            TimeHolder.saveInstanceTimerForPlayer(player);
            LOGGER.info("[HX] Save Procedure: Auto, Timer - Player: {}", player.getName().getString());
        }

        ServerLevel overworld = event.getServer().overworld();
        TimeHolder.saveInstanceTimerForGlobal(overworld);
        LOGGER.info("[HX] Save Procedure: Timer - Global");
    }

    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModDataGenerator.ModModelProvider::new);
    }

    private static void registerGameRules() {
        PermissionHolder.OVERRIDE_COMMAND_BLOCK_PERMISSION.getClass();
    }

    private static void registerAllDeferredRegister(IEventBus modEventBus) {
        ModBlock.BLOCKS.register(modEventBus);
        ModItem.ITEMS.register(modEventBus);
        ModCreativeModeTab.CREATIVE_MODE_TABS.register(modEventBus);
        ModBlockEntity.BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
