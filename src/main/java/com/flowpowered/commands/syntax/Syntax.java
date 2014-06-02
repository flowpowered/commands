package com.flowpowered.commands.syntax;

import java.util.List;

import gnu.trove.list.TIntList;

import org.apache.commons.lang3.tuple.Pair;

public interface Syntax {

    Pair<String, Integer> splitNoEmpties(String input, List<String> output, TIntList paddings);

    Pair<String, Integer> split(String input, List<String> output);

    String unescape(String input);

    String escape(String input);

    String getSeparator();

    FlagSyntax getDefaultFlagSyntax();
}
