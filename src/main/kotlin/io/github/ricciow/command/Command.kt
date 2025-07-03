package io.github.ricciow.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

abstract class Command(val name: String) {
    protected val subCommands = mutableListOf<Command>()

    protected abstract fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>)

    fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        dispatcher.register(build())
    }

    fun build(): LiteralArgumentBuilder<FabricClientCommandSource> {
        val builder = lit(name)

        for (subCommand in subCommands) {
            builder.then(subCommand.build())
        }

        build(builder)

        return builder
    }

    protected fun lit(literal: String): LiteralArgumentBuilder<FabricClientCommandSource> {
        return LiteralArgumentBuilder.literal(literal)
    }

    protected fun <T> arg(name: String, type: ArgumentType<T>): RequiredArgumentBuilder<FabricClientCommandSource, T> {
        return RequiredArgumentBuilder.argument(name, type)
    }

    fun append(command: Command): Command {
        subCommands.add(command)
        return this
    }
}