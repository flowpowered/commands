package com.flowpowered.commands.syntax;

import java.util.List;

public interface Syntax {

    String split(String input, List<String> output);

    String unescape(String input);

    String escape(String input);

    String getSeparator();
}
