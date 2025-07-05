package io.github.ricciow.mixin;

import io.github.ricciow.util.message.IChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChatHudLine.class)
public class ChatHudLineMixin implements IChatHudLine {
    @Unique
    private int pridge$identifier;

    @Override
    public void pridge$setIdentifier(int identifier) {
        pridge$identifier = identifier;
    }

    @Override
    public int pridge$getIdentifier() {
        return pridge$identifier;
    }
}