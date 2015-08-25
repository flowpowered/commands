/*
 * This file is part of Flow Commands, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Flow Powered <https://flowpowered.com/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.flowpowered.commands;

import com.flowpowered.commands.exception.UserFriendlyCommandException;

/**
 * Thrown when an invalid argument is encountered, either from there not being enough input data or invalid input data
 */
public class InvalidCommandArgumentException extends UserFriendlyCommandException implements CommandArgumentException {
    private static final long serialVersionUID = -6994880605421981769L;
    private final String command;
    private final String invalidArgName;
    private final String reason;
    private final boolean silenceable;

    public InvalidCommandArgumentException(String command, String invalidArgName, String reason, boolean silenceable) {
        super("/" + command + " [" + invalidArgName + "] invalid: " + reason); // /command [invalidArg] invalid: reason
        this.command = command;
        this.invalidArgName = invalidArgName;
        this.reason = reason;
        this.silenceable = silenceable;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public String getInvalidArgName() {
        return invalidArgName;
    }

    @Override
    public String getReason() {
        return reason;
    }

    /**
     * Return whether this error is ever appropriate to silence.
     * <p>
     * Silencing is done by using the argument's default value in place of the invalid value. Silenceable exceptions are usually used in case the argument is missing.
     * It is not recommended to use silenceable exceptions in cases where using the default may result in the command performing an action unintended by the player.
     *
     * @return {@code true} if appropriate, {@code false} otherwise
     */
    public boolean isSilenceable() {
        return silenceable;
    }
}
