/*
 * This file is part of Flow Chat and Commands, licensed under the MIT License (MIT).
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
package com.flowpowered.chat;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class SetChatChannel extends ChatChannel {
    private final Set<ChatReceiver> receivers;

    public SetChatChannel(String name) {
        super(name);
        this.receivers = new ConcurrentSkipListSet<>();
    }

    public SetChatChannel(String name, Set<ChatReceiver> receivers) {
        super(name);
        this.receivers = new HashSet<>(receivers);
    }

    @Override
    public Set<ChatReceiver> getReceivers() {
        return Collections.unmodifiableSet(this.receivers);
    }

    @Override
    public boolean isReceiver(ChatReceiver sender) {
        return this.receivers.contains(sender);
    }

    public boolean addReceiver(ChatReceiver sender) {
        return this.receivers.add(sender);
    }

    public boolean removeReceiver(ChatReceiver sender) {
        return this.receivers.remove(sender);
    }
}
