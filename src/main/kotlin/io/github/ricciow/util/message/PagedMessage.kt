package io.github.ricciow.util.message

import io.github.ricciow.Pridge.Companion.LOGGER
import net.minecraft.text.ClickEvent.RunCommand
import net.minecraft.text.HoverEvent.ShowText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import java.util.*

class PagedMessage {
    val id: Int
    private val prefix: Text?
    private val pages: MutableList<Text>
    private val titles: MutableList<Text>
    private val arrowColor: TextColor
    private val disabledArrowColor: TextColor
    private var pageIndex = 0
    private val message: ModifiableMessage

    internal constructor(
        pages: MutableList<Text>,
        title: Text,
        arrowColor: TextColor,
        disabledArrowColor: TextColor?,
        prefix: Text?
    ) {
        this.id = nextId
        this.pages = pages
        this.titles = mutableListOf(title)
        this.arrowColor = arrowColor
        this.disabledArrowColor = disabledArrowColor ?: arrowColor
        this.prefix = prefix
        this.message = ModifiableMessage(buildText(), id)

        try {
            println(toString())
        } catch (e: Exception) {
            LOGGER.error("Error while printing PagedMessage toString", e)
        }
    }

    internal constructor(
        pages: MutableList<Text>,
        titles: MutableList<Text>,
        arrowColor: TextColor,
        disabledArrowColor: TextColor?,
        prefix: Text?
    ) {
        this.id = nextId
        this.pages = pages
        this.titles = titles
        this.arrowColor = arrowColor
        this.disabledArrowColor = disabledArrowColor ?: arrowColor
        this.prefix = prefix
        this.message = ModifiableMessage(buildText(), id)

        try {
            println(toString())
        } catch (e: Exception) {
            LOGGER.error("Error while printing PagedMessage toString", e)
        }
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

    private fun buildLeftStyle(): Style {
        val baseStyle = Style.EMPTY.withColor(disabledArrowColor)

        if (pageIndex != 0) {
            return baseStyle
                .withColor(arrowColor)
                .withClickEvent(RunCommand("pagedmessage $id left"))
                .withHoverEvent(ShowText(Text.literal("Previous page")))
        }

        return baseStyle.withHoverEvent(ShowText(Text.literal("No pages to the Left!")))
    }

    private fun buildRightStyle(): Style {
        val baseStyle = Style.EMPTY.withColor(disabledArrowColor)

        if (pageIndex < pages.size - 1) {
            return baseStyle
                .withColor(arrowColor)
                .withClickEvent(RunCommand("pagedmessage $id right"))
                .withHoverEvent(ShowText(Text.literal("Next page")))
        }

        return baseStyle.withHoverEvent(ShowText(Text.literal("No pages to the Right!")))
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

    fun previousPage() {
        if (pageIndex > 0) {
            setPage(pageIndex - 1)
        }
    }

    override fun toString(): String {
        val titleAndPageList = titles.mapIndexed { index, text ->
            """
            {
                Title ${index + 1}: ${text.string}
                Page ${index + 1}: ${pages.getOrNull(index)?.string ?: "No page available"}
            }
            """.trimIndent()
        }


        val toString =
        """
        PagedMessage: {
            ID: $id
            Prefix: ${prefix?.string ?: "No prefix"}
            Pages: {
                ${titleAndPageList.joinToString("\n")}
            }
            Current Page Index: $pageIndex
            Arrow Color: ${arrowColor.name}
            Disabled Arrow Color: ${disabledArrowColor.name}
        }
        """.trimIndent()
        return toString
    }

    companion object {
        private var nextId = 0
            get(): Int {
                val currentId = field
                field++
                return currentId
            }
    }
}