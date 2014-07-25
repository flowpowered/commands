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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gnu.trove.TCharCollection;
import gnu.trove.TCollections;
import gnu.trove.iterator.TCharIterator;
import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.hash.TCharObjectHashMap;
import gnu.trove.set.TCharSet;
import gnu.trove.set.hash.TCharHashSet;

import com.flowpowered.math.vector.Vector2i;

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
        private final Set<String> longNames;
        private final TCharSet shortNames;
        private final int minArgs;
        private final int maxArgs;
        private CommandArguments args;
        private boolean present;
        private FlagArgCompleter completer;

        public Flag(String[] longNames, char[] shortNames, int minArgs, int maxArgs) {
            this.minArgs = minArgs;
            this.maxArgs = maxArgs;
            this.longNames = new HashSet<>();
            Collections.addAll(this.longNames, longNames);
            this.shortNames = new TCharHashSet(shortNames);
        }

        public Flag(Collection<String> longNames, TCharCollection shortNames, int minArgs, int maxArgs) {
            this.minArgs = minArgs;
            this.maxArgs = maxArgs;
            this.longNames = new HashSet<>(longNames);
            this.shortNames = new TCharHashSet(shortNames);
        }

        public Set<String> getLongNames() {
            return Collections.unmodifiableSet(longNames);
        }

        public TCharSet getShortNames() {
            return TCollections.unmodifiableSet(shortNames);
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

        public FlagArgCompleter getCompleter() {
            return completer;
        }

        public void setCompleter(FlagArgCompleter completer) {
            this.completer = completer;
        }

        public int complete(Command command, CommandSender sender, CommandArguments args, CommandFlags flags, int cursor, List<String> candidates) {
            if (completer == null) {
                return -1;
            }
            return completer.complete(command, sender, args, flags, this, this.args, cursor, candidates);
        }
    }

    public static interface FlagArgCompleter {
        public int complete(Command command, CommandSender sender, CommandArguments args, CommandFlags flags, Flag flag, CommandArguments flagArgs, int offset, List<String> candidates);
    }

    private final FlagSyntax syntax;
    private final FlagSyntax fallbackSyntax;
    private final Map<String, Flag> longFlags = new HashMap<>();
    private final TCharObjectMap<Flag> shortFlags = new TCharObjectHashMap<>();
    private boolean canCompleteNextArgToo = false;

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
            TCharIterator it = f.getShortNames().iterator();
            while (it.hasNext()) {
                this.shortFlags.put(it.next(), f);
            }
        }
        return this;
    }

    // TODO: Add a way to sum/combine two sets of flags

    protected FlagSyntax findSyntax(CommandArguments args) {
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
        return syntax;
    }

    /**
     * Parse flags from the passed {@link CommandArguments} instance
     *
     * @throws ArgumentParseException if an invalid flag is provided
     */
    public void parse(CommandArguments args, String argName) throws ArgumentParseException {
        FlagSyntax syntax = findSyntax(args);
        syntax.parse(this, args, argName);
        args.success(argName, this, true); // This is "fallback value" so that we don't advance the CommandArgument's index. FlagSyntax has already advanced it as many times as needed.
    }

    /**
     * @param args
     * @param argName
     * @param argNumber
     * @param offset
     * @param candidates
     * @return the position in the commandline to which completion will be relative, or -1 if can't complete, or -2 if all the flags were specified, and the command should parse/complete further arguments
     * @throws ArgumentParseException 
     */
    public int complete(Command command, CommandSender sender, CommandArguments args, String argName, int argNumber, int offset, List<String> candidates) throws ArgumentParseException {
        FlagSyntax syntax = findSyntax(args);
        return syntax.complete(command, sender, this, args, argName, args.argumentToOffset(new Vector2i(argNumber, offset)), candidates);
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

    public Map<String, Flag> getLongFlags() {
        return Collections.unmodifiableMap(longFlags);
    }

    public TCharObjectMap<Flag> getShortFlags() {
        return TCollections.unmodifiableMap(shortFlags);
    }

    public void setCanCompleteNextArgToo(boolean can) {
        canCompleteNextArgToo = can;
    }

    public boolean getCanCompleteNextArgToo() {
        return canCompleteNextArgToo;
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
