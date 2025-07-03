package io.github.ricciow.command.commands

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.github.ricciow.Pridge.Companion.CONFIG
import io.github.ricciow.command.Command
import io.github.ricciow.util.scheduleConfigOpen
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object PridgeCommand : Command("pridge") {
    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.executes {
            CONFIG.scheduleConfigOpen()
            return@executes SINGLE_SUCCESS
        }
    }
}