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
package com.flowpowered.commands.flags;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gnu.trove.TCharCollection;
import gnu.trove.TCollections;
import gnu.trove.set.TCharSet;
import gnu.trove.set.hash.TCharHashSet;

import com.flowpowered.commands.Command;
import com.flowpowered.commands.CommandArguments;
import com.flowpowered.commands.CommandSender;

public class Flag {
    private final Set<String> longNames;
    private final TCharSet shortNames;
    private final int minArgs;
    private final int maxArgs;
    private CommandArguments args;
    private boolean present;
    private FlagArgCompleter completer;

    public Flag(String[] longNames, char[] shortNames, int minArgs, int maxArgs) {
        if (minArgs < 0) {
            throw new IllegalArgumentException("minArgs < 0");
        }
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.longNames = new HashSet<>();
        Collections.addAll(this.longNames, longNames);
        this.shortNames = new TCharHashSet(shortNames);
    }

    public Flag(Collection<String> longNames, TCharCollection shortNames, int minArgs, int maxArgs) {
        if (minArgs < 0) {
            throw new IllegalArgumentException("minArgs < 0");
        }
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