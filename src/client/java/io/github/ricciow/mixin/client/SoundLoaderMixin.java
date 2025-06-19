package io.github.ricciow.mixin.client;

import io.github.ricciow.PridgeClient;
import net.minecraft.client.sound.SoundLoader;
import net.minecraft.client.sound.StaticSound;
import net.minecraft.util.Identifier;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Mixin(SoundLoader.class)
public class SoundLoaderMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("SoundLoaderMixin");
    private static final String DYNAMIC_SOUND_NAMESPACE = "dynamicsound";
    private static final Path customSoundsDir = PridgeClient.CONFIG_DIR;

    @Inject(method = "loadStatic", at = @At("HEAD"), cancellable = true)
    private void onLoadStaticSound(Identifier id, CallbackInfoReturnable<CompletableFuture<StaticSound>> cir) {
        if (id.getNamespace().equals(DYNAMIC_SOUND_NAMESPACE)) {
            cir.setReturnValue(pridge$loadDynamicSound(id));
        }
    }

    private CompletableFuture<StaticSound> pridge$loadDynamicSound(Identifier id) {
        return CompletableFuture.supplyAsync(() -> {
            Path soundFile = customSoundsDir.resolve(id.getPath());
            if (!Files.exists(soundFile)) {
                LOGGER.error("Dynamic sound file does not exist: {}", soundFile);
                return null;
            }

            ShortBuffer pcm = null;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer fileBuffer = pridge$readToByteBuffer(soundFile);
                IntBuffer channelsBuffer = stack.mallocInt(1);
                IntBuffer sampleRateBuffer = stack.mallocInt(1);
                pcm = STBVorbis.stb_vorbis_decode_memory(fileBuffer, channelsBuffer, sampleRateBuffer);
                MemoryUtil.memFree(fileBuffer);

                if (pcm == null) {
                    LOGGER.error("Failed to decode OGG file: {}", soundFile);
                    return null;
                }

                int channels = channelsBuffer.get(0);
                int sampleRate = sampleRateBuffer.get(0);

                AudioFormat audioFormat = new AudioFormat(
                        (float) sampleRate,
                        16,
                        channels,
                        true,
                        false
                );

                // This is the corrected block for creating the ByteBuffer and StaticSound
                long address = MemoryUtil.memAddress(pcm);
                int capacityInBytes = pcm.remaining() * Short.BYTES;
                ByteBuffer bytePcm = MemoryUtil.memByteBuffer(address, capacityInBytes);

                return new StaticSound(bytePcm, audioFormat);


            } catch (IOException e) {
                LOGGER.error("Failed to read dynamic sound file: {}", soundFile, e);
                return null;
            }
        });
    }

    private ByteBuffer pridge$readToByteBuffer(Path path) throws IOException {
        byte[] bytes;
        try (InputStream stream = Files.newInputStream(path)) {
            bytes = stream.readAllBytes();
        }
        ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
        buffer.put(bytes).flip();
        return buffer;
    }
}