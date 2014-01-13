/*
 * This file is part of Flow.
 *
 * Copyright (c) 2013 Spout LLC <http://www.spout.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.flowpowered.commands.annotated;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flowpowered.commands.Command;
import com.flowpowered.commands.CommandArguments;
import com.flowpowered.commands.CommandManager;
import com.flowpowered.commands.CommandProvider;
import com.flowpowered.commands.CommandSender;
import com.flowpowered.commands.filter.CommandFilter;

public final class AnnotatedCommandExecutorFactory {
    private final CommandManager manager;
    private final CommandProvider provider;
    private final Logger logger;

    public AnnotatedCommandExecutorFactory(CommandManager manager, CommandProvider provider) {
        this(manager, provider, LoggerFactory.getLogger("Commands.Annotated"));
    }

    public AnnotatedCommandExecutorFactory(CommandManager manager, CommandProvider provider, Logger logger) {
        this.manager = manager;
        this.logger = logger;
        this.provider = provider;
    }

    private boolean validateMethod(Method method, boolean classProcessed) {
        if (hasCommandAnnotation(method)) {
            if (Modifier.isAbstract(method.getModifiers())) {
                logger.warn("Unable to register " + method.getName() + " as a command, method can not be abstract.");
                return false;
            }
            if (classProcessed && !Modifier.isStatic(method.getModifiers())) {
                logger.warn("Unable to register " + method.getName() + " as a command, method must be static.");
                return false;
            }
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 2) {
                logger.warn("Unable to register " + method.getName() + " as a command, method can only have 2 parameters");
                return false;
            }
            if ((CommandSender.class.isAssignableFrom(params[0]) || CommandSender.class.isAssignableFrom(params[1])) &&
                    (CommandArguments.class.equals(params[0]) || CommandArguments.class.equals(params[1])))	{
                return true;
            } else {
                logger.warn("Unable to register " + method.getName() + " as a command, method parameters must be CommandSender and CommandArguments");
                return false;
            }
        }
        return false;
    }

    private boolean hasCommandAnnotation(Method method) {
        return method.isAnnotationPresent(CommandDescription.class);
    }

    private AnnotatedCommandExecutor create(Class<?> commands, Object instance, Command parent) {
        Map<Command, Method> cmdMap = new HashMap<Command, Method>();
        while (commands != null) {
            for (Method method : commands.getDeclaredMethods()) {
                method.setAccessible(true);
                // check the validity of the current method
                if (!validateMethod(method, instance == null)) {
                    continue;
                }

                // create the command
                CommandDescription a = method.getAnnotation(CommandDescription.class);
                Command command = manager.getCommand(provider, a.name());
                if (parent == null) {
                    parent = manager.getRootCommand();
                }
                parent.addChild(command);

                // set annotation data
                command.setDescription(a.desc());
                command.setHelp(a.usage());
                command.setHelp(a.help());
                // add the permissions
                if (method.isAnnotationPresent(Permissible.class)) {
                    command.setPermission(method.getAnnotation(Permissible.class).value());
                }

                if (method.isAnnotationPresent(Filter.class)) {
                    Filter cfa = method.getAnnotation(Filter.class);
                    Class<? extends CommandFilter>[] filterTypes = cfa.value();
                    CommandFilter[] filters = new CommandFilter[filterTypes.length];
                    for (int i = 0; i < filters.length; i++) {
                        try {
                            filters[i] = filterTypes[i].newInstance();
                        } catch (InstantiationException e) {
                            throw new IllegalArgumentException("All CommandFilters must have an empty constructor.");
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    command.addFilters(filters);
                }

                // put the command in our map
                cmdMap.put(command, method);
            }
            commands = commands.getSuperclass();
        }

        // set the executor of the commands
        AnnotatedCommandExecutor exe = new AnnotatedCommandExecutor(instance, cmdMap);
        for (Command cmd : cmdMap.keySet()) {
            cmd.setExecutor(exe);
        }

        return exe;
    }

    /**
     * Registers all the defined commands by method in this class.
     * @param instance the object containing the commands
     */
    public AnnotatedCommandExecutor create(Object instance) {
        return create(instance.getClass(), instance, null);
    }

    /**
     * Registers all the defined commands by method in this class.
     * @param commands the class containing the static commands
     */
    public AnnotatedCommandExecutor create(Class<?> commands) {
        return create(commands, null, null);
    }

    /**
     * Registers all the defined commands by method in this class.
     *
     * @param instance the object containing the commands
     * @param parent to register commands under
     */
    public AnnotatedCommandExecutor create(Object instance, Command parent) {
        return create(instance.getClass(), instance, parent);
    }

    /**
     * Registers all the defined commands by method in this class.
     *
     * @param commands the class containing the static commands
     * @param parent to register commands under
     */
    public AnnotatedCommandExecutor create(Class<?> commands, Command parent) {
        return create(commands, null, parent);
    }
}
