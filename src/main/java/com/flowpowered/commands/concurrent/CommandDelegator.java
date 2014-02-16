package com.flowpowered.commands.concurrent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.flowpowered.commands.Command;
import com.flowpowered.commands.CommandArguments;
import com.flowpowered.commands.CommandException;
import com.flowpowered.commands.CommandExecutor;
import com.flowpowered.commands.CommandSender;
import com.flowpowered.commands.FlowCommandsAccess;

public class CommandDelegator implements CommandExecutor {
    private final Queue<Invokation> queue = new ConcurrentLinkedQueue<>();
    private final CommandExecutor executor;

    public CommandDelegator(CommandExecutor executor) {
        this.executor = executor;
    }

    public boolean processOne() throws CommandException {
        Invokation inv = queue.poll();
        if (inv == null) {
            return false;
        }

        Command cmd = inv.getCommand();
        CommandSender sender = inv.getSender();
        CommandArguments args = inv.getArgs();

        if (!executor.execute(cmd, sender, args)) {
            FlowCommandsAccess.processChild(cmd, sender, args, FlowCommandsAccess.EXECUTE);
        }

        return true;
    }

    @Override
    public boolean execute(Command command, CommandSender sender, CommandArguments args) throws CommandException {
        queue.add(new Invokation(command, sender, args));
        return true;
    }

    public static CommandDelegator delegate(Command command) {
        CommandDelegator delegator = new CommandDelegator(command.getExecutor());
        command.setExecutor(delegator);
        return delegator;
    }

    protected static class Invokation {
        private final Command command;
        private final CommandSender sender;
        private final CommandArguments args;

        public Invokation(Command command, CommandSender sender, CommandArguments args) {
            this.command = command;
            this.sender = sender;
            this.args = args;
        }

        public Command getCommand() {
            return command;
        }

        public CommandSender getSender() {
            return sender;
        }

        public CommandArguments getArgs() {
            return args;
        }
    }
}
