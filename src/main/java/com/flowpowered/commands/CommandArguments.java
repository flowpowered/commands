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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gnu.trove.TCollections;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3f;

import com.flowpowered.commons.StringUtil;

import com.flowpowered.commands.syntax.Syntax;

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
    private final TIntList paddings;
    private int index = 0;
    private int depth = 0;
    private Pair<String, Integer> unclosedQuote;
    private final boolean allUnescaped;
    private final String separator;
    private final Syntax syntax;

    public CommandArguments(List<String> args) {
        this.args = new ArrayList<String>(args);
        this.unclosedQuote = null;
        this.allUnescaped = true;
        this.separator = " ";
        this.syntax = null; // TODO: sure?
        this.paddings = null;
    }

    public CommandArguments(String... args) {
        this(Arrays.asList(args));
    }

    public CommandArguments(String args, Syntax syntax) {
        List<String> split = new ArrayList<>();
        this.paddings = new TIntArrayList();
        this.unclosedQuote = syntax.splitNoEmpties(args, split, paddings);  // modifies the lists
        this.args = split;

        this.allUnescaped = false;
        this.syntax = syntax;
        this.separator = syntax.getSeparator();
    }

    protected CommandArguments(List<String> args, TIntList paddings, Syntax syntax, Pair<String, Integer> unclosedQuote) {
        this.paddings = paddings;
        this.unclosedQuote = unclosedQuote;
        this.args = args;

        this.allUnescaped = false;
        this.syntax = syntax;
        this.separator = syntax.getSeparator();
    }

    /**
     * Returns all the remaining arguments.
     *
     * @return remaining arguments
     */
    public List<String> get() {
        return Collections.unmodifiableList(this.args.subList(this.index, this.args.size()));
    }

    public List<String> getAll() {
        return Collections.unmodifiableList(args);
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

    public Syntax getSyntax() {
        return this.syntax;
    }

    /**
     * Returns whether any more unparsed arguments are present
     *
     * @return whether the current index is less than the total number of arguments
     */
    public boolean hasMore() {
        return this.index < this.args.size();
    }

    /**
     * Returns whether calling {@link #currentArgument(String) currentArgument(argName)} would have any argument to return.
     * <p>
     * Returns true when there's some more unparsed arguments, or the argument with specified name is overriden.
     * If it returns false then calling {@link #currentArgument(String)} with the same argName would result in an exception due to argument not being present.
     * 
     * @param argName
     * @return
     */
    public boolean hasNext(String argName) {
        return hasMore() || hasOverride(argName);
    }

    public int remaining() {
        return this.args.size() - this.index;
    }

    public int getIndex() {
        return index;
    }

    protected TIntList getPaddings() {
        return TCollections.unmodifiableList(paddings);
    }

    protected static TIntList getPaddings(CommandArguments args) {
        return args.getPaddings();
    }

    /**
     * @param begin - from which arg, inclusive
     * @param end - to which arg, exclusive
     * @return
     */
    public CommandArguments subArgs(int begin, int end) {
        List<String> newArgs = new ArrayList<>(this.args.subList(begin, end));
        TIntList newPaddings = new TIntArrayList(this.paddings.subList(begin, end));
        int offset = absoluteArgumentToOffset(new Vector2i(begin, 0));
        if (newPaddings.size() > 0) {
            newPaddings.set(0, offset);
        }
        Pair<String, Integer> newUnclosedQuote = null;
        if (unclosedQuote != null) {
            newUnclosedQuote = new ImmutablePair<>(unclosedQuote.getLeft(), unclosedQuote.getRight());
        }
        return new CommandArguments(newArgs, newPaddings, syntax, newUnclosedQuote);
    }

    public Pair<String, Integer> getUnclosedQuote() {
        return this.unclosedQuote;
    }

    public Vector2i offsetToAbsoluteArgument(int cursor) {
        if (allUnescaped) {
            return null;
        }
        int word = 0;
        final int sepLength = separator.length();
        int length = 0;
        while (word < args.size()) {
            int padding = sepLength * paddings.get(word);
            int wordLength = args.get(word).length() + sepLength;
            length += padding;
            if (cursor < length + wordLength) {
                break;
            }
            length += wordLength;
            ++word;
        }
        return new Vector2i(word, cursor - length);
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
        int i;
        for (i = 0; i < pos.getX(); ++i) {
            length += args.get(i).length() + sepLength * (paddings.get(i) + 1);
        }
        length += (i < paddings.size() ? paddings.get(i) : 0) + pos.getY();
        return length;
    }

    public int argumentToOffset(Vector2i pos) {
        return absoluteArgumentToOffset(pos.add(index, 0));
    }

    public int complete(String argName, int cursor, SortedSet<String> potentialCandidates, List<String> candidates) {
        return complete(argName, offsetToArgument(cursor), potentialCandidates, candidates);
    }

    public int complete(String argName, int argNumber, int offset, SortedSet<String> potentialCandidates, List<String> candidates) {
        return complete(argName, new Vector2i(argNumber, offset), potentialCandidates, candidates);
    }

    public int complete(String argName, Vector2i position, SortedSet<String> potentialCandidates, List<String> candidates) {
        return complete(argName, position, potentialCandidates, 0, candidates);
    }

    public int complete(String argName, Vector2i position, SortedSet<String> potentialCandidates, int potentialCandidatesOffset, List<String> candidates) {
        // TODO: Add an option to ignore case
        if (hasOverride(argName)) { // Don't call us like that.
            // Is this too harsh? Maybe, but I guess overrides will be used very rarely, and many devs will probably never encounter them.
            // So if their clients encounter this situation, we'd better produce some meaningful and descriptive error, instead of silently "return -1 no completion for you".
            throw new IllegalStateException(error(argName, "The caller didn't check that the argument is overriden."));
        }
        String rawStart;
        try {
            rawStart = currentArgument(argName, true, false).substring(potentialCandidatesOffset, Math.max(potentialCandidatesOffset, position.getY()));
        } catch (InvalidArgumentException e) {
            throw new IllegalArgumentException("Position " + position + " (cursor " + argumentToOffset(position) + ") is outside of args.", e); // Because whet else could it be?
        }
        return completeRaw(rawStart, position.add(index, 0), potentialCandidates, potentialCandidatesOffset, false, candidates);
    }

    public int completeRaw(String rawStart, Vector2i absolutePosition, SortedSet<String> potentialCandidates, int potentialCandidatesOffset, boolean potentialCandidatesRaw, List<String> candidates) {
        String start = unescape(rawStart);
        SortedSet<String> matches = potentialCandidates.tailSet(start);
        String unclosedQuote = getReachedUnclosedQuote();
        for (String match : matches) {
            if (!match.startsWith(start)) {
                break;
            }
            String subMatch = match.substring(start.length());
            if (!potentialCandidatesRaw) {
                subMatch = escape(subMatch);
            }
            candidates.add(rawStart + subMatch + unclosedQuote + getSeparator());
        }
        if (candidates.isEmpty()) {
            if (unclosedQuote == "") {
                return -1;
            }
            candidates.add(unclosedQuote + getSeparator());
            return absoluteArgumentToOffset(absolutePosition);
        }
        return absoluteArgumentToOffset(new Vector2i(absolutePosition.getX(), potentialCandidatesOffset));
    }

    public int mergeCompletions(String argName, int result1, List<String> candidates1, int result2, List<String> candidates2, List<String> outCandidates) {
        int diff = result2 - result1;
        if (diff < 0) {
            // Swap them
            return mergeCompletions(argName, result2, candidates2, result1, candidates1, outCandidates);
        }
        if (result1 < 0) {
            outCandidates.addAll(candidates2);
            return result2;
        }
        // Now that they're in order, result1 is before result2, so it's the final one we return. Therefore, candidates1 don't need processing.
        outCandidates.addAll(candidates1);

        if (diff == 0) {
            // Optimization - don't need to add prefixes.
            outCandidates.addAll(candidates2);
            return result1;
        }
        Vector2i pos1 = offsetToArgument(result1);

        String closingQuote = getReachedUnclosedQuote() + getSeparator();
        if (candidates2.size() == 1 && candidates2.contains(closingQuote)) {
            // If the further one has only closing quote, it means it's the automatic closing quote from CommandArguments.complete(), which means it has no idea how to complete this.
            // candidates1 has something better, because it completes relatively to something earlier. Therefore we discard candidates2's closing quote.
            // And because we've just discarded the only entry of candidates2, we can as well discard the whole list.
            return result1;
        }
        try {
            String prefix = currentArgument(argName, true, false).substring(pos1.getY(), pos1.getY() + diff);
            for (String candidate : candidates2) {
                outCandidates.add(prefix + candidate);
            }
        } catch (InvalidArgumentException e) {
            throw new IllegalStateException(e);
        }

        return result1;
    }

    protected String getReachedUnclosedQuote() {
        Pair<String, Integer> quote = getUnclosedQuote();
        if (quote != null) {
            int quoteArgNumber = offsetToArgument(quote.getRight()).getX();
            if (quoteArgNumber <= 0) {
                return quote.getLeft();
            }
        }
        return "";
    }

    public int completeAndMerge(String argName, Vector2i position, SortedSet<String> potentialCandidates, int result2, List<String> candidates2, List<String> outCandidates) {
        List<String> candidates1 = new ArrayList<String>();
        int result1 = complete(argName, position, potentialCandidates, candidates1);
        return mergeCompletions(argName, result1, candidates1, result2, candidates2, outCandidates);
    }

    public String getPastCommandString() {
        return this.commandString.toString().trim();
    }

    // State control

    /**
     * Called when an error has occurred while parsing the specified argument due to the argument being invalid
     * Example:
     * <pre>
     *   if (success) {
     *       return success(argName, myValue);
     *     } else {
     *       throw failure(argName, "I dun goofed", false);
     *     }
     * </pre>
     *
     * @param argName The name of the argument
     * @param error The error that occurred
     * @param silenceable Whether the error can be silenced by using the default value for the argument
     * @see InvalidArgumentException for more detail about meanings of args
     * @return The exception -- must be thrown
     */
    public InvalidArgumentException failure(String argName, String error, boolean silenceable) {
        return new InvalidArgumentException(this.commandString.toString().trim(), argName, error, silenceable);
    }

    /**
     * Called when an error has occurred while parsing the specified argument despite the arguments being correct
     * Example:
     * <pre>
     *     switch (x) {
     *         ...
     *     default:
     *         throw error(argName, "How did we get here?");
     *     }
     * </pre>
     *
     * @param argName The name of the argument
     * @param error The error that occurred
     * @see ArgumentParseException for more detail about meanings of args
     * @return The exception -- must be thrown
     */
    public ArgumentParseException error(String argName, String error) {
        return new ArgumentParseException(this.commandString.toString().trim(), argName, error);
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

        String valueOverride = getOverride(argName); // Add to parsed command string
        this.commandString.append(' ');

        if (valueOverride != null) {
            this.commandString.append(valueOverride);
        } else if (this.index >= this.args.size()) {
            this.commandString.append(" [").append(argName).append("]");
        } else {
            if (!fallbackValue) {
                this.commandString.append(this.args.get(this.index));
                this.index++; // And increment index
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
     * @throws InvalidArgumentException if the error is not appropriate to be silenced
     */
    public <T> T potentialDefault(InvalidArgumentException e, T def) throws InvalidArgumentException {
        if (e.isSilenceable()) {
            return success(e.getInvalidArgName(), def, true);
        } else {
            throw e;
        }
    }

    private static final Pattern QUOTE_ESCAPE_REGEX = Pattern.compile("\\\\([\"'])");
    private static final Pattern QUOTE_START_REGEX = Pattern.compile("(?:^| )(['\"])");
    private static final String QUOTE_END_REGEX = "[^\\\\](%s)(?: |$)";
    private static final Pattern QUOTE_TOESCAPE_REGEX = Pattern.compile("\"'");

    /**
     * Return the current argument, without advancing the argument index.
     * Combines quoted strings provided as arguments as necessary.
     * If there are no arguments remaining, the default value is returned.
     *
     * @param argName The name of the argument
     * @return The argument with the current index.
     * @throws InvalidArgumentException if an invalid quoted string was attempted to be used
     * @see #success(String, Object)
     * @see #failure(String, String, boolean, String...)
     * @see #popString(String) for getting a string-typed argument
     */
    public String currentArgument(String argName) throws InvalidArgumentException {
        return currentArgument(argName, false);
    }

    public String currentArgument(String argName, boolean ignoreUnclosedQuote) throws InvalidArgumentException {
        return currentArgument(argName, ignoreUnclosedQuote, true);
    }

    public String currentArgument(String argName, boolean ignoreUnclosedQuote, boolean unescape) throws InvalidArgumentException {
        if (hasOverride(argName)) {
            return getOverride(argName);
        }

        if (this.index >= this.args.size()) {
            throw failure(argName, "Argument not present", true);
        }

        if (!ignoreUnclosedQuote && (this.index + 1 == this.args.size()) && (this.unclosedQuote != null)) {
            throw failure(argName, "Unmatched quoted string! Quote char: " + this.unclosedQuote, false);
        }

        String current = this.args.get(this.index);

        if (allUnescaped || !unescape) {
            return current;
        }

        if (syntax != null) {
            return syntax.unescape(current);
        }

        return defaultUnescape(current);
    }

    public String unescape(String input) {
        if (syntax != null) {
            return syntax.unescape(input);
        }

        return defaultUnescape(input);
    }

    public String escape(String input) {
        if (syntax != null) {
            return syntax.escape(input);
        }
        return defaultEscape(input);
    }

    protected String defaultUnescape(String input) {
        StringBuffer buf = new StringBuffer(input.length());
        Matcher startMatcher = QUOTE_START_REGEX.matcher(input);
        int index = 0;
        while (startMatcher.find(index)) {
            int endOfStart = startMatcher.end(1);
            String quote = StringUtil.escapeRegex(startMatcher.group(1));
            Pattern endPattern = Pattern.compile(QUOTE_END_REGEX.replaceFirst("%s", quote));
            startMatcher.appendReplacement(buf, startMatcher.group().replaceFirst(quote, ""));
            Matcher endMatcher = endPattern.matcher(input);
            if (endMatcher.find(endOfStart)) {
                buf.append(input, endOfStart, endMatcher.start(1));
                index = endMatcher.end(1);
            } else {
                startMatcher.appendTail(buf);
                index = input.length();
            }
        }
        if (index < input.length()) {
            buf.append(input, index, input.length());
        }
        return QUOTE_ESCAPE_REGEX.matcher(buf).replaceAll("$1");
    }

    protected String defaultEscape(String input) {
        return QUOTE_TOESCAPE_REGEX.matcher(input).replaceAll("\\\\$0");
    }

    public boolean setArgOverride(String name, String value) {
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
        return ++this.index < this.args.size();
    }

    /**
     *
     * @throws InvalidArgumentException when unparsed arguments are present.
     */
    public void assertCompletelyParsed() throws InvalidArgumentException {
        if (this.index < this.args.size()) {
            if (index == args.size() - 1 && args.get(index).isEmpty()) {
                return;
            }
            throw failure("...", "Too many arguments are present!", false);
        }
    }

    // Argument storage methods

    public String popString(String argName) throws InvalidArgumentException {
        String arg = currentArgument(argName);
        return success(argName, arg);
    }

    public String popString(String argName, String def) throws InvalidArgumentException {
        try {
            return popString(argName);
        } catch (InvalidArgumentException e) {
            return potentialDefault(e, def);
        }
    }

    public int popInteger(String argName) throws InvalidArgumentException {
        String arg = currentArgument(argName);
        try {
            return success(argName, Integer.parseInt(arg));
        } catch (NumberFormatException e) {
            throw failure(argName, "Input '" + arg + "' is not an integer you silly!", false);
        }
    }

    public int popInteger(String argName, int def) throws InvalidArgumentException {
        try {
            return popInteger(argName);
        } catch (InvalidArgumentException e) {
            return potentialDefault(e, def);
        }
    }

    public float popFloat(String argName) throws InvalidArgumentException {
        String arg = currentArgument(argName);
        try {
            return success(argName, Float.parseFloat(arg));
        } catch (NumberFormatException e) {
            throw failure(argName, "Input '" + arg + "' is not a float you silly!", false);
        }
    }

    public float popFloat(String argName, float def) throws InvalidArgumentException {
        try {
            return popFloat(argName);
        } catch (InvalidArgumentException e) {
            return potentialDefault(e, def);
        }
    }

    public double popDouble(String argName) throws InvalidArgumentException {
        String arg = currentArgument(argName);
        try {
            return success(argName, Double.parseDouble(arg));
        } catch (NumberFormatException e) {
            throw failure(argName, "Input '" + arg + "' is not a double you silly!", false);
        }
    }

    public double popDouble(String argName, double def) throws InvalidArgumentException {
        try {
            return popDouble(argName);
        } catch (InvalidArgumentException e) {
            return potentialDefault(e, def);
        }
    }

    public boolean popBoolean(String argName) throws InvalidArgumentException {
        String str = currentArgument(argName);
        if (!str.equalsIgnoreCase("true") && !str.equalsIgnoreCase("false")) {
            throw failure(argName, "Value '" + str + "' is not a boolean you silly!", false);
        }
        return success(argName, Boolean.parseBoolean(str));
    }

    public boolean popBoolean(String argName, boolean def) throws InvalidArgumentException {
        try {
            return popBoolean(argName);
        } catch (InvalidArgumentException e) {
            return potentialDefault(e, def);
        }
    }

    public String popSubCommand() throws InvalidArgumentException {
        return popString(SUBCOMMAND_ARGNAME + this.depth++);
    }

    public CommandFlags popFlags(String argName, CommandFlags flags) throws InvalidArgumentException {
        flags.parse(this, argName);
        return flags;
    }

    // TODO: A version w/o command and sender, that ignores callbacks?
    public int completeFlags(Command command, CommandSender sender, String argName, CommandFlags flags, int cursor, List<String> candidates) throws InvalidArgumentException {
        return flags.complete(command, sender, this, argName, cursor, candidates);
    }

    /**
     * Pop a {@link Vector3f}.
     * Accepts either x y z or x,y,z syntax
     * TODO support relative syntax
     *
     * @param argName The name of the argument
     * @return A parsed vector
     * @throws InvalidArgumentException if not enough coordinates are provided or the coordinates are not floats
     */
    public Vector3f popVector3(String argName) throws InvalidArgumentException {
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
        } catch (InvalidArgumentException e) {
            throw failure(argName, e.getReason(), e.isSilenceable());
        }
    }

    public Vector3f popVector3(String argName, Vector3f def) throws InvalidArgumentException {
        try {
            return popVector3(argName);
        } catch (InvalidArgumentException e) {
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
     * @throws InvalidArgumentException if no argument is present or an unknown element is chosen.
     */
    public <T extends Enum<T>> T popEnumValue(String argName, Class<T> enumClass) throws InvalidArgumentException {
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
    public <T extends Enum<T>> T popEnumValue(String argName, Class<T> enumClass, T def) throws InvalidArgumentException {
        try {
            return popEnumValue(argName, enumClass);
        } catch (InvalidArgumentException e) {
            return potentialDefault(e, def);
        }
    }

    /**
     * Returns a string including every remaining argument
     *
     * @return string from specified arg on
     */
    public String popRemainingStrings(String argName) throws InvalidArgumentException {
        if (hasOverride(argName)) {
            return success(argName, currentArgument(argName));
        }
        if (!hasMore()) {
            throw failure(argName, "No arguments present", true);
        }
        StringBuilder builder = new StringBuilder();
        while (hasMore()) {
            popRemainingStringSegment(argName, builder, true, false);
        }
        String ret = builder.toString();
        try {
            assertCompletelyParsed(); // If not, there's a bug
        } catch (InvalidArgumentException e) {
            throw new IllegalStateException("Doesn't have more but still not completely parsed.", e); // If it's a bug, don't blame the user - don't throw a UserFriendlyCommandException
        }
        return success(argName, ret);
    }

    public String popRemainingStrings(String argName, String def) throws InvalidArgumentException {
        try {
            return popRemainingStrings(argName);
        } catch (InvalidArgumentException e) {
            return potentialDefault(e, def);
        }
    }

    public int completeRemainingStrings(String argName, int cursor, SortedSet<String> potentialCandidates, List<String> candidates) {
        return completeRemainingStrings(argName, cursor, potentialCandidates, 0, candidates);
    }

    public int completeRemainingStrings(String argName, int cursor, SortedSet<String> potentialCandidates, int potentialCandidatesOffset, List<String> candidates) {
        if (hasOverride(argName)) {
            success(argName, getOverride(argName));
        }
        Vector2i pos = offsetToArgument(cursor);
        int base = argumentToOffset(new Vector2i(0, 0));
        if (pos.getX() >= remaining()) {
            throw new IllegalArgumentException("Position " + pos + " (curosr " + cursor + ") is outside of args!");
        }
        StringBuilder builder = new StringBuilder();
        try {
            for (int i = 0; i < pos.getX(); ++i) {
                popRemainingStringSegment(argName, builder, false, false);
            }
            popRemainingStringSegment(argName, builder, false, true);
        } catch (InvalidArgumentException e) {
            throw new IllegalStateException("We did the checks, but currentArgument threw something anyway", e);
        }
        String rawStart = builder.toString().substring(potentialCandidatesOffset, Math.max(potentialCandidatesOffset, cursor - base));
        return completeRaw(rawStart, offsetToAbsoluteArgument(cursor), potentialCandidates, potentialCandidatesOffset, true, candidates);
    }

    protected void popRemainingStringSegment(String argName, StringBuilder builder, boolean unescape, boolean dontAdvance) throws InvalidArgumentException {
        for (int i = 0; i < paddings.get(index); ++i) {
            builder.append(separator);
        }
        builder.append(currentArgument(argName, true, unescape));
        if (dontAdvance) {
            return;
        }
        advance();
        if (hasMore()) {
            builder.append(separator);
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
        return key != null && this.argOverrides.containsKey(key);
    }

    public String getOverride(String key) {
        return this.argOverrides.get(key);
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

    public CommandFlags getFlags(String key) {
        return get(key, CommandFlags.class);
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
