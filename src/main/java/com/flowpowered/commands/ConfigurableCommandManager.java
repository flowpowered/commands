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
package com.flowpowered.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.flowpowered.cerealization.config.Configuration;
import com.flowpowered.cerealization.config.ConfigurationNode;
import com.flowpowered.cerealization.config.ConfigurationNodeSource;

public class ConfigurableCommandManager extends CommandManager {
    protected static final String COMMAND_SELF_KEY = "=";
    protected static final Pattern COMMAND_NAME_PATTERN = Pattern.compile("([^.:]*)[.:](.*)");
    private final Configuration config;
    private final Multimap<String, ConfigurationNode> nodes = HashMultimap.create();
    private final Logger logger;
    private boolean readingConfig = false;

    public ConfigurableCommandManager(Configuration config) {
        this(config, LoggerFactory.getLogger("Commands.Config"));
    }

    public ConfigurableCommandManager(Configuration config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void readConfig() {
        if (this.readingConfig) {
            throw new IllegalStateException("Cannot reenter readConfig!");
        }
        this.readingConfig = true;
        final Command root = getRootCommand();
        for (Map.Entry<String, ConfigurationNode> child : this.config.getChild("root").getChildren().entrySet()) {
            try {
                checkConfig(child.getKey(), child.getValue(), root);
            } catch (Exception e) {
                this.logger.warn("Exception while reading config", e);
            }
        }
        this.readingConfig = false;
    }

    private void checkConfig(String aliasName, ConfigurationNode node, Command parent) {
        if (!node.hasChildren()) {
            List<String> alias = node.getStringList();
            if (alias != null && !alias.isEmpty()) {
                parent.overwriteAlias(aliasName, new Alias(alias, parent));
                for (String commandName : alias) {
                    // TODO: allow typing of short-names
                    parent = parent.getChild(commandName);
                }
            } else {
                String command = node.getString();
                // TODO: validate name against an alpha-numeric pattern
                if (command.split(":").length != 2) {
                    throw new UnsupportedOperationException("Commands in the command config must be in the full form of 'provider:name'");
                }
                Command c = getCommand(command);
                parent.insertChild(aliasName, c);
                parent = c;
            }
            this.nodes.put(parent.getName(), node);
            return;
        }

        // TODO: add duplication checking
        if (!node.hasChild(COMMAND_SELF_KEY)) {
            // TODO: Maybe we should create blank command to allow admins organize their command better?
            throw new IllegalStateException("Malformed configuration! Must have command at every node!");
        }
        List<String> alias = node.getChild(COMMAND_SELF_KEY).getStringList();
        if (alias != null && !alias.isEmpty()) {
            parent.overwriteAlias(aliasName, new Alias(alias, parent));
            for (String commandName : alias) {
                // TODO: allow typing of short-names
                parent = parent.getChild(commandName);
            }
        } else {
            String command = node.getChild(COMMAND_SELF_KEY).getString();
            // TODO: validate name against an alpha-numeric pattern
            if (command.split(":").length != 2) {
                throw new UnsupportedOperationException("Commands in the command config must be in the full form of 'provider:name'");
            }
            Command c = getCommand(command);
            parent.insertChild(aliasName, c);
            parent = c;
        }
        this.nodes.put(parent.getName(), node);

        for (Map.Entry<String, ConfigurationNode> child : node.getChildren().entrySet()) {
            if (!child.getKey().equals(COMMAND_SELF_KEY)) {
                try {
                    checkConfig(child.getKey(), child.getValue(), parent);
                } catch (Exception e) {
                    this.logger.warn("Exception while reading config", e);
                }
            }
        }
    }

    @Override
    public void onCommandChildChange(Command parent, String nodeName, Command before, Command after) {
        if (this.readingConfig || before == after) {
            return;
        }
        this.nodes.put(getRootCommand().getName(), this.config.getNode("root"));
        String name = parent.getName();
        String beforeName = before == null ? null : before.getName();
        String afterName = after == null ? null : after.getName();
        Collection<ConfigurationNode> commandNodes = this.nodes.get(name);

        if (beforeName != null) {
            List<Map<String, Object>> lostChildren = new LinkedList<>();
            for (Iterator<ConfigurationNode> it = this.nodes.get(beforeName).iterator(); it.hasNext();) {
                ConfigurationNode child = it.next();
                ConfigurationNodeSource node = child.getParent();
                if (commandNodes.contains(child)) {
                    if (node.hasChildren()) {
                        Map<String, Object> values = node.getValues();
                        values.remove(COMMAND_SELF_KEY);
                        lostChildren.add(values);
                    }
                    child.remove();
                    it.remove();
                }
            }
        }
        if (afterName != null) {
            for (ConfigurationNode node : commandNodes) {
                Object oldValue = node.getValue();
                ConfigurationNode child = node.getChild(nodeName, true);
                if (oldValue instanceof String) {
                    node.getChild(COMMAND_SELF_KEY, true).setValue(oldValue);
                }
                constructConfig(after, child);
            }
        }
        this.nodes.removeAll(getRootCommand().getName());
    }

    @Override
    public void onAliasChange(Command parent, String nodeName, Alias before, Alias after) {
        if (this.readingConfig || (before != null && before.equals(after))) {
            return;
        }
        this.nodes.put(getRootCommand().getName(), this.config.getNode("root"));
        String name = parent.getName();
        Collection<ConfigurationNode> commandNodes = this.nodes.get(name);

        if (before != null) {
            for (ConfigurationNode node : commandNodes) {
                node.removeChild(nodeName);
            }
        }
        if (after != null) {
            for (ConfigurationNode node : commandNodes) {
                node.getChild(nodeName, true).setValue(after.getPath());
            }
        }
    }

    private void constructConfig(Command command, ConfigurationNode node) {
        constructConfig(command, node, new HashSet<Command>());
    }

    private void constructConfig(Command command, ConfigurationNode node, Set<Command> visited) {
        if (visited.contains(command)) {
            this.logger.warn("Found a cycle in command structure at command \"" + command.getName() + "\"");
            return;
        }
        visited.add(command);
        if (command.getChildren().isEmpty()) {
            node.setValue(command.getName());
        } else {
            for (Map.Entry<String, Alias> entry : command.getAliases().entrySet()) {
                ConfigurationNode child = node.getChild(entry.getKey(), true);
                child.setValue(new ArrayList<>(entry.getValue().getPath()));
            }
            for (Map.Entry<String, Command> entry : command.getChildren().entrySet()) {
                ConfigurationNode child = node.getChild(entry.getKey(), true);
                constructConfig(entry.getValue(), child, new HashSet<>(visited));
            }
            node.getChild(COMMAND_SELF_KEY, true).setValue(command.getName());
        }

        this.nodes.put(command.getName(), node);
    }
}
