package io.github.ricciow.format;

import io.github.ricciow.util.TextParser;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class FormatRule {
    public abstract String toString();

    public abstract FormatResult process(String text);

    public void initialize() {

    }
}

class RegexFormatRule extends FormatRule {
    String trigger;
    String finalFormat;
    Map<String, Map<String, String>> groupFormatting;

    transient Pattern pattern;

    @Override
    public void initialize() {
        if (trigger != null) {
            pattern = Pattern.compile(trigger);
        }
    }

    public String toString() {
        return trigger;
    }

    public FormatResult process(String text) {
        Matcher matcher = pattern.matcher(text);

        if (matcher.matches()) {
            if (groupFormatting == null || groupFormatting.isEmpty()) {
                return new FormatResult(text, matcher.replaceAll(finalFormat), false, true);
            } else {
                String result = finalFormat;

                for (int i = 1; i <= matcher.groupCount(); i++) {
                    String groupIndex = String.valueOf(i);
                    String capturedText = matcher.group(i);
                    String replacementText = capturedText;

                    if (groupFormatting.containsKey(groupIndex)) {
                        Map<String, String> conditionalFormats = groupFormatting.get(groupIndex);

                        if (conditionalFormats.containsKey(capturedText)) {
                            String formatPattern = conditionalFormats.get(capturedText);
                            replacementText = formatPattern.replace("${str}", capturedText);
                        } else if (conditionalFormats.containsKey("defaultStr")) {
                            String formatPattern = conditionalFormats.get("defaultStr");
                            replacementText = formatPattern.replace("${str}", capturedText);
                        }
                    }

                    result = result.replace("$" + i, replacementText);
                }
                return new FormatResult(text, result, false, true);
            }
        }
        return null;
    }
}

class StringFormatRule extends FormatRule {
    String trigger;
    String finalFormat;

    public String toString() {
        return trigger;
    }

    public FormatResult process(String text) {
        if(Objects.equals(trigger, text)) {
            return new FormatResult(text, finalFormat, false, true);
        }
        return null;
    }
}

class StringArrayFormatRule extends FormatRule {
    List<String> trigger;
    String finalFormat;

    public  String toString() {
        return trigger.toString();
    }

    public FormatResult process(String text) {
        if(trigger.contains(text)) {
            return new FormatResult(text, finalFormat.replace("${msg}", text), false, true);
        }
        return null;
    }
}

class SpecialFormatRule extends FormatRule {
    String trigger;
    String functionName;

    transient Pattern pattern;

    @Override
    public void initialize() {
        if(trigger != null) {
            pattern = Pattern.compile(trigger);
        }
    }

    public  String toString() {
        return trigger;
    }

    public FormatResult process(String text) {
        if(pattern == null || functionName == null) {
            return null;
        }

        Matcher matcher = pattern.matcher(text);

        if (matcher.matches()) {
            return SpecialFunctions.run(this.functionName, text, matcher);
        }

        return null;
    }
}