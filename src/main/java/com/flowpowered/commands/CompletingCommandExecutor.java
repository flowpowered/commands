package com.flowpowered.commands;

import java.util.List;

public interface CompletingCommandExecutor extends CommandExecutor {
    /**
     * @param command
     * @param sender
     * @param args
     * @param argNumber
     * @param offset
     * @param candidates
     * @return the position in the commandline to which completion will be relative, or -1 if can't complete, or -2 if subcommands should be processed
     */
    public int complete(Command command, CommandSender sender, CommandArguments args, int argNumber, int offset, List<String> candidates);
}
