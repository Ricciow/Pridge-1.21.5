package io.github.ricciow.mixin;

import io.github.ricciow.Pridge;
import io.github.ricciow.util.PridgeLogger;
import net.minecraft.client.sound.NonRepeatingAudioStream;
import net.minecraft.client.sound.OggAudioStream;
import net.minecraft.client.sound.SoundLoader;
import net.minecraft.client.sound.StaticSound;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Mixin(SoundLoader.class)
public class SoundLoaderMixin {
    @Inject(method = "loadStatic(Lnet/minecraft/util/Identifier;)Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"), cancellable = true)
    private void onLoadStaticSound(Identifier id, CallbackInfoReturnable<CompletableFuture<StaticSound>> cir) {
        if (id.getNamespace().equals("dynamicsound")) {
            cir.setReturnValue(CompletableFuture.supplyAsync(() -> {
                Path soundFile = Pridge.INSTANCE.getCONFIG_DIR().resolve(id.getPath());
                try (FileInputStream fish = new FileInputStream(soundFile.toFile())) {
                    try (NonRepeatingAudioStream audioStream = new OggAudioStream(fish)) {
                        return new StaticSound(audioStream.readAll(), audioStream.getFormat());
                    }
                } catch (IOException e) {
                    PridgeLogger.INSTANCE.error("Failed to read dynamic sound file: " + soundFile, e, null);
                    return null;
                }
            }));
        }
    }
}