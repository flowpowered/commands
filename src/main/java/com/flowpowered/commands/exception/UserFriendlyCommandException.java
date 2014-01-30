package com.flowpowered.commands.exception;

import com.flowpowered.commands.CommandException;


/**
 * An exception that is expected to occur when a user invokes a command, and doesn't signify any abnormal situation.
 * <p>
 * It can occur when eg. user provided wrong arguments to the method, has insufficient permissions, provided an unreachable URL, etc.
 * <p>
 * If the command is invoked by a user, the {@link #getMessage() message} of this exception should be presented to the user.
 * 
 */
public class UserFriendlyCommandException extends CommandException {
    private static final long serialVersionUID = 8583142220176394240L;

    public UserFriendlyCommandException(String message) {
        super(message);
    }

    public UserFriendlyCommandException(Throwable cause) {
        super(cause);
    }

    public UserFriendlyCommandException(String message, Throwable cause) {
        super(message, cause);
    }

}
