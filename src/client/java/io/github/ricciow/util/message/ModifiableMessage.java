package io.github.ricciow.util.message;

import io.github.ricciow.PridgeClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

public class ModifiableMessage {
    private final ChatHud CHAT_HUD;
    private Text text;
    private boolean sent = false;
    private final String id;

    ModifiableMessage(Text initialText, String id) {
        this.id = id;
        text = initialText;
        CHAT_HUD =  MinecraftClient.getInstance().inGameHud.getChatHud();

        updateMessage();
    }

    public void modify(Text text) {
        this.text = text;
        updateMessage();
    }

    private void updateMessage() {
        if(text == null || CHAT_HUD == null) {
            PridgeClient.LOGGER.warn("text or CHAT_HUD is null, not updating/sending ModifiableMessage");
            return;
        }

        IdentifiableChatHud chatHud = (IdentifiableChatHud) CHAT_HUD;

        if(sent) {
            chatHud.pridge$replaceIdentifiableMessage(id, text);
        }
        else {
            chatHud.pridge$addIdentifiableMessage(id, text);
            sent = true;
        }
    }
}
