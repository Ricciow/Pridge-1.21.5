/*
 * STuF.java
 *
 * This file is licensed under the STuF Software License.
 * Copyright (c) Stuffy 2024. All rights reserved.
 *
 * Key conditions include non-commercial use and a 1 million user limit.
 * Full license text: https://github.com/stuffyerface/STuF/blob/main/LICENSE.md
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES, OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.ricciow.util;

import java.util.ArrayList;
import java.util.List;

//TODO: Create our own message serializer-deserializer for links
public class STuF {

    // The specific character set used for encoding and decoding.
    private static final String CHAR_SET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Decodes a string.
     * @param input The String to Decode.
     * @return The decoded URL as a String.
     * @throws IllegalArgumentException if the input string is not in the correct STuF format.
     */
    public static String decode(String input) {
        if (input == null || !input.startsWith("l$")) {
            throw new IllegalArgumentException("String does not appear to be in STuF");
        }

        char prefix = input.charAt(2);
        char suffix = input.charAt(3);
        int pipeIndex = input.indexOf('|');
        if (pipeIndex == -1) {
            throw new IllegalArgumentException("Invalid STuF format: Missing '|' separator.");
        }

        String dotIndicesStr = input.substring(4, pipeIndex);
        String urlBody = input.substring(pipeIndex + 1);

        List<Integer> dotIndices = new ArrayList<>();
        for (char c : dotIndicesStr.toCharArray()) {
            dotIndices.add(Character.getNumericValue(c));
        }

        int splitPoint = 9 - dotIndices.size();
        String first9 = urlBody.substring(0, splitPoint);
        String then = urlBody.substring(splitPoint).replace('^', '.');

        String url = first9 + then;
        url = charInc(url, -1); // Use the new circular shift logic

        StringBuilder urlBuilder = new StringBuilder(url);
        for (int index : dotIndices) {
            if (index <= urlBuilder.length()) {
                urlBuilder.insert(index, '.');
            }
        }
        url = urlBuilder.toString();

        if (prefix == 'h') {
            url = "http://" + url;
        } else if (prefix == 'H') {
            url = "https://" + url;
        }

        switch (suffix) {
            case '1': url += ".png"; break;
            case '2': url += ".jpg"; break;
            case '3': url += ".jpeg"; break;
            case '4': url += ".gif"; break;
        }

        return url;
    }

    /**
     * Encodes a URL.
     * @param url The URL string to encode.
     * @return The encoded STuF string.
     */
    public static String encode(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("Input URL cannot be null or empty.");
        }

        StringBuilder encodedBuilder = new StringBuilder("l$");

        // 1. Handle prefix
        if (url.startsWith("http://")) {
            encodedBuilder.append('h');
            url = url.substring(7);
        } else if (url.startsWith("https://")) {
            encodedBuilder.append('H');
            url = url.substring(8);
        } else {
            encodedBuilder.append(' '); // Placeholder if no prefix
        }

        // 2. Handle suffix
        if (url.endsWith(".png")) {
            encodedBuilder.append('1');
            url = url.substring(0, url.length() - 4);
        } else if (url.endsWith(".jpg")) {
            encodedBuilder.append('2');
            url = url.substring(0, url.length() - 4);
        } else if (url.endsWith(".jpeg")) {
            encodedBuilder.append('3');
            url = url.substring(0, url.length() - 5);
        } else if (url.endsWith(".gif")) {
            encodedBuilder.append('4');
            url = url.substring(0, url.length() - 4);
        } else {
            encodedBuilder.append('0');
        }

        // 3. Find dot indices
        StringBuilder dotIndicesBuilder = new StringBuilder();
        for (int i = 0; i < url.length() && i <= 8; i++) {
            if (url.charAt(i) == '.') {
                dotIndicesBuilder.append(i);
                if (dotIndicesBuilder.length() >= 9) break;
            }
        }

        // 4. Prepare the URL body for encoding
        String first9 = url.substring(0, Math.min(url.length(), 9));
        String then = (url.length() > 9) ? url.substring(9).replace('.', '^') : "";
        first9 = first9.replace(".", "");
        String shifted = charInc(first9 + then, 1);

        // 5. Assemble the final string
        encodedBuilder.append(dotIndicesBuilder.toString());
        encodedBuilder.append('|');
        encodedBuilder.append(shifted);

        return encodedBuilder.toString();
    }

    /**
     * Shifts each character in a string by a given amount, wrapping around a specific character set.
     * @param str The string to process.
     * @param amount The amount to shift each character by (can be negative).
     * @return The new string with shifted characters.
     */
    private static String charInc(String str, int amount) {
        StringBuilder result = new StringBuilder();
        for (char c : str.toCharArray()) {
            int index = CHAR_SET.indexOf(c);
            if (index == -1) {
                // If the character is not in our set (e.g., '/', '?', '='), append it as-is.
                result.append(c);
            } else {
                int offset = index + amount;
                // Java's % operator can return negative, so we add the length to ensure it's positive before the modulo.
                int newIndex = (offset % CHAR_SET.length() + CHAR_SET.length()) % CHAR_SET.length();
                result.append(CHAR_SET.charAt(newIndex));
            }
        }
        return result.toString();
    }
}
