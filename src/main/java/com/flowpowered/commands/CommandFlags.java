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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.hash.TCharObjectHashMap;

import com.flowpowered.commands.syntax.DefaultFlagSyntax;
import com.flowpowered.commands.syntax.FlagSyntax;
import com.flowpowered.commands.syntax.Syntax;

/**
 * Handle parsing and storing values of flags for a {@link CommandArguments}
 * Flags are stored in the same map as other command arguments in the attached CommandArguments instance.
 */
public class CommandFlags {
    public static final String FLAG_ARGNAME = "flags.";

    public static class Flag {
        private final String[] longNames;
        private final char[] shortNames;
        private final int minArgs;
        private final int maxArgs;
        private CommandArguments args;
        private boolean present;

        // TODO: Sets maybe?
        public Flag(String[] longNames, char[] shortNames, int minArgs, int maxArgs) {
            this.minArgs = minArgs;
            this.maxArgs = maxArgs;
            this.longNames = longNames;
            this.shortNames = shortNames;
        }

        public String[] getLongNames() {
            return longNames;
        }

        public char[] getShortNames() {
            return shortNames;
        }

        public boolean isPresent() {
            return present;
        }

        public void setPresent(boolean present) {
            this.present = present;
        }

        public int getMaxArgs() {
            return maxArgs;
        }

        public int getMinArgs() {
            return minArgs;
        }

        public void setArgs(CommandArguments value) {
            this.args = value;
        }

        public CommandArguments getArgs() {
            return args;
        }
    }

    private final FlagSyntax syntax;
    private final FlagSyntax fallbackSyntax;
    private final Map<String, Flag> longFlags = new HashMap<>();
    private final TCharObjectMap<Flag> shortFlags = new TCharObjectHashMap<>();

    public CommandFlags() {
        this(null);
    }

    public CommandFlags(FlagSyntax syntax) {
        this(syntax, DefaultFlagSyntax.INSTANCE);
    }

    public CommandFlags(FlagSyntax syntax, FlagSyntax fallbackSyntax) {
        if (fallbackSyntax == null) {
            throw new IllegalArgumentException("The fallbacSyntax must not be null");
        }
        this.syntax = syntax;
        this.fallbackSyntax = fallbackSyntax;
    }

    public CommandFlags add(Flag... flags) {
        return add(Arrays.asList(flags));
    }

    public CommandFlags add(List<Flag> flags) {
        for (Flag f : flags) {
            for (String name : f.getLongNames()) {
                this.longFlags.put(name, f);
            }
            for (char name : f.getShortNames()) {
                this.shortFlags.put(name, f);
            }
        }
        return this;
    }

    /**
     * Parse flags from the passed {@link CommandArguments} instance
     *
     * @throws ArgumentParseException if an invalid flag is provided
     */
    public void parse(CommandArguments args, String argName) throws ArgumentParseException {
        FlagSyntax syntax = this.syntax;

        if (syntax == null) {
            Syntax cmdSyntax = args.getSyntax();
            if (cmdSyntax != null) {
                syntax = cmdSyntax.getDefaultFlagSyntax();
            }
        }
        if (syntax == null) {
            syntax = this.fallbackSyntax;
        }
        syntax.parse(this, args, argName);
        args.success(argName, this);
    }

    public boolean hasFlag(String name) {
        return longFlags.containsKey(name) || (name.length() == 1 && shortFlags.containsKey(name.charAt(0)));
    }

    public boolean hasFlag(char shortName) {
        return shortFlags.containsKey(shortName);
    }

    public Flag getLongFlag(String name) {
        return longFlags.get(name);
    }

    public Flag getFlag(String name) {
        Flag flag = longFlags.get(name);
        if (flag == null && name.length() == 1) {
            flag = shortFlags.get(name.charAt(0));
        }
        return flag;
    }

    public Flag getFlag(char shortName) {
        return shortFlags.get(shortName);
    }

    // Flag addition helpers

    public CommandFlags b(String... names) {
        Flag flag = new Flag(names, new char[0], 0, 0);
        this.add(flag);
        return this;
    }

    public CommandFlags b(char... shortNames) {
        Flag flag = new Flag(new String[0], shortNames, 0, 0);
        this.add(flag);
        return this;
    }

    public CommandFlags v(String... names) {
        Flag flag = new Flag(names, new char[0], 1, 1);
        this.add(flag);
        return this;
    }

    public CommandFlags v(char... shortNames) {
        Flag flag = new Flag(new String[0], shortNames, 1, 1);
        this.add(flag);
        return this;
    }

}
