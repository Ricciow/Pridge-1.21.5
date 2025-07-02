package io.github.ricciow.config.categories

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class DeveloperCategory {
    @Expose
    @ConfigOption(name = "Toggle Formatter", desc = "Disable/Enable Pridge formatter")
    @ConfigEditorBoolean
    @JvmField
    var enabled = true

    @Expose
    @ConfigOption(name = "Developer Mode", desc = "Enable some dev stuff")
    @ConfigEditorBoolean
    @JvmField
    var devEnabled = false

    @Expose
    @ConfigOption(name = "Auto update", desc = "Updates the formattings automatically upon loading Minecraft")
    @ConfigEditorBoolean
    @JvmField
    var autoUpdate = true
}