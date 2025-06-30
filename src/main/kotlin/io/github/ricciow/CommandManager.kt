package io.github.ricciow

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import java.util.concurrent.CompletableFuture
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

/**
 * Manages the registration of client-side commands.
 * Commands are added to a list and then registered all at once.
 */
class CommandManager {

    private val commands = mutableListOf<LiteralArgumentBuilder<FabricClientCommandSource>>()

    fun addCommand(command: LiteralArgumentBuilder<FabricClientCommandSource>) {
        commands.add(command)
    }

    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, registryAccess ->
            for (command in commands) {
                dispatcher.register(command)
            }
        }
    }
}

class StringListSuggestionProvider(private val stringList: List<String>) : SuggestionProvider<FabricClientCommandSource> {
    override fun getSuggestions(
        context: CommandContext<FabricClientCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        for (type in stringList) {
            builder.suggest(type)
        }
        return builder.buildFuture()
    }
}