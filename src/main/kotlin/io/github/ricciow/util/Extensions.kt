package io.github.ricciow.util

import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.managed.ManagedConfig
import io.github.ricciow.Pridge.Companion.mc
import net.minecraft.client.network.ClientPlayerEntity

enum class ChatType(val prefix: String) {
    ALL("/ac"),
    PARTY("/pc"),
    GUILD("/gc"),
    PRIVATE("/msg")
}

fun ClientPlayerEntity?.chatHypixel(type: ChatType, message: String?) {
    if (message != null) {
        sendCommand("${type.prefix} $message")
    }
}

fun ClientPlayerEntity?.sendCommand(command: String): Boolean {
    return this?.networkHandler?.sendCommand(command.substring(1)) ?: false
}

fun ClientPlayerEntity?.sendMessage(message: String) {
    if (message.startsWith("/")) {
        sendCommand(message)
    } else {
        this?.networkHandler?.sendChatMessage(message)
    }
}

fun <T : Config> ManagedConfig<T>.scheduleConfigOpen() {
    mc.send { openConfigGui() }
}