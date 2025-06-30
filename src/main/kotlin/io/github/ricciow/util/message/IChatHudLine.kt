package io.github.ricciow.util.message

import net.minecraft.client.gui.hud.ChatHudLine

interface IChatHudLine {
    fun `pridge$getIdentifier`(): String?

    fun `pridge$setIdentifier`(identifier: String?)
}

fun ChatHudLine.getPridgeIdentifier(): String? {
    return cast().`pridge$getIdentifier`()
}

fun ChatHudLine.setPridgeIdentifier(identifier: String?) {
    cast().`pridge$setIdentifier`(identifier)
}

fun ChatHudLine.cast(): IChatHudLine {
    return this as IChatHudLine
}