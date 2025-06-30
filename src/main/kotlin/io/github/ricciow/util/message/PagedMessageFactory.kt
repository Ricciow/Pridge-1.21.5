package io.github.ricciow.util.message

import com.mojang.brigadier.arguments.StringArgumentType
import io.github.ricciow.Pridge.Companion.COMMAND_MANAGER
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.minecraft.text.Text
import net.minecraft.text.TextColor

object PagedMessageFactory {
    var lastPagedMessage: PagedMessage? = null

    /**
     * Creates a paged message, only the last sent paged message will be able to change pages.
     */
    fun createPagedMessage(
        pages: MutableList<Text>,
        title: Text,
        arrowColor: TextColor,
        disabledArrowColor: TextColor?,
        prefix: Text?
    ) {
        lastPagedMessage?.disablePaging()
        lastPagedMessage = PagedMessage(pages, title, arrowColor, disabledArrowColor, prefix)
    }

    fun createPagedMessage(
        pages: MutableList<Text>,
        titles: MutableList<Text>,
        arrowColor: TextColor,
        disabledArrowColor: TextColor?,
        prefix: Text?
    ) {
        lastPagedMessage?.disablePaging()
        lastPagedMessage = PagedMessage(pages, titles, arrowColor, disabledArrowColor, prefix)
    }

    fun initialize() {
        COMMAND_MANAGER.addCommand(
            literal("pagedmessage")
                .then(argument("direction", StringArgumentType.word())
                        .executes { commandContext ->
                            if (lastPagedMessage != null) {
                                if (StringArgumentType.getString(commandContext, "direction") == "left") {
                                    lastPagedMessage!!.lastPage()
                                } else {
                                    lastPagedMessage!!.nextPage()
                                }
                            }
                            1
                        }
                )
        )
    }
}