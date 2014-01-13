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

import java.util.HashSet;
import java.util.Set;

import com.flowpowered.permissions.PermissionDomain;
import com.flowpowered.permissions.PermissionSubject;

public class PermissionChatChannel extends ChatChannel {
    private final PermissionDomain domain;
    private final String permission;

    public PermissionChatChannel(String name, String permission, PermissionDomain domain) {
        super(name);
        this.domain = domain;
        this.permission = permission;
    }

    @Override
    public Set<ChatReceiver> getReceivers() {
        Set<PermissionSubject> permitted = this.domain.getAllWithPermission(this.permission);
        Set<ChatReceiver> receivers = new HashSet<ChatReceiver>();
        // FIXME: Optimize this - if we find a quick way of comparing sets, we can set up some kind of cache, and use cached receiver list if the subject list is the same.
        for (PermissionSubject subject : permitted) {
            if (subject instanceof ChatReceiver) {
                receivers.add((ChatReceiver) subject);
            }
        }
        return receivers;
    }

    @Override
    public boolean isReceiver(ChatReceiver sender) {
        return sender.hasPermission(this.permission, this.domain);
    }
}
