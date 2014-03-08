package com.flowpowered.commands.converters;

import com.flowpowered.commands.ArgumentParseException;
import com.flowpowered.commands.CommandArguments;

public interface ArgumentConverter {
    Object convert(CommandArguments args, String argName, Class<?> type) throws ArgumentParseException;
}
