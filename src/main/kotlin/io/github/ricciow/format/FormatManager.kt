package io.github.ricciow.format

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import io.github.ricciow.Pridge
import io.github.ricciow.Pridge.Companion.CONFIG_DIR
import io.github.ricciow.Pridge.Companion.CONFIG_I
import io.github.ricciow.Pridge.Companion.LOGGER
import io.github.ricciow.Pridge.Companion.MOD_ID
import io.github.ricciow.util.UrlContentFetcher
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

    fun loadFromConfigAndSave() {
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

    fun initialize() {
        if (CONFIG_I.developerCategory.autoUpdate) {
            loadFromGithubAndSave()
            return
        }
        loadFromConfigAndSave()
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
    fun loadFromDefaultAssetAndSave() {
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

    fun loadFromGithub() {
        val format = UrlContentFetcher.fetchContentFromURL(CONFIG_I.developerCategory.formatURL)
        this.config = if (format == null) {
            LOGGER.error("Failed to load format from GitHub. The URL may be incorrect or the content is not accessible.")
            null
        } else {
            GSON.fromJson(format, ChatFormat::class.java)
        }
    }

    /**
     * Reads the default format file from the mod's assets and parses it.
     * @throws IOException If the asset file cannot be found or read.
     */
    fun loadFromDefaultAsset() {
        // Construct the path to the file inside the JAR
        val assetPath = "assets/$MOD_ID/formats_default.json"

        Pridge::class.java.getClassLoader().getResourceAsStream(assetPath).use { stream ->
            this.config = if (stream == null) {
                LOGGER.error("Default format asset not found at path: {}", assetPath)
                ChatFormat()
            } else {
                try {
                    InputStreamReader(stream).use { reader ->
                        GSON.fromJson(reader, ChatFormat::class.java)
                    }
                } catch (e: Exception) {
                    LOGGER.error("Failed to read default format asset from path: {}", assetPath, e)
                    ChatFormat()
                }
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
        for (rule in config!!.formats) {
            val result = rule.process(inputText)
            if (result != null) {
                if (CONFIG_I.developerCategory.devEnabled) {
                    LOGGER.info("Ran the format rule: {}", rule)
                }
                return result
            }
        }

        // If we get here, it means the loop finished and no rule returned a non-null result.
        // In this case, we return the original text as the fallback.
        LOGGER.warn("No rule found to format message: $inputText")
        return FormatResult(inputText)
    }
}