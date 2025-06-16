package io.github.ricciow;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.notenoughupdates.moulconfig.common.IMinecraft;
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor;
import io.github.notenoughupdates.moulconfig.managed.ManagedConfig;
import io.github.ricciow.config.PridgeConfig;
import io.github.ricciow.format.FormatManager;
import io.github.ricciow.rendering.ImagePreviewRenderer;
import io.github.ricciow.sounds.DynamicSoundPlayer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PridgeClient implements ClientModInitializer {

	private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	public static final String MOD_ID = "pridge";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static FormatManager FORMATS;
	public static ChatManager CHAT_MANAGER;
	public static ManagedConfig<PridgeConfig> CONFIG;
	public static final CommandManager COMMAND_MANAGER = new CommandManager();

	public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
	public static DynamicSoundPlayer SOUND_PLAYER;

	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing Pridge (Client)...");

		initializeConfig();

		SOUND_PLAYER = new DynamicSoundPlayer(CONFIG_DIR.resolve("sounds"));

		// Load the formats
		FORMATS = new FormatManager(MOD_ID);

		// Create and register the Chat Manager
		CHAT_MANAGER = new ChatManager(FORMATS);
		CHAT_MANAGER.register();

		try {
			Files.createDirectories(CONFIG_DIR.resolve("sounds"));
		} catch (IOException e) {
			LoggerFactory.getLogger(MOD_ID).error("Could not create custom sounds directory!", e);
		}

		COMMAND_MANAGER.addCommand(literal("testSound")
			.then(argument("sound", StringArgumentType.string())
			.executes(commandContext -> {
				SOUND_PLAYER.play(StringArgumentType.getString(commandContext, "sound"));
				return Command.SINGLE_SUCCESS;
			})));

		// Image Preview
		final Identifier IMAGE_PREVIEW_LAYER = Identifier.of("image-preview-mod", "preview-layer");
		final ImagePreviewRenderer imagePreviewRenderer = new ImagePreviewRenderer();

		HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> {
			layeredDrawer.attachLayerAfter(IdentifiedLayer.CHAT, IMAGE_PREVIEW_LAYER, imagePreviewRenderer::onHudRender);
		});

		// Register the client side commands
		COMMAND_MANAGER.register();

		LOGGER.info("Pridge has been successfully initialized.");
	}

	private void initializeConfig() {
		//Load the config
		File configFile = new File(CONFIG_DIR
				.resolve("settings.json")
				.toUri()
		);

		CONFIG = ManagedConfig.create(configFile, PridgeConfig.class);

		COMMAND_MANAGER.addCommand(
				literal("pridge")
				.executes(context -> {
					MinecraftClient.getInstance().send(() -> {
						MoulConfigEditor<PridgeConfig> editor = CONFIG.getEditor();

						IMinecraft.instance.openWrappedScreen(editor);
					});
					return Command.SINGLE_SUCCESS;
				})
		);

		//Add saving logic every 60s
		final Runnable saveTask = () -> {
			LOGGER.info("Performing scheduled config save...");
			CONFIG.saveToFile();
		};

		scheduler.scheduleAtFixedRate(saveTask, 60, 60, TimeUnit.SECONDS);

		//Add shutdown Hook to save the config
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			LOGGER.info("Pridge shutting down, saving config...");
			CONFIG.saveToFile();
		}));
	}

	public static PridgeConfig getConfig() {
		return CONFIG.getInstance();
	}
}