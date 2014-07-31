package com.flowpowered.commands.flags;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import gnu.trove.TCharCollection;

import com.flowpowered.commands.CommandArguments;

public class MultiFlag extends Flag {
    private List<CommandArguments> allArgs = new LinkedList<>();
    private int times = 0;

    public MultiFlag(String[] longNames, char[] shortNames, int minArgs, int maxArgs) {
        super(longNames, shortNames, minArgs, maxArgs);
    }

    public MultiFlag(Collection<String> longNames, TCharCollection shortNames, int minArgs, int maxArgs) {
        super(longNames, shortNames, minArgs, maxArgs);
    }

    public List<CommandArguments> getAllArgs() {
        return Collections.unmodifiableList(allArgs);
    }

    public void addArgs(CommandArguments args) {
        allArgs.add(args);
    }

    @Override
    public void setArgs(CommandArguments args) {
        addArgs(args);
        super.setArgs(args);
    }

    public boolean setDefaultArgs(CommandArguments args) {
        if (allArgs.contains(args)) {
            super.setArgs(args);
            return true;
        }
        return false;
    }

    public int getTimesPresent() {
        return times;
    }

    @Override
    public void setPresent(boolean present) {
        if (present) {
            ++times;
        } else if (times > 0) {
            --times;
        }
    }

    @Override
    public boolean isPresent() {
        return getTimesPresent() > 0;
    }
}
