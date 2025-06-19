package io.github.ricciow.format;

import io.github.ricciow.PridgeClient;
import io.github.ricciow.config.PridgeConfig;
import io.github.ricciow.util.ColorCode;
import io.github.ricciow.util.STuF;
import io.github.ricciow.util.TextParser;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        int maxPerPage = getConfig().botCategory.getLineCount();

        String bestiary = matcher.group(1);
        String user = matcher.group(2);
        String profile = matcher.group(3);
        String message = matcher.group(4);

        // A single, more robust pattern to capture all data points at once
        Pattern bestiaryDataPattern = Pattern.compile("(\\w[\\w\\s]*?) (\\d+)/(\\d+)(?: \\(([\\d.]+)\\))?");

        Text prefix = TextParser.parse(String.format("\n &6&l%s bestiary - &f&l%s (&f&l%s)&6&l\n ", bestiary, user, profile));
        List<Text> pages = new ArrayList<>();
        StringBuilder currentPageContent = new StringBuilder();
        int entriesOnPage = 0;

        // Use the single pattern on the whole message
        Matcher entryMatcher = bestiaryDataPattern.matcher(message);
        while (entryMatcher.find()) {
            if (entriesOnPage == maxPerPage) {
                pages.add(TextParser.parse(currentPageContent.toString()));
                currentPageContent.setLength(0); // More efficient than new StringBuilder()
                entriesOnPage = 0;
            }

            String name = entryMatcher.group(1).trim();
            String current = entryMatcher.group(2);
            String max = entryMatcher.group(3);
            String valueStr = entryMatcher.group(4); // Can be null

            if(entriesOnPage != 0) {
                currentPageContent.append("\n");
            }

            currentPageContent.append(String.format(" &e&l%s &f&l%s&e&l/&f&l%s", name, current, max));

            if (valueStr != null) {
                double value = Double.parseDouble(valueStr);
                String color;
                if (value > 1)         color = ColorCode.GREEN.getCode();
                else if (value > 0.75) color = ColorCode.YELLOW.getCode();
                else if (value > 0.5)  color = ColorCode.GOLD.getCode();
                else if (value > 0.25) color = ColorCode.RED.getCode();
                else                   color = ColorCode.DARK_RED.getCode();
                currentPageContent.append(String.format(" &e&l(%s&l%s&e&l)", color, valueStr));
            } else {
                currentPageContent.append(" &e&l(&a&lPro&e&l)");
            }
            entriesOnPage++;
        }

        // Add the final page if it has any content
        if (currentPageContent.length() > 0) {
            if(!pages.isEmpty()) {
                while (entriesOnPage < maxPerPage) {
                    currentPageContent.append("\n");
                    entriesOnPage += 1;
                }
            }
            pages.add(TextParser.parse(currentPageContent.toString()));
        }

        List<Text> titles = new ArrayList<>();
        for(int i = 1; i <= pages.size(); i++) {
            titles.add(TextParser.parse(String.format("&6Page (%s/%s)", i, pages.size())));
        }

        return new FormatResult(pages, titles, TextColor.fromFormatting(Formatting.DARK_AQUA), TextColor.fromFormatting(Formatting.GRAY), prefix, false, true);
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
        int maxPerPage = getConfig().botCategory.getLineCount();

        String skill = matcher.group(1);
        String user = matcher.group(2);
        String profile = matcher.group(3);
        String message = matcher.group(4);

        // 1. Replaced the two patterns with a single, more direct one.
        // This pattern captures all necessary groups in one pass.
        Pattern dataPattern = Pattern.compile("([\\w\\s]*) (\\d+)/(\\d+) \\(([^)]+)\\)");

        Text prefix = TextParser.parse(String.format("\n &6&l%s collections - &f&l%s (&f&l%s)&6&l\n ",
                capitalizeFirstLetter(skill), user, profile));

        List<Text> pages = new ArrayList<>();
        StringBuilder currentPageContent = new StringBuilder();
        int entriesOnPage = 0;

        // 2. The loop is now simpler, with no nested matching.
        Matcher entryMatcher = dataPattern.matcher(message);
        while (entryMatcher.find()) {
            if (entriesOnPage == maxPerPage) {
                pages.add(TextParser.parse(currentPageContent.toString()));
                currentPageContent.setLength(0);
                entriesOnPage = 0;
            }

            // 3. All data is retrieved directly from the single 'entryMatcher'.
            String name = entryMatcher.group(1).trim();
            String current = entryMatcher.group(2);
            String max = entryMatcher.group(3);
            String progress = entryMatcher.group(4); // e.g., "1,234" or "1,234/5,678"

            if (entriesOnPage != 0) {
                currentPageContent.append("\n");
            }

            int currentVal = Integer.parseInt(current);
            int maxVal = Integer.parseInt(max);
            int medianVal = Math.floorDiv(maxVal, 4);

            ColorCode numberColor;
            if(currentVal == maxVal) {
                numberColor = ColorCode.GREEN;
            }
            else if (currentVal > medianVal) {
                numberColor = ColorCode.GOLD;
            }
            else {
                numberColor = ColorCode.RED;
            }

            String formattedProgress = progress.replace("/", "&e&l/&f&l");

            currentPageContent.append(String.format(" &e&l%s %s&l%s&e&l/%s&l%s &e&l(&f&l%s&e&l)",
                    name, numberColor.getCode(), current, numberColor.getCode(), max, formattedProgress));

            entriesOnPage++;
        }

        // This logic for handling the final page remains the same.
        if (currentPageContent.length() > 0) {
            if(!pages.isEmpty()) {
                while (entriesOnPage < maxPerPage) {
                    currentPageContent.append("\n");
                    entriesOnPage++;
                }
            }
            pages.add(TextParser.parse(currentPageContent.toString()));
        }

        // This logic for generating titles remains the same.
        List<Text> titles = new ArrayList<>();
        for(int i = 1; i <= pages.size(); i++) {
            titles.add(TextParser.parse(String.format("&6Page (%s/%s)", i, pages.size())));
        }

        // The return statement is the same.
        return new FormatResult(pages, titles, TextColor.fromFormatting(Formatting.DARK_AQUA), TextColor.fromFormatting(Formatting.GRAY), prefix, false, true);
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