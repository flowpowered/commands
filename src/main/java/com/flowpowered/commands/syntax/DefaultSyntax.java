package com.flowpowered.commands.syntax;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

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
