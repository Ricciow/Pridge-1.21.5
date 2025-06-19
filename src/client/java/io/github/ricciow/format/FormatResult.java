package io.github.ricciow.format;

import io.github.ricciow.PridgeClient;
import io.github.ricciow.config.PridgeConfig;
import io.github.ricciow.util.TextParser;
import io.github.ricciow.util.message.PagedMessage;
import io.github.ricciow.util.message.PagedMessageFactory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FormatResult {
    public String originalString;
    private Text finalText;
    public boolean discordText;
    public boolean botText;

    private PagedMessage pagedMessage;
    private static PridgeConfig CONFIG;
    private boolean disableOutput = false;

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

    /**
     * Result for a paged message with a singular title
     */
    FormatResult(List<Text> pages, Text title, TextColor arrowColor, @Nullable TextColor disabledArrowColor, @Nullable Text prefix, boolean discordText, boolean botText) {
        this.discordText = discordText;
        this.botText = botText;

        MutableText finalPrefix = getPrefix();
        if(prefix != null) {
            finalPrefix.append(" ");
            finalPrefix.append(prefix);
        }

        disableOutput = true;
        PagedMessageFactory.createPagedMessage(pages, title, arrowColor, disabledArrowColor, finalPrefix);
    }

    /**
     * Result for a paged message with multiple titles
     */
    FormatResult(List<Text> pages, List<Text> title, TextColor arrowColor, @Nullable TextColor disabledArrowColor, @Nullable Text prefix, boolean discordText, boolean botText) {
        this.discordText = discordText;
        this.botText = botText;

        MutableText finalPrefix = getPrefix();
        if(prefix != null) {
            finalPrefix.append(" ");
            finalPrefix.append(prefix);
        }

        disableOutput = true;
        PagedMessageFactory.createPagedMessage(pages, title, arrowColor, disabledArrowColor, finalPrefix);
    }


    public MutableText getPrefix() {
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

        return TextParser.parse(prefix.toString());
    }

    public Text getText() {
        if(disableOutput) return null;

        MutableText mainText = getPrefix();
        mainText.append(" ");
        return mainText.append(finalText);
    }

    @Override
    public String toString() {
        if(disableOutput) return "Text output disabled for this result - Probably a paged message";
        return getText().getString();
    }
}
