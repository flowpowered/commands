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
package com.flowpowered.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommandManager {
    private ConcurrentMap<String, Command> allCommands = new ConcurrentHashMap<>();
    private ConcurrentMap<String, ConcurrentMap<String, Command>> commandsByProvider = new ConcurrentHashMap<>();
    private Command rootCommand;
    private final boolean caseSensitive;

    public CommandManager() {
        this(true);
    }

    public CommandManager(boolean createRoot) {
        this(createRoot, true);
    }

    public CommandManager(boolean createRoot, boolean caseSensitive) {
        if (createRoot) {
            rootCommand = getCommand("flow", "root");
        }
        this.caseSensitive = caseSensitive;
    }

    /**
     * Note: This setting concerns only command child/alias lookup. Unique command names are always case INSENSITIVE.
     * @return
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public Command getStoredCommand(String uniqueName) {
        return allCommands.get(uniqueName.toLowerCase(Locale.ENGLISH));
    }

    public void onCommandChildChange(Command parent, String nodeName, Command before, Command after) {
        // Do nothing
    }

    public void onAliasChange(Command parent, String nodeName, Alias before, Alias after) {
        // Do nothing
    }

    public Command getRootCommand() {
        return rootCommand;
    }

    public void setRootCommand(Command rootCommand) {
        this.rootCommand = rootCommand;
    }

    public Command getCommandByPath(String... path) throws CommandException {
        return getCommandByPath(Arrays.asList(path));
    }

    public Command getCommandByPath(List<String> path) throws CommandException {
        return rootCommand.getDescendant(path);
    }

    public void setPath(Command command, String... path) throws CommandException {
        setPath(Arrays.asList(path), command);
    }

    public void setPath(List<String> path, Command command) throws CommandException {
        int last = path.size() - 1;
        getCommandByPath(path.subList(0, last)).insertChild(path.get(last), command);
    }

    public Command clearPath(String... alias) throws CommandException {
        return clearPath(Arrays.asList(alias));
    }

    public Command clearPath(List<String> alias) throws CommandException {
        int last = alias.size() - 1;
        return getCommandByPath(alias.subList(0, last)).removeChild(alias.get(last));
    }

    protected Command getCommand(String fullName) {
        String[] split = fullName.split(":");
        return getCommand(split[0], split[1]);
    }

    protected Command getCommand(String provider, String name) {
        String fullName = provider + ":" + name;
        provider = provider.toLowerCase(Locale.ENGLISH);
        Command command = newCommand(fullName);
        Command old = allCommands.putIfAbsent(fullName.toLowerCase(Locale.ENGLISH), command);
        // If old is null, there wasn't such command before and we've put the new one.
        // Otherwise, there was already such command in the map, and we retrieved it w/o putting the new one in the map.
        if (old == null) {
            commandsByProvider.putIfAbsent(provider, new ConcurrentHashMap<String, Command>());
            commandsByProvider.get(provider).put(name.toLowerCase(Locale.ENGLISH), command);
            return command;
        }
        return old;
    }

    public Command getCommand(CommandProvider provider, String id) {
        return getCommand(provider.getName(), id);
    }

    protected Command newCommand(String name) {
        return new Command(name, this);
    }

    public boolean clearCommands(CommandProvider provider) {
        return clearCommands(provider.getName());
    }

    public boolean clearCommands(String provider) {
        ConcurrentMap<String, Command> commands = commandsByProvider.get(provider.toLowerCase(Locale.ENGLISH));
        if (commands == null) {
            return false;
        }
        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            entry.getValue().clear();
        }
        return true;
    }

    public void addAlias(List<String> path, List<String> destination) throws CommandException, AliasAlreadyCreatedException {
        int last = path.size() - 1;
        addAlias(getCommandByPath(path.subList(0, last)), path.get(last), destination);
    }

    public void addAlias(Command parent, String name, List<String> destination) throws AliasAlreadyCreatedException {
        Alias alias = new Alias(destination, parent);
        parent.addAlias(name, alias);
    }

    public void executeCommand(CommandSender sender, String commandString) throws CommandException {
        executeCommand(sender, new CommandArguments(commandString.split(" ")));
    }

    public void executeCommand(CommandSender sender, CommandArguments args) throws CommandException {
        rootCommand.execute(sender, args);
    }

    public String normalizeChildName(String name) {
        if (caseSensitive) {
            return name;
        }
        return name.toLowerCase(Locale.ENGLISH);
    }
}
