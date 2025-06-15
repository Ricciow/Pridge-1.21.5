package io.github.ricciow.config.categories;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class GuildCategory {
    @Expose
    @ConfigOption(name="Modify normal guild messages", desc="Allow the mod to modify the original Guild > for any messages")
    @ConfigEditorBoolean
    public boolean modify_normal_guild_messages = true;

    @Expose
    @ConfigOption(name="Enable boop/boo messages", desc="Sends a message whenever you receive a Boop/Boo")
    @ConfigEditorBoolean
    public boolean thanks_for_the_boop = true;

    @Expose
    @ConfigOption(name="Modify join/leave messages", desc="Will add a custom colored Join/Leave message")
    @ConfigEditorBoolean
    public boolean modify_join_leave = true;

    @Expose
    @ConfigOption(name="Guild prefix new name", desc="What Guild > will be modified to")
    @ConfigEditorText
    public String name = "&2Pridge >";
}