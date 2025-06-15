package io.github.ricciow.config.categories;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class DeveloperCategory {
    @Expose
    @ConfigOption(name="Toggle Formatter", desc="Disable/Enable Pridge formatter")
    @ConfigEditorBoolean
    public boolean enabled = true;

    @Expose
    @ConfigOption(name="Developer Mode", desc="Enable some dev stuff")
    @ConfigEditorBoolean
    public boolean dev_enabled = false;

    @Expose
    @ConfigOption(name="Auto update", desc="Updates the formattings automatically upon loading Minecraft")
    @ConfigEditorBoolean
    public boolean auto_update = true;
}
