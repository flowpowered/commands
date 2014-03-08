/*
 * This file is part of Flow Commands, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <https://spout.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.flowpowered.commands.arguments;

import java.util.List;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3f;

public interface CommandArguments {

    public static final String SUBCOMMAND_ARGNAME = "subcommand:";

    /**
     * Returns all the remaining arguments.
     *
     * @return all arguments
     */
    List<String> get();

    /**
     * Returns the length of the arguments.
     *
     * @return length of arguments
     */
    int length();

    int getDepth();

    String getSeparator();

    /**
     * Returns whether any more unparsed arguments are present
     *
     * @return whether the current index is less than the total number of arguments
     */
    boolean hasMore();

    int remaining();

    String getUnclosedQuote();

    Vector2i offsetToAbsoluteArgument(int cursor);

    Vector2i offsetToArgument(int cursor);

    int absoluteArgumentToOffset(Vector2i pos);

    int argumentToOffset(Vector2i pos);

    CommandFlags flags();

    String getPastCommandString();

    String unescape(String input);

    // State control

    /**
     * Called when an error has occurred while parsing the specified argument
     * Example:
     * <pre>
     *   if (success) {
     *       return success(argName, myValue);
     *     } else {
     *       throw failure(argName, "I dun goofed", "some", "other", "options");
     *     }
     * </pre>
     *
     * @param argName The name of the argument
     * @param error The error that occurred
     * @param silenceable Whether the error is caused by syntax of single argument/permanently invalid provided value (or not)
     * @see ArgumentParseException for more detail about meanings of args
     * @return The exception -- must be thrown
     */
    ArgumentParseException failure(String argName, String error, boolean silenceable);

    /**
     * Must be called when an argument has been successfully parsed
     * This stores the parsed value into the map, appends the string value to the map, and advances the index.
     *
     * @param argName     The name of the arg
     * @param parsedValue The parsed value of the argument
     * @param <T>         The type of the parsed value
     * @return {@code parsedValue}
     */

    <T> T success(String argName, T parsedValue);

    <T> T success(String argName, T parsedValue, boolean fallbackValue);

    /**
     * This method should be called in methods that can potentially return a default value.
     *
     * @param e The thrown exception
     * @param def The default value that could be returned
     * @param <T> The type of the argument
     * @return The default value, if error is safe to silence
     * @throws ArgumentParseException if the error is not appropriate to be silenced
     */
    <T> T potentialDefault(ArgumentParseException e, T def) throws ArgumentParseException;

    /**
     * Return the current argument, without advancing the argument index.
     * Combines quoted strings provided as arguments as necessary.
     * If there are no arguments remaining, the default value is returned.
     *
     * @param argName The name of the argument
     * @return The argument with the current index.
     * @throws ArgumentParseException if an invalid quoted string was attempted to be used
     * @see #success(String, Object)
     * @see #failure(String, String, boolean, String...)
     * @see #popString(String) for getting a string-typed argument
     */
    String currentArgument(String argName) throws ArgumentParseException;

    String currentArgument(String argName, boolean ignoreUnclosedQuote) throws ArgumentParseException;

    String currentArgument(String argName, boolean ignoreUnclosedQuote, boolean unescape) throws ArgumentParseException;

    boolean setArgOverride(String name, String value);

    /**
     * Increase the argument 'pointer' by one without storing any arguments
     *
     * @return Whether there is an argument present at the incremented index
     */
    boolean advance();

    /**
     *
     * @throws ArgumentParseException when unparsed arguments are present.
     */
    void assertCompletelyParsed() throws ArgumentParseException;

    // Argument storage methods

    <T> T pop(String argName, Class<T> type) throws ArgumentParseException;

    <T> T pop(String argName, Class<T> type, T def) throws ArgumentParseException;

    String popString(String argName) throws ArgumentParseException;

    String popString(String argName, String def) throws ArgumentParseException;

    int popInteger(String argName) throws ArgumentParseException;

    int popInteger(String argName, int def) throws ArgumentParseException;

    float popFloat(String argName) throws ArgumentParseException;

    float popFloat(String argName, float def) throws ArgumentParseException;

    double popDouble(String argName) throws ArgumentParseException;

    double popDouble(String argName, double def) throws ArgumentParseException;

    boolean popBoolean(String argName) throws ArgumentParseException;

    boolean popBoolean(String argName, boolean def) throws ArgumentParseException;

    String popSubCommand() throws ArgumentParseException;

    /**
     * Pop a {@link Vector3f}.
     * Accepts either x y z or x,y,z syntax
     * TODO support relative syntax
     *
     * @param argName The name of the argument
     * @return A parsed vector
     * @throws ArgumentParseException if not enough coordinates are provided or the coordinates are not floats
     */
    Vector3f popVector3(String argName) throws ArgumentParseException;

    Vector3f popVector3(String argName, Vector3f def) throws ArgumentParseException;

    /**
     * Pop an enum value from the arguments list.
     * Values are checked by index and by uppercased name.
     *
     * @param argName The name of the argument
     * @param enumClass The enum class to
     * @param <T> The type of enum
     * @return The enum value
     * @throws ArgumentParseException if no argument is present or an unknown element is chosen.
     */
    <T extends Enum<T>> T popEnumValue(String argName, Class<T> enumClass) throws ArgumentParseException;

    /**
     * @see #popEnumValue(String, Class) non-defaulted version
     */
    <T extends Enum<T>> T popEnumValue(String argName, Class<T> enumClass, T def) throws ArgumentParseException;

    /**
     * Returns a string including every remaining argument
     *
     * @return string from specified arg on
     */
    String popRemainingStrings(String argName) throws ArgumentParseException;

    String popRemainingStrings(String argName, String def) throws ArgumentParseException;

    // Parsed argument access methods

    <T> T get(String key, Class<T> type);

    <T> T get(String key, Class<T> type, T def);

    boolean has(String key);

    boolean hasOverride(String key);

    String getString(String key);

    String getString(String key, String def);

    int getInteger(String key, int def);

    float getFloat(String key, float def);

    /**
     * Returns the arguments in an array.
     *
     * @return arguments
     */
    String[] toArray();

}
