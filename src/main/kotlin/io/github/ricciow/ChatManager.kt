package io.github.ricciow

import io.github.ricciow.Pridge.CONFIG_I
import io.github.ricciow.Pridge.mc
import io.github.ricciow.format.FormatManager
import io.github.ricciow.sounds.DynamicSoundPlayer
import io.github.ricciow.util.*
import io.github.ricciow.util.TextParser.parse
import io.github.ricciow.util.TextParser.parseHoverable
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.util.Formatting
import java.util.regex.Pattern

interface ChatResult {
    fun handle(): Boolean
}

data class BotMessageResult(
    val message: String
) : ChatResult {
    override fun handle(): Boolean {
        if (handlePartial(message)) {
            PridgeLogger.dev("Partial message was handled: $message")
        }

        var finalContent: String
        if (multiMessageBuffered) {
            finalContent = multiMessage
            multiMessageBuffered = false
            multiMessage = ""
        } else if (multiMessage.isNotEmpty()) {
            return false
        } else {
            finalContent = message
        }

        val formattedContent = FormatManager.formatText(finalContent)
        PridgeLogger.dev("Message was formatted to: $formattedContent")
        ChatUtils.sendMessage(formattedContent.getText())
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

    companion object {
        private var multiMessageBuffered = false
        private var multiMessage = ""
        private const val SPLIT_CHAR = "➩"
    }
}

data class PlayerMessageResult(
    val user: String,
    val original: String,
    val message: String
) : ChatResult {
    override fun handle(): Boolean {
        if (CONFIG_I.guildCategory.modifyNormalGuildMessages) {
            val parsable = TextParser.replaceMinecraftColorCodes(original)
            ChatUtils.sendMessage(parse(parsable.replace("&2Guild >", CONFIG_I.guildCategory.name)))
            return false
        }
        return true
    }
}

enum class StatusAction(val color: ColorCode) {
    JOINED(ColorCode.GREEN), LEFT(ColorCode.RED);

    fun actionString(): String {
        return when (this) {
            JOINED -> "${color.getMcCode()}joined"
            LEFT -> "${color.getMcCode()}left"
        }
    }
}

data class StatusChatResult(
    val user: String,
    val action: StatusAction
) : ChatResult {
    override fun handle(): Boolean {
        if (CONFIG_I.guildCategory.modifyJoinLeave) {
            val guildTag =
                if (CONFIG_I.guildCategory.modifyNormalGuildMessages) CONFIG_I.guildCategory.name else "&2Guild >"
            ChatUtils.sendMessage(
                parse("$guildTag ${ColorCode.GOLD.getMcCode()}${user} ${action.actionString()}")
            )
            return false
        }
        return true
    }
}

data class PrivateChatResult(
    val user: String,
    val message: String
) : ChatResult {
    override fun handle(): Boolean {
        if (CONFIG_I.guildCategory.thanksForTheBoop) {
            val boopboo = BoopBooType.fromString(message)
            if (boopboo != null) {
                return BoopBooResult(user, boopboo).handle()
            }
        }

        return true
    }
}

enum class BoopBooType {
    BOOP, BOO;

    companion object {
        fun fromString(type: String): BoopBooType? {
            return when (type) {
                "Boop!" -> BOOP
                "Boo!" -> BOO
                else -> null
            }
        }
    }
}

data class BoopBooResult(
    val user: String,
    val type: BoopBooType
) : ChatResult {
    override fun handle(): Boolean {
        mc.player.chatHypixel(
            ChatType.GUILD, when (type) {
                BoopBooType.BOOP -> CONFIG_I.guildCategory.boopReplyMessage.replace("{user}", user)
                BoopBooType.BOO -> CONFIG_I.guildCategory.booReplyMessage.replace("{user}", user)
            }
        )
        return true
    }
}

object ChatManager {
    private val GUILD_CHAT_PATTERN = Regex("""^Guild > (?:\[(?<hypixelRank>[\w+]+)] )?(?<username>\w{2,16})(?: \[(?<guildRank>[\w+]+)])?: (?<content>.+?)(?: <@\w+>)?$""")
    private val STATUS_CHAT_PATTERN = Regex("""^Guild > (?<username>\w{2,16}) (?<action>joined|left)\.$""")
    private val PRIVATE_CHAT_PATTERN = Regex("""^From (?:\[(?<hypixelRank>[\w+]+)] )?(?<name>\w{2,16}): (?<content>.+)$""")

    fun initialize() {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            if (!CONFIG_I.developerCategory.enabled) return@register true
            if (overlay) return@register true

            PridgeLogger.dev("Received message: $message")

            val cleanRawMessage = Formatting.strip(message.string) ?: return@register true

            if (checkWordFilters(cleanRawMessage)) return@register false

            checkSounds(cleanRawMessage)

            // Add handlers for different chat types
            val chatResult =
                GUILD_CHAT_PATTERN.matchEntire(cleanRawMessage)?.let { handleGuildChat(it, message.string) } ?: // Breaks clean chain :wah:
                STATUS_CHAT_PATTERN.matchEntire(cleanRawMessage)?.let(::handleStatusChat) ?:
                PRIVATE_CHAT_PATTERN.matchEntire(cleanRawMessage)?.let(::handlePrivateChat) ?:
                return@register true

            chatResult.handle()
        }
    }

    private fun handleGuildChat(matchResult: MatchResult, original: String): ChatResult? {
        val username = matchResult.groups["username"]!!.value
        val content = matchResult.groups["content"]!!.value

        // Only proceed if we successfully found a username.
        if (username.equals(CONFIG_I.botCategory.ign, ignoreCase = true)) {
            return BotMessageResult(content)
        }
        return PlayerMessageResult(username, original, content)
    }

    private fun handleStatusChat(matchResult: MatchResult): StatusChatResult? {
        val username = matchResult.groups["username"]!!.value
        val actionString = matchResult.groups["action"]!!.value
        val action = when (actionString) {
            "joined" -> StatusAction.JOINED
            "left" -> StatusAction.LEFT
            else -> return null // Invalid action, do not handle
        }
        return StatusChatResult(username, action)
    }

    private fun handlePrivateChat(matchResult: MatchResult): PrivateChatResult? {
        val name = matchResult.groups["name"]!!.value.trim()
        val content = matchResult.groups["content"]!!.value.trim()
        return PrivateChatResult(name, content)
    }

    private fun checkWordFilters(message: String): Boolean {
        val wordFilters = try {
            val filterStr = CONFIG_I.filtersCategory.rawFilter
            if (filterStr.isBlank()) null
            Regex(filterStr)
        } catch (e: Exception) {
            PridgeLogger.dev("Failed to compile word filters regex: ${e.message}")
            null
        }

        if (wordFilters != null) {
            if (wordFilters.matches(message)) {
                if (CONFIG_I.filtersCategory.placeholder) {
                    ChatUtils.sendMessage(parseHoverable("&c&lA message has been filtered.", message))
                }
                return true
            }
        }
        return false
    }

    private fun checkSounds(message: String) {
        if (CONFIG_I.soundsCategory.enabled) {
            DynamicSoundPlayer.playSoundIfMessageContains(message)
        }
    }
}