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
package com.flowpowered.chat;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.flowpowered.commands.CommandSender;

public class SetChatChannel extends ChatChannel {
    private final Set<CommandSender> receivers;

    public SetChatChannel(String name) {
        super(name);
        receivers = new ConcurrentSkipListSet<>();
    }

    public SetChatChannel(String name, Set<CommandSender> receivers) {
        super(name);
        this.receivers = new HashSet<>(receivers);
    }

    @Override
    public Set<CommandSender> getReceivers() {
        return Collections.unmodifiableSet(receivers);
    }

    @Override
    public boolean isReceiver(CommandSender sender) {
        return receivers.contains(sender);
    }

    public boolean addReceiver(CommandSender sender) {
        return receivers.add(sender);
    }

    public boolean removeReceiver(CommandSender sender) {
        return receivers.remove(sender);
    }
}
