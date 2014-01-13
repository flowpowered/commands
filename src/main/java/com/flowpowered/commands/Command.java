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
package com.flowpowered.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.flowpowered.commands.filter.CommandFilter;
import com.flowpowered.commons.Named;

public class Command implements Named {
    private final String name;
    private final String simpleName;
    protected final ReadWriteLock childLock = new ReentrantReadWriteLock();
    protected final ReadWriteLock aliasLock = new ReentrantReadWriteLock();
    private final CommandManager manager;
    private final Map<String, Command> children = new HashMap<>();
    private final Map<String, Alias> aliases = new HashMap<>();
    private final SortedSet<CommandFilter> filters = new ConcurrentSkipListSet<>();
    private CommandExecutor executor;
    private String permission;
    private String help, usage, desc;

    protected Command(String name, CommandManager manager) {
        this.simpleName = getSimpleName(name);
        this.name = name;
        this.manager = manager;
    }

    /**
     * Processes this can for the specific {@link ProcessingMode}. This first applies filters, calls {@code ProcessingMode.step}, then processes children and aliases in the same way.
     *
     * @param sender the sender of the command
     * @param args the arguments passed
     * @param mode the mode in which to process
     * @throws CommandException
     */
    protected void process(CommandSender sender, CommandArguments args, ProcessingMode mode) throws CommandException {
        if (!hasPermission(sender)) {
            throw new CommandException("Not enough permissions to execute this command.");
        }

        for (CommandFilter filter : this.filters) {
            filter.validate(this, sender, args);
        }

        if (mode.step(this, sender, args)) {
            return;
        }
        processChild(sender, args, mode);
    }

    protected void processChild(CommandSender sender, CommandArguments args, ProcessingMode mode) throws CommandException {
        String childName;
        try {
            childName = args.popSubCommand();
        } catch (ArgumentParseException e) {
            return;
        }
        this.childLock.readLock().lock();
        try {
            Command child = getChild(childName);
            if (child != null) {
                child.process(sender, args, mode);
                return;
            }
        } finally {
            this.childLock.readLock().unlock();
        }
        this.aliasLock.readLock().lock();
        try {
            Alias alias = this.aliases.get(childName);
            if (alias != null) {
                alias.process(sender, args, mode);
                return;
            }
        } finally {
            this.aliasLock.readLock().unlock();
        }
    }

    /**
     * Gets the command that is the descendant of this command for a certain path.
     *
     * @param path
     * @return
     * @throws CommandException
     */
    public Command getDescendant(List<String> path) throws CommandException {
        Get getter = new Command.Get();
        process(null, new CommandArguments(path), getter);
        return getter.getCommand();
    }

    /**
     * Executes this command with the specified args, using the supplied {@link CommandExecutor}.
     *
     * @param sender
     * @param args
     * @throws CommandException
     */
    public void execute(CommandSender sender, CommandArguments args) throws CommandException {
        process(sender, args, EXECUTE);
    }

    /**
     * {@code ProcessingMode} allows different types of processing in {@code process}.
     */
    protected static interface ProcessingMode {
        /**
         * @return {@code true} if the command processing is done, {@code false} if child commands can be called
         */
        boolean step(Command command, CommandSender sender, CommandArguments args) throws CommandException;
    }

    protected static final Execute EXECUTE = new Execute();

    protected static class Execute implements ProcessingMode {
        @Override
        public boolean step(Command command, CommandSender sender, CommandArguments args) throws CommandException {
            CommandExecutor executor = command.getExecutor();
            return executor != null && executor.execute(command, sender, args);
        }
    }

    protected static class Get implements ProcessingMode {
        private Command command = null;

        @Override
        public boolean step(Command command, CommandSender sender, CommandArguments args) throws CommandException {
            if (args.hasMore()) {
                return false;
            }
            this.command = command;
            return true;
        }

        public Command getCommand() {
            return this.command;
        }
    }

    // ---------- Basic properties

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * 
     * @return the simple name of this command, void of any periods or colons.
     */
    public String getSimpleName() {
        return this.simpleName;
    }

    public CommandExecutor getExecutor() {
        return this.executor;
    }

    public void setExecutor(CommandExecutor executor) {
        this.executor = executor;
    }

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean hasPermission(CommandSender sender) {
        String permission = this.permission;
        if (permission == null || sender == null) {
            return true;
        }
        return sender.hasPermission(permission);
    }

    public String getHelp() {
        return this.help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public String getDescription() {
        return this.desc;
    }

    public void setDescription(String description) {
        this.desc = description;
    }

    public String getUsage() {
        return this.usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public Set<CommandFilter> getFilters() {
        return Collections.unmodifiableSortedSet(this.filters);
    }

    public boolean hasFilter(CommandFilter filter) {
        return this.filters.contains(filter);
    }

    public boolean addFilter(CommandFilter filter) {
        return this.filters.add(filter);
    }

    public boolean addFilters(CommandFilter... filter) {
        return this.filters.addAll(Arrays.asList(filter));
    }

    public boolean removeFilter(CommandFilter filter) {
        return this.filters.remove(filter);
    }

    public CommandManager getManager() {
        return this.manager;
    }

    /**
     * Clears the executor and filters of this command.
     */
    public void clear() {
        setExecutor(null);
        this.filters.clear();
    }

    // ---------- Children

    public Map<String, Command> getChildren() {
        return Collections.unmodifiableMap(this.children);
    }

    /**
     * This tries to return the child with the given name as smartly as possible.
     * If the full name of the command is given, it will check first if that exists. If it doesn't, it checks if the child is mapped by simple name.
     * If the short name of the command is given, it will only check for that.
     *
     * @param name
     * @return
     */
    public Command getChild(String name) {
        this.childLock.readLock().lock();
        try {
            Command get = this.children.get(name);
            try {
                return get == null ? this.children.get(getSimpleName(name)) : get;
            } catch (IllegalArgumentException e) {
            }
            return null;
        } finally {
            this.childLock.readLock().unlock();
        }
    }

    public boolean hasChild(String name) {
        this.childLock.readLock().lock();
        try {
            return this.children.containsKey(name);
        } finally {
            this.childLock.readLock().unlock();
        }
    }

    /**
     * This maps {@code command} to {@code name} as a child, moving any existing command to use its full name instead.
     *
     * @param name the name to map {@code command} to
     * @param command the command to add as a child
     */
    public void insertChild(String name, Command command) {
        if (command == null) {
            throw new IllegalArgumentException("Invalid command! Must not be null!");
        }
        if (command.getManager() != this.manager) {
            throw new IllegalArgumentException("Tried to put command from different manager.");
        }
        this.childLock.writeLock().lock();
        try {
            Command old = this.children.put(name, command);
            this.manager.onCommandChildChange(this, name, old, command);
            if (old != null && old != command) {
                this.manager.onCommandChildChange(this, old.getName(), this.children.put(old.getName(), old), old);
            }
        } finally {
            this.childLock.writeLock().unlock();
        }
    }

    /**
     * This maps {@code command} to {@code name} as a child. However, if an existing command is mapped to {@code name}, the {@code command} is not mapped.
     *
     * @param name the name to map {@code command} to
     * @param command the command to add as a child
     * @return the old command or {@code null} if none existed and {@code command} was successfully mapped
     */
    public Command addChildIfAbsent(String name, Command command) {
        if (name == null) {
            throw new IllegalArgumentException("Invalid name! Must not be null!");
        }
        if (command == null) {
            throw new IllegalArgumentException("Invalid command! Must not be null!");
        }
        if (command.getManager() != this.manager) {
            throw new IllegalArgumentException("Tried to put command from different manager.");
        }
        this.childLock.writeLock().lock();
        try {
            Command previous = this.children.get(name);
            if (previous == null) {
                this.children.put(name, command);
                this.manager.onCommandChildChange(this, name, previous, command);
            }
            return previous;
        } finally {
            this.childLock.writeLock().unlock();
        }
    }

    /**
     * This maps {@code command} to {@code name} as a child. However, if the child already exists, an {@link ChildAlreadyExistException} is thrown.
     *
     * @param name the name to map {@code command} to
     * @param command the command to add as a child
     * @throws ChildAlreadyExistException
     */
    public void addChild(String name, Command command) throws ChildAlreadyExistException {
        if (name == null) {
            throw new IllegalArgumentException("Invalid name! Must not be null!");
        }
        if (command == null) {
            throw new IllegalArgumentException("Invalid command! Must not be null!");
        }
        if (command.getManager() != this.manager) {
            throw new IllegalArgumentException("Tried to put command from different manager.");
        }
        this.childLock.writeLock().lock();
        try {
            if (this.children.get(name) != null) {
                throw new ChildAlreadyExistException("Child already exists for name: " + name + " for command: " + this.name);
            }
            this.children.put(name, command);
            this.manager.onCommandChildChange(this, name, null, command);
        } finally {
            this.childLock.writeLock().unlock();
        }
    }

    /**
     * This maps {@code command} as a child.
     * It first checks if there is a command mapped to {@code command.getSimpleName()}. If there is not, it maps {@code command} to {@code command.getSimpleName()}.
     * If there is, it maps {@code command} to {@code command.getName()}.
     *
     * @param command the command to add as a child
     */
    public void addChild(Command command) {
        if (command == null) {
            throw new IllegalArgumentException("Invalid command! Must not be null!");
        }
        if (command.getManager() != this.manager) {
            throw new IllegalArgumentException("Tried to put command from different manager.");
        }
        this.childLock.writeLock().lock();
        try {
            if (this.children.get(command.getSimpleName()) == null) {
                // TODO: logging?
                this.children.put(command.getSimpleName(), command);
                this.manager.onCommandChildChange(this, command.getSimpleName(), null, command);
            } else {
                Command old = this.children.put(command.getName(), command);
                this.manager.onCommandChildChange(this, command.getName(), old, command);
            }
        } finally {
            this.childLock.writeLock().unlock();
        }
    }

    /**
     * Removes the child with {@code name}.
     *
     * @param name
     * @return the removed child, or {@code null} if no command was mapped
     */
    public Command removeChild(String name) {
        this.childLock.writeLock().lock();
        try {
            Command old = this.children.remove(name);
            this.manager.onCommandChildChange(this, name, old, null);
            return old;
        } finally {
            this.childLock.writeLock().unlock();
        }
    }

    /**
     * Gets the alias mapped to the specific name.
     *
     * @param name
     * @return
     */
    public Alias getAlias(String name) {
        this.aliasLock.readLock().lock();
        try {
            return this.aliases.get(name);
        } finally {
            this.aliasLock.readLock().unlock();
        }
    }

    /**
     * Overwrites any existing Alias mapped to {@code name} with {@code alias}.
     *
     * @param name
     * @param alias
     */
    public void overwriteAlias(String name, Alias alias) {
        this.aliasLock.writeLock().lock();
        try {
            Alias previous = this.aliases.put(name, alias);
            this.manager.onAliasChange(this, name, previous, alias);
        } finally {
            this.aliasLock.writeLock().unlock();
        }
    }

    /**
     * This maps {@code alias} to {@code name}. However, if the child already exists, an {@link AliasAlreadyCreatedException} is thrown.
     *
     * @param name the name to map {@code alias} to
     * @param alias the alias to add
     * @throws AliasAlreadyCreatedException
     */
    public void addAlias(String name, Alias alias) throws AliasAlreadyCreatedException {
        this.aliasLock.writeLock().lock();
        try {
            Alias previous = this.aliases.get(name);
            if (previous != null && previous != alias) {
                throw new AliasAlreadyCreatedException("Alias already created for name: " + name + " for command: " + this.name);
            }
            this.aliases.put(name, alias);
            this.manager.onAliasChange(this, name, previous, alias);
        } finally {
            this.aliasLock.writeLock().unlock();
        }
    }

    /**
     * This maps {@code alias} to {@code name}. However, if an existing Alias is mapped to {@code name}, the {@code alias} is not mapped.
     *
     * @param name the name to map {@code alias} to
     * @param alias the alias to add
     * @return the old alias or {@code null} if none existed and {@code alias} was successfully mapped
     */
    public Alias addAliasIfAbsent(String name, Alias alias) {
        this.aliasLock.writeLock().lock();
        try {
            Alias previous = this.aliases.get(name);
            if (previous == null) {
                this.aliases.put(name, alias);
                this.manager.onAliasChange(this, name, previous, alias);
            }
            return previous;
        } finally {
            this.aliasLock.writeLock().unlock();
        }
    }

    /**
     * Removes the alias with {@code name}.
     *
     * @param name
     * @return the alias that was removed or {@code null} if no alias was mapped
     */
    public Alias removeAlias(String name) {
        this.aliasLock.writeLock().lock();
        try {
            Alias removed = this.aliases.remove(name);
            if (removed != null) {
                this.manager.onAliasChange(this, name, removed, null);
            }
            return removed;
        } finally {
            this.aliasLock.writeLock().unlock();
        }
    }

    public boolean hasAlias(String name) {
        this.aliasLock.readLock().lock();
        try {
            return this.aliases.containsKey(name);
        } finally {
            this.aliasLock.readLock().unlock();
        }
    }

    public Map<String, Alias> getAliases() {
        return Collections.unmodifiableMap(this.aliases);
    }

    // ---------- Object overrides

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.name).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Command)) {
            return false;
        }
        Command other = (Command) obj;
        return new EqualsBuilder().append(this.name, other.name).build();
    }

    public static String getSimpleName(String fullName) {
        String names[] = fullName.split(":");
        if (names.length != 2) {
            throw new IllegalArgumentException("Invalid command name! Must be full name!");
        }
        if (!names[1].contains(".")) {
            return names[1];
        }
        String[] path = names[1].split("\\.");
        return path[path.length - 1];
    }

    @Override
    public String toString() {
        return "Command{" + "name=" + this.name + '}';
    }

}
