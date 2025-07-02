package io.github.ricciow.util

/**
 * Represents a Minecraft color code, which is prefixed by the section sign (§).
 */
enum class ColorCode(val code: String) {
    // Enum constants with their associated character value
    BLACK("0"),
    DARK_BLUE("1"),
    DARK_GREEN("2"),
    DARK_AQUA("3"),
    DARK_RED("4"),
    DARK_PURPLE("5"),
    GOLD("6"),
    GRAY("7"),
    DARK_GRAY("8"),
    BLUE("9"),
    GREEN("a"),
    AQUA("b"),
    RED("c"),
    LIGHT_PURPLE("d"),
    YELLOW("e"),
    WHITE("f");

    fun getMcCode(): String {
        return "§${code}"
    }

    /**
     * Returns the color code as a string, including the section sign prefix,
     * ready to be used in Minecraft text.
     */
    override fun toString(): String {
        return "§${code}&${code}"
    }
}