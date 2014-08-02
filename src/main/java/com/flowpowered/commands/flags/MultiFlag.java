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
package com.flowpowered.commands.flags;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import gnu.trove.TCharCollection;

import com.flowpowered.commands.CommandArguments;

public class MultiFlag extends Flag {
    private List<CommandArguments> allArgs = new LinkedList<>();
    private int times = 0;

    public MultiFlag(String[] longNames, char[] shortNames, int minArgs, int maxArgs) {
        super(longNames, shortNames, minArgs, maxArgs);
    }

    public MultiFlag(Collection<String> longNames, TCharCollection shortNames, int minArgs, int maxArgs) {
        super(longNames, shortNames, minArgs, maxArgs);
    }

    public List<CommandArguments> getAllArgs() {
        return Collections.unmodifiableList(allArgs);
    }

    public void addArgs(CommandArguments args) {
        allArgs.add(args);
    }

    @Override
    public void setArgs(CommandArguments args) {
        addArgs(args);
        super.setArgs(args);
    }

    public boolean setDefaultArgs(CommandArguments args) {
        if (allArgs.contains(args)) {
            super.setArgs(args);
            return true;
        }
        return false;
    }

    public int getTimesPresent() {
        return times;
    }

    @Override
    public void setPresent(boolean present) {
        if (present) {
            ++times;
        } else if (times > 0) {
            --times;
        }
    }

    @Override
    public boolean isPresent() {
        return getTimesPresent() > 0;
    }
}
