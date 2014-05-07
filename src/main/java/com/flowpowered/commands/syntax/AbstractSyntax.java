package com.flowpowered.commands.syntax;

import java.util.Iterator;
import java.util.List;

import gnu.trove.list.TIntList;

import org.apache.commons.lang3.tuple.Pair;

public abstract class AbstractSyntax implements Syntax {

    private final String separator;

    public AbstractSyntax(String separator) {
        this.separator = separator;
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

}