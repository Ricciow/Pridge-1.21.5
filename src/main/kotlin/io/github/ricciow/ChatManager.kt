package io.github.ricciow

import io.github.ricciow.Pridge.Companion.CONFIG_I
import io.github.ricciow.Pridge.Companion.LOGGER
import io.github.ricciow.Pridge.Companion.mc
import io.github.ricciow.format.FormatManager
import io.github.ricciow.format.FormatResult
import io.github.ricciow.sounds.DynamicSoundPlayer
import io.github.ricciow.util.ChatType
import io.github.ricciow.util.ColorCode
import io.github.ricciow.util.TextParser.parse
import io.github.ricciow.util.TextParser.parseHoverable
import io.github.ricciow.util.chatHypixel
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.gui.hud.ChatHud
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Matcher
import java.util.regex.Pattern

interface ChatResult {
    fun handle()
}

data class GuildChatResult(
    val user: String,
    val message: String,
    val isBot: Boolean = false
) : ChatResult {
    override fun handle() {

    }
}

enum class StatusAction(val color: ColorCode) {
    JOINED(ColorCode.GREEN), LEFT(ColorCode.RED)
}

data class StatusChatResult(
    val user: String,
    val action: StatusAction
) : ChatResult {
    override fun handle() {
//        val gcat = CONFIG_I.guildCategory
//        if (gcat.modifyJoinLeave) {
//            val guildTag = if (gcat.modifyNormalGuildMessages) gcat.name else "&2Guild >"
//            sendMessage(
//                parse(
//                    "$guildTag ${ColorCode.GOLD.getMcCode()}${originalMessage.string.split(" ")[2]} ${action.color}${user}"
//                )
//            )
//            return false
//        }
    }
}

data class PrivateChatResult(
    val user: String,
    val message: String
) : ChatResult {
    override fun handle() {

    }
}

enum class BoopBooType {
    BOOP, BOO
}

data class BoopBooResult(
    val user: String,
    val type: BoopBooType
) : ChatResult {
    override fun handle() {
        mc.player.chatHypixel(
            ChatType.GUILD, when (type) {
                BoopBooType.BOOP -> "Thanks for the Boop, $user!"
                BoopBooType.BOO -> "AAH! You scared me, $user!"
            }
        )
    }
}

object ChatManager {
    private val GUILD_CHAT_PATTERN: Pattern = Pattern.compile("^Guild > (.*?): (.*)$")
    private val STATUS_CHAT_PATTERN: Pattern = Pattern.compile("^Guild > (.*?) (joined|left)\\.$")
    private val PRIVATE_CHAT_PATTERN: Pattern = Pattern.compile("^From (.*?): (.*)$")

    private var multiMessageBuffered = false
    private var multiMessage = ""
    private const val SPLIT_CHAR = "➩"

    lateinit var chatHud: ChatHud

    fun initialize() {
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

    fun onReceiveChatMessage(message: Text, overlay: Boolean): Boolean/*true=show original in chat*/ {
        try {
            if (!CONFIG_I.developerCategory.enabled) return true
            if (overlay) return true

            if (CONFIG_I.developerCategory.devEnabled) {
                LOGGER.info("Received message: $message")
            }

            val cleanRawMessage = Formatting.strip(message.string) ?: return true

            //Word filters
            val wordFilters = CONFIG_I.filtersCategory.rawFilter
            if (wordFilters.isNotEmpty()) {
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
                DynamicSoundPlayer.playSoundIfMessageContains(cleanRawMessage)
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
        } catch (e: Exception) {
            LOGGER.error("Error while processing chat message: $message", e)
        }
        return true
    }

    private fun onReceiveGuildMessage(originalMessage: Text, guildMatcher: Matcher): Boolean {
        val userInfo = guildMatcher.group(1).trim()
        var chatContent = guildMatcher.group(2).trim()

        chatContent = chatContent.replace("<@\\S+>".toRegex(), "").trim()

        val userOptional = userInfo.split(" ").firstOrNull { part ->
            !part.startsWith("[") && !part.endsWith("]")
        } ?: return true;

        // Only proceed if we successfully found a username.
        if (userOptional.equals(CONFIG_I.botCategory.ign, ignoreCase = true)) {
            return onReceiveBotMessage(chatContent)
        }
        return onReceivePlayerMessage(originalMessage)
    }

    private fun onReceiveBotMessage(chatContent: String): Boolean {
        if (handlePartial(chatContent)) {
            if (CONFIG_I.developerCategory.devEnabled) {
                LOGGER.info("Partial message was handled: $chatContent")
            }
        }

        var finalContent: String
        if (multiMessageBuffered) {
            finalContent = multiMessage
            multiMessageBuffered = false
            multiMessage = ""
        } else if (multiMessage.isNotEmpty()) {
            return false
        } else {
            finalContent = chatContent
        }

        val formattedContent = FormatManager.formatText(finalContent)
        if (CONFIG_I.developerCategory.devEnabled) {
            LOGGER.info("Message was formatted to: {}", formattedContent)
        }
        sendMessage(formattedContent)
        return false
    }

    private fun handlePartial(message: String): Boolean {
        if (!message.contains(SPLIT_CHAR)) {
            return false
        } else {
            multiMessage += message.replace(SPLIT_CHAR, "")
            multiMessageBuffered = !message.endsWith(SPLIT_CHAR)
            return true
        }
    }

    private fun onReceivePlayerMessage(originalMessage: Text): Boolean {
        if (!CONFIG_I.guildCategory.modifyNormalGuildMessages) return true
        sendMessage(parse(originalMessage.string.replace("§2Guild >", CONFIG_I.guildCategory.name)))
        return false
    }

    private fun onReceiveStatusMessage(originalMessage: Text, matcher: Matcher): Boolean {
        if (CONFIG_I.guildCategory.modifyJoinLeave) {
            val guildTag =
                if (CONFIG_I.guildCategory.modifyNormalGuildMessages) CONFIG_I.guildCategory.name else "&2Guild >"
            val colorCode = if (matcher.group(2) == "left") ColorCode.RED else ColorCode.GREEN
            sendMessage(
                parse(
                    "$guildTag ${ColorCode.GOLD.getMcCode()}${originalMessage.string.split(" ")[2]} ${colorCode.getMcCode()}${
                        matcher.group(
                            2
                        )
                    }"
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
                mc.player.chatHypixel(
                    ChatType.GUILD, when (matcher.group(2)) {
                        "Boop!" -> "Thanks for the Boop, $user!"
                        "Boo!" -> "AAH! You scared me, $user!"
                        else -> null
                    }
                )
            }
        }
        return true
    }
}