package io.github.ricciow.util.message;

import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class PagedMessage{

    private Text prefix;
    private List<Text> pages;
    private List<Text> titles;
    private TextColor arrowColor;
    private TextColor disabledArrowColor;
    private Integer pageNumber = 0;
    private final ModifiableMessage message;
    private final String id;
    private boolean disabled = false;

    PagedMessage(List<Text> pages, Text title, TextColor arrowColor, @Nullable TextColor disabledArrowColor, @Nullable Text prefix) {
        this.pages =  pages;
        this.titles = List.of(title);
        this.arrowColor = arrowColor;
        this.disabledArrowColor = disabledArrowColor != null ? disabledArrowColor : arrowColor;
        this.id = UUID.randomUUID().toString();
        this.prefix = prefix;
        message = new ModifiableMessage(buildText(), id);
    }

    PagedMessage(List<Text> pages, List<Text> titles, TextColor arrowColor, @Nullable TextColor disabledArrowColor, @Nullable Text prefix) {
        this.pages =  pages;
        this.titles = titles;
        this.arrowColor = arrowColor;
        this.disabledArrowColor = disabledArrowColor != null ? disabledArrowColor : arrowColor;
        this.id = UUID.randomUUID().toString();
        this.prefix = prefix;
        message = new ModifiableMessage(buildText(), id);
    }

    private Text buildText() {
        Text title = null;
        if(pageNumber <= titles.size()-1) {
            title = titles.get(pageNumber);
        }
        if(title == null) {
            title = titles.getFirst();
            if(title == null) {
                title = Text.literal("No title found");
            }
        }

        MutableText baseText = (prefix != null) ? prefix.copy() : Text.literal("");
        baseText.append(Text.literal("<< ")
                .setStyle(buildLeftStyle())
        );
        baseText.append(title);
        baseText.append(Text.literal(" >>")
                .setStyle(buildRightStyle())
        );
        baseText.append("\n");
        baseText.append(pages.get(pageNumber));

        return baseText;
    }

    private Style buildLeftStyle() {
        Style baseStyle = Style.EMPTY.withColor(disabledArrowColor);
        if(disabled) {
            return baseStyle
                    .withHoverEvent(new HoverEvent.ShowText(Text.literal("Paging Disabled")));
        }

        if(pageNumber != 0) {
            return baseStyle
                    .withColor(arrowColor)
                    .withClickEvent(new ClickEvent.RunCommand("pagedmessage left"))
                    .withHoverEvent(new HoverEvent.ShowText(Text.literal("Previous page")));
        }

        return baseStyle
                .withHoverEvent(new HoverEvent.ShowText(Text.literal("No pages to the Left!")));
    }

    private Style buildRightStyle() {
        Style baseStyle = Style.EMPTY.withColor(disabledArrowColor);
        if(disabled) {
            return baseStyle
                    .withHoverEvent(new HoverEvent.ShowText(Text.literal("Paging Disabled")));
        }

        if(pageNumber != pages.size()-1) {
            return baseStyle
                    .withColor(arrowColor)
                    .withClickEvent(new ClickEvent.RunCommand("pagedmessage right"))
                    .withHoverEvent(new HoverEvent.ShowText(Text.literal("Next page")));
        }

        return baseStyle
                .withHoverEvent(new HoverEvent.ShowText(Text.literal("No pages to the Right!")));
    }

    public void disablePaging() {
        disabled = true;
        message.modify(buildText());
    }

    public void setPage(Integer page) {
        if(page >= 0 && page <= pages.size()-1) {
            pageNumber = page;
            message.modify(buildText());
        }
    }

    public void nextPage() {
        if(pageNumber < pages.size()-1) {
            setPage(pageNumber + 1);
        }
    }

    public void lastPage() {
        if(pageNumber > 0) {
            setPage(pageNumber - 1);
        }
    }

    @Override
    public String toString() {
        return "Paged Message with id: " + id;
    }
}
