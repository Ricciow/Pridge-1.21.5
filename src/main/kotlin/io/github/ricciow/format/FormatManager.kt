package io.github.ricciow.format

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import io.github.ricciow.CommandManager
import io.github.ricciow.Pridge
import io.github.ricciow.Pridge.Companion.CONFIG_DIR
import io.github.ricciow.Pridge.Companion.CONFIG_I
import io.github.ricciow.Pridge.Companion.LOGGER
import io.github.ricciow.Pridge.Companion.MOD_ID
import io.github.ricciow.StringListSuggestionProvider
import io.github.ricciow.util.TextParser.parse
import io.github.ricciow.util.UrlContentFetcher
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.net.URISyntaxException
import java.nio.file.Files

object FormatManager {
    private val configFile = CONFIG_DIR.resolve(MOD_ID).resolve("formats.json")
    var config: ChatFormat? = null
    private var lastReloadType = "default"

    private val GSON = GsonBuilder()
        .registerTypeAdapterFactory(
            RuntimeTypeAdapterFactory
                .of(FormatRule::class.java, "type")
                .registerSubtype(RegexFormatRule::class.java, "regex")
                .registerSubtype(StringFormatRule::class.java, "string")
                .registerSubtype(StringArrayFormatRule::class.java, "stringarray")
                .registerSubtype(SpecialFormatRule::class.java, "special")
        )
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create()

    fun initialize() {
        load()

        CommandManager.addCommand(
            ClientCommandManager.literal("reloadprigeformattings")
                .then(
                    ClientCommandManager.argument<String?>("type", StringArgumentType.word())
                        .suggests(
                            StringListSuggestionProvider(
                                mutableListOf(
                                    "assets",
                                    "github",
                                    "config",
                                    "default"
                                )
                            )
                        )
                        .executes { context ->
                            var reloadType = StringArgumentType.getString(context, "type").lowercase()
                            when (reloadType) {
                                "assets" -> loadFromDefaultAssetAndSave()
                                "github" -> loadFromGithubAndSave()
                                "config" -> loadFromConfig()
                                else -> {
                                    reloadType = "default"
                                    load()
                                }
                            }

                            lastReloadType = reloadType
                            context.getSource().sendFeedback(parse("&a&lReloaded Formattings with $reloadType"))
                            Command.SINGLE_SUCCESS
                        }
                )
                .executes { context ->
                    when (lastReloadType) {
                        "assets" -> loadFromDefaultAssetAndSave()
                        "github" -> loadFromGithubAndSave()
                        "config" -> loadFromConfig()
                        else -> {
                            load()
                        }
                    }
                    context.getSource().sendFeedback(parse("&a&lReloaded Formattings with $lastReloadType"))
                    Command.SINGLE_SUCCESS
                }
        )
    }

    fun loadFromGithubAndSave() {
        try {
            loadFromGithub()
            save()
            LOGGER.info("Loaded formattings from GitHub")

            //Initialize Patterns
            if (config != null) {
                for (rule in config!!.formats) {
                    rule.initialize()
                }
            }
        } catch (e: IOException) {
            LOGGER.error("Failed to load from github:", e)
        } catch (e: URISyntaxException) {
            LOGGER.error("Failed to load from github:", e)
        }
    }

    fun loadFromConfig() {
        if (Files.exists(configFile)) {
            try {
                FileReader(configFile.toFile()).use { reader ->
                    LOGGER.info("Loading existing format file...")
                    config = GSON.fromJson(reader, ChatFormat::class.java)
                    if (config == null) {
                        throw IOException("Format file is empty or corrupted.")
                    }
                    LOGGER.info("Format loaded successfully.")

                    //Initialize Patterns
                    if (config != null) {
                        for (rule in config!!.formats) {
                            rule.initialize()
                        }
                    }
                }
            } catch (e: IOException) {
                LOGGER.error("Failed to load format file! Creating a new default format from asset.", e)
                loadFromDefaultAssetAndSave()
            } catch (e: JsonSyntaxException) {
                LOGGER.error("Failed to load format file! Creating a new default format from asset.", e)
                loadFromDefaultAssetAndSave()
            }
        } else {
            LOGGER.info("No format file found. Creating a new default format from asset...")
            loadFromDefaultAssetAndSave()
        }
    }

    fun load() {
        if (CONFIG_I.developerCategory.autoUpdate) {
            loadFromGithubAndSave()
            return
        }
        loadFromConfig()
    }

    fun save() {
        try {
            Files.createDirectories(configFile.parent)
            FileWriter(configFile.toFile()).use { writer ->
                GSON.toJson(config, writer)
                LOGGER.info("Format saved successfully to {}", configFile)
            }
        } catch (e: IOException) {
            LOGGER.error("Failed to save format file.", e)
        }
    }

    /**
     * Loads the default config from the bundled assets, sets it as the current config, and saves it.
     */
    private fun loadFromDefaultAssetAndSave() {
        try {
            loadFromDefaultAsset()
            save()
        } catch (e: IOException) {
            LOGGER.error("FATAL: Could not load default format from assets! The mod may not function correctly.", e)
            // If loading from assets fails, create an empty format as a last resort.
            this.config = ChatFormat()
        } finally {
            if (config != null) {
                for (rule in config!!.formats) {
                    rule.initialize()
                }
            }
        }
    }

    private fun loadFromGithub() {
        val format = UrlContentFetcher.fetchContentFromURL(CONFIG_I.developerCategory.formatURL)
        this.config = GSON.fromJson(format, ChatFormat::class.java)
    }

    /**
     * Reads the default format file from the mod's assets and parses it.
     * @throws IOException If the asset file cannot be found or read.
     */
    @Throws(IOException::class)
    private fun loadFromDefaultAsset() {
        // Construct the path to the file inside the JAR
        val assetPath = "assets/${MOD_ID}/formats_default.json"

        Pridge::class.java.getClassLoader().getResourceAsStream(assetPath).use { stream ->
            if (stream == null) {
                throw IOException("Default format asset not found at: $assetPath")
            }
            InputStreamReader(stream).use { reader ->
                this.config = GSON.fromJson(reader, ChatFormat::class.java)
            }
        }
    }

    /**
     * Processes a given text through all loaded format rules in order.
     * It iterates through the rules and returns the result from the FIRST rule
     * that successfully processes the text.
     * If no rule matches, the original, unmodified text is returned.
     *
     * @param inputText The text to be formatted.
     * @return The formatted text, or the original text if no rule matched.
     */
    fun formatText(inputText: String): FormatResult {
        // Safety check to ensure the format has been loaded.
        if (config == null || config!!.formats.isEmpty()) {
            LOGGER.warn("Formatting is not loaded, cannot format text.")
            return FormatResult(inputText)
        }

        // Loop through every rule in the order they appear in the JSON file.
        for (rule in config!!.formats) {
            // Ask the current rule to process the text.
            val result: FormatResult? = rule.process(inputText)

            // The contract of our process() method is to return null if there's no match.
            // So, if the result is NOT null, we have found our match!
            if (result != null) {
                if (CONFIG_I.developerCategory.devEnabled) {
                    LOGGER.info("Ran the format rule: {}", rule)
                }
                return result
            }
        }

        // If we get here, it means the loop finished and no rule returned a non-null result.
        // In this case, we return the original text as the fallback.
        return FormatResult(inputText)
    }
}