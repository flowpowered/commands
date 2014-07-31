package com.flowpowered.commands.flags;

import java.util.List;

import com.flowpowered.commands.Command;
import com.flowpowered.commands.CommandArguments;
import com.flowpowered.commands.CommandSender;

public interface FlagArgCompleter {
    public int complete(Command command, CommandSender sender, CommandArguments args, CommandFlags flags, Flag flag, CommandArguments flagArgs, int offset, List<String> candidates);
}