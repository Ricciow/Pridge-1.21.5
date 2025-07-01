package io.github.ricciow.format

import io.github.ricciow.Pridge.Companion.LOGGER
import java.util.regex.Pattern

abstract class FormatRule {
    abstract fun process(text: String): FormatResult?
    abstract fun initialize()
}

class RegexFormatRule : FormatRule() {
    lateinit var trigger: String
    lateinit var finalFormat: String
    lateinit var groupFormatting: MutableMap<Int, MutableMap<String, String>>

    @Transient
    var pattern: Pattern? = null

    override fun initialize() {
        pattern = Pattern.compile(trigger)
    }

    override fun toString(): String {
        return trigger
    }

    override fun process(text: String): FormatResult? {
        val pattern = this.pattern ?: run {
            LOGGER.warn("Pattern for {} is null", trigger)
            return null
        }

        val matcher = pattern.matcher(text)

        if (!matcher.matches()) return null

        if (groupFormatting.isEmpty()) {
            return FormatResult(matcher.replaceAll(finalFormat), botText = true)
        }

        var result = finalFormat

        for (i in 1..matcher.groupCount()) {
            val capturedText = matcher.group(i)
            var replacementText = capturedText

            groupFormatting[i]?.let { conditionalFormats ->
                replacementText = when {
                    conditionalFormats.containsKey(capturedText) ->
                        conditionalFormats[capturedText]?.replace($$"${str}", capturedText) ?: capturedText

                    conditionalFormats.containsKey("defaultStr") ->
                        conditionalFormats["defaultStr"]?.replace($$"${str}", capturedText) ?: capturedText

                    else -> capturedText
                }
            }

            result = result.replace("$$i", replacementText)
        }

        return FormatResult(result, botText = true)
    }
}

class StringFormatRule : FormatRule() {
    lateinit var trigger: String
    lateinit var finalFormat: String

    override fun toString(): String {
        return trigger
    }

    override fun process(text: String): FormatResult? {
        if (trigger == text) {
            return FormatResult(finalFormat, botText = true)
        }
        return null
    }

    override fun initialize() {
    }
}

class StringArrayFormatRule : FormatRule() {
    lateinit var trigger: MutableList<String>
    lateinit var finalFormat: String

    override fun toString(): String {
        return trigger.toString()
    }

    override fun process(text: String): FormatResult? {
        if (trigger.contains(text)) {
            return FormatResult(finalFormat.replace($$"${msg}", text), botText = true)
        }
        return null
    }

    override fun initialize() {
    }
}

internal class SpecialFormatRule : FormatRule() {
    lateinit var trigger: String
    var functionName: String? = null

    @Transient
    var pattern: Pattern? = null

    override fun initialize() {
        pattern = Pattern.compile(trigger)
    }

    override fun toString(): String {
        return trigger
    }

    override fun process(text: String): FormatResult? {
        if (pattern == null || functionName == null) {
            return null
        }

        val matcher = pattern!!.matcher(text)

        if (matcher.matches()) {
            return SpecialFunctions.run(this.functionName!!, text, matcher)
        }

        return null
    }
}