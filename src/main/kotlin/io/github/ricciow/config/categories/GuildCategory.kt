package io.github.ricciow.config.categories

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class GuildCategory {
    @Expose
    @ConfigOption(
        name = "Modify normal guild messages",
        desc = "Allow the mod to modify the original Guild > for any messages"
    )
    @ConfigEditorBoolean
    @JvmField
    var modifyNormalGuildMessages = true

    @Expose
    @ConfigOption(name = "Enable boop/boo messages", desc = "Sends a message whenever you receive a Boop/Boo")
    @ConfigEditorBoolean
    @JvmField
    var thanksForTheBoop = true

    @Expose
    @ConfigOption(name = "Boop reply message", desc = "The message to send when you receive a Boop! 'Thanks for the Boop, {user}!', can have multiple {user}")
    @ConfigEditorText
    @JvmField
    var boopReplyMessage = "Thanks for the Boop, {user}!"

    @Expose
    @ConfigOption(name = "Enable boop/boo messages", desc = "The message to send when you receive a Boop! 'AAH! You scared me, {user}!', can have multiple {user}")
    @ConfigEditorText
    @JvmField
    var booReplyMessage = "AAH! You scared me, {user}!"

    @Expose
    @ConfigOption(name = "Modify join/leave messages", desc = "Will add a custom colored Join/Leave message")
    @ConfigEditorBoolean
    @JvmField
    var modifyJoinLeave = true

    @Expose
    @ConfigOption(name = "Guild prefix new name", desc = "What Guild > will be modified to")
    @ConfigEditorText
    @JvmField
    var name = "&2Pridge >"
}