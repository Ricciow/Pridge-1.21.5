package io.github.ricciow.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.ricciow.util.ChatUtils;
import io.github.ricciow.util.message.IChatHudLineKt;
import io.github.ricciow.util.message.IdentifiableChatHud;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin implements IdentifiableChatHud {
    @Inject(
            method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V",
            at = @At("HEAD")
    )
    private void onAddNewChatHudLine(ChatHudLine line, CallbackInfo ci) {
        var id = ChatUtils.INSTANCE.getNextMessageId();

        if (id != 0) {
            IChatHudLineKt.cast(line).pridge$setIdentifier(id);
            ChatUtils.INSTANCE.setNextMessageId(0);
        }
    }

    @Inject(
            method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V",
            at = @At("TAIL")
    )
    private void onTail(ChatHudLine msg, CallbackInfo ci) {
        ChatUtils.INSTANCE.setNextMessageId(0);
    }

    @Inject(
            method = "refresh",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/ChatHud;addVisibleMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V"
            )
    )
    private void onRefresh(CallbackInfo ci, @Local ChatHudLine line) {
        var id = IChatHudLineKt.cast(line).pridge$getIdentifier();

        if (id != 0) {
            ChatUtils.INSTANCE.setNextMessageId(id);
        }
    }
}
