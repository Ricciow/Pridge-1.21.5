package io.github.ricciow.mixin;

import io.github.ricciow.Pridge;
import net.minecraft.client.sound.NonRepeatingAudioStream;
import net.minecraft.client.sound.OggAudioStream;
import net.minecraft.client.sound.SoundLoader;
import net.minecraft.client.sound.StaticSound;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Mixin(SoundLoader.class)
public class SoundLoaderMixin {
    @Unique
    private static final String DYNAMIC_SOUND_NAMESPACE = "dynamicsound";

    @Inject(method = "loadStatic(Lnet/minecraft/util/Identifier;)Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"), cancellable = true)
    private void onLoadStaticSound(Identifier id, CallbackInfoReturnable<CompletableFuture<StaticSound>> cir) {
        if (id.getNamespace().equals(DYNAMIC_SOUND_NAMESPACE)) {
            cir.setReturnValue(CompletableFuture.supplyAsync(() -> {
                Path soundFile = Pridge.Companion.getCONFIG_DIR().resolve(id.getPath());
                try (FileInputStream fish = new FileInputStream(soundFile.toFile())) {
                    try (NonRepeatingAudioStream audioStream = new OggAudioStream(fish)) {
                        return new StaticSound(audioStream.readAll(), audioStream.getFormat());
                    }
                } catch (IOException e) {
                    Pridge.Companion.getLOGGER().error("Failed to read dynamic sound file: {}", soundFile, e);
                    return null;
                }
            }));
        }
    }
}