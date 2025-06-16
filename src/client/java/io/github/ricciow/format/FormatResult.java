package io.github.ricciow.format;

import io.github.ricciow.PridgeClient;
import io.github.ricciow.config.PridgeConfig;
import io.github.ricciow.util.TextParser;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class FormatResult {
    public String originalString;
    private Text finalText;
    public boolean discordText;
    public boolean botText;

    private boolean simple = false;
    private static PridgeConfig CONFIG;

    FormatResult(String originalString, Text finalText, boolean discordText, boolean botText) {
        this.originalString = originalString;
        this.finalText = finalText;
        this.discordText = discordText;
        this.botText = botText;
    }

    FormatResult(String originalString, String finalText, boolean discordText, boolean botText) {
        this.originalString = originalString;
        this.finalText = TextParser.parse(finalText);
        this.discordText = discordText;
        this.botText = botText;
    }

    /**
     * Doesnt modify the message at all
     * @param originalString the original string
     */
    FormatResult(String originalString) {
        this.originalString = originalString;
        this.finalText = TextParser.parse(originalString);
        this.discordText = false;
        this.botText = true;
    }

    public Text getText() {
        if(CONFIG == null) {
            CONFIG = PridgeClient.getConfig();
        }

        StringBuilder prefix = new StringBuilder(CONFIG.guildCategory.name);

        if(botText) {
            prefix.append(" ").append(CONFIG.botCategory.name);
        }
        if(discordText) {
            prefix.append(" ").append(CONFIG.discordCategory.representation);
        }

        prefix.append(" ");

        MutableText mainText = TextParser.parse(prefix.toString());
        return mainText.append(finalText);
    }

    @Override
    public String toString() {
        return getText().getString();
    }
}
