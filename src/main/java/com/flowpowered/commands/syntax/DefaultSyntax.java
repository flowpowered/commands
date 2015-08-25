/*
 * This file is part of Flow Commands, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Flow Powered <https://flowpowered.com/>
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.flowpowered.commands.syntax.flags.DefaultFlagSyntax;
import com.flowpowered.commands.syntax.flags.FlagSyntax;

public class DefaultSyntax extends AbstractSyntax {

    public DefaultSyntax() {
        super(" ", DefaultFlagSyntax.INSTANCE);
    }

    public DefaultSyntax(FlagSyntax flagSyntax) {
        super(" ", flagSyntax);
    }

    @Override
    public Pair<String, Integer> split(String input, List<String> output) {
        return parse(input, output, false, true);
    }

    @Override
    public String unescape(String input) {
        List<String> out = new ArrayList<>(1);
        parse(input, out, true, false);
        return out.get(0);
    }

    @Override
    public String escape(String input) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            switch (c) {
                case ' ':
                case '"':
                case '\'':
                case '\\':
                    out.append('\\');
            }
            out.append(c);
        }
        return out.toString();
    }

    private Pair<String, Integer> parse(String input, List<String> output, boolean unescape, boolean split) {
        StringBuilder current = new StringBuilder();
        char quoteChar = 0;
        int quoteStart = -1;
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            if (split && c == ' ' && quoteChar == 0) {
                output.add(current.toString());
                current = new StringBuilder();
                continue;
            }
            if (!unescape) {
                current.append(c);
            }
            if (c == '\\') {
                ++i;
                current.append(input.charAt(i));
                continue;
            }
            if (quoteChar != 0 && c == quoteChar) {
                quoteChar = 0;
                continue;
            }
            if (quoteChar == 0 && (c == '\'' || c == '"')) {
                quoteChar = c;
                quoteStart = i;
                continue;
            }
            if (unescape) {
                current.append(c);
            }
        }
        output.add(current.toString());
        if (quoteChar != 0) {
            return new ImmutablePair<>(String.valueOf(quoteChar), quoteStart);
        }
        return null;
    }

    public static final DefaultSyntax INSTANCE = new DefaultSyntax();

}
