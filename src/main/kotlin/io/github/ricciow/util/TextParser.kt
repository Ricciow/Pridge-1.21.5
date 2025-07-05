package io.github.ricciow.util

import net.minecraft.text.HoverEvent.ShowText
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object TextParser {
    /**
     * Parses a string with legacy '&' color codes into a Minecraft Text component.
     * @param text The string to parse.
     * @return A MutableText object with proper formatting.
     */
    fun parse(text: String): MutableText {
        val mainText = "".toText()
        val parts = text.split("(?=[&§])".toRegex()) // Split before each '&' but keep it.
        var currentStyle = Style.EMPTY

        for (part in parts) {
            if ((part.startsWith("&") || part.startsWith("§")) && part.length > 1) {
                val formatting = Formatting.byCode(part[1])
                if (formatting != null) {
                    currentStyle = if (formatting.isColor) {
                        Style.EMPTY.withColor(formatting)
                    } else {
                        currentStyle.withFormatting(formatting)
                    }
                }
                mainText.append(part.substring(2).toText(currentStyle))
            } else {
                mainText.append(part.toText(currentStyle))
            }
        }
        return mainText
    }

    fun parseHoverable(text: String, hover: String): MutableText {
        return parse(text).apply {
            style = Style.EMPTY.withHoverEvent(ShowText(parse(hover)))
        }
    }

    fun parseHoverable(text: String, hover: Text): MutableText {
        return parse(text).apply {
            style = Style.EMPTY.withHoverEvent(ShowText(hover))
        }
    }
}