/*
 * This file is part of Flow Commands, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <http://www.spout.org/>
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import org.slf4j.Logger;

import com.flowpowered.commands.CommandSender;
import com.flowpowered.commands.converters.ArgumentConverterSet;
import com.flowpowered.commands.syntax.Syntax;
import com.flowpowered.commons.StringUtil;
import com.flowpowered.math.vector.Vector2i;

/**
 * This class is used as a wrapper for command arguments to make them easily
 * parse-able.
 *
 * Please note that the javadocs for the pop* methods describe how input is currently handled. Handling may
 * change over time, and while efforts are made to retain backwards compatibility it is not always possible.
 */
public class DefaultCommandArguments extends AbstractCommandArguments {
    private final StringBuilder commandString = new StringBuilder();
    private final Map<String, Object> parsedArgs = new HashMap<String, Object>();
    private final Map<String, String> argOverrides = new HashMap<String, String>();
    private final List<String> args;
    private final TIntList paddings;
    private final CommandFlags flags;
    int index = 0;
    private int depth = 0;
    private String unclosedQuote;
    private final boolean allUnescaped;
    private final String separator;
    private final Syntax syntax;
    private final ArgumentConverterSet converters = new ArgumentConverterSet(); // TODO: Don't hardcode it here.

    public DefaultCommandArguments(List<String> args) {
        this.args = new ArrayList<String>(args);
        this.flags = new CommandFlags(this);
        this.unclosedQuote = null;
        this.allUnescaped = true;
        this.separator = " ";
        this.syntax = null; // TODO: sure?
        this.paddings = null;
    }

    public DefaultCommandArguments(String... args) {
        this(Arrays.asList(args));
    }

    public DefaultCommandArguments(String args, Syntax syntax) {
        List<String> split = new ArrayList<>();
        this.paddings = new TIntArrayList();
        this.unclosedQuote = syntax.split(args, split); // modifies the list
        int i = 0;
        paddings.add(0);
        Iterator<String> itr = split.iterator();
        while (itr.hasNext()) {
            if (itr.next().isEmpty()) {
                if (!itr.hasNext()) {
                    break;
                }
                paddings.set(i, paddings.get(i) + 1);
                itr.remove();
            } else {
                paddings.add(0);
                ++i;
            }
        }
        this.args = split;
        this.flags = null; // new CommandFlags(this);
        this.allUnescaped = false;
        this.syntax = syntax;
        this.separator = syntax.getSeparator();
    }

    @Override
    public List<String> get() {
        return this.args.subList(this.index, this.args.size());
    }

    List<String> getLive() {
        return this.args;
    }

    @Override
    public int length() {
        return this.args.size();
    }

    @Override
    public int getDepth() {
        return this.depth;
    }

    @Override
    public String getSeparator() {
        return separator;
    }

    @Override
    public boolean hasMore() {
        return this.index < this.args.size();
    }

    @Override
    public int remaining() {
        return this.args.size() - this.index;
    }

    @Override
    public String getUnclosedQuote() {
        return this.unclosedQuote;
    }

    @Override
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

    @Override
    public Vector2i offsetToArgument(int cursor) {
        Vector2i v = offsetToAbsoluteArgument(cursor);
        if (v != null) {
            return v.sub(index, 0);
        }
        return null;
    }

    @Override
    public int absoluteArgumentToOffset(Vector2i pos) {
        int length = 0;
        final int sepLength = separator.length();
        int i;
        for (i = 0; i < pos.getX(); ++i) {
            length += args.get(i).length() + sepLength * (paddings.get(i) + 1);
        }
        length += paddings.get(i) + pos.getY();
        return length;
    }

    @Override
    public int argumentToOffset(Vector2i pos) {
        return absoluteArgumentToOffset(pos.add(index, 0));
    }

    @Override
    public CommandFlags flags() {
        return this.flags;
    }

    @Override
    public String getPastCommandString() {
        return this.commandString.toString().trim();
    }

    // State control

    @Override
    public ArgumentParseException failure(String argName, String error, boolean silenceable) {
        return new ArgumentParseException(this.commandString.toString().trim(), argName, error, silenceable);
    }

    @Override
    public <T> T success(String argName, T parsedValue) {
        return success(argName, parsedValue, false);
    }

    @Override
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
            }
        }

        return parsedValue;
    }

    @Override
    public <T> T potentialDefault(ArgumentParseException e, T def) throws ArgumentParseException {
        if (e.isSilenceable()) {
            return success(e.getInvalidArgName(), def, true);
        } else {
            throw e;
        }
    }

    private static final Pattern QUOTE_ESCAPE_REGEX = Pattern.compile("\\\\([\"'])");
    private static final Pattern QUOTE_START_REGEX = Pattern.compile("(?:^| )(['\"])");
    private static final String QUOTE_END_REGEX = "[^\\\\](%s)(?: |$)";

    @Override
    public String currentArgument(String argName, boolean ignoreUnclosedQuote, boolean unescape) throws ArgumentParseException {
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

        if (allUnescaped || !unescape) {
            return current;
        }

        if (syntax != null) {
            return syntax.unescape(current);
        }

        return defaultUnescape(current);
    }

    @Override
    public String unescape(String input) {
        if (syntax != null) {
            return syntax.unescape(input);
        }

        return defaultUnescape(input);
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

    @Override
    public boolean setArgOverride(String name, String value) {
        if (!this.argOverrides.containsKey(name)) {
            this.argOverrides.put(name, value);
            return true;
        }
        return false;
    }

    @Override
    public boolean advance() {
        return ++this.index < this.args.size();
    }

    @Override
    public void assertCompletelyParsed() throws ArgumentParseException {
        if (this.index < this.args.size()) {
            throw failure("...", "Too many arguments are present!", false);
        }
    }

    // Argument storage methods

    @Override
    public <T> T pop(String argName, Class<T> type) throws ArgumentParseException {
        T arg = converters.convert(this, argName, type);
        return success(argName, arg);
    }

    @Override
    public String popSubCommand() throws ArgumentParseException {
        return popString(SUBCOMMAND_ARGNAME + this.depth++);
    }

    @Override
    public String popRemainingStrings(String argName) throws ArgumentParseException {
        if (!hasMore()) {
            failure(argName, "No arguments present", true);
        }
        StringBuilder builder = new StringBuilder();
        if (hasOverride(argName)) {
            return success(argName, currentArgument(argName));
        }
        while (hasMore()) {
            for (int i = 0; i < paddings.get(index); ++i) {
                builder.append(separator);
            }
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

    // Command utility methods

    public void logAndNotify(Logger logger, CommandSender source, String message) {
        source.sendMessage(message);
        if (logger != null) {
            logger.info(message); // TODO: If and why do we want this here?
        }
    }

    // Parsed argument access methods

    @Override
    public <T> T get(String key, Class<T> type) {
        return get(key, type, null);
    }

    @Override
    public <T> T get(String key, Class<T> type, T def) {
        Object o = this.parsedArgs.get(key);

        if (o == null) {
            return def;
        } else if (type.isInstance(o)) {
            return type.cast(o);
        }
        throw new RuntimeException("Incorrect argument type " + type.getName() + " for argument " + key);
    }

    @Override
    public boolean has(String key) {
        return this.parsedArgs.containsKey(key);
    }

    @Override
    public boolean hasOverride(String key) {
        return this.argOverrides.containsKey(key);
    }

    @Override
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
