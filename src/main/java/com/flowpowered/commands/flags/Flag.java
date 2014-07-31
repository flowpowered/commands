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