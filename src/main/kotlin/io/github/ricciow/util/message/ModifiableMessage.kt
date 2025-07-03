package io.github.ricciow.util.message

import io.github.ricciow.Pridge.Companion.LOGGER
import io.github.ricciow.Pridge.Companion.mc
import net.minecraft.client.gui.hud.ChatHud
import net.minecraft.text.Text

class ModifiableMessage(private var text: Text?, private val id: Int) {
    private val chatHud: ChatHud?
        get() = mc.inGameHud.chatHud
    private var sent = false

    init {
        updateMessage()
    }

    fun modify(text: Text) {
        this.text = text
        updateMessage()
    }

    private fun updateMessage() {
        if (text == null || chatHud == null) {
            val nullThing = when (text) {
                null if chatHud == null -> "text and chatHud"
                null -> "text"
                else -> "chatHud"
            }
            LOGGER.warn("$nullThing is null, not updating/sending ModifiableMessage")
            return
        }

        if (sent) {
            chatHud!!.replaceIdentifiableMessage(id, text!!)
        } else {
            chatHud!!.addIdentifiableMessage(id, text!!)
            sent = true
        }
    }
}