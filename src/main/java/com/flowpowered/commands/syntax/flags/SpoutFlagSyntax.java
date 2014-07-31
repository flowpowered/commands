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
package com.flowpowered.commands.syntax.flags;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.flowpowered.commands.Command;
import com.flowpowered.commands.CommandArguments;
import com.flowpowered.commands.flags.CommandFlags;
import com.flowpowered.commands.flags.Flag;
import com.flowpowered.commands.CommandSender;
import com.flowpowered.commands.InvalidArgumentException;

public class SpoutFlagSyntax implements FlagSyntax {
    public static final Pattern FLAG_REGEX = Pattern.compile("^-(?<key>-?[\\w]+)(?:=(?<value>.*))?$");

    private final boolean overrideArgs;

    public SpoutFlagSyntax(boolean overrideArgs) {
        super();
        this.overrideArgs = overrideArgs;
    }

    @Override
    public void parse(CommandFlags flags, CommandArguments args, String name) throws InvalidArgumentException {
        int i = 0;
        while (args.hasMore()) {
            String curArgName = CommandFlags.FLAG_ARGNAME + name + ":" + i;
            String current = args.currentArgument(curArgName);
            Matcher matcher = FLAG_REGEX.matcher(current);
            if (!matcher.matches()) {
                break;
            }
            String flagName = matcher.group("key");
            args.success(curArgName, current);
            if (flagName.startsWith("-")) { // long --flag
                flagName = flagName.substring(1);
                handleFlag(flags, args, curArgName, flagName, matcher.group("value"));
            } else {
                for (char c : flagName.toCharArray()) {
                    handleFlag(flags, args, curArgName, String.valueOf(c), null);
                }
            }
        }

    }

    @Override
    public int complete(Command command, CommandSender sender, CommandFlags flags, CommandArguments args, String name, int cursor, List<String> candidates) throws InvalidArgumentException {
        // Feel free to implement this if you care.
        // And if you found some common logic in implementation of this method and DefaultFlagSyntax.complete, and managed to refactor it out of there, that would be more than awesome.
        return -1;
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
     * @throws InvalidArgumentException when an invalid flag is presented.
     */

    protected void handleFlag(CommandFlags flags, CommandArguments args, String curArgName, String name, String value) throws InvalidArgumentException {
        Flag f = flags.getFlag(name);
        if (f == null && (value == null || !overrideArgs)) {
            throw args.failure(name, "Undefined flag presented", false);
        } else if (f != null) {
            name = f.getLongNames().iterator().next(); // FIXME: How do we know the set is consistent about it?
        }

        if (overrideArgs && args.has(name)) {
            throw args.failure(name, "This argument has already been provided, parsed, and eaten by a command!", false);
        }

        if (value != null) {
            if (overrideArgs) {
                args.setArgOverride(name, value);
                args.success(name, value, true);
            }
            if (f != null) {
                f.setArgs(new CommandArguments(args.getLogger(), value));
            }
        } else if (f.getMinArgs() > 1) {
            throw new IllegalStateException("Tried to parse a multi-argument flag with SpoutFlagSyntax");
        } else if (f.getMinArgs() == 1) {
            if (!args.hasMore()) {
                throw args.failure(name, "No value for flag requiring value!", false);
            }
            curArgName = curArgName + ":1";
            value = args.currentArgument(curArgName);
            if (overrideArgs) {
                args.setArgOverride(name, value);
                args.success(name, value, true);
            }
            f.setArgs(new CommandArguments(args.getLogger(), value));
            args.success(curArgName, value);
        } else {
            if (overrideArgs) {
                args.setArgOverride(name, "true");
                args.success(name, true, true);
            }
        }
        if (f != null) {
            f.setPresent(true);
        }
    }

    public static SpoutFlagSyntax INSTANCE = new SpoutFlagSyntax(false);
    public static SpoutFlagSyntax INSTANCE_OVERRIDING = new SpoutFlagSyntax(true);
}
