/*
 * This file is part of Flow Commands, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <https://spout.org/>
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
package com.flowpowered.commands.arguments;

import com.flowpowered.commands.exception.UserFriendlyCommandException;

/**
 * Thrown when an invalid argument is encountered, either from there not being enough input data or invalid input data
 */
public class ArgumentParseException extends UserFriendlyCommandException {
    private static final long serialVersionUID = -6994880605421981769L;
    private final String command;
    private final String invalidArgName;
    private final String reason;
    private final boolean silenceable;

    public ArgumentParseException(String command, String invalidArgName, String reason, boolean silenceable) {
        super("/" + command + " [" + invalidArgName + "] invalid: " + reason); // /command [invalidArg] invalid: reason
        this.command = command;
        this.invalidArgName = invalidArgName;
        this.reason = reason;
        this.silenceable = silenceable;
    }

    public String getCommand() {
        return command;
    }

    public String getInvalidArgName() {
        return invalidArgName;
    }

    public String getReason() {
        return reason;
    }

    /**
     * Return whether this error is ever appropriate to silence.
     * Reasons for choosing either value are provided in the return value section.
     *
     * @return {@code true}: User has provided invalid syntax for argument (permanent failure)<br>
     *         {@code false}: User has not provided enough arguments or other error (eg: tried to refer to offline player or unloaded world)
     */
    public boolean isSilenceable() {
        return silenceable;
    }
}
