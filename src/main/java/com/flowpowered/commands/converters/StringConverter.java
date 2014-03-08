package com.flowpowered.commands.converters;

import com.flowpowered.commands.arguments.ArgumentParseException;
import com.flowpowered.commands.arguments.CommandArguments;

public class StringConverter implements ArgumentConverter {

    @Override
    public String convert(CommandArguments args, String argName, Class<?> type) throws ArgumentParseException {
        if (!String.class.isAssignableFrom(type)) {
            return null;
        }
        return args.currentArgument(argName);
    }

}
