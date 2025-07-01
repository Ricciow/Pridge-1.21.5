package io.github.ricciow.mixin;

import io.github.ricciow.util.message.IChatHudLineKt;
import io.github.ricciow.util.message.IdentifiableChatHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.ListIterator;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin implements IdentifiableChatHud {

    @Final
    @Shadow
    private List<ChatHudLine> messages;

    @Final
    @Shadow
    private MinecraftClient client;
    @Shadow
    private int scrolledLines;

    @Shadow
    private void refresh() {
    }

    @Shadow
    public void addMessage(Text message) {
    }

    @Shadow
    private void logChatMessage(ChatHudLine message) {
    }

    public void pridge$addIdentifiableMessage(@NotNull String id, @NotNull Text message) {
        addMessage(message);

        if (!messages.isEmpty()) {
            IChatHudLineKt.cast(messages.getFirst()).pridge$setIdentifier(id);
        }
    }

    public void pridge$removeIdentifiableMessage(@NotNull String id) {
        int initialSize = messages.size();

        messages.removeIf(line -> {
            String lineId = IChatHudLineKt.cast(line).pridge$getIdentifier();
            return lineId != null && lineId.equals(id);
        });

        int finalSize = messages.size();
        int removedCount = initialSize - finalSize;

        if (removedCount > 0) {
            refresh(); // Refresh chat only if something changed
        }
    }

    public void pridge$replaceIdentifiableMessage(@NotNull String id, @NotNull Text message) {

        boolean replaced = false;

        ListIterator<ChatHudLine> iterator = messages.listIterator();

        while (iterator.hasNext()) {
            ChatHudLine oldLine = iterator.next();
            String lineId = IChatHudLineKt.cast(oldLine).pridge$getIdentifier();
            if (id.equals(lineId)) {
                ChatHudLine newLine = new ChatHudLine(oldLine.creationTick(), message, null, this.client.isConnectedToLocalServer() ? MessageIndicator.singlePlayer() : MessageIndicator.system());

                IChatHudLineKt.cast(newLine).pridge$setIdentifier(id);

                iterator.set(newLine);
                logChatMessage(newLine);
                replaced = true;
            }
        }

        if (replaced) {
            int tempScrolledLines = scrolledLines;
            refresh();
            scrolledLines = tempScrolledLines;
        }
    }
}
