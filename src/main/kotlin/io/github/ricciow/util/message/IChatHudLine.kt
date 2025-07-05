package io.github.ricciow.util.message

import net.minecraft.client.gui.hud.ChatHudLine

interface IChatHudLine {
    fun `pridge$getIdentifier`(): Int

    fun `pridge$setIdentifier`(identifier: Int)
}

inline var ChatHudLine.pridgeId
    get() = cast().`pridge$getIdentifier`()
    set(value) = cast().`pridge$setIdentifier`(value)

fun ChatHudLine.cast(): IChatHudLine {
    return this as IChatHudLine
}