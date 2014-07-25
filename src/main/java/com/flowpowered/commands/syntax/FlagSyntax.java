package com.flowpowered.commands.syntax;

import java.util.List;

import com.flowpowered.commands.ArgumentParseException;
import com.flowpowered.commands.Command;
import com.flowpowered.commands.CommandArguments;
import com.flowpowered.commands.CommandFlags;
import com.flowpowered.commands.CommandSender;

public interface FlagSyntax {

    void parse(CommandFlags flags, CommandArguments args, String name) throws ArgumentParseException;

    int complete(Command command, CommandSender sender, CommandFlags flags, CommandArguments args, String name, int cursor, List<String> candidates) throws ArgumentParseException;

}
