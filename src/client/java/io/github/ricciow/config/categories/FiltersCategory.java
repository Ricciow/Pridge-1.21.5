package io.github.ricciow.config.categories;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class FiltersCategory {
    @Expose
    @ConfigOption(name="Add a placeholder", desc="Adds a text when a message is blocked")
    @ConfigEditorBoolean
    public boolean placeholder = true;

    @Expose
    @ConfigOption(name="Filter", desc="Words which if they are in the message, the message gets blocked (uses a RegExp), Example: word1|word2|word3")
    @ConfigEditorText
    public String rawFilter = "";

    public String[] getFilters() {
        return rawFilter.split("\\|");
    }
}
