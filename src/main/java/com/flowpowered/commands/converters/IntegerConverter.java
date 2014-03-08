package com.flowpowered.commands.converters;

import com.flowpowered.commands.arguments.ArgumentParseException;
import com.flowpowered.commands.arguments.CommandArguments;

public class IntegerConverter implements ArgumentConverter {

    @Override
    public Integer convert(CommandArguments args, String argName, Class<?> type) throws ArgumentParseException {
        if (!(Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type))) {
            return null;
        }
        String arg = args.currentArgument(argName);
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw args.failure(argName, "Input '" + arg + "' is not an integer you silly!", false);
        }
    }

}
