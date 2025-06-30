package io.github.ricciow

import io.github.ricciow.Pridge.Companion.CONFIG_I
import io.github.ricciow.Pridge.Companion.LOGGER
import io.github.ricciow.Pridge.Companion.SOUND_PLAYER
import io.github.ricciow.Pridge.Companion.mc
import io.github.ricciow.format.FormatManager
import io.github.ricciow.format.FormatResult
import io.github.ricciow.util.ColorCode
import io.github.ricciow.util.TextParser.parse
import io.github.ricciow.util.TextParser.parseHoverable
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.gui.hud.ChatHud
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Matcher
import java.util.regex.Pattern

class ChatManager(private val formatManager: FormatManager) {
    private var incompleteMessage = ""
    private val splitChar = "➩"

    lateinit var chatHud: ChatHud

    fun register() {
        ClientReceiveMessageEvents.ALLOW_GAME.register(::onReceiveChatMessage)
    }

    private fun sendMessage(message: Text?) {
        if (!::chatHud.isInitialized) {
            chatHud = mc.inGameHud.chatHud
        }
        chatHud.addMessage(message)
    }

    private fun sendMessage(result: FormatResult) {
        val message = result.getText()
        if (message != null) {
            sendMessage(message)
        }
    }

    fun onReceiveChatMessage(message: Text, overlay: Boolean): Boolean {
        try {
            if (CONFIG_I.developerCategory.enabled) {
                if (overlay) return true

                if (CONFIG_I.developerCategory.devEnabled) {
                    LOGGER.info("Received message: {}", message)
                }

                val cleanRawMessage = Formatting.strip(message.string)
                if (cleanRawMessage == null) {
                    return true
                }

                //Word filters
                val wordFilters = CONFIG_I.filtersCategory.rawFilter
                if (wordFilters != "") {
                    val filterRegex = Pattern.compile(wordFilters)
                    val filterMatches = filterRegex.matcher(cleanRawMessage)

                    if (filterMatches.find()) {
                        if (CONFIG_I.filtersCategory.placeholder) {
                            sendMessage(parseHoverable("&c&lA message has been filtered.", message))
                        }
                        return false
                    }
                }

                //Sound Player
                if (CONFIG_I.soundsCategory.enabled) {
                    SOUND_PLAYER.checkForSounds(cleanRawMessage)
                }

                //Guild Chat Message handling
                val guildMatcher = GUILD_CHAT_PATTERN.matcher(cleanRawMessage)
                if (guildMatcher.matches()) {
                    return onReceiveGuildMessage(message, guildMatcher)
                }

                //Join/Leave handling
                val statusMatcher = STATUS_CHAT_PATTERN.matcher(cleanRawMessage)
                if (statusMatcher.matches()) {
                    return onReceiveStatusMessage(message, statusMatcher)
                }

                //Direct Message handling
                val privateMatcher = PRIVATE_CHAT_PATTERN.matcher(cleanRawMessage)
                if (privateMatcher.matches()) {
                    return onReceivePrivateMessage(message, privateMatcher)
                }
            }
        } catch (e: Exception) {
            LOGGER.error("FATAL: An error occurred on the message: {}", message.string, e)
        }
        return true
    }

    private fun onReceiveGuildMessage(originalMessage: Text, guildMatcher: Matcher): Boolean {
        val userInfo = guildMatcher.group(1).trim()
        var chatContent = guildMatcher.group(2).trim()

        chatContent = chatContent.replace("<@\\S+>".toRegex(), "").trim()

        val userOptional = userInfo.split(" ")
            .filter {
                !it.isEmpty()
            }.firstOrNull { part ->
                !part.startsWith("[") && !part.endsWith("]")
            }

        // Only proceed if we successfully found a username.
        if (userOptional != null) {
            if (userOptional.equals(CONFIG_I.botCategory.ign, ignoreCase = true)) {
                return onReceiveBotMessage(originalMessage, userInfo, chatContent)
            }
            return onReceivePlayerMessage(originalMessage, userInfo, chatContent)
        }
        return true
    }

    private fun onReceiveBotMessage(originalMessage: Text, userInfo: String, chatContent: String): Boolean {
        val startsWithSplit = chatContent.startsWith(splitChar)
        val endsWithSplit = chatContent.endsWith(splitChar)
        val isBuffering = !incompleteMessage.isEmpty()

        var finalContent: String

        if (isBuffering) {
            if (startsWithSplit) {
                val middlePart = chatContent.substring(1)
                if (endsWithSplit) {
                    incompleteMessage += middlePart.substring(0, middlePart.length - 1)
                    if (CONFIG_I.developerCategory.devEnabled) {
                        LOGGER.info("Incomplete message was hid: {}", incompleteMessage)
                    }
                    return false
                } else {
                    finalContent = incompleteMessage + middlePart
                    incompleteMessage = ""
                }
            } else {
                incompleteMessage = ""
                finalContent = chatContent
            }
        } else {
            if (endsWithSplit) {
                incompleteMessage = chatContent.substring(0, chatContent.length - 1)
                if (CONFIG_I.developerCategory.devEnabled) {
                    LOGGER.info("Incomplete message was hid: {}", incompleteMessage)
                }
                return false
            } else {
                finalContent = chatContent
            }
        }

        val formattedContent = formatManager.formatText(finalContent)
        if (CONFIG_I.developerCategory.devEnabled) {
            LOGGER.info("Message was formatted to: {}", formattedContent)
        }
        sendMessage(formattedContent)
        return false
    }

    private fun onReceivePlayerMessage(originalMessage: Text, userInfo: String, chatContent: String): Boolean {
        if (CONFIG_I.guildCategory.modifyNormalGuildMessages) {
            sendMessage(parse(originalMessage.string.replace("§2Guild >", CONFIG_I.guildCategory.name)))
            return false
        }
        return true
    }

    private fun onReceiveStatusMessage(originalMessage: Text, matcher: Matcher): Boolean {
        if (CONFIG_I.guildCategory.modifyJoinLeave) {
            val guildTag =
                if (CONFIG_I.guildCategory.modifyNormalGuildMessages) CONFIG_I.guildCategory.name else "&2Guild >"
            val colorCode = if (matcher.group(2) == "left") ColorCode.RED else ColorCode.GREEN
            sendMessage(
                parse(
                    "$guildTag ${ColorCode.GOLD.getMcCode()}${originalMessage.string.split(" ")[2]} ${colorCode.getMcCode()}${matcher.group(2)}"
                )
            )
            return false
        }
        return true
    }

    private fun onReceivePrivateMessage(originalMessage: Text, matcher: Matcher): Boolean {
        if (CONFIG_I.guildCategory.thanksForTheBoop) {
            val userInfo = matcher.group(1).trim()

            val user = userInfo.split(" ").firstOrNull {
                !it.startsWith("[") && !it.endsWith("]")
            }

            if (user != null) {
                if (mc.player != null) {
                    if (matcher.group(2) == "Boop!") {
                        mc.player!!.networkHandler.sendChatCommand("gc Thanks for the Boop, $user!")
                    } else if (matcher.group(2) == "Boo!") {
                        mc.player!!.networkHandler.sendChatCommand("gc AAH! You scared me, $user!")
                    }
                }
            }
        }
        return true
    }

    companion object {
        private val GUILD_CHAT_PATTERN: Pattern = Pattern.compile("^Guild > (.*?): (.*)$")
        private val STATUS_CHAT_PATTERN: Pattern = Pattern.compile("^Guild > (.*?) (joined|left)\\.$")
        private val PRIVATE_CHAT_PATTERN: Pattern = Pattern.compile("^From (.*?): (.*)$")
    }
}