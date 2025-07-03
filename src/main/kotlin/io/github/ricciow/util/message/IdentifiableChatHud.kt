package io.github.ricciow.util.message

import net.minecraft.client.gui.hud.ChatHud
import net.minecraft.text.Text

interface IdentifiableChatHud {
    fun `pridge$addIdentifiableMessage`(id: Int, message: Text)

    fun `pridge$removeIdentifiableMessage`(id: Int)

    fun `pridge$replaceIdentifiableMessage`(id: Int, message: Text)
}

fun ChatHud.addIdentifiableMessage(id: Int, message: Text) {
    return (this as IdentifiableChatHud).`pridge$addIdentifiableMessage`(id, message)
}

fun ChatHud.removeIdentifiableMessage(id: Int) {
    return (this as IdentifiableChatHud).`pridge$removeIdentifiableMessage`(id)
}

fun ChatHud.replaceIdentifiableMessage(id: Int, message: Text) {
    return (this as IdentifiableChatHud).`pridge$replaceIdentifiableMessage`(id, message)
}
