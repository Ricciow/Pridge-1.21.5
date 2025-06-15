package io.github.ricciow.config.categories;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class LinkCategory {
    @Expose
    @ConfigOption(name="Enable Link formatting", desc="To get your right to not see links?")
    @ConfigEditorBoolean
    public boolean enabled = true;

    @Expose
    @ConfigOption(name="Link Representation", desc="What any links will be turned into")
    @ConfigEditorText
    public String representation = "&a&l[Link]";
}
