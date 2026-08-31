package core.yaoquan.hanxu;

import core.yaoquan.hanxu.api.*;
import core.yaoquan.hanxu.api.define.SaveDat;
import core.yaoquan.hanxu.registry.QuickSendPacket;
import core.yaoquan.hanxu.registry.config.GeneralConfig;
import core.yaoquan.hanxu.registry.ModDataGenerator;
import core.yaoquan.hanxu.registry.command.builder.CommandBuilder;
import core.yaoquan.hanxu.registry.config.PermissionConfig;
import core.yaoquan.hanxu.registry.object.ModBlock;
import core.yaoquan.hanxu.registry.object.ModBlockEntity;
import core.yaoquan.hanxu.registry.object.ModCreativeModeTab;
import core.yaoquan.hanxu.registry.object.ModItem;
import core.yaoquan.hanxu.test.TestCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.config.ModConfig;
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
        modContainer.registerConfig(ModConfig.Type.COMMON, GeneralConfig.SPEC_GENERAL, "core_hanxu-general.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, PermissionConfig.SPEC_PERMISSION, "core_hanxu-permission.toml");

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
        LOGGER.info("[HX] Rebuild Procedure: Timer - Global.");

        AttributeHolder.loadAttributeForGlobal(overworld);
        LOGGER.info("[HX] Rebuild Procedure: Attribute - Global.");

        AttributeHolder.registerAllYamlAttributes();
        LOGGER.info("[HX] Rebuild Procedure: Attribute - Yaml Attribute Callbacks.");

        VariableHolder.loadAllVariables(overworld);
        LOGGER.info("[HX] Rebuild Procedure: Variable - Variables.");

        WeatherHolder.registerAllYamlWeathers();
        LOGGER.info("[HX] Rebuild Procedure: Weather - Yaml Definitions.");

        WeatherHolder.loadAllLevelStates();
        LOGGER.info("[HX] Rebuild Procedure: Weather States.");

        WeatherHolder.pickupUnclaimedStates();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandBuilder.registerCommand(event.getDispatcher());
        LOGGER.info("[HX] Register Procedure: Commands.");
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        if (PermissionHolder.Storage.autoAuthorizePermission) {
            boolean hasPermissionLevel = player.getPersistentData()
                    .contains("core.yaoquan.hanxu.player_permission_level");

            // Changeable from config.
            int autoLevel = GeneralConfig.setAutoPermissionLevelAuthorize.getAsInt();

            if (!hasPermissionLevel) {
                player.getPersistentData()
                        .putInt("core.yaoquan.hanxu.player_permission_level", autoLevel);

                LOGGER.info("[HX] Auto authorize permission to new player. (Level: {})", autoLevel);
            }
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            TimeHolder.loadInstanceTimerForPlayer(serverPlayer);
            LOGGER.info("[HX] Load Procedure: Timer - Player: {}", serverPlayer.getName().getString());

            AttributeHolder.loadAttributeForPlayer(serverPlayer);
            LOGGER.info("[HX] Load Procedure: Attribute - Player: {}", serverPlayer.getName().getString());

            QuickSendPacket.sendRegisteredTermPacket(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TimeHolder.saveInstanceTimerForPlayer(player);
            LOGGER.info("[HX] Save Procedure: Timer - Player: {}", player.getName().getString());

            AttributeHolder.saveAttributeToPlayer(player);
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

        AttributeHolder.saveAttributeToGlobal(overworld);
        LOGGER.info("[HX] Save Procedure: Attribute - Global");

        SaveDat.saveToWorld(overworld);
    }

    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModDataGenerator.ModModelProvider::new);
    }

    private static void registerGameRules() {
        PermissionHolder.Storage.ignorePermissionLevel.getClass();
        PermissionHolder.Storage.nonPlayerSourcePermissionLevel.getClass();
        LOGGER.info("[HX] Custom Game Rule Registered.");
    }

    private static void registerAllDeferredRegister(IEventBus modEventBus) {
        ModBlock.BLOCKS.register(modEventBus);
        ModItem.ITEMS.register(modEventBus);
        ModCreativeModeTab.CREATIVE_MODE_TABS.register(modEventBus);
        ModBlockEntity.BLOCK_ENTITY_TYPES.register(modEventBus);
        LOGGER.info("[HX] Deferred Register Registered.");
    }
}
