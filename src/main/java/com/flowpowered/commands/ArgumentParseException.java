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


/**
 * Thrown when there's a problem parsing arguments and there's a need to capture the context of the problem.
 * May be wrapped inside a (subclass of) RumtimeException if the problem is the programmer's fault.
 */
public class ArgumentParseException extends CommandException implements CommandArgumentException {
    private static final long serialVersionUID = -6994880605421981769L;
    private final String command;
    private final String invalidArgName;
    private final String reason;

    public ArgumentParseException(String command, String invalidArgName, String reason) {
        super("\"/" + command + "\" - couldn't parse argument [" + invalidArgName + "]: " + reason); // "/command" couldn't parse argument [invalidArg]: reason
        this.command = command;
        this.invalidArgName = invalidArgName;
        this.reason = reason;
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

}
