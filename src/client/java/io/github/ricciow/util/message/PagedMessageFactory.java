package io.github.ricciow.util.message;

import io.github.ricciow.PridgeClient;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class PagedMessageFactory {

    static PagedMessage lastPagedMessage;

    /**
     * Creates a paged message, only the last sent paged message will be able to change pages.
     */
    public static void createPagedMessage(List<Text> pages, Text title, TextColor arrowColor, @Nullable TextColor disabledArrowColor) {
        if(lastPagedMessage != null) {
            lastPagedMessage.disablePaging();
        }
        lastPagedMessage = new PagedMessage(pages, title, arrowColor, disabledArrowColor);
    }

    public static void createPagedMessage(List<Text> pages, List<Text> titles, TextColor arrowColor, @Nullable TextColor disabledArrowColor) {
        if(lastPagedMessage != null) {
            lastPagedMessage.disablePaging();
        }
        lastPagedMessage = new PagedMessage(pages, titles, arrowColor, disabledArrowColor);
    }

    public static void initialize() {
        PridgeClient.COMMAND_MANAGER.addCommand(
            literal("pagedmessage")
                .then(argument("direction", word())
                    .executes(commandContext -> {
                        if(lastPagedMessage != null) {
                            if (getString(commandContext, "direction").equals("left")) {
                                lastPagedMessage.lastPage();
                            } else {
                                lastPagedMessage.nextPage();
                            }
                        }
                        return 1;
                    })
                )
        );
    }
}
