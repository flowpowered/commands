package com.flowpowered.commands.syntax;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gnu.trove.iterator.TCharIterator;
import gnu.trove.list.TCharList;
import gnu.trove.list.array.TCharArrayList;

import com.flowpowered.commands.ArgumentParseException;
import com.flowpowered.commands.CommandArguments;
import com.flowpowered.commands.CommandFlags;
import com.flowpowered.commands.CommandFlags.Flag;

public class DefaultFlagSyntax implements FlagSyntax {
    public static final Pattern LONG_FLAG_REGEX = Pattern.compile("^--(?<key>[\\w][\\w-]*)$");
    public static final Pattern SHORT_FLAG_REGEX = Pattern.compile("^-(?<key>[\\w]+)$");

    @Override
    public void parse(CommandFlags flags, CommandArguments args, String name) throws ArgumentParseException {
        int i = 0;
        String current;
        while (args.hasMore()) {
            String curArgName = CommandFlags.FLAG_ARGNAME + name + ":" + i;
            current = args.currentArgument(curArgName);
            Matcher lMatcher = LONG_FLAG_REGEX.matcher(current);
            Matcher sMatcher = SHORT_FLAG_REGEX.matcher(current);
            if (lMatcher.matches()) {
                String flagName = lMatcher.group("key");
                Flag flag = flags.getLongFlag(flagName);
                if (flag == null) {
                    return;
                }
                args.success(curArgName, current);
                parseFlagArgs(args, name, curArgName, flagName, flag);
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
                        return;
                    }
                    if (it.hasNext() && flag.getMinArgs() != 0) {
                        throw args.failure(name, "Flag " + flagName + " requires " + flag.getMinArgs() + " arguments, but none were present.", false);
                    }
                    flag.setPresent(true);
                }
                args.success(curArgName, current);
                parseFlagArgs(args, name, curArgName, String.valueOf(flagName), flag);
            } else {
                return;
            }
        }

    }

    protected void parseFlagArgs(CommandArguments args, String name, String curArgName, String flagName, Flag flag) throws ArgumentParseException {
        int begin = args.getIndex();
        int argNum = 0;
        while (argNum < flag.getMaxArgs() && args.hasMore()) {
            String curFlagArgName = curArgName + ":" + argNum;
            String current = args.currentArgument(curFlagArgName);
            if (LONG_FLAG_REGEX.matcher(current).matches() || SHORT_FLAG_REGEX.matcher(current).matches()) {
                // It's next flag!
                // TODO: Also match "--" as end of flags.
                break;
            }
            // TODO: Check for overrides, and copy them to the new args somehow
            ++argNum;
            args.success(curFlagArgName, current);
        }
        if (argNum < flag.getMinArgs()) {
            throw args.failure(name, "Flag " + flagName + " requires " + flag.getMinArgs() + " arguments, but only " + argNum + " was present.", false);
        }
        CommandArguments subArgs = args.subArgs(begin, args.getIndex());
        flag.setArgs(subArgs);
        // TODO: Put the flag itself in the CommandArguments as an already parsed arg?
    }

    public static final DefaultFlagSyntax INSTANCE = new DefaultFlagSyntax();
}
