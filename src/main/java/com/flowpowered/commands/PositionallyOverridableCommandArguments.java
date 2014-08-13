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
import java.util.List;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import com.flowpowered.math.vector.Vector2i;

import com.flowpowered.commands.syntax.Syntax;

public class PositionallyOverridableCommandArguments extends CommandArguments {
    private final TIntObjectMap<String> overrides = new TIntObjectHashMap<>();
    private int overrideIndexOffset = 0;

    protected PositionallyOverridableCommandArguments(List<String> args, TIntList paddings, Syntax syntax, Pair<String, Integer> unclosedQuote) {
        super(args, paddings, syntax, unclosedQuote);
    }

    protected PositionallyOverridableCommandArguments(List<String> args, TIntList paddings, Syntax syntax, Pair<String, Integer> unclosedQuote, Logger logger) {
        super(args, paddings, syntax, unclosedQuote, logger);
    }

    public PositionallyOverridableCommandArguments(CommandArguments args, TIntObjectMap<String> overrides) {
        this(args);
        this.overrides.putAll(overrides);
    }

    public PositionallyOverridableCommandArguments(CommandArguments args) {
        this(new ArrayList<>(args.getAll()), new TIntArrayList(getPaddings(args)), args.getSyntax(), copyUnclosedQuote(args.getUnclosedQuote()), args.getLogger());
    }

    @Override
    public boolean hasOverride(String key) {
        return hasOverride();
    }

    public boolean hasOverride() {
        return overrides.containsKey(getIndex() + overrideIndexOffset);
    }

    @Override
    public String getOverride(String key) {
        return getOverride();
    }

    public String getOverride() {
        return overrides.get(getIndex() + overrideIndexOffset);
    }

    @Override
    public boolean setArgOverride(String key, String value) {
        return false;
    }

    public boolean setArgOverride(int index, String value) {
        if (!overrides.containsKey(index)) {
            overrides.put(index, value);
            return true;
        }
        return false;
    }

    @Override
    public PositionallyOverridableCommandArguments subArgs(int begin, int end) {
        List<String> newArgs = new ArrayList<>(getAll().subList(begin, end));
        TIntList newPaddings = new TIntArrayList(getPaddings().subList(begin, end));
        int offset = absoluteArgumentToOffset(new Vector2i(begin, 0));
        newPaddings.set(0, offset);
        Pair<String, Integer> unclosedQuote = getUnclosedQuote();
        Pair<String, Integer> newUnclosedQuote = null;
        if (unclosedQuote != null) {
            newUnclosedQuote = new ImmutablePair<>(unclosedQuote.getLeft(), unclosedQuote.getRight());
        }
        return new PositionallyOverridableCommandArguments(newArgs, newPaddings, getSyntax(), newUnclosedQuote, getLogger());
    }

    @Override
    public <T> T success(String argName, T parsedValue, boolean fallbackValue) {
        boolean hadOverride = hasOverride();
        T result = super.success(argName, parsedValue, fallbackValue);
        if (hadOverride) {
            ++overrideIndexOffset;
        }
        return result;
    }

    protected static Pair<String, Integer> copyUnclosedQuote(Pair<String, Integer> unclosedQuote) {
        if (unclosedQuote != null) {
            return new ImmutablePair<>(unclosedQuote.getLeft(), unclosedQuote.getRight());
        }
        return null;

    }
}
