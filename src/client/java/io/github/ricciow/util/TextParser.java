package io.github.ricciow.util;

import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.net.URI;

public class TextParser {

    /**
     * Parses a string with legacy '&' color codes into a Minecraft Text component.
     * @param text The string to parse.
     * @return A MutableText object with proper formatting.
     */
    public static MutableText parse(String text) {
        MutableText mainText = Text.literal("");
        String[] parts = text.split("(?=&)"); // Split before each '&' but keep it.
        Style currentStyle = Style.EMPTY;

        for (String part : parts) {
            if (part.startsWith("&") && part.length() > 1) {
                Formatting formatting = Formatting.byCode(part.charAt(1));
                if (formatting != null) {
                    if (formatting.isColor()) {
                        currentStyle = Style.EMPTY.withColor(formatting);
                    } else {
                        currentStyle = currentStyle.withFormatting(formatting);
                    }
                }
                mainText.append(Text.literal(part.substring(2)).setStyle(currentStyle));
            } else {
                mainText.append(Text.literal(part).setStyle(currentStyle));
            }
        }
        return mainText;
    }

    public static MutableText parseHoverable(String text, String hover) {
        MutableText mainText = parse(text);

        HoverEvent hoverEvent = new HoverEvent.ShowText(parse(hover));

        Style hoverStyle = Style.EMPTY.withHoverEvent(hoverEvent);

        mainText.setStyle(hoverStyle);

        return mainText;
    }

    public static MutableText parseHoverable(String text, Text hover) {
        MutableText mainText = parse(text);

        HoverEvent hoverEvent = new HoverEvent.ShowText(hover);

        Style hoverStyle = Style.EMPTY.withHoverEvent(hoverEvent);

        mainText.setStyle(hoverStyle);

        return mainText;
    }
}