package io.github.ricciow.command

import io.github.ricciow.command.commands.PagedMessageCommand
import io.github.ricciow.command.commands.PridgeCommand
import io.github.ricciow.command.commands.PridgeSoundsCommand
import io.github.ricciow.command.commands.ReloadFormattingsCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object CommandManager {
    val COMMANDS = arrayOf(
        PridgeCommand
            .append(ReloadFormattingsCommand)
            .append(PridgeSoundsCommand),
        PagedMessageCommand,
    )

    fun initialize() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            COMMANDS.forEach {
                it.register(dispatcher)
            }
        }
    }
}