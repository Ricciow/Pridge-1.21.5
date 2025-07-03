package io.github.ricciow.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.text.Text
import java.util.concurrent.CompletableFuture

class StringListArgumentType(private val stringList: List<String>) : ArgumentType<String> {
    constructor(vararg strings: String) : this(strings.toList())

    override fun parse(reader: StringReader): String {
        val input = reader.readString()
        return stringList.find { it == input }
            ?: throw SimpleCommandExceptionType(
                Text.literal("Invalid argument: '$input'. Expected one of: ${stringList.joinToString(", ")}")
            ).createWithContext(reader)
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return CommandSource.suggestMatching(stringList, builder)
    }
}