/*
 * This file is part of Flow Commands, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <http://www.spout.org/>
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
package com.flowpowered.commands.annotated;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.flowpowered.commands.Command;
import com.flowpowered.commands.CommandArguments;
import com.flowpowered.commands.CommandException;
import com.flowpowered.commands.CommandExecutor;
import com.flowpowered.commands.CommandSender;

/**
 * Allows for method-registration of commands.
 */
public final class AnnotatedCommandExecutor implements CommandExecutor {
    private final Object instance;
    private final Map<Command, Method> cmdMap;

    protected AnnotatedCommandExecutor(Object instance, Map<Command, Method> cmdMap) {
        this.instance = instance;
        this.cmdMap = cmdMap;
    }

    @Override
    public boolean execute(Command command, CommandSender sender, CommandArguments args) throws CommandException {
        Method method = cmdMap.get(command);
        if (method != null) {
            method.setAccessible(true);
            try {
                Object ret;
                // Support backwards arguments //TODO: Should we?
                if (CommandSender.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    ret = method.invoke(instance, sender, args);
                } else {
                    ret = method.invoke(instance, args, sender);
                }
                if (ret instanceof Boolean && !((Boolean) ret).booleanValue()) {
                    return false;
                }
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof CommandException) {
                    throw (CommandException) cause;
                }

                throw new WrappedCommandException(e);
            } catch (Exception e) {
                throw new WrappedCommandException(e);
            }
        }
        return true;
    }
}
