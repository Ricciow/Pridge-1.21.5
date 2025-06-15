package io.github.ricciow.sounds;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.ricciow.PridgeClient;
import io.github.ricciow.config.PridgeConfig;
import io.github.ricciow.util.TextParser;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class DynamicSoundPlayer {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicSoundPlayer");
    private final Path SOUNDS_DIR;
    private final String MOD_ID = PridgeClient.MOD_ID;
    private static final PridgeConfig CONFIG = PridgeClient.getConfig();

    public DynamicSoundPlayer(Path SOUNDS_DIR) {
        this.SOUNDS_DIR = SOUNDS_DIR;

        //Create sounds directory if it doesn't exist
        loadFromDefaultAsset();

        //Create sounds command
        PridgeClient.COMMAND_MANAGER.addCommand(
            ClientCommandManager
                .literal("pridgesounds")
                    .then(
                        ClientCommandManager.argument("sound name", StringArgumentType.greedyString())
                            .executes(context -> {
                                String argument = StringArgumentType.getString(context, "sound name");
                                if(isSound(argument)) {
                                    play(argument.replaceAll(" ", "_"));
                                    context.getSource().sendFeedback(TextParser.parse("&6&lPlaying sound: &e" + argument));
                                }
                                else {
                                    context.getSource().sendFeedback(TextParser.parse("&c&lSound not found"));
                                }
                                return Command.SINGLE_SUCCESS;
                            })
                    )
                    .executes(context -> {
                        StringBuilder builder = new StringBuilder("&6&lAvailable Sounds: &e");

                        List<String> soundNames = getSoundNames().stream().map(name -> name.replaceAll("_", " ")).toList();
                        builder.append(String.join("&f, &e", soundNames));

                        context.getSource().sendFeedback(TextParser.parse(builder.toString()));
                        return Command.SINGLE_SUCCESS;
                    })
        );
    }

    public void play(String fileName) {
        Path soundFile = SOUNDS_DIR.resolve(fileName + ".ogg");
        if (!Files.exists(soundFile)) {
            LOGGER.warn("Attempted to play a dynamic sound that does not exist: {}", fileName);
            return;
        }

        Identifier dynamicId = Identifier.of("dynamicsound", fileName);

        // This is the correct constructor for a non-looping, non-positional sound effect.
        PositionedSoundInstance soundInstance = new PositionedSoundInstance(
                dynamicId,                    // The sound identifier our mixin will catch
                SoundCategory.MASTER,         // The sound category for volume control
                PridgeClient.getConfig().soundsCategory.getVolume(),                         // Volume
                1.0f,                         // Pitch
                SoundInstance.createRandom(), // A new random instance provided by the API
                false,                        // isRepeatable: false for a one-shot effect
                0,                            // repeatDelay
                SoundInstance.AttenuationType.NONE, // No distance-based volume reduction
                0.0, 0.0, 0.0,                // World position (unused because relative is true)
                true                          // isRelative: true, so it plays at the player's location
        );

        // Play the sound using the vanilla SoundManager. Our mixins will do the rest.
        MinecraftClient.getInstance().getSoundManager().play(soundInstance);
    }

    /**
     * Reads the default sounds folder from the mod's assets
     * and makes a copy of them on the config folder.
     */
    private void loadFromDefaultAsset() {
        // Only copy upon first load
        if(Files.exists(SOUNDS_DIR)) return;
        System.out.println("Copying sound files...");

        Optional<ModContainer> modContainerOpt = FabricLoader.getInstance().getModContainer(MOD_ID);
        if (modContainerOpt.isEmpty()) {
            return;
        }
        System.out.println("Copying sound files... 2");

        try {
            Path sourceSoundsPath = modContainerOpt.get().findPath("assets/" + MOD_ID + "/sounds")
                    .orElseThrow(() -> new IOException("Could not find sounds directory in mod assets!"));
            Files.createDirectories(SOUNDS_DIR);

            System.out.println("Copying sound files... 3");

            try (Stream<Path> stream = Files.walk(sourceSoundsPath)) {
                stream.forEach(sourcePath -> {
                    try {
                        // Create a corresponding destination path for each file/dir in the stream.
                        Path destinationPath = SOUNDS_DIR.resolve(sourceSoundsPath.relativize(sourcePath).toString());

                        // If it's a directory, create it. If it's a file, copy it.
                        if (Files.isDirectory(sourcePath)) {
                            Files.createDirectories(destinationPath);
                        } else {
                            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                        System.out.println("Copying sound files... 4");
                    } catch (IOException ignored) {
                        System.out.println("Copying sound files... 5");
                    }
                });
            }
        } catch (IOException e) {
            LOGGER.error("Failed to copy asset files:", e);
        }
    }

    /**
     * Gets the names for sounds in config/pridge/sounds
     * @return List<String> sound names without the .ogg extension
     */
    public List<String> getSoundNames() {
        if (Files.exists(SOUNDS_DIR) && Files.isDirectory(SOUNDS_DIR)) {
            try (Stream<Path> stream = Files.list(SOUNDS_DIR)) {
                return stream
                        .filter(Files::isRegularFile)                                          // Ensure it's a file, not a directory
                        .map(Path::getFileName)                                                // Get the file name as a Path
                        .map(Path::toString)                                                   // Convert Path to String
                        .filter(name -> name.endsWith(".ogg"))                           // Filter for .ogg files
                        .map(name -> name.substring(0, name.length() - ".ogg".length())) // Remove the .ogg extension
                        .toList();
            } catch (IOException e) {
                PridgeClient.LOGGER.error("Error listing sound files: {}", e.getMessage());
            }
        }
        return List.of();
    }

    public boolean isSound(String sound) {
        List<String> oggFileNames = getSoundNames();

        Optional<String> soundToPlay = oggFileNames.stream()
                .filter(soundName -> soundName.replaceAll("_" , "").equals(sound))
                .findFirst();

        return  soundToPlay.isPresent();
    }

    /**
     * Plays a sound if a string message contains *soundname*
     */
    public void checkForSounds(String message) {
        List<String> oggFileNames = getSoundNames();

        Optional<String> soundToPlay = oggFileNames.stream()
                .filter(soundName -> message.contains("*" + soundName.replaceAll("_", " ") + "*"))
                .findFirst(); // Plays the first matching sound found

        if(soundToPlay.isPresent()) {
            String soundPlayed = soundToPlay.get();
            if(CONFIG.developerCategory.dev_enabled) {
                LOGGER.info("Played {} sound for the message: {}", soundPlayed, message);
            }
            play(soundPlayed);
        }
    }
}