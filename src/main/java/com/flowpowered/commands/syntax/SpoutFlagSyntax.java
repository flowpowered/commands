package com.flowpowered.commands.syntax;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.flowpowered.commands.ArgumentParseException;
import com.flowpowered.commands.Command;
import com.flowpowered.commands.CommandArguments;
import com.flowpowered.commands.CommandFlags;
import com.flowpowered.commands.CommandFlags.Flag;
import com.flowpowered.commands.CommandSender;

public class SpoutFlagSyntax implements FlagSyntax {
    public static final Pattern FLAG_REGEX = Pattern.compile("^-(?<key>-?[\\w]+)(?:=(?<value>.*))?$");

    private final boolean overrideArgs;

    public SpoutFlagSyntax(boolean overrideArgs) {
        super();
        this.overrideArgs = overrideArgs;
    }

    @Override
    public void parse(CommandFlags flags, CommandArguments args, String name) throws ArgumentParseException {
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
    public int complete(Command command, CommandSender sender, CommandFlags flags, CommandArguments args, String name, int cursor, List<String> candidates) throws ArgumentParseException {
        // TODO Auto-generated method stub
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
     * @throws ArgumentParseException when an invalid flag is presented.
     */

    protected void handleFlag(CommandFlags flags, CommandArguments args, String curArgName, String name, String value) throws ArgumentParseException {
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
                f.setArgs(new CommandArguments(value));
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
            f.setArgs(new CommandArguments(value));
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
