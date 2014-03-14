package com.flowpowered.commands.syntax;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public interface Syntax {

    Pair<String, Integer> split(String input, List<String> output);

    String unescape(String input);

    String escape(String input);

    String getSeparator();
}
