package io.github.ricciow.mixin.client;

import io.github.ricciow.PridgeClient;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.floatprovider.FloatSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(SoundManager.class)
public class SoundManagerMixin {

    private static final String DYNAMIC_SOUND_NAMESPACE = "dynamicsound";
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void onGetSound(Identifier id, CallbackInfoReturnable<WeightedSoundSet> cir) {
        if (id.getNamespace().equals(DYNAMIC_SOUND_NAMESPACE)) {
            // --- FIX 1: Use FloatSupplier for volume and pitch ---
            Sound sound = new Sound(
                    id,
                    (random) -> 1.0f,
                    (random) -> 1.0f,
                    1,
                    Sound.RegistrationType.FILE,
                    false,
                    false,
                    16
            );

            WeightedSoundSet soundSet = new WeightedSoundSet(id, null);
            soundSet.add(sound);

            cir.setReturnValue(soundSet);
        }
    }
}