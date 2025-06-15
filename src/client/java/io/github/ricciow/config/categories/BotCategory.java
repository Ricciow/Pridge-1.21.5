package io.github.ricciow.config.categories;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class BotCategory {
    @Expose
    @ConfigOption(name="New bot name", desc="What bot messages will be labeled as")
    @ConfigEditorText
    public String name = "&b&l(Bot)";

    @Expose
    @ConfigOption(name="Bot IGN", desc="The username of the bot being utilized")
    @ConfigEditorText
    public String ign = "NqekMyBeloved";
}
