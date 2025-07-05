package io.github.ricciow.util

import io.github.ricciow.Pridge.CONFIG_I
import io.github.ricciow.Pridge.mc
import io.github.ricciow.util.message.pridgeId
import net.minecraft.client.gui.hud.ChatHudLine
import net.minecraft.client.gui.hud.MessageIndicator
import net.minecraft.text.Text

object ChatUtils {
    var nextMessageId = -1

    fun info(string: String, id: Int = 0) = sendMessage(CONFIG_I.guildCategory.name.toText().append(string), id)

    fun info(text: Text, id: Int = 0) = sendMessage(CONFIG_I.guildCategory.name.toText().append(text), id)

    fun sendMessage(string: String?, id: Int = 0) = sendMessage(string?.toText(), id)

    fun sendMessage(text: Text?, id: Int = 0, replaceInPlace: Boolean = false) {
        var fullText = text ?: return

        if (CONFIG_I.developerCategory.devEnabled) {
            fullText = "{$id}".toText().append(text)
        }

        mc.executeSync {
            if (id != 0) {
                if (replaceInPlace) {
                    replaceInPlace(fullText, id)
                    return@executeSync
                } else {
                    deletePreviousMessage(id)
                }
            }
            nextMessageId = id
            mc.inGameHud.chatHud.addMessage(fullText)
        }
    }

    private fun replaceInPlace(text: Text, id: Int) {
        var replaced = false

        val messages = mc.inGameHud.chatHud.messages
        for (i in messages.indices) {
            val currentLine = messages[i]
            val lineId = currentLine.pridgeId
            if (lineId > 0 && id == lineId) {
                messages[i] = ChatHudLine(
                    currentLine.creationTick(),
                    text,
                    null,
                    if (mc.isConnectedToLocalServer) MessageIndicator.singlePlayer() else MessageIndicator.system()
                ).apply {
                    pridgeId = id
                }
                replaced = true
            }
        }

        if (replaced) {
            val tempScrolledLines = mc.inGameHud.chatHud.scrolledLines
            mc.inGameHud.chatHud.refresh()
            mc.inGameHud.chatHud.scrolledLines = tempScrolledLines
        }
    }

    private fun deletePreviousMessage(id: Int) {
        val initialSize = mc.inGameHud.chatHud.messages.size

        mc.inGameHud.chatHud.messages.removeIf {
            it.pridgeId == id
        }

        if (initialSize != mc.inGameHud.chatHud.messages.size) {
            mc.inGameHud.chatHud.refresh()
        }
    }
}