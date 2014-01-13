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

import java.util.Set;

import com.flowpowered.commons.Named;

public abstract class ChatChannel implements Named {
    public static final String MSG_TYPE_PREFIX = "chan:";
    private final String name;

    public ChatChannel(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public abstract Set<ChatReceiver> getReceivers();

    public boolean isReceiver(ChatReceiver sender) {
        return getReceivers().contains(sender);
    }

    public void broadcast(String message) {
        for (ChatReceiver receiver : getReceivers()) {
            sendMessage(message, receiver);
        }
    }

    public void broadcast(ChatReceiver from, String message) {
        // TODO: Once we have formats, use the sender somehow
        broadcast(message);
    }

    public void sendMessage(String message, ChatReceiver to) {
        to.sendMessageRaw(message, MSG_TYPE_PREFIX + this.name);
    }

}
