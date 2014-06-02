package com.flowpowered.commands.syntax;

import java.util.regex.Pattern;

import com.flowpowered.commands.ArgumentParseException;
import com.flowpowered.commands.CommandArguments;
import com.flowpowered.commands.CommandFlags;

public class SpoutFlagSyntax implements FlagSyntax {
    public static final Pattern FLAG_REGEX = Pattern.compile("^-(?<key>-?[\\w]+)(?:=(?<value>.*))?$");

    @Override
    public void parse(CommandFlags flags, CommandArguments args, String name) throws ArgumentParseException {
        // TODO;
    }
    public static SpoutFlagSyntax INSTANCE = new SpoutFlagSyntax();
}
