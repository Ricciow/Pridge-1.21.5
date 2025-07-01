package io.github.ricciow.format

import io.github.ricciow.Pridge.Companion.CONFIG_I
import io.github.ricciow.util.TextParser.parse
import io.github.ricciow.util.message.PagedMessageFactory
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.TextColor

class FormatResult {
    lateinit var originalString: String
    private var finalText: Text? = null
    var discordText: Boolean
    var botText: Boolean

    private var disableOutput = false

    constructor(originalString: String, finalText: Text, discordText: Boolean, botText: Boolean) {
        this.originalString = originalString
        this.finalText = finalText
        this.discordText = discordText
        this.botText = botText
    }

    constructor(originalString: String, finalText: String, discordText: Boolean, botText: Boolean)
            : this(originalString, parse(finalText), discordText, botText)

    /**
     * Doesnt modify the message at all
     * @param originalString the original string
     */
    constructor(originalString: String)
            : this(originalString, originalString, false, true)

    /**
     * Result for a paged message with a singular title
     */
    constructor(
        pages: MutableList<Text>,
        title: Text,
        arrowColor: TextColor,
        disabledArrowColor: TextColor?,
        prefix: Text?,
        discordText: Boolean,
        botText: Boolean
    ) {
        this.discordText = discordText
        this.botText = botText

        val finalPrefix = getPrefix()
        if (prefix != null) {
            finalPrefix.append(" ")
            finalPrefix.append(prefix)
        }

        disableOutput = true
        PagedMessageFactory.createPagedMessage(pages, title, arrowColor, disabledArrowColor, finalPrefix)
    }

    /**
     * Result for a paged message with multiple titles
     */
    internal constructor(
        pages: MutableList<Text>,
        title: MutableList<Text>,
        arrowColor: TextColor,
        disabledArrowColor: TextColor?,
        prefix: Text?,
        discordText: Boolean,
        botText: Boolean
    ) {
        this.discordText = discordText
        this.botText = botText

        val finalPrefix = getPrefix()
        if (prefix != null) {
            finalPrefix.append(" ")
            finalPrefix.append(prefix)
        }

        disableOutput = true
        PagedMessageFactory.createPagedMessage(pages, title, arrowColor, disabledArrowColor, finalPrefix)
    }


    fun getPrefix(): MutableText {
        val prefix = StringBuilder(CONFIG_I.guildCategory.name)

        if (botText) {
            prefix.append(" ").append(CONFIG_I.botCategory.name)
        }
        if (discordText) {
            prefix.append(" ").append(CONFIG_I.discordCategory.representation)
        }

        return parse(prefix.toString())
    }

    fun getText(): Text? {
        if (disableOutput) return null

        val mainText = getPrefix()
        mainText.append(" ")
        return mainText.append(finalText)
    }

    override fun toString(): String {
        if (disableOutput) return "Text output disabled for this result - Probably a paged message"
        return getText()!!.string
    }
}