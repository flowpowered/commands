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
package com.flowpowered.commands.syntax;

import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gnu.trove.iterator.TCharIterator;
import gnu.trove.list.TCharList;
import gnu.trove.list.array.TCharArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.flowpowered.math.vector.Vector2i;

import com.flowpowered.commands.Command;
import com.flowpowered.commands.CommandArguments;
import com.flowpowered.commands.CommandFlags;
import com.flowpowered.commands.CommandFlags.Flag;
import com.flowpowered.commands.CommandSender;
import com.flowpowered.commands.InvalidArgumentException;
import com.flowpowered.commands.PositionallyOverridableCommandArguments;

public class DefaultFlagSyntax implements FlagSyntax {
    public static final Pattern LONG_FLAG_REGEX = Pattern.compile("^--(?<key>[\\w][\\w-]*)$");
    public static final Pattern SHORT_FLAG_REGEX = Pattern.compile("^-(?<key>[\\w]+)$");
    public static final Pattern END_OF_FLAG_ARGS = Pattern.compile("^--");
    public static final String REGEX_GROUP_NAME = "key";

    private final boolean useEndOfFlags, useEndOfFlagArgs;
    private final StrictnessMode longFlagStrictness, shortFlagStrictness;

    /**
     * @param useEndOfFlags            whether "--" should be parsed as end of flags mark, and
     * @param useEndOfFlagArgs         whether anything starting with "--" should be treated as end of flag's arguments and not part of them, even when it's not a valid long flag.
     * @param unknownLongFlagBehavior  action to be taken when an unknown long flag is encountered
     * @param unknownShortFlagBehavior action to be taken when an unknown short flag is encountered
     */
    public DefaultFlagSyntax(boolean useEndOfFlags, boolean useEndOfFlagArgs, StrictnessMode unknownLongFlagBehavior, StrictnessMode unknownShortFlagBehavior) {
        this.useEndOfFlags = useEndOfFlags;
        this.useEndOfFlagArgs = useEndOfFlagArgs;
        this.longFlagStrictness = unknownLongFlagBehavior;
        this.shortFlagStrictness = unknownShortFlagBehavior;
    }

    @Override
    public void parse(CommandFlags flags, CommandArguments args, String name) throws InvalidArgumentException {
        int i = 0;
        while (args.hasMore()) {
            String curArgName = CommandFlags.FLAG_ARGNAME + name + ":" + i;
            Pair<String, Flag> flag = parseFlag(flags, args, name, curArgName);
            if (flag == null) {
                return;
            }
            if (flag.getRight() != null) { // Otherwise we're skipping an unknown flag due to StrictessMode.SKIP
                parseFlagArgs(args, flags, name, curArgName, flag.getLeft(), flag.getRight());
            }
            ++i;
        }

    }

    protected Pair<String, Flag> parseFlag(CommandFlags flags, CommandArguments args, String name, String curArgName) throws InvalidArgumentException {
        Pair<String, Flag> flagWithArgs;
        String current = args.currentArgument(curArgName);
        Matcher lMatcher = LONG_FLAG_REGEX.matcher(current);
        Matcher sMatcher = SHORT_FLAG_REGEX.matcher(current);
        if (lMatcher.matches()) {
            String flagName = lMatcher.group(REGEX_GROUP_NAME);
            Flag flag = flags.getLongFlag(flagName);
            if (flag == null) {
                switch (longFlagStrictness) {
                    case SKIP:
                        args.success(curArgName, current);
                        return new ImmutablePair<>(flagName, null);
                    case STOP:
                        return null;
                    case THROW:
                        throw args.failure(name, "Unknown long flag: " + flagName, false);
                }
            }
            args.success(curArgName, current);
            flagWithArgs = new ImmutablePair<>(flagName, flag);
            flag.setPresent(true);
        } else if (sMatcher.matches()) {
            String key = sMatcher.group(REGEX_GROUP_NAME);
            TCharList chars = TCharArrayList.wrap(key.toCharArray());
            TCharIterator it = chars.iterator();
            char flagName = 0;
            Flag lastFlag = null;
            while (it.hasNext()) {
                flagName = it.next();
                Flag flag = flags.getFlag(flagName);
                if (flag == null) {
                    // TODO: Add more modes for short flags, like skip if not first in word
                    switch (shortFlagStrictness) {
                        case SKIP:
                            continue;
                        case STOP:
                            // FIXME: Sets the previous short flags in the word as present, but doesn't call args.success(). Is that right?
                            return null;
                        case THROW:
                            throw args.failure(name, "Unknown short flag: " + flagName, false);
                    }
                }
                if (it.hasNext() && flag.getMinArgs() != 0) {
                    throw args.failure(name, "Flag " + flagName + " requires " + flag.getMinArgs() + " arguments, but none were present.", false);
                }
                flag.setPresent(true);
                lastFlag = flag;
            }
            args.success(curArgName, current);
            flagWithArgs = new ImmutablePair<>(String.valueOf(flagName), lastFlag);
        } else {
            if (useEndOfFlags && END_OF_FLAG_ARGS.matcher(current).matches()) {
                args.success(curArgName, current);
            }
            return null;
        }
        return flagWithArgs;
    }

    @Override
    public int complete(Command command, CommandSender sender, CommandFlags flags, CommandArguments args, String name, int cursor, List<String> candidates) throws InvalidArgumentException {
        int i = 0;
        while (args.hasMore()) {
            String curArgName = CommandFlags.FLAG_ARGNAME + name + ":" + i;
            Vector2i argPos = args.offsetToArgument(cursor);
            if (argPos.getX() > 0 || args.hasOverride(curArgName)) {
                Pair<String, Flag> flag = parseFlag(flags, args, name, curArgName);
                if (flag == null) {
                    return -2; // End of flags
                }
                if (flag.getRight() == null) {
                    // We're skipping an unknown flag due to StrictessMode.SKIP
                    ++i;
                    continue;
                }
                if (argPos.getX() > flag.getRight().getMaxArgs()) {
                    parseFlagArgs(args, flags, name, curArgName, flag.getLeft(), flag.getRight());
                } else {
                    // It MIGHT be the flag's args.
                    parseFlagArgs(args, flags, name, curArgName, flag.getLeft(), flag.getRight(), true);
                    if (flag.getRight().getArgs().remaining() >= argPos.getX()) {
                        // It IS flag's args.
                        int result = flag.getRight().complete(command, sender, args, flags, cursor, candidates);
                        if (result > -2) {
                            return result;
                        }
                        // Or, if it returned -2, they're stupid, because the cursor was in their args.
                        // But some other syntax might want them to return -2 in some cases, so we can't blame them.
                        // TODO: Log this?
                        return -1;
                    }
                }
            } else {
                // It's us!
                return completeFlag(flags, args, name, curArgName, argPos, candidates);
            }
            ++i;
        }
        // End of args
        // How exactly did we end up here?
        throw new IllegalStateException(args.error(name, "WTF? Completion request at " + cursor + " is outside of args?"));
    }

    protected int completeFlag(CommandFlags flags, CommandArguments args, String name, String curArgName, Vector2i argPos, List<String> candidates) throws InvalidArgumentException {
        flags.setCanCompleteNextArgToo(true); // Who knows, maybe it can also be a non-flag?
        String current = args.currentArgument(curArgName, true);
        Matcher sMatcher = SHORT_FLAG_REGEX.matcher(current);
        if (sMatcher.matches()) {
            String key = sMatcher.group(REGEX_GROUP_NAME);
            // TODO: Maybe make sure the previous short flags make sense?
            Flag f = flags.getFlag(key.charAt(key.length() - 1)); // the last flag
            if (f == null && shortFlagStrictness != StrictnessMode.SKIP) {
                return -1; // In case you wonder: no, it's not -2, because it's us that were hit by the cursor, not the next arg.
            }
            TreeSet<String> potentialCandidates = new TreeSet<>();
            if (f == null || f.getMinArgs() == 0) {
                for (char c : flags.getShortFlags().keys()) {
                    potentialCandidates.add(String.valueOf(c));
                }
            }
            if (f != null && f.getMaxArgs() > 0) {
                potentialCandidates.add(""); // We can as well move on to the next argument now.
            }
            // TODO: Maybe don't add space after short flag completion?
            return args.complete(curArgName, argPos, potentialCandidates, argPos.getY(), candidates);
        } else {
            TreeSet<String> potentialCandidates = new TreeSet<>();
            for (String flagName : flags.getLongFlags().keySet()) {
                potentialCandidates.add("--" + flagName);
            }
            for (char c : flags.getShortFlags().keys()) {
                potentialCandidates.add("-" + String.valueOf(c));
            }
            return args.complete(curArgName, argPos, potentialCandidates, candidates);
        }
    }

    protected void parseFlagArgs(CommandArguments args, CommandFlags flags, String name, String curArgName, String flagName, Flag flag) throws InvalidArgumentException {
        parseFlagArgs(args, flags, name, curArgName, flagName, flag, false);
    }

    protected void parseFlagArgs(CommandArguments args, CommandFlags flags, String name, String curArgName, String flagName, Flag flag, boolean completing) throws InvalidArgumentException {
        int begin = args.getIndex();
        TIntObjectMap<String> overrides = new TIntObjectHashMap<String>();
        int argNum = 0;
        while (argNum < flag.getMaxArgs() && args.hasMore()) {
            String curFlagArgName = curArgName + ":" + argNum;
            String current = args.currentArgument(curFlagArgName, completing);
            Matcher lMatcher = LONG_FLAG_REGEX.matcher(current);
            Matcher sMatcher = SHORT_FLAG_REGEX.matcher(current);
            Matcher eMatcher = END_OF_FLAG_ARGS.matcher(current);
            if ((useEndOfFlags && eMatcher.matches()) || (useEndOfFlagArgs && eMatcher.find())
                    || (lMatcher.matches() && flags.hasFlag(lMatcher.group(REGEX_GROUP_NAME)))
                    || (sMatcher.matches() && flags.hasFlag(sMatcher.group(REGEX_GROUP_NAME).charAt(0)))) {
                // It's next flag!
                break;
            }
            if (args.hasOverride(curFlagArgName)) {
                overrides.put(argNum, args.getOverride(curFlagArgName));
            }
            ++argNum;
            args.success(curFlagArgName, current);
        }
        if (!completing && argNum < flag.getMinArgs()) {
            throw args.failure(name, "Flag " + flagName + " requires " + flag.getMinArgs() + " arguments, but only " + argNum + " was present.", false);
        }
        CommandArguments subArgs = args.subArgs(begin, args.getIndex());
        flag.setArgs(new PositionallyOverridableCommandArguments(subArgs, overrides));
        // TODO: Put the flag itself in the CommandArguments as an already parsed arg?
    }

    /**
     * Action that should be taken when an unknown flag is encountered.
     */
    public static enum StrictnessMode {
        /**
         * Causes the syntax to skip (a.k.a. ignore) the unknown flag and continue parsing further flags.
         */
        SKIP,
        /**
         * Causes the syntax to stop parsing further flags, and behave like the flags end just before the unknown flag.
         */
        STOP,
        /**
         * Causes the syntax to throw an exception produced by {@link CommandArguments#failure(String, String, boolean)}.
         */
        THROW
    }

    public static final DefaultFlagSyntax INSTANCE = new DefaultFlagSyntax(true, false, StrictnessMode.STOP, StrictnessMode.STOP);
}
