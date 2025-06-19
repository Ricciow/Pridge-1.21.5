package io.github.ricciow.util.message;

import io.github.ricciow.util.TextParser;
import net.minecraft.text.*;

import java.util.List;
import java.util.UUID;

public class PagedMessage{
    public List<Text> pages;
    public Text title;
    private TextColor arrowColor;
    public Integer pageNumber = 0;
    private final ModifiableMessage message;
    private final String id;
    private boolean disabled = false;

    PagedMessage(List<Text> pages, Text title, TextColor arrowColor) {
        this.pages =  pages;
        this.title = title;
        this.arrowColor = arrowColor;
        this.id = UUID.randomUUID().toString();
        message = new ModifiableMessage(buildText(), id);
    }

    private Text buildText() {
        MutableText baseText = Text.literal("");
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
        Style baseStyle = Style.EMPTY.withColor(arrowColor);
        if(disabled) return baseStyle;

        return baseStyle
            .withClickEvent(new ClickEvent.RunCommand("pagedmessage left"))
            .withHoverEvent(new HoverEvent.ShowText(Text.literal("Previous page")));
    }

    private Style buildRightStyle() {
        Style baseStyle = Style.EMPTY.withColor(arrowColor);
        if(disabled) return baseStyle;

        return baseStyle
            .withClickEvent(new ClickEvent.RunCommand("pagedmessage right"))
            .withHoverEvent(new HoverEvent.ShowText(Text.literal("Next page")));
    }

    public void disablePaging() {
        disabled = true;
        message.modify(buildText());
    }

    public void setPage(Integer page) {
        pageNumber = page;
        message.modify(buildText());
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
        return "Paged Message id: " + id;
    }
}
