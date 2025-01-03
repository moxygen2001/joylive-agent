/*
 * Copyright © ${year} ${owner} (${email})
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.live.agent.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * StringUtils
 */
public class StringUtils {

    /**
     * A predicate that tests if a character is a comma.
     */
    public static final Predicate<Character> COMMA = o -> o == ',';

    /**
     * A predicate that tests if a character is a comma or semicolon.
     */
    public static final Predicate<Character> SEMICOLON_COMMA = o -> {
        switch (o) {
            case ',':
            case ';':
                return true;
            default:
                return false;
        }
    };

    /**
     * A predicate that tests if a character is a comma, semicolon, or whitespace.
     */
    public static final Predicate<Character> SEMICOLON_COMMA_WHITESPACE = o -> {
        switch (o) {
            case ',':
            case ';':
                return true;
            default:
                return Character.isWhitespace(o);
        }
    };

    /**
     * The empty String {@code ""}.
     */
    public static final String EMPTY = "";

    /**
     * Checks if the given character sequence is empty (null or has a length of 0).
     *
     * @param cs The character sequence to check.
     * @return true if the character sequence is null or has a length of 0, false otherwise.
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * Checks if the given character sequence is blank (null, has a length of 0, or contains only whitespace characters).
     *
     * @param cs The character sequence to check.
     * @return true if the character sequence is null, has a length of 0, or contains only whitespace characters; false otherwise.
     */
    public static boolean isBlank(CharSequence cs) {
        int len;
        if (cs == null || (len = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the given character sequence is blank (null, has a length of 0, or contains only whitespace characters).
     *
     * @param cs The character sequence to check.
     * @return true if the character sequence is null, has a length of 0, or contains only whitespace characters; false otherwise.
     */
    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * Checks if two CharSequences are equal or if one of them is an empty string.
     *
     * @param cs1 the first CharSequence to compare
     * @param cs2 the second CharSequence to compare
     * @return true if the CharSequences are equal or if one of them is an empty string, false otherwise
     */
    public static boolean isEqualsOrEmpty(CharSequence cs1, CharSequence cs2) {
        if (cs1 != null && cs1.length() > 0) {
            return cs1.equals(cs2);
        } else {
            return cs2 == null || cs2.length() == 0;
        }
    }

    /**
     * Splits the provided source string into an array of strings, using the provided character as the delimiter.
     *
     * @param source The string to be split.
     * @param ch     The delimiter character.
     * @return An array of strings.
     */
    public static String[] split(final String source, final char ch) {
        return split(source, o -> o == ch, null);
    }

    /**
     * Splits the provided source string into an array of strings based on a predefined set of delimiters.
     *
     * @param source The string to be split.
     * @return An array of strings.
     */
    public static String[] split(final String source) {
        return split(source, SEMICOLON_COMMA_WHITESPACE, null);
    }

    /**
     * Splits the given source string based on a specified predicate logic.
     *
     * @param source    The source string to split.
     * @param predicate A character predicate that determines whether a character should be considered a splitting point.
     * @return An array of strings.
     */
    public static String[] split(final String source, final Predicate<Character> predicate) {
        return split(source, predicate, null);
    }

    /**
     * Splits the given source string based on a specified predicate and processes each segment using a provided handler function.
     * This method iterates over the characters of the source string and uses the predicate to decide where to split.
     * Once a segment is identified, it is processed by the handler function if provided, before being added to the result.
     *
     * @param source    The source string to be split.
     * @param predicate A predicate that determines whether a character should be considered a splitting point.
     * @param handler   A function that processes each extracted segment before adding it to the result. May be null.
     * @return An array of strings resulting.
     */
    public static String[] split(final String source,
                                 final Predicate<Character> predicate,
                                 final Function<String, String> handler) {
        List<String> targets = splitList(source, predicate, handler);
        return targets.isEmpty() ? new String[0] : targets.toArray(new String[0]);
    }

    /**
     * Splits the given string by the specified delimiter string.
     * This method supports splitting by both single character and multi-character delimiters.
     * If the delimiter is null or empty, it defaults to using a comma as the delimiter.
     * For single-character delimiters, it utilizes a more optimized path.
     *
     * @param value     The string to be split.
     * @param delimiter The delimiter string used for splitting the value. Can be a single character or multiple characters.
     * @return An array of strings.
     */
    public static String[] split(final String value, final String delimiter) {
        List<String> targets = splitList(value, delimiter);
        return targets.isEmpty() ? new String[0] : targets.toArray(new String[0]);
    }

    /**
     * Splits the provided source string into a list of strings, using the provided character as the delimiter.
     *
     * @param source The string to be split.
     * @param ch     The delimiter character.
     * @return A list of strings.
     */
    public static List<String> splitList(final String source, final char ch) {
        return splitList(source, o -> o == ch, null);
    }

    /**
     * Splits the provided source string into a list of strings based on a predefined set of delimiters.
     *
     * @param source The string to be split.
     * @return A list of strings.
     */
    public static List<String> splitList(final String source) {
        return splitList(source, SEMICOLON_COMMA_WHITESPACE, null);
    }

    /**
     * Splits the given source string based on a specified predicate logic.
     *
     * @param source    The source string to split.
     * @param predicate A character predicate that determines whether a character should be considered a splitting point.
     * @return A list of strings.
     */
    public static List<String> splitList(final String source, final Predicate<Character> predicate) {
        return splitList(source, predicate, null);
    }

    /**
     * Splits the given source string based on a specified predicate logic and applies a handler function to each resulting substring.
     *
     * @param source    The source string to split.
     * @param predicate A character predicate that determines whether a character should be considered a splitting point.
     * @param handler   A function that takes a substring as input and returns a processed substring.
     * @return A list of processed strings.
     */
    public static List<String> splitList(final String source,
                                         final Predicate<Character> predicate,
                                         final Function<String, String> handler) {
        List<String> result = new ArrayList<>();
        splitList(source, predicate, handler, result::add);
        return result;
    }

    /**
     * Splits the given source string into a list of substrings, using the specified predicate logic to determine the substring separation,
     * applies a handler function to each resulting substring, and then applies a consumer function to the processed substrings.
     *
     * @param source    The source string to be split.
     * @param predicate A character predicate that determines whether a character should be considered a substring separator.
     * @param handler   A function that takes a substring as input and returns a processed substring.
     * @param consumer  A consumer function that takes a processed substring as input and performs an action.
     */
    public static void splitList(final String source,
                                 final Predicate<Character> predicate,
                                 final Function<String, String> handler,
                                 final Consumer<String> consumer) {
        if (source == null || source.isEmpty() || predicate == null || consumer == null) {
            return;
        }
        int start = -1;
        int end = -1;
        char ch;
        String part;
        int length = source.length();

        // Iterate over characters
        for (int i = 0; i < length; i++) {
            ch = source.charAt(i);
            // Check if the character matches the predicate
            if (predicate.test(ch)) {
                // If there is a segment before this character
                if (start >= 0) {
                    part = source.substring(start, end + 1);
                    part = handler == null ? part : handler.apply(part);
                    consumer.accept(part);
                    start = -1;
                    end = -1;
                }
            } else {
                if (start == -1) {
                    start = i;
                }
                end = i;
            }
        }
        // Handle the last segment
        if (start >= 0) {
            part = source.substring(start, length);
            part = handler == null ? part : handler.apply(part);
            consumer.accept(part);
        }
    }

    /**
     * Splits the given input string into a list of substrings, using the specified delimiter.
     *
     * @param value     The input string to be split.
     * @param delimiter The delimiter used to separate the substrings.
     * @return A list of substrings.
     */
    public static List<String> splitList(final String value, final String delimiter) {
        List<String> result = new ArrayList<>();
        splitList(value, delimiter, result::add);
        return result;
    }

    /**
     * Splits the given value string into a list of substrings, using the specified delimiter to determine the substring separation,
     * and applies a consumer function to each resulting substring.
     *
     * @param value     The value string to be split.
     * @param delimiter The delimiter string used to separate the substrings.
     * @param consumer  A consumer function that takes a substring as input and performs an action.
     */
    public static void splitList(final String value, final String delimiter, final Consumer<String> consumer) {
        if (value == null || value.isEmpty() || consumer == null) {
            return;
        } else if (delimiter == null || delimiter.isEmpty()) {
            splitList(value, SEMICOLON_COMMA, null, consumer);
        } else if (delimiter.length() == 1) {
            char ch = delimiter.charAt(0);
            splitList(value, c -> ch == c, null, consumer);
        } else {
            int length = value.length();
            int maxPos = delimiter.length() - 1;
            int start = 0;
            int pos = 0;
            int end = 0;
            for (int i = 0; i < length; i++) {
                if (value.charAt(i) == delimiter.charAt(pos)) {
                    if (pos++ == maxPos) {
                        if (end > start) {
                            consumer.accept(value.substring(start, end + 1));
                        }
                        pos = 0;
                        start = i + 1;
                    }
                } else {
                    end = i;
                }
            }
            if (start < length) {
                consumer.accept(value.substring(start, length));
            }
        }
    }

    /**
     * Splits the given source string into a map of key-value pairs, using a predefined set of delimiters to determine the key-value separation.
     *
     * @param source The source string to be split.
     * @return A map of key-value pairs.
     */
    public static Map<String, String> splitMap(final String source) {
        return splitMap(source, SEMICOLON_COMMA);
    }

    /**
     * Splits the given source string into a map of key-value pairs, using the specified predicate logic to determine the key-value separation.
     *
     * @param source    The source string to be split.
     * @param predicate A character predicate that determines whether a character should be considered a key-value separator.
     * @return A map of key-value pairs.
     */
    public static Map<String, String> splitMap(final String source, final Predicate<Character> predicate) {
        Map<String, String> result = new HashMap<>();
        splitMap(source, predicate, result::put);
        return result;
    }

    /**
     * Splits the given source string into key-value pairs, using the specified predicate logic to determine the key-value separation, and applies a consumer function to each resulting pair.
     *
     * @param source    The source string to be split.
     * @param predicate A character predicate that determines whether a character should be considered a key-value separator.
     * @param consumer  A consumer function that takes a key-value pair as input and performs an action.
     */
    public static void splitMap(final String source, final Predicate<Character> predicate, BiConsumer<String, String> consumer) {
        if (source == null || source.isEmpty() || consumer == null || predicate == null) {
            return;
        }
        int start = -1;
        int end = -1;
        char ch;
        int pos = -1;
        int length = source.length();

        // Iterate over characters
        for (int i = 0; i < length; i++) {
            ch = source.charAt(i);
            if (ch == '=' && pos < 0) {
                pos = i;
            }
            // Check if the character matches the predicate
            if (predicate.test(ch)) {
                // If there is a segment before this character
                if (start >= 0) {
                    if (pos > 0) {
                        consumer.accept(source.substring(start, pos), source.substring(pos + 1, end + 1));
                    }
                    start = -1;
                    end = -1;
                    pos = -1;
                }
            } else {
                if (start == -1) {
                    start = i;
                }
                end = i;
            }
        }
        // Handle the last segment
        if (start >= 0) {
            if (pos > 0) {
                consumer.accept(source.substring(start, pos), source.substring(pos + 1, length));
            }
        }
    }

    /**
     * Joins an array of strings into a single string with a specified separator.
     * This method will skip any blank strings (as determined by the isNotBlank function).
     * It assumes isNotBlank and StringUtils.EMPTY are defined elsewhere in your codebase.
     *
     * @param strings   The array of strings to join.
     * @param separator The separator to use between each string.
     * @return A string that consists of the input strings separated by the specified separator.
     */
    public static String join(String[] strings, String separator) {
        if (strings == null || strings.length == 0) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            if (isNotBlank(string)) {
                sb.append(string).append(separator);
            }
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - separator.length()) : StringUtils.EMPTY;
    }

    /**
     * Concatenates a URL with a single path, handling edge cases for slashes.
     *
     * @param url  The base URL.
     * @param path The path to append.
     * @return The concatenated URL.
     */
    public static String url(String url, String path) {
        if (path == null || path.isEmpty()) {
            return url;
        } else if (url == null) {
            return path;
        } else if (url.endsWith("/")) {
            return url + (path.startsWith("/") ? path.substring(1) : path);
        } else {
            return url + (path.startsWith("/") ? path : "/" + path);
        }
    }

    /**
     * Concatenates a URL with multiple paths, handling edge cases for slashes.
     *
     * @param url   The base URL.
     * @param paths The paths to append.
     * @return The concatenated URL.
     */
    public static String url(String url, String... paths) {
        if (paths == null) {
            return url;
        }
        String result = url;
        for (String path : paths) {
            result = url(result, path);
        }
        return result;
    }
}