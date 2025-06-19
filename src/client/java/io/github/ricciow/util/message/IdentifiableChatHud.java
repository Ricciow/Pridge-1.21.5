package io.github.ricciow.util.message;

import net.minecraft.text.Text;
public interface IdentifiableChatHud {

    default void pridge$addIdentifiableMessage(String id, Text message) {}

    default void pridge$removeIdentifiableMessage(String id) {}

    default void pridge$replaceIdentifiableMessage(String id, Text message) {}
}
