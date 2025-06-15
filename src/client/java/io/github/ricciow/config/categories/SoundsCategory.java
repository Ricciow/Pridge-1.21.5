package io.github.ricciow.config.categories;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class SoundsCategory {

    @Expose
    @ConfigOption(name="Enable Sounds", desc="Toggle the special sounds when a message containing the sound name is sent")
    @ConfigEditorBoolean
    public boolean enabled = true;

    @Expose
    @ConfigOption(name="Sounds Volume", desc="How loud the sounds will be 0-200%")
    @ConfigEditorSlider(minValue = 0f, maxValue = 200f, minStep = 1f)
    public Float volume = 100f;

    public Float getVolume() {
        return volume/100;
    }
}
