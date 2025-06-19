package io.github.ricciow.mixin.client;

import io.github.ricciow.util.message.IdentifiableChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChatHudLine.class)
public class ChatHudLineMixin implements IdentifiableChatHudLine {
    @Unique
    @Nullable
    private String pridge$identifier = null;

    public void pridge$setIdentifier(@Nullable String id) {
        this.pridge$identifier = id;
    }

    @Nullable
    public String pridge$getIdentifier() {
        return this.pridge$identifier;
    }
}