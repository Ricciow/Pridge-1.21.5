package io.github.ricciow.mixin;

import io.github.ricciow.util.message.IChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChatHudLine.class)
public class ChatHudLineMixin implements IChatHudLine {
    @Unique
    private String pridge$identifier = null;

    public void pridge$setIdentifier(@NotNull String identifier) {
        pridge$identifier = identifier;
    }

    public @NotNull String pridge$getIdentifier() {
        return pridge$identifier;
    }
}