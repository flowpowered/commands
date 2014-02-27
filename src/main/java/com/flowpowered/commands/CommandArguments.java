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
package com.flowpowered.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3f;

/**
 * This class is used as a wrapper for command arguments to make them easily
 * parse-able.
 *
 * Please note that the javadocs for the pop* methods describe how input is currently handled. Handling may
 * change over time, and while efforts are made to retain backwards compatibility it is not always possible.
 */
public class CommandArguments {
    public static final String SUBCOMMAND_ARGNAME = "subcommand:";

    private final StringBuilder commandString = new StringBuilder();
    private final Map<String, Object> parsedArgs = new HashMap<String, Object>();
    private final Map<String, String> argOverrides = new HashMap<String, String>();
    private final List<String> args;
    private final CommandFlags flags;
    int index = 0;
    private int depth = 0;
    private boolean currentArgUnescaped = false;
    private String unclosedQuote;
    private final boolean allUnescaped;
    private final String separator;

    public CommandArguments(List<String> args) {
        this.args = new ArrayList<String>(args);
        this.flags = new CommandFlags(this);
        this.unclosedQuote = null;
        this.allUnescaped = false;
        this.separator = " ";
    }

    public CommandArguments(String... args) {
        this(Arrays.asList(args));
    }

    public CommandArguments(String args, String separator) {
        List<String> split = new ArrayList<>(Arrays.asList(args.split(Pattern.quote(separator))));
        this.unclosedQuote = unquote(split, separator); // modifies the list
        if (args.endsWith(separator)) {
            split.add("");
        }
        this.args = split;
        this.flags = new CommandFlags(this);
        this.allUnescaped = true;
        this.separator = separator;
    }

    /**
     * Returns all the remaining arguments.
     *
     * @return all arguments
     */
    public List<String> get() {
        return this.args.subList(this.index, this.args.size());
    }

    /**
     * Gives the mutable list of argument strings currently in use by this.
     *
     * @return the arguments
     */
    List<String> getLive() {
        return this.args;
    }

    /**
     * Returns the length of the arguments.
     *
     * @return length of arguments
     */
    public int length() {
        return this.args.size();
    }

    public int getDepth() {
        return this.depth;
    }

    public String getSeparator() {
        return separator;
    }

    /**
     * Returns whether any more unparsed arguments are present
     *
     * @return whether the current index is less than the total number of arguments
     */
    public boolean hasMore() {
        return this.index < this.args.size();
    }

    public int remaining() {
        return this.args.size() - this.index;
    }

    public String getUnclosedQuote() {
        return this.unclosedQuote;
    }

    public Vector2i offsetToAbsoluteArgument(int cursor) {
        if (!allUnescaped) {
            return null;
        }
        int word = 0;
        final int sepLength = separator.length();
        int length = 0;
        while (word < args.size()) {
            int wordLength = args.get(word).length() + sepLength;
            if (cursor < length + wordLength) {
                break;
            }
            length += wordLength;
            ++word;
        }
        return new Vector2i(word, cursor - length);
        /*while (word < args.size() && length <= cursor) {
            ++word;
            length += args.get(word).length() + sepLength;
        }*/
    }

    public Vector2i offsetToArgument(int cursor) {
        Vector2i v = offsetToAbsoluteArgument(cursor);
        if (v != null) {
            return v.sub(index, 0);
        }
        return null;
    }

    public int absoluteArgumentToOffset(Vector2i pos) {
        int length = 0;
        final int sepLength = separator.length();
        for (int i = 0; i < pos.getX(); ++i) {
            length += args.get(i).length() + sepLength;
        }
        length += pos.getY();
        return length;
    }

    public int argumentToOffset(Vector2i pos) {
        return absoluteArgumentToOffset(pos.add(index, 0));
    }

    public CommandFlags flags() {
        return this.flags;
    }

    public String getPastCommandString() {
        return this.commandString.toString().trim();
    }

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
    public ArgumentParseException failure(String argName, String error, boolean silenceable) {
        return new ArgumentParseException(this.commandString.toString().trim(), argName, error, silenceable);
    }

    /**
     * Must be called when an argument has been successfully parsed
     * This stores the parsed value into the map, appends the string value to the map, and advances the index.
     *
     * @param argName     The name of the arg
     * @param parsedValue The parsed value of the argument
     * @param <T>         The type of the parsed value
     * @return {@code parsedValue}
     */

    public <T> T success(String argName, T parsedValue) {
        return success(argName, parsedValue, false);
    }

    public <T> T success(String argName, T parsedValue, boolean fallbackValue) {
        if (argName != null) { // Store arg
            this.parsedArgs.put(argName, parsedValue);
        }

        String valueOverride = this.argOverrides.get(argName); // Add to parsed command string
        this.commandString.append(' ');

        if (valueOverride != null) {
            this.commandString.append(valueOverride);
        } else if (this.index >= this.args.size()) {
            this.commandString.append(" [").append(argName).append("]");
        } else {
            this.commandString.append(this.args.get(this.index));
            if (!fallbackValue) {
                this.index++; // And increment index
                currentArgUnescaped = false;
            }
        }

        return parsedValue;
    }

    /**
     * This method should be called in methods that can potentially return a default value.
     *
     * @param e The thrown exception
     * @param def The default value that could be returned
     * @param <T> The type of the argument
     * @return The default value, if error is safe to silence
     * @throws ArgumentParseException if the error is not appropriate to be silenced
     */
    public <T> T potentialDefault(ArgumentParseException e, T def) throws ArgumentParseException {
        if (e.isSilenceable()) {
            return success(e.getInvalidArgName(), def, true);
        } else {
            throw e;
        }
    }

    private static final Pattern QUOTE_START_REGEX = Pattern.compile("^('|\")"), QUOTE_END_REGEX = Pattern.compile("[^\\\\]?('|\")$"), QUOTE_ESCAPE_REGEX = Pattern.compile("\\\\([\"'])");

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
    public String currentArgument(String argName) throws ArgumentParseException {
        return currentArgument(argName, false);
    }

    public String currentArgument(String argName, boolean ignoreUnclosedQuote) throws ArgumentParseException {
        if (argName != null && this.argOverrides.containsKey(argName)) {
            return this.argOverrides.get(argName);
        }

        if (this.index >= this.args.size()) {
            throw failure(argName, "Argument not present", true);
        }

        if (!ignoreUnclosedQuote && (this.index + 1 == this.args.size()) && (this.unclosedQuote != null)) {
            throw failure(argName, "Unmatched quoted string! Quote char: " + this.unclosedQuote, false);
        }

        // Quoted argument parsing -- comparts and removes unnecessary arguments
        String current = this.args.get(this.index);

        if (currentArgUnescaped || allUnescaped) {
            return current;
        }

        Matcher start = QUOTE_START_REGEX.matcher(current);
        if (start.find()) { // We've found a quoted string
            boolean foundEnd = false;
            String quoteChar = start.group(1);
            StringBuffer quotedBuilder = new StringBuffer(2 * current.length());

            current = current.substring(1);
            for (boolean first = true; ((this.index + 1) < this.args.size() || first) && !foundEnd; first = false) {
                if (!first) {
                    current = this.args.remove(this.index + 1);
                }

                Matcher end = QUOTE_END_REGEX.matcher(current);
                if (end.find() && end.group(1).equals(quoteChar)) { // End character found here
                    foundEnd = true;
                    current = current.substring(0, current.length() - 1);
                }

                if (!first) {
                    quotedBuilder.append(" ");
                }
                Matcher escape = QUOTE_ESCAPE_REGEX.matcher(current); // Replace escaped strings
                while (escape.find()) {
                    escape.appendReplacement(quotedBuilder, escape.group(1));
                }
                escape.appendTail(quotedBuilder);
            }

            if (!(foundEnd || ignoreUnclosedQuote)) { // Unmatched: "quoted string
                this.unclosedQuote = quoteChar;
                throw failure(argName, "Unmatched quoted string! Quote char: " + quoteChar, false);
            }
            this.args.set(this.index, (current = quotedBuilder.toString()));
        } else {
            Matcher escape = QUOTE_ESCAPE_REGEX.matcher(current);
            if (escape.find()) {
                StringBuffer replace = new StringBuffer(current.length() - 1);
                escape.appendReplacement(replace, escape.group(1));
                while (escape.find()) {
                    escape.appendReplacement(replace, escape.group(1));
                }
                escape.appendTail(replace);
                current = current.substring(1);
            }
        }
        currentArgUnescaped = true;

        return current;
    }

    protected String unquote(List<String> args, String separator) {
        ListIterator<String> itr = args.listIterator();
        while (itr.hasNext()) {
            String current = itr.next();
            Matcher start = QUOTE_START_REGEX.matcher(current);
            if (start.find()) { // We've found a quoted string
                boolean foundEnd = false;
                String quoteChar = start.group(1);
                StringBuffer quotedBuilder = new StringBuffer(2 * current.length());
                current = current.substring(1);
                for (boolean first = true; (itr.hasNext() || first) && !foundEnd; first = false) {
                    if (!first) {
                        current = itr.next();
                    }

                    Matcher end = QUOTE_END_REGEX.matcher(current);
                    if (end.find() && end.group(1).equals(quoteChar)) { // End character found here
                        foundEnd = true;
                        current = current.substring(0, current.length() - 1);
                    }

                    if (!first) {
                        quotedBuilder.append(separator);
                    }
                    Matcher escape = QUOTE_ESCAPE_REGEX.matcher(current); // Replace escaped strings
                    while (escape.find()) {
                        escape.appendReplacement(quotedBuilder, escape.group(1));
                    }
                    escape.appendTail(quotedBuilder);

                    itr.remove();
                }
                itr.add(quotedBuilder.toString());

                if (!foundEnd) {
                    return quoteChar;
                }

            } else {
                Matcher escape = QUOTE_ESCAPE_REGEX.matcher(current);
                if (escape.find()) {
                    StringBuffer replace = new StringBuffer(current.length() - 1);
                    escape.appendReplacement(replace, escape.group(1));
                    while (escape.find()) {
                        escape.appendReplacement(replace, escape.group(1));
                    }
                    escape.appendTail(replace);
                    itr.remove();
                    itr.add(replace.toString());
                }
            }
        }

        return null;
    }

    protected boolean setArgOverride(String name, String value) {
        if (!this.argOverrides.containsKey(name)) {
            this.argOverrides.put(name, value);
            return true;
        }
        return false;
    }

    /**
     * Increase the argument 'pointer' by one without storing any arguments
     *
     * @return Whether there is an argument present at the incremented index
     */
    public boolean advance() {
        currentArgUnescaped = false;
        return ++this.index < this.args.size();
    }

    /**
     *
     * @throws ArgumentParseException when unparsed arguments are present.
     */
    public void assertCompletelyParsed() throws ArgumentParseException {
        if (this.index < this.args.size()) {
            throw failure("...", "Too many arguments are present!", false);
        }
    }

    // Argument storage methods

    public String popString(String argName) throws ArgumentParseException {
        String arg = currentArgument(argName);
        return success(argName, arg);
    }

    public String popString(String argName, String def) throws ArgumentParseException {
        try {
            return popString(argName);
        } catch (ArgumentParseException e) {
            return potentialDefault(e, def);
        }
    }

    public int popInteger(String argName) throws ArgumentParseException {
        String arg = currentArgument(argName);
        try {
            return success(argName, Integer.parseInt(arg));
        } catch (NumberFormatException e) {
            throw failure(argName, "Input '" + arg + "' is not an integer you silly!", false);
        }
    }

    public int popInteger(String argName, int def) throws ArgumentParseException {
        try {
            return popInteger(argName);
        } catch (ArgumentParseException e) {
            return potentialDefault(e, def);
        }
    }

    public float popFloat(String argName) throws ArgumentParseException {
        String arg = currentArgument(argName);
        try {
            return success(argName, Float.parseFloat(arg));
        } catch (NumberFormatException e) {
            throw failure(argName, "Input '" + arg + "' is not a float you silly!", false);
        }
    }

    public float popFloat(String argName, float def) throws ArgumentParseException {
        try {
            return popFloat(argName);
        } catch (ArgumentParseException e) {
            return potentialDefault(e, def);
        }
    }

    public double popDouble(String argName) throws ArgumentParseException {
        String arg = currentArgument(argName);
        try {
            return success(argName, Double.parseDouble(arg));
        } catch (NumberFormatException e) {
            throw failure(argName, "Input '" + arg + "' is not a double you silly!", false);
        }
    }

    public double popDouble(String argName, double def) throws ArgumentParseException {
        try {
            return popDouble(argName);
        } catch (ArgumentParseException e) {
            return potentialDefault(e, def);
        }
    }

    public boolean popBoolean(String argName) throws ArgumentParseException {
        String str = currentArgument(argName);
        if (!str.equalsIgnoreCase("true") && !str.equalsIgnoreCase("false")) {
            throw failure(argName, "Value '" + str + "' is not a boolean you silly!", false);
        }
        return success(argName, Boolean.parseBoolean(str));
    }

    public boolean popBoolean(String argName, boolean def) throws ArgumentParseException {
        try {
            return popBoolean(argName);
        } catch (ArgumentParseException e) {
            return potentialDefault(e, def);
        }
    }

    public String popSubCommand() throws ArgumentParseException {
        return popString(SUBCOMMAND_ARGNAME + this.depth++);
    }

    /**
     * Pop a {@link Vector3f}.
     * Accepts either x y z or x,y,z syntax
     * TODO support relative syntax
     *
     * @param argName The name of the argument
     * @return A parsed vector
     * @throws ArgumentParseException if not enough coordinates are provided or the coordinates are not floats
     */
    public Vector3f popVector3(String argName) throws ArgumentParseException {
        try {
            float x, y, z;
            if (currentArgument(argName).contains(",")) {
                String[] els = currentArgument(argName).split(",");
                if (els.length < 3) {
                    throw failure(argName, "Must provide 3 coordinates", false);
                }
                x = Float.parseFloat(els[0]);
                y = Float.parseFloat(els[1]);
                z = Float.parseFloat(els[2]);
            } else {
                x = popFloat(null);
                y = popFloat(null);
                z = popFloat(null);
            }
            return success(argName, new Vector3f(x, y, z));
        } catch (ArgumentParseException e) {
            throw failure(argName, e.getReason(), e.isSilenceable());
        }
    }

    public Vector3f popVector3(String argName, Vector3f def) throws ArgumentParseException {
        try {
            return popVector3(argName);
        } catch (ArgumentParseException e) {
            return potentialDefault(e, def);
        }
    }

    private static final int MAX_ARG_FULLPRINT = 5;

    private static String buildEnumError(Class<? extends Enum<?>> enumClass) {
        Enum<?>[] constants = enumClass.getEnumConstants();
        String itemList;
        if (constants.length > MAX_ARG_FULLPRINT) {
            itemList = "an element of " + enumClass.getSimpleName();
        } else {
            boolean first = true;
            StringBuilder build = new StringBuilder();
            for (Enum<?> e : constants) {
                if (!first) {
                    build.append(", ");
                }
                build.append("'").append(e.name()).append("'");
                first = false;
            }
            itemList = build.toString();
        }
        return "Invalid " + enumClass.getSimpleName() + "; Must be 0-" + constants.length + " or " + itemList + ".";
    }

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
    public <T extends Enum<T>> T popEnumValue(String argName, Class<T> enumClass) throws ArgumentParseException {
        String key = currentArgument(argName);
        T[] constants = enumClass.getEnumConstants();
        T value;
        try {
            int index = Integer.parseInt(key);
            if (index < 0 || index >= constants.length) {
                throw failure(argName, buildEnumError(enumClass), false);
            }
            value = constants[index];
        } catch (NumberFormatException e) {
            try {
                value = Enum.valueOf(enumClass, key.toUpperCase());
            } catch (IllegalArgumentException e2) {
                throw failure(argName, buildEnumError(enumClass), false);
            }
        }
        return success(argName, value);

    }

    /**
     * @see #popEnumValue(String, Class) non-defaulted version
     */
    public <T extends Enum<T>> T popEnumValue(String argName, Class<T> enumClass, T def) throws ArgumentParseException {
        try {
            return popEnumValue(argName, enumClass);
        } catch (ArgumentParseException e) {
            return potentialDefault(e, def);
        }
    }

    /**
     * Returns a string including every remaining argument
     *
     * @return string from specified arg on
     */
    public String popRemainingStrings(String argName) throws ArgumentParseException {
        if (!hasMore()) {
            failure(argName, "No arguments present", true);
        }
        StringBuilder builder = new StringBuilder();
        if (hasOverride(argName)) {
            return success(argName, currentArgument(argName));
        }
        while (hasMore()) {
            builder.append(currentArgument(argName, true));
            advance();
            if (hasMore()) {
                builder.append(separator);
            }
        }
        String ret = builder.toString();
        assertCompletelyParsed(); // If not, there's a bug
        return success(argName, ret);
    }

    public String popRemainingStrings(String argName, String def) throws ArgumentParseException {
        try {
            return popRemainingStrings(argName);
        } catch (ArgumentParseException e) {
            return potentialDefault(e, def);
        }
    }

    // Command utility methods

    public void logAndNotify(Logger logger, CommandSender source, String message) {
        source.sendMessage(message);
        if (logger != null) {
            logger.info(message); // TODO: If and why do we want this here?
        }
    }

    // Parsed argument access methods
    public <T> T get(String key, Class<T> type) {
        return get(key, type, null);
    }

    public <T> T get(String key, Class<T> type, T def) {
        Object o = this.parsedArgs.get(key);

        if (o == null) {
            return def;
        } else if (type.isInstance(o)) {
            return type.cast(o);
        }
        throw new RuntimeException("Incorrect argument type " + type.getName() + " for argument " + key);
    }

    public boolean has(String key) {
        return this.parsedArgs.containsKey(key);
    }

    public boolean hasOverride(String key) {
        return this.argOverrides.containsKey(key);
    }

    public String getString(String key) {
        return get(key, String.class);
    }

    public String getString(String key, String def) {
        return get(key, String.class, def);
    }

    public int getInteger(String key, int def) {
        Integer i = get(key, Integer.class);
        if (i == null) {
            return def;
        }
        return i;
    }

    public float getFloat(String key, float def) {
        Float f = get(key, Float.class);
        if (f == null) {
            return def;
        }
        return f;
    }

    /**
     * Returns the arguments in an array.
     *
     * @return arguments
     */
    public String[] toArray() {
        return this.args.toArray(new String[this.args.size()]);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String arg : this.args.subList(0, this.index)) {
            builder.append(arg).append(" ");
        }
        builder.append('^');
        for (String arg : this.args.subList(this.index, this.args.size())) {
            builder.append(arg).append(" ");
        }
        return builder.toString();
    }
}
