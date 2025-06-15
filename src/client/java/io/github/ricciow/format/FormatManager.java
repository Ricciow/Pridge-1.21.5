package io.github.ricciow.format;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import io.github.ricciow.PridgeClient;

import io.github.ricciow.util.UrlContentFetcher;
import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FormatManager {

    private static final Logger LOGGER = PridgeClient.LOGGER;
    private static final Gson GSON;
    private static final String FORMAT_URL = "https://raw.githubusercontent.com/Ricciow/Pridge-1.21.5/master/src/main/resources/assets/pridge/formats_default.json";

    static {
        RuntimeTypeAdapterFactory<FormatRule> ruleAdapterFactory = RuntimeTypeAdapterFactory
                .of(FormatRule.class, "type")
                .registerSubtype(RegexFormatRule.class, "regex")
                .registerSubtype(StringFormatRule.class, "string")
                .registerSubtype(StringArrayFormatRule.class, "stringarray")
                .registerSubtype(SpecialFormatRule.class, "special");

        GSON = new GsonBuilder()
                .registerTypeAdapterFactory(ruleAdapterFactory)
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }

    private final String modId;
    private final Path configFile;
    public ChatFormat config;

    public FormatManager(String modId) {
        this.modId = modId;
        this.configFile = FabricLoader.getInstance().getConfigDir()
                .resolve(modId)
                .resolve("formats.json");
        load();
    }

    public void load() {
        if(PridgeClient.getConfig().developerCategory.auto_update) {
            try {
                LOGGER.info("Loaded formattings from GitHub");
                loadFromGithub();
                save();
                return;
            } catch (IOException | URISyntaxException e) {
                LOGGER.error("Failed to load from github:", e);
            }
        }
        if (Files.exists(configFile)) {
            try (FileReader reader = new FileReader(configFile.toFile())) {
                LOGGER.info("Loading existing format file...");
                config = GSON.fromJson(reader, ChatFormat.class);
                if (config == null) {
                    throw new IOException("Format file is empty or corrupted.");
                }
                LOGGER.info("Format loaded successfully.");
            } catch (IOException | JsonSyntaxException e) {
                LOGGER.error("Failed to load format file! Creating a new default format from asset.", e);
                loadFromDefaultAssetAndSave();
            }
        } else {
            LOGGER.info("No format file found. Creating a new default format from asset...");
            loadFromDefaultAssetAndSave();
        }

        // IMPORTANT: Initialize all rules AFTER the config object has been populated,
        // regardless of whether it came from a user file or the default asset.
        if (config != null && config.getFormats() != null) {
            for (FormatRule rule : config.getFormats()) {
                rule.initialize();
            }
        }
    }

    public void save() {
        try {
            Files.createDirectories(configFile.getParent());
            try (FileWriter writer = new FileWriter(configFile.toFile())) {
                GSON.toJson(config, writer);
                LOGGER.info("Format saved successfully to {}", configFile);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save format file.", e);
        }
    }

    /**
     * Loads the default config from the bundled assets, sets it as the current config, and saves it.
     */
    private void loadFromDefaultAssetAndSave() {
        try {
            loadFromDefaultAsset();
            save();
        } catch (IOException e) {
            LOGGER.error("FATAL: Could not load default format from assets! The mod may not function correctly.", e);
            // If loading from assets fails, create an empty format as a last resort.
            this.config = new ChatFormat();
        }
    }

    private void loadFromGithub() throws IOException, URISyntaxException {
        String format = UrlContentFetcher.fetchContentFromURL(FORMAT_URL);
        this.config = GSON.fromJson(format, ChatFormat.class);
    }

    /**
     * Reads the default format file from the mod's assets and parses it.
     * @throws IOException If the asset file cannot be found or read.
     */
    private void loadFromDefaultAsset() throws IOException {
        // Construct the path to the file inside the JAR
        String assetPath = "assets/" + this.modId + "/formats_default.json";

        // Use the ClassLoader to get the resource as a stream
        try (InputStream stream = PridgeClient.class.getClassLoader().getResourceAsStream(assetPath)) {
            if (stream == null) {
                throw new IOException("Default format asset not found at: " + assetPath);
            }
            try (InputStreamReader reader = new InputStreamReader(stream)) {
                this.config = GSON.fromJson(reader, ChatFormat.class);
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
    public FormatResult formatText(String inputText) {
        // Safety check to ensure the format has been loaded.
        if (config == null || config.getFormats() == null) {
            LOGGER.warn("Formatting is not loaded, cannot format text.");
            return new FormatResult(inputText);
        }

        // Loop through every rule in the order they appear in the JSON file.
        for (FormatRule rule : config.getFormats()) {
            // Ask the current rule to process the text.
            FormatResult result = rule.process(inputText);

            // The contract of our process() method is to return null if there's no match.
            // So, if the result is NOT null, we have found our match!
            if (result != null) {
                // Immediately return the result from the first matching rule.
                LOGGER.info("Ran the format rule: {}", rule);
                return result;
            }
        }

        // If we get here, it means the loop finished and no rule returned a non-null result.
        // In this case, we return the original text as the fallback.
        return new FormatResult(inputText);
    }
}