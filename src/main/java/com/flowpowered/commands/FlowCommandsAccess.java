package com.flowpowered.commands;

import com.flowpowered.commands.Command.ProcessingMode;

/**
 * A utility class for accessing protected members of classes in com.flowpowered.commands package.
 * I hope kitskub finds it less gross than making Command.ProcessingMode and Command.process public.
 */
public class FlowCommandsAccess {
    private FlowCommandsAccess() {
    }

    public static void process(Command command, CommandSender sender, CommandArguments args, ProcessingMode mode) throws CommandException {
        command.process(sender, args, mode);
    }

    public static void processChild(Command command, CommandSender sender, CommandArguments args, ProcessingMode mode) throws CommandException {
        command.processChild(sender, args, mode);
    }

    public static interface CommandProcessingMode extends ProcessingMode {
    }

    public static ProcessingMode EXECUTE = Command.EXECUTE;
}
