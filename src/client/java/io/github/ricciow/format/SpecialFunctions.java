package io.github.ricciow.format;

import io.github.ricciow.PridgeClient;
import io.github.ricciow.config.PridgeConfig;
import io.github.ricciow.util.STuF;
import io.github.ricciow.util.TextParser;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mutable;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpecialFunctions {

    @FunctionalInterface
    public interface SpecialFunction {
        FormatResult run(String originalText, Matcher matcher);
    }

    private static final Map<String, SpecialFunction> registry = new HashMap<>();

    private static PridgeConfig CONFIG;
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[LINK\\]\\((l\\$[^)]+)\\)");

    static {
        registry.put("discord", SpecialFunctions::discordHandler);
        registry.put("contest1", SpecialFunctions::contest1Handler);
        registry.put("contest2", SpecialFunctions::contest2Handler);
        registry.put("contest3", SpecialFunctions::contest3Handler);
        registry.put("contest4", SpecialFunctions::contest4Handler);
        registry.put("bestiary", SpecialFunctions::bestiaryHandler);
        registry.put("bestiary2", SpecialFunctions::bestiary2Handler);
        registry.put("collection", SpecialFunctions::collectionHandler);
    }

    /**
     * Gets a function from the corresponding name
     * @param functionName Name of the function
     * @return An optional containing a single function to be run with .run()
     */
    public static Optional<SpecialFunction> get(String functionName) {
        return Optional.ofNullable(registry.get(functionName));
    }

    /**
     * Runs a function with the corresponding name
     * @param functionName Name of the function to be run
     * @param matcher RegExp matcher to be passed through the function
     * @return String containing the result of the function or null if there isn't a function
     */
    public static FormatResult run(String functionName, String originalText, Matcher matcher) {
        SpecialFunction function = registry.get(functionName);

        if(function != null) {
            return function.run(originalText, matcher);
        }

        return null;
    }

    //<editor-fold desc="Helper Methods">

    /**
     * Formats a time value, returning an empty string if the value is 0.
     * @param timeValue The numeric value of the time unit.
     * @param suffix The suffix to append (e.g., "h", "m", "s").
     * @return A formatted string like " 10h" or an empty string.
     */
    private static String timeFunc(int timeValue, String suffix) {
        if (timeValue == 0) {
            return "";
        }
        return " " + timeValue + suffix;
    }

    /**
     * Capitalizes the first letter of a string.
     * @param str The string to capitalize.
     * @return The capitalized string.
     */
    private static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Placeholder for a function that formats hyperlinks.
     * In a real mod, this would involve creating chat components with click events.
     * @return A formatted string or null if no links are found.
     */
    private static Text formatLink(String originalText, Matcher matcher) {
        if(CONFIG.linkCategory.enabled) {
            //Decode URL
            String URL = STuF.decode(matcher.group(1));

            //Make Click thing
            ClickEvent openUrlEvent = new ClickEvent.OpenUrl(URI.create(URL));
            Style linkStyle = Style.EMPTY.withClickEvent(openUrlEvent);
            MutableText result = TextParser.parse(CONFIG.linkCategory.representation);
            result.setStyle(linkStyle);

            return result;
        }

        return TextParser.parse(originalText);

    }

    private static PridgeConfig getConfig() {
        if(CONFIG == null) {
            CONFIG = PridgeClient.getConfig();
        }
        return CONFIG;
    }

    private static FormatResult contest1Handler(String originalText, Matcher matcher) {
        String crop = matcher.group(1);
        int hours = Integer.parseInt(matcher.group(2));
        int minutes = Integer.parseInt(matcher.group(3));
        int seconds = Integer.parseInt(matcher.group(4));

        String hoursStr = timeFunc(hours, "h");
        String minutesStr = timeFunc(minutes, "m");
        String secondsStr = timeFunc(seconds, "s");

        String result = String.format("&eNext %s contest in%s%s%s", crop, hoursStr, minutesStr, secondsStr);

        return new FormatResult(originalText, result, false, true);
    }

    private static FormatResult contest2Handler(String originalText, Matcher matcher) {
        String crop1 = matcher.group(1);
        String crop2 = matcher.group(2);
        String crop3 = matcher.group(3);
        // Groups 4 (minutesActive) and 5 (secondsActive) are unused in the final format string.
        String nextCrop = matcher.group(6);
        int hours = Integer.parseInt(matcher.group(7));
        int minutes = Integer.parseInt(matcher.group(8));
        int seconds = Integer.parseInt(matcher.group(9));

        String hoursStr = timeFunc(hours, "h");
        String minutesStr = timeFunc(minutes, "m");
        String secondsStr = timeFunc(seconds, "s");

        String result = String.format("\n &a&lActive Contest\n &6%s, %s, %s\n&eNext %s contest in&f%s%s%s",
                        crop1, crop2, crop3, nextCrop, hoursStr, minutesStr, secondsStr);

        return new FormatResult(originalText, result, false, true);
    }

    private static FormatResult contest3Handler(String originalText, Matcher matcher) {
        String crop1 = matcher.group(1);
        String crop2 = matcher.group(2);
        String crop3 = matcher.group(3);
        // Groups 4 (minutesActive) and 5 (secondsActive) are unused.
        String crop4 = matcher.group(6);
        String crop5 = matcher.group(7);
        String crop6 = matcher.group(8);
        int hours = Integer.parseInt(matcher.group(9));
        int minutes = Integer.parseInt(matcher.group(10));
        int seconds = Integer.parseInt(matcher.group(11));

        String hoursStr = timeFunc(hours, "h");
        String minutesStr = timeFunc(minutes, "m");
        String secondsStr = timeFunc(seconds, "s");

        String result = String.format("\n &a&lActive Contest\n &6%s, %s, %s\n &e&lNext: \n &6%s, %s, %s\n &eIn&f%s%s%s",
                        crop1, crop2, crop3, crop4, crop5, crop6, hoursStr, minutesStr, secondsStr);

        return new FormatResult(originalText, result, false, true);
    }

    private static FormatResult contest4Handler(String originalText, Matcher matcher) {
        String crop1 = matcher.group(1);
        String crop2 = matcher.group(2);
        String crop3 = matcher.group(3);
        int hours = Integer.parseInt(matcher.group(4));
        int minutes = Integer.parseInt(matcher.group(5));
        int seconds = Integer.parseInt(matcher.group(6));

        String hoursStr = timeFunc(hours, "h");
        String minutesStr = timeFunc(minutes, "m");
        String secondsStr = timeFunc(seconds, "s");

        String result = String.format("&e&lNext:\n &6%s, %s, %s\n &eIn&f%s%s%s",
                        crop1, crop2, crop3, hoursStr, minutesStr, secondsStr);

        return new FormatResult(originalText, result, false, true);
    }

    private static FormatResult bestiaryHandler(String originalText, Matcher matcher) {
        String bestiary = matcher.group(1);
        String user = matcher.group(2);
        String profile = matcher.group(3);
        String message = matcher.group(4);

        Pattern bestiaryEntryPattern = Pattern.compile("(\\w[\\w\\s]* \\d+/\\d+(?: \\([\\d.]+\\))? )");
        Pattern bestiaryDataPattern = Pattern.compile("(\\w[\\w\\s]*) (\\d+)/(\\d+)(?: \\(([\\d.]+)\\))?");

        StringBuilder newMsg = new StringBuilder(String.format("\n &6&l%s bestiary - &f&l%s (&f&l%s)&6&l:", bestiary, user, profile));

        Matcher entryMatcher = bestiaryEntryPattern.matcher(message);
        while (entryMatcher.find()) {
            String entry = entryMatcher.group(1);
            Matcher dataMatcher = bestiaryDataPattern.matcher(entry);

            if (dataMatcher.find()) {
                String name = dataMatcher.group(1).trim();
                String current = dataMatcher.group(2);
                String max = dataMatcher.group(3);
                String valueStr = dataMatcher.group(4); // Can be null

                newMsg.append(String.format("\n &e&l%s &f&l%s&e&l/&f&l%s", name, current, max));

                if (valueStr != null) {
                    double value = Double.parseDouble(valueStr);
                    String color;
                    if (value > 1)        color = "&a";
                    else if (value > 0.75)color = "&e";
                    else if (value > 0.5) color = "&6";
                    else if (value > 0.25)color = "&c";
                    else                  color = "&4";
                    newMsg.append(String.format(" &e&l(%s&l%s&e&l)", color, valueStr));
                } else {
                    newMsg.append(" &e&l(&a&lPro&e&l)");
                }
            }
        }
        String result = newMsg.toString();

        return new FormatResult(originalText, result, false, true);
    }

    private static FormatResult bestiary2Handler(String originalText, Matcher matcher) {
        String mob = matcher.group(1);
        String user = matcher.group(2);
        String profile = matcher.group(3);
        String num = matcher.group(4);

        String str = (Integer.parseInt(num) > 0) ? "&a&lPro" : "&4&l0";

        String result = String.format("&f&l%s (&f&l%s)&6&l:\n &6&l%s - &f&l%s&e&l/&f&l0 &e&l(%s&e&l)",
                        user, profile, mob, num, str);

        return new FormatResult(originalText, result, false, true);
    }

    private static FormatResult collectionHandler(String originalText, Matcher matcher) {
        String skill = matcher.group(1);
        String user = matcher.group(2);
        String profile = matcher.group(3);
        String message = matcher.group(4);

        Pattern entryPattern = Pattern.compile("(\\w[\\w\\s]* \\d+/\\d+ \\([\\d,]+(?:/[\\d,]+)?\\) )");
        Pattern dataPattern = Pattern.compile("([\\w\\s]*) (\\d+)/(\\d+) \\(([\\d,]+(?:/[\\d,]+)?)\\)");

        StringBuilder newMsg = new StringBuilder(String.format("&6&l%s collections - &f&l%s (&f&l%s)&6&l:",
                capitalizeFirstLetter(skill), user, profile));

        Matcher entryMatcher = entryPattern.matcher(message);
        while (entryMatcher.find()) {
            Matcher dataMatcher = dataPattern.matcher(entryMatcher.group(1));
            if (dataMatcher.find()) {
                String name = dataMatcher.group(1).trim();
                String current = dataMatcher.group(2);
                String max = dataMatcher.group(3);
                String progress = dataMatcher.group(4); // e.g., "1,234" or "1,234/5,678"

                String formattedProgress = progress.replace("/", "&e&l/&f&l");

                newMsg.append(String.format("\n &e&l%s &f&l%s&e&l/&f&l%s &e&l(&f&l%s&e&l)",
                        name, current, max, formattedProgress));
            }
        }

        String result = newMsg.toString();
        return new FormatResult(originalText, result, false, true);
    }

    private static FormatResult discordHandler(String originalText, Matcher matcher) {
        String user = matcher.group(1);
        String message = matcher.group(2);
        String userName;

        // This logic handles cases where the message contains ": " and the regex captures it as part of the user.
        // It correctly reassembles the user and message parts.
        if (user.contains(": ")) {
            String[] parts = user.split(": ");
            userName = parts[0];
            StringBuilder messageBuilder = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                messageBuilder.append(parts[i]).append(": ");
            }
            messageBuilder.append(message);
            message = messageBuilder.toString();
        } else {
            userName = user;
        }

//        checkForSounds(message);

        MutableText finalMessage = TextParser.parse(
                getConfig().discordCategory.name_color.get().getCode() +
                userName +
                getConfig().discordCategory.message_color.get().getCode() + ": ");

        String[] parts = message.split(" ");
        boolean starting = true;
        for (String part : parts) {

            if (part.isEmpty()) {
                continue;
            }

            Matcher partMatcher = LINK_PATTERN.matcher(part);
            Text formattedPart;
            //Treat links
            if(partMatcher.matches()) {
                formattedPart = formatLink(part, partMatcher);
            }
            else {
                formattedPart = TextParser.parse(getConfig().discordCategory.message_color.get().getCode() + part);
            }

            //Add to the end of the message;
            if(!starting) {
                finalMessage.append(" ");
            }
            else {
                starting = false;
            }
            finalMessage.append(formattedPart);
        }

        return new FormatResult(originalText, finalMessage, true, false);
    }
}