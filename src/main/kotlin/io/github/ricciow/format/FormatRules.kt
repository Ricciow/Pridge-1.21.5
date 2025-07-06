package io.github.ricciow.format

import io.github.ricciow.util.PridgeLogger

abstract class FormatRule<T : Any> {
    lateinit var trigger: T
    lateinit var finalFormat: String
    open fun initialize() {}
    abstract fun process(text: String): FormatResult?
}

class RegexFormatRule : FormatRule<String>() {
    lateinit var groupFormatting: MutableMap<Int, MutableMap<String, String>>

    @Transient
    var pattern: Regex? = null

    override fun initialize() {
        pattern = try {
            trigger.toRegex()
        } catch (e: Exception) {
            PridgeLogger.error("Failed to compile regex pattern for $trigger", e)
            null
        }
    }

    override fun process(text: String): FormatResult? {
        val regex = this.pattern ?: run {
            PridgeLogger.warn("Pattern for $trigger is null")
            return null
        }

        val matchResult = regex.matchEntire(text) ?: return null

        if (groupFormatting.isEmpty()) {
            return FormatResult(regex.replace(text, finalFormat), botText = true)
        }

        var result = finalFormat

        for (i in 1..matchResult.groupValues.lastIndex) {
            val capturedText = matchResult.groupValues[i]
            var replacementText = capturedText

            groupFormatting[i]?.let { format ->
                replacementText = when {
                    format.containsKey(capturedText) ->
                        format[capturedText]?.replace($$"${str}", capturedText) ?: capturedText

                    format.containsKey("defaultStr") ->
                        format["defaultStr"]?.replace($$"${str}", capturedText) ?: capturedText

                    else -> capturedText
                }
            }

            result = result.replace("$$i", replacementText)
        }

        return FormatResult(result, botText = true)
    }

    override fun toString(): String {
        return trigger
    }
}

class StringFormatRule : FormatRule<String>() {
    override fun process(text: String): FormatResult? {
        if (trigger == text) {
            return FormatResult(finalFormat, botText = true)
        }
        return null
    }

    override fun toString(): String {
        return trigger
    }
}

class StringArrayFormatRule : FormatRule<MutableList<String>>() {
    override fun process(text: String): FormatResult? {
        if (trigger.contains(text)) {
            return FormatResult(finalFormat.replace($$"${msg}", text), botText = true)
        }
        return null
    }

    override fun toString(): String {
        return trigger.toString()
    }
}

class SpecialFormatRule : FormatRule<String>() {
    lateinit var functionName: String

    @Transient
    var pattern: Regex? = null

    override fun initialize() {
        pattern = try {
            trigger.toRegex()
        } catch (e: Exception) {
            PridgeLogger.error("Failed to compile regex pattern for $trigger", e)
            null
        }
    }

    override fun process(text: String): FormatResult? {
        val regex = this.pattern ?: return null

        val matchResult = regex.matchEntire(text) ?: return null

        return SpecialFunctions.run(this.functionName, text, matchResult)
    }

    override fun toString(): String {
        return trigger
    }
}