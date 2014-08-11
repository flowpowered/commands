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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handle parsing and storing values of flags for a {@link CommandArguments}
 * Flags are stored in the same map as other command arguments in the attached CommandArguments instance.
 */
public class CommandFlags {
    public static final Pattern FLAG_REGEX = Pattern.compile("^-(?<key>-?[\\w]+)(?:=(?<value>.*))?$");

    public static class Flag {
        private final String[] names;
        private final boolean value;

        public Flag(boolean value, String... names) {
            this.value = value;
            this.names = names;
        }

        public String[] getNames() {
            return names;
        }

        public boolean isValue() {
            return value;
        }

        /**
         * Create a new value flag with the specified aliases
         *
         * @param names The aliases for the flag
         * @return The flag object
         */
        public static Flag v(String... names) {
            return new Flag(true, names);
        }

        /**
         * Create a new boolean flag with the specified aliases
         *
         * @param names The aliases for the flag
         * @return The flag object
         */
        public static Flag b(String... names) {
            return new Flag(false, names);
        }
    }

    private final CommandArguments args;
    private final Map<String, Flag> flags = new HashMap<String, Flag>();

    public CommandFlags(CommandArguments args) {
        this.args = args;
    }

    public void registerFlags(Flag... flags) {
        registerFlags(Arrays.asList(flags));
    }

    public void registerFlags(List<Flag> flags) {
        for (Flag f : flags) {
            for (String name : f.getNames()) {
                this.flags.put(name, f);
            }
        }
    }

    /**
     * Parse flags from the attached {@link CommandArguments} instance
     *
     * @return Whether any flags were parsed
     * @throws ArgumentParseException if an invalid flag is provided
     */
    public boolean parse() throws ArgumentParseException {
        boolean anyFlags = false;
        int oldIndex = args.index; // Make argument index invalid when parsing flags
        args.index = args.length() + 1;
        for (Iterator<String> it = args.getLive().iterator(); it.hasNext();) {
            if (tryExtractFlags(it)) {
                anyFlags = true;
            }
        }
        args.index = oldIndex;
        return anyFlags;
    }

    /**
     * Handle a flag 'word' -- an element in the arguments list
     * May result in multiple flags
     *
     * @param it The iterator to draw the arguments from
     * @return Whether any flags were successfully parsed
     * @throws ArgumentParseException if an invalid or incomplete flag is provided
     */
    protected boolean tryExtractFlags(Iterator<String> it) throws ArgumentParseException {
        String arg = it.next();
        Matcher match = FLAG_REGEX.matcher(arg);
        if (!match.matches()) {
            return false;
        }

        String rawFlag = match.group("key");
        try {
            NumberFormat.getInstance().parse(rawFlag);
            // If it's a number, it's not a flag
            return false;
        } catch (ParseException ex) {
        }

        it.remove();

        if (rawFlag.startsWith("-")) { // Long flag in form --flag
            rawFlag = rawFlag.substring(1);
            handleFlag(it, rawFlag, match.group("value"));
        } else {
            for (char c : rawFlag.toCharArray()) {
                handleFlag(it, String.valueOf(c), null);
            }
        }
        return true;
    }

    /**
     * Handles a flag.
     * 3 flag types:
     * <ul>
     *     <li>{@code --name=value} - These flags do not have to be defined in advance, and can replace any named positional arguments</li>
     *     <li>{@code -abc [value]} - These must be defined in advance, can optionally have a value. Multiple flags can be combined in one 'word'</li>
     *     <li>{@code --name [value]} - These must be defined in advance, can optionally have a value. Each 'word' contains one multicharacter flag name</li>
     * </ul>
     *
     * @param it The iterator to source values from
     * @param name The name of the argument
     * @param value A predefined argument, for the first type of flag (shown above)
     * @throws ArgumentParseException when an invalid flag is presented.
     */
    protected void handleFlag(Iterator<String> it, String name, String value) throws ArgumentParseException {
        Flag f = flags.get(name);
        if (f == null && value == null) {
            throw args.failure(name, "Undefined flag presented", false);
        } else if (f != null) {
            name = f.getNames()[0];
        }

        if (args.has(name)) {
            throw args.failure(name, "This argument has already been provided!", false);
        }

        if (value != null) {
            args.setArgOverride(name, value);
            args.popString(name);
        } else if (f.isValue()) {
            if (!it.hasNext()) {
                throw args.failure(name, "No value for flag requiring value!", false);
            }
            args.setArgOverride(name, it.next());
            args.popString(name);
            it.remove();
        } else {
            args.setArgOverride(name, "true");
            args.popBoolean(name);
        }
    }

    public boolean hasFlag(String flag) {
        Flag f = flags.get(flag);
        if (f == null) {
            return false;
        }
        flag = f.getNames()[0];
        return args.has(flag);
    }
}
