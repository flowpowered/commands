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

    @Override
    public void parse(CommandFlags flags, CommandArguments args, String name) throws InvalidArgumentException {
        int i = 0;
        while (args.hasMore()) {
            String curArgName = CommandFlags.FLAG_ARGNAME + name + ":" + i;
            Pair<String, Flag> flag = parseFlag(flags, args, name, curArgName);
            if (flag == null) {
                return;
            }
            parseFlagArgs(args, name, curArgName, flag.getLeft(), flag.getRight());
            ++i;
        }

    }

    protected Pair<String, Flag> parseFlag(CommandFlags flags, CommandArguments args, String name, String curArgName) throws InvalidArgumentException {
        Pair<String, Flag> flagWithArgs;
        String current = args.currentArgument(curArgName);
        Matcher lMatcher = LONG_FLAG_REGEX.matcher(current);
        Matcher sMatcher = SHORT_FLAG_REGEX.matcher(current);
        if (lMatcher.matches()) {
            String flagName = lMatcher.group("key");
            Flag flag = flags.getLongFlag(flagName);
            if (flag == null) {
                return null;
            }
            args.success(curArgName, current);
            flagWithArgs = new ImmutablePair<>(flagName, flag);
            flag.setPresent(true);
        } else if (sMatcher.matches()) {
            String key = sMatcher.group("key");
            TCharList chars = TCharArrayList.wrap(key.toCharArray());
            TCharIterator it = chars.iterator();
            char flagName = 0;
            Flag flag = null;
            while (it.hasNext()) {
                flagName = it.next();
                flag = flags.getFlag(flagName);
                if (flag == null) {
                    // TODO: Throw sth? or maybe continue parsing other flags?
                    return null;
                }
                if (it.hasNext() && flag.getMinArgs() != 0) {
                    throw args.failure(name, "Flag " + flagName + " requires " + flag.getMinArgs() + " arguments, but none were present.", false);
                }
                flag.setPresent(true);
            }
            args.success(curArgName, current);
            flagWithArgs = new ImmutablePair<>(String.valueOf(flagName), flag);
        } else {
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
                if (argPos.getX() > flag.getRight().getMaxArgs()) {
                    parseFlagArgs(args, name, curArgName, flag.getLeft(), flag.getRight());
                } else {
                    // It MIGHT be the flag's args.
                    parseFlagArgs(args, name, curArgName, flag.getLeft(), flag.getRight(), true);
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
            String key = sMatcher.group("key");
            // TODO: Maybe make sure the previous short flags make sense?
            Flag f = flags.getFlag(key.charAt(key.length() - 1)); // the last flag
            if (f == null) {
                return -1; // TODO: -2 maybe ?
            }
            TreeSet<String> potentialCandidates = new TreeSet<>();
            if (f.getMinArgs() <= 0) {  // TODO: Does a negative value mean anything?
                for (char c : flags.getShortFlags().keys()) {
                    potentialCandidates.add(String.valueOf(c));
                }
            }
            if (f.getMaxArgs() > 0) {
                potentialCandidates.add(""); // We can as well move on to the next argument now.
            }
            // TODO: Maybe don't add space after short flag completion?
            args.complete(curArgName, argPos, potentialCandidates, argPos.getY(), candidates);
            return args.argumentToOffset(argPos); // No part of the candidates overlaps what is already typed, so complete relatively to the cursor.
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

    protected void parseFlagArgs(CommandArguments args, String name, String curArgName, String flagName, Flag flag) throws InvalidArgumentException {
        parseFlagArgs(args, name, curArgName, flagName, flag, false);
    }

    protected void parseFlagArgs(CommandArguments args, String name, String curArgName, String flagName, Flag flag, boolean completing) throws InvalidArgumentException {
        int begin = args.getIndex();
        TIntObjectMap<String> overrides = new TIntObjectHashMap<String>();
        int argNum = 0;
        while (argNum < flag.getMaxArgs() && args.hasMore()) {
            String curFlagArgName = curArgName + ":" + argNum;
            String current = args.currentArgument(curFlagArgName, completing);
            if (LONG_FLAG_REGEX.matcher(current).matches() || SHORT_FLAG_REGEX.matcher(current).matches()) { // FIXME: This also matches negative numbers, even if they're not registered flags.
                // It's next flag!
                // TODO: Also match "--" as end of flags.
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

    public static final DefaultFlagSyntax INSTANCE = new DefaultFlagSyntax();
}
