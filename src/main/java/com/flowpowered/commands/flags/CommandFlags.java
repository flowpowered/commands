/*
 * This file is part of Flow Commands, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Flow Powered <https://flowpowered.com/>
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
package com.flowpowered.commands.flags;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gnu.trove.TCollections;
import gnu.trove.iterator.TCharIterator;
import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.hash.TCharObjectHashMap;

import com.flowpowered.commands.Command;
import com.flowpowered.commands.CommandArguments;
import com.flowpowered.commands.CommandSender;
import com.flowpowered.commands.InvalidCommandArgumentException;
import com.flowpowered.commands.syntax.Syntax;
import com.flowpowered.commands.syntax.flags.DefaultFlagSyntax;
import com.flowpowered.commands.syntax.flags.FlagSyntax;

/**
 * A registry of possible long and short flags parsed from the same position in the {@link CommandArguments}
 */
public class CommandFlags {
    public static final String FLAG_ARGNAME = "flags.";

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

    public CommandFlags add(Collection<Flag> flags) {
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

    public void addAll(CommandFlags flags) {
        Set<Flag> uniqueFlags = new HashSet<>();
        uniqueFlags.addAll(flags.getLongFlags().values());
        uniqueFlags.addAll(flags.getShortFlags().valueCollection());
        add(uniqueFlags);
    }

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
     * @throws InvalidCommandArgumentException if an invalid flag is provided
     */
    public void parse(CommandArguments args, String argName) throws InvalidCommandArgumentException {
        FlagSyntax syntax = findSyntax(args);
        syntax.parse(this, args, argName);
        args.success(argName, this, true); // This is "fallback value" so that we don't advance the CommandArgument's index. FlagSyntax has already advanced it as many times as needed.
    }

    /**
     * @param command
     * @param sender
     * @param args
     * @param argName
     * @param cursor
     * @param candidates
     * @return the position in the commandline to which completion will be relative, or -1 if can't complete, or -2 if all the flags were specified, and the command should parse/complete further arguments
     * @throws InvalidCommandArgumentException
     */
    public int complete(Command command, CommandSender sender, CommandArguments args, String argName, int cursor, List<String> candidates) throws InvalidCommandArgumentException {
        FlagSyntax syntax = findSyntax(args);
        return syntax.complete(command, sender, this, args, argName, cursor, candidates);
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
        return add(new Flag(names, new char[0], 0, 0));
    }

    public CommandFlags b(char... shortNames) {
        return add(new Flag(new String[0], shortNames, 0, 0));
    }

    public CommandFlags b(String longName, char shortName) {
        return add(new Flag(new String[] { longName }, new char[] { shortName }, 0, 0));
    }

    public CommandFlags v(String... names) {
        return add(new Flag(names, new char[0], 1, 1));
    }

    public CommandFlags v(char... shortNames) {
        return add(new Flag(new String[0], shortNames, 1, 1));
    }

    public CommandFlags v(String longName, char shortName) {
        return add(new Flag(new String[] { longName }, new char[] { shortName }, 1, 1));
    }

    public CommandFlags f(int minArgs, int maxArgs, char... shortNames) {
        return add(new Flag(new String[0], shortNames, minArgs, maxArgs));
    }

    public CommandFlags f(int minArgs, int maxArgs, String... names) {
        return add(new Flag(names, new char[0], minArgs, maxArgs));
    }

    public CommandFlags f(int minArgs, int maxArgs, String longName, char shortName) {
        return add(new Flag(new String[] { longName }, new char[] { shortName }, minArgs, maxArgs));
    }
}
