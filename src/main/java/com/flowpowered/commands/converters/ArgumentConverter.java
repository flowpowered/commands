package com.flowpowered.commands.converters;

import com.flowpowered.commands.arguments.ArgumentParseException;
import com.flowpowered.commands.arguments.CommandArguments;

public interface ArgumentConverter {
    Object convert(CommandArguments args, String argName, Class<?> type) throws ArgumentParseException;
}
