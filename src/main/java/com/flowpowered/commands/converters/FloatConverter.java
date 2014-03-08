package com.flowpowered.commands.converters;

import com.flowpowered.commands.ArgumentParseException;
import com.flowpowered.commands.CommandArguments;

public class FloatConverter implements ArgumentConverter {

    @Override
    public Float convert(CommandArguments args, String argName, Class<?> type) throws ArgumentParseException {
        if (!(Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type))) {
            return null;
        }
        String arg = args.currentArgument(argName);
        try {
            return Float.parseFloat(arg);
        } catch (NumberFormatException e) {
            throw args.failure(argName, "Input '" + arg + "' is not a float you silly!", false);
        }
    }

}
