package com.flowpowered.commands.converters;

import com.flowpowered.commands.ArgumentParseException;
import com.flowpowered.commands.CommandArguments;

public class BooleanConverter implements ArgumentConverter {

    @Override
    public Boolean convert(CommandArguments args, String argName, Class<?> type) throws ArgumentParseException {
        if (!(Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type))) {
            return null;
        }
        String arg = args.currentArgument(argName);
        if (!arg.equalsIgnoreCase("true") && !arg.equalsIgnoreCase("false")) {
            throw args.failure(argName, "Value '" + arg + "' is not a boolean you silly!", false);
        }
        return Boolean.parseBoolean(arg);
    }

}
