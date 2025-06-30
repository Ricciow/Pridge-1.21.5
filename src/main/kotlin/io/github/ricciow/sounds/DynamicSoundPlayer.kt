package io.github.ricciow.sounds

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import io.github.ricciow.Pridge.Companion.COMMAND_MANAGER
import io.github.ricciow.Pridge.Companion.CONFIG_I
import io.github.ricciow.Pridge.Companion.MOD_ID
import io.github.ricciow.Pridge.Companion.mc
import io.github.ricciow.StringListSuggestionProvider
import io.github.ricciow.util.TextParser
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.realms.RealmsError.LOGGER
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension

class DynamicSoundPlayer(private val soundsDir: Path) {

    init {
        //Create sounds directory if it doesn't exist
        loadFromDefaultAsset()

        //Create sounds command
        COMMAND_MANAGER.addCommand(
            literal("pridgesounds").then(
                    argument("sound name", StringArgumentType.greedyString())
                        .suggests(StringListSuggestionProvider(getSoundNames()))
                        .executes { context ->
                            val argument = StringArgumentType.getString(context, "sound name")
                            if (isSound(argument)) {
                                play(argument.replace(" ".toRegex(), "_"))
                                context.source.sendFeedback(TextParser.parse("&6&lPlaying sound: &e$argument"))
                            } else {
                                context.source.sendFeedback(TextParser.parse("&c&lSound not found"))
                            }
                            Command.SINGLE_SUCCESS
                        }
                )
                .executes { context ->
                    val builder = StringBuilder("&6&lAvailable Sounds: &e")
                    builder.append(getSoundNames().joinToString("&f, &e") { name ->
                        name.replace("_", " ")
                    })

                    context.source.sendFeedback(TextParser.parse(builder.toString()))
                    Command.SINGLE_SUCCESS
                }
        )
    }

    fun play(fileName: String) {
        val soundFile = soundsDir.resolve("$fileName.ogg")
        if (!Files.exists(soundFile)) {
            LOGGER.warn("Attempted to play a dynamic sound that does not exist: {}", fileName)
            return
        }

        val soundInstance = PositionedSoundInstance.master(
            SoundEvent(Identifier.of("dynamicsound", fileName), null),
            CONFIG_I.soundsCategory.getVolume(),
            1.0f
        )

        // Play the sound using the vanilla SoundManager. Our mixins will do the rest.
        mc.soundManager.play(soundInstance)
    }

    /**
     * Reads the default sounds folder from the mod's assets
     * and makes a copy of them on the config folder.
     */
    private fun loadFromDefaultAsset() {
        if (Files.exists(soundsDir)) return

        val modContainer = FabricLoader.getInstance().getModContainer(MOD_ID).orElse(null) ?: return

        try {
            val sourceSoundsPath = modContainer.findPath("assets/$MOD_ID/sounds")
                .orElseThrow {
                    IOException("Could not find sounds directory in mod assets!")
                }
            Files.createDirectories(soundsDir)

            Files.walk(sourceSoundsPath).use { stream ->
                stream.forEach { sourcePath ->
                    runCatching {
                        val destinationPath = soundsDir.resolve(sourceSoundsPath.relativize(sourcePath).toString())
                        if (Files.isDirectory(sourcePath)) {
                            Files.createDirectories(destinationPath)
                        } else {
                            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            LOGGER.error("Failed to copy asset files:", e)
        }
    }

    fun getSoundNames(): List<String> =
        try {
            soundsDir.takeIf { it.exists() && it.isDirectory() }
                ?.listDirectoryEntries("*.ogg")
                ?.map { it.nameWithoutExtension }
                ?: emptyList()
        } catch (e: IOException) {
            LOGGER.error("Error listing sound files", e)
            emptyList()
        }

    fun isSound(sound: String) = getSoundNames().any { soundName ->
        soundName == sound.replace(" ", "_")
    }

    /**
     * Plays a sound if a string message contains *soundname*
     */
    fun checkForSounds(message: String) {
        val soundToPlay = getSoundNames().firstOrNull { soundName ->
            message.contains("*${soundName.replace("_", " ")}*")
        }

        soundToPlay?.let {
            if (CONFIG_I.developerCategory.devEnabled) {
                LOGGER.info("Played {} sound for the message: {}", it, message)
            }
            play(it)
        }
    }
}