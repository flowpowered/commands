package com.flowpowered.commands.syntax;

import com.flowpowered.commands.ArgumentParseException;
import com.flowpowered.commands.CommandArguments;
import com.flowpowered.commands.CommandFlags;

public interface FlagSyntax {

    void parse(CommandFlags flags, CommandArguments args, String name) throws ArgumentParseException;

}
