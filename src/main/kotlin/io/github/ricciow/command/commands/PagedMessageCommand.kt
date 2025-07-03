package io.github.ricciow.command.commands

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.github.ricciow.command.Command
import io.github.ricciow.util.message.PagedMessageFactory
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.Text

object PagedMessageCommand : Command("pagedmessage") {
    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.then(
            arg("message id", IntegerArgumentType.integer(0, Integer.MAX_VALUE)).then(
                arg(
                    "direction",
                    StringArgumentType.word()
                ).executes { commandContext ->
                    val messageId = IntegerArgumentType.getInteger(commandContext, "message id")
                    val direction = StringArgumentType.getString(commandContext, "direction")
                    val message = PagedMessageFactory.getMessageById(messageId) ?: return@executes 0.also {
                        commandContext.source.sendError(Text.literal("No paged message found with ID '$messageId'"))
                    }
                    if (direction == "left") {
                        message.previousPage()
                    } else {
                        message.nextPage()
                    }
                    return@executes SINGLE_SUCCESS
                })
        )
    }
}