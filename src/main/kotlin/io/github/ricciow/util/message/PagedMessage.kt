package io.github.ricciow.util.message

import net.minecraft.text.ClickEvent.RunCommand
import net.minecraft.text.HoverEvent.ShowText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import java.util.*

class PagedMessage {
    private val prefix: Text?
    private val pages: MutableList<Text>
    private val titles: MutableList<Text>
    private val arrowColor: TextColor
    private val disabledArrowColor: TextColor
    private var pageIndex = 0
    private val message: ModifiableMessage
    private val id: String
    private var disabled = false

    internal constructor(
        pages: MutableList<Text>,
        title: Text,
        arrowColor: TextColor,
        disabledArrowColor: TextColor?,
        prefix: Text?
    ) {
        this.pages = pages
        this.titles = mutableListOf(title)
        this.arrowColor = arrowColor
        this.disabledArrowColor = disabledArrowColor ?: arrowColor
        this.id = UUID.randomUUID().toString()
        this.prefix = prefix
        message = ModifiableMessage(buildText(), id)
    }

    internal constructor(
        pages: MutableList<Text>,
        titles: MutableList<Text>,
        arrowColor: TextColor,
        disabledArrowColor: TextColor?,
        prefix: Text?
    ) {
        this.pages = pages
        this.titles = titles
        this.arrowColor = arrowColor
        this.disabledArrowColor = disabledArrowColor ?: arrowColor
        this.id = UUID.randomUUID().toString()
        this.prefix = prefix
        message = ModifiableMessage(buildText(), id)
    }

    private fun buildText(): Text {
        var title: Text? = null
        if (pageIndex in 0 until pages.size) {
            title = titles.getOrNull(pageIndex) ?: titles.firstOrNull() ?: Text.literal("No title found")
        }

        val baseText = if (prefix != null) prefix.copy() else Text.literal("")
        baseText.append(Text.literal("<< ").setStyle(buildLeftStyle()))
        baseText.append(title)
        baseText.append(Text.literal(" >>").setStyle(buildRightStyle()))
        baseText.append("\n")
        baseText.append(pages[pageIndex])

        return baseText
    }

    private fun buildLeftStyle(): Style? {
        val baseStyle = Style.EMPTY.withColor(disabledArrowColor)
        if (disabled) {
            return baseStyle.withHoverEvent(ShowText(Text.literal("Paging Disabled")))
        }

        if (pageIndex != 0) {
            return baseStyle
                .withColor(arrowColor)
                .withClickEvent(RunCommand("pagedmessage left"))
                .withHoverEvent(ShowText(Text.literal("Previous page")))
        }

        return baseStyle.withHoverEvent(ShowText(Text.literal("No pages to the Left!")))
    }

    private fun buildRightStyle(): Style? {
        val baseStyle = Style.EMPTY.withColor(disabledArrowColor)
        if (disabled) {
            return baseStyle.withHoverEvent(ShowText(Text.literal("Paging Disabled")))
        }

        if (pageIndex < pages.size - 1) {
            return baseStyle
                .withColor(arrowColor)
                .withClickEvent(RunCommand("pagedmessage right"))
                .withHoverEvent(ShowText(Text.literal("Next page")))
        }

        return baseStyle.withHoverEvent(ShowText(Text.literal("No pages to the Right!")))
    }

    fun disablePaging() {
        disabled = true
        message.modify(buildText())
    }

    fun setPage(page: Int) {
        if (page in 0 until pages.size) {
            pageIndex = page
            message.modify(buildText())
        }
    }

    fun nextPage() {
        if (pageIndex < pages.size - 1) {
            setPage(pageIndex + 1)
        }
    }

    fun lastPage() {
        if (pageIndex > 0) {
            setPage(pageIndex - 1)
        }
    }

    override fun toString(): String {
        return "Paged Message with id: $id"
    }
}