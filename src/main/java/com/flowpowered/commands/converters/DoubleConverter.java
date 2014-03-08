package com.flowpowered.commands.converters;

import com.flowpowered.commands.arguments.ArgumentParseException;
import com.flowpowered.commands.arguments.CommandArguments;

public class DoubleConverter implements ArgumentConverter {

    @Override
    public Double convert(CommandArguments args, String argName, Class<?> type) throws ArgumentParseException {
        if (!(Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type))) {
            return null;
        }
        String arg = args.currentArgument(argName);
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException e) {
            throw args.failure(argName, "Input '" + arg + "' is not a double you silly!", false);
        }
    }

}
