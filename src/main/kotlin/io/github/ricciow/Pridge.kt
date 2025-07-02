package io.github.ricciow

import com.mojang.brigadier.Command
import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.managed.ManagedConfig
import io.github.ricciow.config.PridgeConfig
import io.github.ricciow.format.FormatManager
import io.github.ricciow.rendering.ImagePreviewRenderer
import io.github.ricciow.util.message.PagedMessageFactory
import kotlinx.io.IOException
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Pridge : ClientModInitializer {
    private val pridgeScheduler = Executors.newSingleThreadScheduledExecutor()

    override fun onInitializeClient() {
        LOGGER.info("[Pridge] Initializing...")

        initializeConfig()

        PagedMessageFactory.initialize()

        FormatManager.initialize()

        ChatManager.register()

        try {
            Files.createDirectories(CONFIG_DIR.resolve("sounds"))
        } catch (e: IOException) {
            LOGGER.error("Failed to create sounds directory: ${e.message}", e)
        }

        val imagePreviewLayer = Identifier.of("image-preview-mod", "preview-layer")
        val imagePreviewRenderer = ImagePreviewRenderer()

        HudLayerRegistrationCallback.EVENT.register { layeredDrawer ->
            layeredDrawer.attachLayerAfter(IdentifiedLayer.CHAT, imagePreviewLayer, imagePreviewRenderer::onHudRender)
        }

        CommandManager.register()

        LOGGER.info("[Pridge] Initialized successfully!")
    }

    private fun initializeConfig() {
        //Load the config
        val configFile = CONFIG_DIR.resolve("settings.json").toFile()

        CONFIG = ManagedConfig.create(configFile, PridgeConfig::class.java)

        CommandManager.addCommand(
            literal("pridge").executes { context ->
                mc.send { IMinecraft.instance.openWrappedScreen(CONFIG.getEditor()) }
                Command.SINGLE_SUCCESS
            }
        )

        //Add saving logic every 60s
        val saveTask = Runnable {
            LOGGER.info("Performing scheduled config save...")
            CONFIG.saveToFile()
        }

        pridgeScheduler.scheduleAtFixedRate(saveTask, 60, 60, TimeUnit.SECONDS)

        //Add shutdown Hook to save the config
        Runtime.getRuntime().addShutdownHook(Thread {
            LOGGER.info("Pridge shutting down, saving config...")
            CONFIG.saveToFile()
        })
    }

    companion object {
        const val MOD_ID = "pridge"
        val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)
        val CONFIG_DIR: Path = FabricLoader.getInstance().configDir.resolve(MOD_ID)

        val mc: MinecraftClient
            get() = MinecraftClient.getInstance()

        lateinit var CONFIG: ManagedConfig<PridgeConfig>
        val CONFIG_I: PridgeConfig
            get() = CONFIG.instance // since instance is mutable, we cant just have it as a variable
    }
}