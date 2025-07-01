package io.github.ricciow.config.categories

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class SoundsCategory {
    @Expose
    @ConfigOption(
        name = "Enable Sounds",
        desc = "Toggle the special sounds when a message containing the sound name is sent"
    )
    @ConfigEditorBoolean
    @JvmField
    var enabled = true

    @Expose
    @ConfigOption(name = "Sounds Volume", desc = "How loud the sounds will be 0-200%")
    @ConfigEditorSlider(minValue = 0f, maxValue = 200f, minStep = 1f)
    @JvmField
    var volume = 100f

    fun getVolume(): Float {
        return volume / 100f
    }
}