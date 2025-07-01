package io.github.ricciow.util.message

import net.minecraft.client.gui.hud.ChatHud
import net.minecraft.text.Text

interface IdentifiableChatHud {
    fun `pridge$addIdentifiableMessage`(id: String, message: Text)

    fun `pridge$removeIdentifiableMessage`(id: String)

    fun `pridge$replaceIdentifiableMessage`(id: String, message: Text)
}

fun ChatHud.addIdentifiableMessage(id: String, message: Text) {
    return (this as IdentifiableChatHud).`pridge$addIdentifiableMessage`(id, message)
}

fun ChatHud.removeIdentifiableMessage(id: String) {
    return (this as IdentifiableChatHud).`pridge$removeIdentifiableMessage`(id)
}

fun ChatHud.replaceIdentifiableMessage(id: String, message: Text) {
    return (this as IdentifiableChatHud).`pridge$replaceIdentifiableMessage`(id, message)
}
