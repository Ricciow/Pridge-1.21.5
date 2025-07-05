package io.github.ricciow.util.message

import io.github.ricciow.util.ChatUtils
import net.minecraft.text.Text

class ModifiableMessage(private var text: Text, private val id: Int) {

    init {
        updateMessage(false)
    }

    fun modify(text: Text) {
        this.text = text
        updateMessage(true)
    }

    private fun updateMessage(replaceExisting: Boolean) {
        ChatUtils.sendMessage(text, id, replaceExisting)
    }
}