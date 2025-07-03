package io.github.ricciow.util.message

import net.minecraft.client.gui.hud.ChatHudLine

interface IChatHudLine {
    fun `pridge$getIdentifier`(): Int

    fun `pridge$setIdentifier`(identifier: Int)
}

fun ChatHudLine.getPridgeIdentifier(): Int {
    return cast().`pridge$getIdentifier`()
}

fun ChatHudLine.setPridgeIdentifier(identifier: Int) {
    cast().`pridge$setIdentifier`(identifier)
}

fun ChatHudLine.cast(): IChatHudLine {
    return this as IChatHudLine
}