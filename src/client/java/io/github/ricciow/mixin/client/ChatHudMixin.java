package io.github.ricciow.mixin.client;

import io.github.ricciow.util.message.IdentifiableChatHud;
import io.github.ricciow.util.message.IdentifiableChatHudLine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.ListIterator;

@Mixin(ChatHud.class)
public class ChatHudMixin implements IdentifiableChatHud {
    @Final
    @Shadow
    private List<ChatHudLine> messages;

    @Final
    @Shadow
    private MinecraftClient client;

    @Shadow
    private void refresh() {}

    @Shadow
    public void addMessage(Text message) {}

    @Shadow
    private void logChatMessage(ChatHudLine message) {}

    public void pridge$addIdentifiableMessage(String id, Text message) {
        addMessage(message);

        if (!messages.isEmpty()) {
            ChatHudLine chatLine = messages.getFirst();
            ((IdentifiableChatHudLine) (Object) chatLine).pridge$setIdentifier(id);
        }
    }

    public void pridge$removeIdentifiableMessage(String id) {
        int initialSize = messages.size();

        messages.removeIf(line -> {
            String lineId = ((IdentifiableChatHudLine) (Object) line).pridge$getIdentifier();
            return lineId != null && lineId.equals(id);
        });

        int finalSize = messages.size();
        int removedCount = initialSize - finalSize;

        if (removedCount > 0) {
            refresh(); // Refresh chat only if something changed
        }
    }

    public void pridge$replaceIdentifiableMessage(String id, Text message) {
        if(id == null || message == null) return;

        boolean replaced = false;

        ListIterator<ChatHudLine> iterator = messages.listIterator();

        while (iterator.hasNext()){
            ChatHudLine oldLine = iterator.next();
            String lineId = ((IdentifiableChatHudLine) (Object) oldLine).pridge$getIdentifier();
            if(id.equals(lineId)) {
                ChatHudLine newLine = new ChatHudLine(oldLine.creationTick(), message, null, this.client.isConnectedToLocalServer() ? MessageIndicator.singlePlayer() : MessageIndicator.system());

                ((IdentifiableChatHudLine) (Object) newLine).pridge$setIdentifier(id);

                iterator.set(newLine);
                logChatMessage(newLine);
                replaced = true;
            }
        }

        if(replaced) {
            refresh();
        }
    }
}
