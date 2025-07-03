package io.github.ricciow.util.message

import net.minecraft.text.Text
import net.minecraft.text.TextColor

object PagedMessageFactory {
    private val pagedMessages = mutableMapOf<Int, PagedMessage>()
    fun getMessageById(id: Int): PagedMessage? {
        return pagedMessages[id]
    }

    /**
     * Creates a paged message, only the last sent paged message will be able to change pages.
     */
    fun createPagedMessage(
        pages: MutableList<Text>,
        title: Text,
        arrowColor: TextColor,
        disabledArrowColor: TextColor,
        prefix: Text?
    ) {
        val pagedMessage = PagedMessage(pages, title, arrowColor, disabledArrowColor, prefix)
        pagedMessages[pagedMessage.id] = pagedMessage
    }

    fun createPagedMessage(
        pages: MutableList<Text>,
        titles: MutableList<Text>,
        arrowColor: TextColor,
        disabledArrowColor: TextColor,
        prefix: Text?
    ) {
        val pagedMessage = PagedMessage(pages, titles, arrowColor, disabledArrowColor, prefix)
        pagedMessages[pagedMessage.id] = pagedMessage
    }
}