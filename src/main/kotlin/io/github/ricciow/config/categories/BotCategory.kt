package io.github.ricciow.config.categories

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class BotCategory {
    @Expose
    @ConfigOption(name = "New bot name", desc = "What bot messages will be labeled as")
    @ConfigEditorText
    @JvmField
    var name = "&b&l(Bot)"

    @Expose
    @ConfigOption(name = "Bot IGN", desc = "The username of the bot being utilized")
    @ConfigEditorText
    @JvmField
    var ign = "NqekMyBeloved"

    @Expose
    @ConfigOption(
        name = "Line Limit",
        desc = "The limit of data lines on a paged message, set it to 0 to have no limit, default = 10"
    )
    @ConfigEditorSlider(minValue = 0f, maxValue = 20f, minStep = 1f)
    @JvmField
    var lineCount = 10

    fun getLineCount(): Int {
        if (lineCount == 0) {
            return 16384
        }
        return lineCount
    }

}