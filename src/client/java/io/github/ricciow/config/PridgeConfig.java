package io.github.ricciow.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.ricciow.config.categories.*;

public class PridgeConfig extends Config {
    @Override
    public String getTitle() {
        return "Pridge";
    }

    @Override
    public boolean isValidRunnable(int runnableId) {
        return false;
    }

    @Expose
    @Category(name="Guild", desc="Guild Settings")
    public GuildCategory guildCategory = new GuildCategory();

    @Expose
    @Category(name="Discord", desc="Discord Settings")
    public DiscordCategory discordCategory = new DiscordCategory();

    @Expose
    @Category(name="Sounds", desc="Special Sounds")
    public SoundsCategory soundsCategory = new SoundsCategory();

    @Expose
    @Category(name="Bot", desc="Bot related Settings")
    public BotCategory botCategory = new BotCategory();

    @Expose
    @Category(name="Links", desc="Link Settings")
    public LinkCategory linkCategory = new LinkCategory();

    @Expose
    @Category(name="Filters", desc="Word Filters")
    public FiltersCategory filtersCategory = new FiltersCategory();

    @Expose
    @Category(name="Developer", desc="Developer mode configurations")
    public DeveloperCategory developerCategory = new DeveloperCategory();
}
