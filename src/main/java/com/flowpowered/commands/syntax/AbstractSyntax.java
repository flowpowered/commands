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
package com.flowpowered.commands.syntax;

import java.util.Iterator;
import java.util.List;

import gnu.trove.list.TIntList;

import org.apache.commons.lang3.tuple.Pair;

import com.flowpowered.commands.syntax.flags.FlagSyntax;

public abstract class AbstractSyntax implements Syntax {

    private final String separator;
    private final FlagSyntax flagSyntax;

    public AbstractSyntax(String separator) {
        this(separator, null);
    }

    public AbstractSyntax(String separator, FlagSyntax defaultFlagSyntax) {
        this.separator = separator;
        this.flagSyntax = defaultFlagSyntax;
    }

    @Override
    public Pair<String, Integer> splitNoEmpties(String input, List<String> output, TIntList paddings) {
        Pair<String, Integer> unclosedQuote = split(input, output);
        paddings.clear();
        paddings.add(0);
        int i = 0;
        Iterator<String> itr = output.iterator();
        while (itr.hasNext()) {
            if (itr.next().isEmpty()) {
                if (!itr.hasNext()) {
                    break;
                }
                paddings.set(i, paddings.get(i) + 1);
                itr.remove();
            } else {
                paddings.add(0);
                ++i;
            }
        }
        return unclosedQuote;
    }

    @Override
    public String getSeparator() {
        return separator;
    }

    @Override
    public FlagSyntax getDefaultFlagSyntax() {
        return flagSyntax;
    }
}
