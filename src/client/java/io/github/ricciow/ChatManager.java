package io.github.ricciow;

import io.github.ricciow.config.PridgeConfig;
import io.github.ricciow.format.FormatManager;
import io.github.ricciow.format.FormatResult;
import io.github.ricciow.format.SpecialFunctions;
import io.github.ricciow.util.ColorCode;
import io.github.ricciow.util.TextParser;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatManager {

    private final FormatManager FormatManager;

    private String incompleteMessage = "";
    private final String splitChar = "➩";
    private final PridgeConfig CONFIG;

    private static final Pattern GUILD_CHAT_PATTERN = Pattern.compile("^Guild > (.*?): (.*)$");
    private static final Pattern STATUS_CHAT_PATTERN = Pattern.compile("^Guild > (.*?) (joined|left)\\.$");
    private static final Pattern PRIVATE_CHAT_PATTERN = Pattern.compile("^From (.*?): (.*)$");

    private ChatHud CHAT_HUD;

    public ChatManager(FormatManager FormatManager) {
        this.FormatManager = FormatManager;

        CONFIG = PridgeClient.getConfig();
    }

    public void register() {
        ClientReceiveMessageEvents.ALLOW_GAME.register(this::onReceiveChatMessage);
    }

    private void sendMessage(Text message) {
        if(CHAT_HUD == null) {
            CHAT_HUD = MinecraftClient.getInstance().inGameHud.getChatHud();
        }
        CHAT_HUD.addMessage(message);
    }

    public boolean onReceiveChatMessage(Text message, boolean overlay) {
        if(CONFIG.developerCategory.enabled) {
            if(overlay) return true;

            String cleanRawMessage = Formatting.strip(message.getString());
            if (cleanRawMessage == null) {
                return true;
            }

            String wordFilters = CONFIG.filtersCategory.rawFilter;
            if(!Objects.equals(wordFilters, "")) {
                Pattern filterRegex = Pattern.compile(wordFilters);
                Matcher filterMatches = filterRegex.matcher(cleanRawMessage);

                if(filterMatches.find()) {
                    if(CONFIG.filtersCategory.placeholder) {
                        sendMessage(TextParser.parseHoverable("&c&lA message has been filtered.", message));
                    }
                    return false;
                }
            }

            if(CONFIG.soundsCategory.enabled) {
                PridgeClient.SOUND_PLAYER.checkForSounds(cleanRawMessage);
            }

            //Guild Chat Message handling
            Matcher guildMatcher = GUILD_CHAT_PATTERN.matcher(cleanRawMessage);
            if (guildMatcher.matches()) {
                return onReceiveGuildMessage(message, guildMatcher);
            }

            //Join/Leave handling
            Matcher statusMatcher = STATUS_CHAT_PATTERN.matcher(cleanRawMessage);
            if (statusMatcher.matches()) {
                return onReceiveStatusMessage(message, statusMatcher);
            }

            //Direct Message handling
            Matcher privateMatcher = PRIVATE_CHAT_PATTERN.matcher(cleanRawMessage);
            if(privateMatcher.matches()) {
                return onReceivePrivateMessage(message, privateMatcher);
            }
        }
        return true;
    }

    private boolean onReceiveGuildMessage(Text originalMessage, Matcher guildMatcher) {
        String userInfo = guildMatcher.group(1).trim();
        String chatContent = guildMatcher.group(2).trim();

        chatContent = chatContent.replaceAll("<@\\S+>", "").trim();

        Optional<String> userOptional = Arrays.stream(userInfo.split(" "))
                .filter(part -> !part.startsWith("[") && !part.endsWith("]"))
                .findFirst();

        // Only proceed if we successfully found a username.
        if (userOptional.isPresent()) {
            if(userOptional.get().equalsIgnoreCase(CONFIG.botCategory.ign)) {
                return onReceiveBotMessage(originalMessage, userInfo, chatContent);
            }
            return onReceivePlayerMessage(originalMessage, userInfo, chatContent);
        }
        return true;
    }

    private boolean onReceiveBotMessage(Text originalMessage, String userInfo, String chatContent) {
        boolean startsWithSplit = chatContent.startsWith(splitChar);
        boolean endsWithSplit = chatContent.endsWith(splitChar);
        boolean isBuffering = !incompleteMessage.isEmpty();

        String finalContent = null;

        if (isBuffering) {
            if (startsWithSplit) {
                String middlePart = chatContent.substring(1);
                if (endsWithSplit) {
                    incompleteMessage += middlePart.substring(0, middlePart.length() - 1);
                    return false;
                } else {
                    finalContent = incompleteMessage + middlePart;
                    incompleteMessage = "";
                }
            } else {
                incompleteMessage = "";
                finalContent = chatContent;
            }
        } else {
            if (endsWithSplit) {
                incompleteMessage = chatContent.substring(0, chatContent.length() - 1);
                return false;
            } else {
                finalContent = chatContent;
            }
        }

        if (finalContent != null) {
            FormatResult formattedContent = FormatManager.formatText(finalContent);
            if (formattedContent != null) {
                sendMessage(formattedContent.getText());
                return false;
            }
        }

        return true;
    }

    private boolean onReceivePlayerMessage(Text originalMessage, String userInfo, String chatContent) {
        if(CONFIG.guildCategory.modify_normal_guild_messages) {
            sendMessage(TextParser.parse(originalMessage.getString().replace("§2Guild >", CONFIG.guildCategory.name)));
            return false;
        }
        return true;
    }

    private boolean onReceiveStatusMessage(Text originalMessage, Matcher matcher) {
        if(CONFIG.guildCategory.modify_join_leave) {

            String guildTag = CONFIG.guildCategory.modify_normal_guild_messages ? CONFIG.guildCategory.name : "&2Guild >";
            ColorCode colorCode = matcher.group(2).equals("left") ? ColorCode.RED : ColorCode.GREEN;

            String finalResult = guildTag + " " + ColorCode.GOLD.getCode() + originalMessage.getString().split(" ")[2] + " " + colorCode.getCode() + matcher.group(2);
            sendMessage(TextParser.parse(finalResult));
            return false;
        }
        return true;
    }

    private boolean onReceivePrivateMessage(Text originalMessage, Matcher matcher) {
        if(CONFIG.guildCategory.thanks_for_the_boop) {
            String userInfo = matcher.group(1).trim();

            Optional<String> userOptional = Arrays.stream(userInfo.split(" "))
                    .filter(part -> !part.startsWith("[") && !part.endsWith("]"))
                    .findFirst();

            if(userOptional.isPresent()) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    if (matcher.group(2).equals("Boop!")) {
                        client.player.networkHandler.sendChatCommand("gc Thanks for the Boop, " + userOptional.get() + "!");
                    } else if (matcher.group(2).equals("Boo!")) {
                        client.player.networkHandler.sendChatCommand("gc AAH! You scared me, " + userOptional.get() + "!");
                    }
                }
            }

        }
        return true;
    }
}