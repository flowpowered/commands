package com.flowpowered.commands.exception;

import com.flowpowered.commands.Command;

public class UnknownSubcommandException extends UserFriendlyCommandException {
    private static final long serialVersionUID = 1788649361912438898L;
    private final Command parent;
    private final String commandLine;
    private final String child;

    public UnknownSubcommandException(Command parent, String commandLine, String child) {
        super("Unknown command: /" + commandLine);
        this.commandLine = commandLine;
        this.child = child;
        this.parent = parent;
    }

    public String getCommandLines() {
        return this.commandLine;
    }

    public String getChildName() {
        return this.child;
    }

    public Command getParent() {
        return this.parent;
    }
}
