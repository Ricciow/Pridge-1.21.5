package io.github.ricciow.util; // Or any other package you prefer

/**
 * Represents a Minecraft color code, which is prefixed by the section sign (§).
 */
public enum ColorCode {
    // Enum constants with their associated character value
    BLACK('0'),
    DARK_BLUE('1'),
    DARK_GREEN('2'),
    DARK_AQUA('3'),
    DARK_RED('4'),
    DARK_PURPLE('5'),
    GOLD('6'),
    GRAY('7'),
    DARK_GRAY('8'),
    BLUE('9'),
    GREEN('a'),
    AQUA('b'),
    RED('c'),
    LIGHT_PURPLE('d'),
    YELLOW('e'),
    WHITE('f');

    // 1. A field to store the value for each enum constant
    private final char code;

    // 2. A constructor to initialize the field
    // This constructor is implicitly private.
    ColorCode(char code) {
        this.code = code;
    }

    // 3. A "getter" method to access the value from outside
    public String getCode() {
        return "§" + this.code;
    }

    /**
     * Returns the color code as a string, including the section sign prefix,
     * ready to be used in Minecraft text.
     */
    @Override
    public String toString() {
        return "§" + this.code + "&" + this.code;
    }
}