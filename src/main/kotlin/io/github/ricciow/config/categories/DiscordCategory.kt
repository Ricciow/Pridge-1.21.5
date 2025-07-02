package io.github.ricciow.config.categories

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property
import io.github.ricciow.util.ColorCode

class DiscordCategory {

    @Expose
    @ConfigOption(name = "Discord Message Color", desc = "The color which the discord message will be displayed as")
    @ConfigEditorDropdown
    @JvmField
    var messageColor: Property<ColorCode> = Property.of(ColorCode.WHITE)

    @Expose
    @ConfigOption(name = "Discord Name Color", desc = "The color which the discord username will be displayed as")
    @ConfigEditorDropdown
    @JvmField
    var nameColor: Property<ColorCode> = Property.of(ColorCode.YELLOW)

    @Expose
    @ConfigOption(name = "Discord Representation", desc = "The prefix for discord messages")
    @ConfigEditorText
    @JvmField
    var representation: String = "&9&l(Discord)"
}