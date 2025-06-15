package io.github.ricciow.rendering;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.lwjgl.glfw.GLFW;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImagePreviewRenderer {

    private static final Pattern OGP_IMAGE_REGEX = Pattern.compile("<meta property=\"(?:og:image|twitter:image)\" content=\"(?<url>.+?)\".*?/?>");
    private static final Pattern IMG_TAG_REGEX = Pattern.compile("<img.*?src=\"(?<url>.+?)\".*?>");
    private static final Identifier PREVIEW_TEXTURE_ID = Identifier.of("image_preview", "preview_texture");

    private final MinecraftClient client = MinecraftClient.getInstance();

    private String currentUrl = null;
    private String loadingUrl = null;

    private int imageWidth = 100;
    private int imageHeight = 100;
    private boolean hasTexture = false;

    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        ChatHud chatHud = client.inGameHud.getChatHud();

        if (!chatHud.isChatFocused()) {
            if (this.hasTexture) {
                clearTexture();
            }
            return;
        }

        double mouseX = client.mouse.getX() * (double) client.getWindow().getScaledWidth() / client.getWindow().getWidth();
        double mouseY = client.mouse.getY() * (double) client.getWindow().getScaledHeight() / client.getWindow().getHeight();
        Style style = chatHud.getTextStyleAt(mouseX, mouseY);

        String url = null;
        if (style != null) {
            ClickEvent clickEvent = style.getClickEvent();
            if (clickEvent instanceof ClickEvent.OpenUrl openUrlEvent) {
                url = openUrlEvent.uri().toString();
            }
        }

        handleUrl(url);

        if (this.hasTexture) {
            renderPreview(drawContext);
        }
    }

    private void handleUrl(String url) {
        if (url == null) {
            if (this.currentUrl != null) {
                this.currentUrl = null;
                clearTexture();
            }
            return;
        }
        if (!url.startsWith("http")) {
            return;
        }
        if (url.contains("imgur.com/") && !url.contains("i.imgur")) {
            final String[] split = url.split("/");
            url = String.format("https://i.imgur.com/%s.png", split[split.length - 1]);
        }
        if (url.equals(this.currentUrl) || url.equals(this.loadingUrl)) {
            return;
        }
        this.loadingUrl = url;
        this.currentUrl = url;
        clearTexture();
        String finalUrl = url;
        CompletableFuture.runAsync(() -> loadImage(finalUrl));
    }

    private void loadImage(String url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setRequestProperty("User-Agent", "Fabric Image Previewer/1.0");
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            try (InputStream stream = connection.getInputStream()) {
                String contentType = connection.getHeaderField("Content-Type");
                if (contentType != null && contentType.contains("text/html")) {
                    String body = IOUtils.toString(stream, StandardCharsets.UTF_8);
                    String imageURL = "";
                    Matcher matcher;
                    if ((matcher = OGP_IMAGE_REGEX.matcher(body)).find()) {
                        imageURL = matcher.group("url");
                    } else if ((matcher = IMG_TAG_REGEX.matcher(body)).find()) {
                        imageURL = matcher.group("url");
                    }
                    if (!imageURL.isEmpty()) {
                        if (imageURL.startsWith("/")) {
                            URL urlObj = URI.create(url).toURL();
                            imageURL = urlObj.getProtocol() + "://" + urlObj.getHost() + imageURL;
                        }
                        loadImage(imageURL);
                        return;
                    }
                }
                final NativeImage image = NativeImage.read(stream);
                client.execute(() -> {
                    if (url.equals(this.loadingUrl)) {
                        registerTexture(image);
                        this.loadingUrl = null;
                    } else {
                        image.close();
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Failed to load image preview from " + url);
            if (url.equals(this.loadingUrl)) {
                this.loadingUrl = null;
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void registerTexture(NativeImage image) {
        this.imageWidth = image.getWidth();
        this.imageHeight = image.getHeight();
        NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> PREVIEW_TEXTURE_ID.toString(), image);
        client.getTextureManager().registerTexture(PREVIEW_TEXTURE_ID, texture);
        this.hasTexture = true;
    }

    private void clearTexture() {
        if (this.hasTexture) {
            this.hasTexture = false;
            client.execute(() -> {
                if (client.getTextureManager().getTexture(PREVIEW_TEXTURE_ID) != null) {
                    client.getTextureManager().destroyTexture(PREVIEW_TEXTURE_ID);
                }
            });
        }
    }

    private void renderPreview(DrawContext drawContext) {
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        float aspectRatio = (float) imageWidth / imageHeight;
        float desiredWidth = screenWidth * 0.5f;
        long handle = client.getWindow().getHandle();
        if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_SHIFT)) {
            desiredWidth = screenWidth * 0.8f;
        }
        if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL)) {
            desiredWidth = this.imageWidth;
        }
        float finalWidth = desiredWidth;
        float finalHeight = finalWidth / aspectRatio;
        if (finalHeight > screenHeight) {
            finalHeight = screenHeight;
            finalWidth = finalHeight * aspectRatio;
        }
        int intWidth = (int) finalWidth;
        int intHeight = (int) finalHeight;
        int x = 0;
        int y = 0;
        drawContext.drawTexture(
                RenderLayer::getGuiTextured,
                PREVIEW_TEXTURE_ID,
                x, y,
                0.0f, 0.0f,
                intWidth, intHeight,
                intWidth, intHeight
        );
    }
}