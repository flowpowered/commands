package com.flowpowered.commands.converters;

import com.flowpowered.commands.ArgumentParseException;
import com.flowpowered.commands.CommandArguments;

public class EnumConverter implements ArgumentConverter {

    @Override
    public Enum<?> convert(CommandArguments args, String argName, Class<?> type) throws ArgumentParseException {
        if (!Enum.class.isAssignableFrom(type)) {
            return null;
        }
        return convertEnum(args, argName, type);
    }

    protected <T extends Enum<T>> T convertEnum(CommandArguments args, String argName, Class<?> type) throws ArgumentParseException {
        @SuppressWarnings("unchecked")
        Class<T> enumClass = (Class<T>) type;
        String arg = args.currentArgument(argName);
        T[] constants = enumClass.getEnumConstants();
        T value;
        try {
            int index = Integer.parseInt(arg);
            if (index < 0 || index >= constants.length) {
                throw args.failure(argName, buildEnumError(enumClass), false);
            }
            value = constants[index];
        } catch (NumberFormatException e) {
            try {
                value = Enum.valueOf(enumClass, arg.toUpperCase());
            } catch (IllegalArgumentException e2) {
                throw args.failure(argName, buildEnumError(enumClass), false);
            }
        }
        return value;
    }

    private static final int MAX_ARG_FULLPRINT = 5;

    private static String buildEnumError(Class<? extends Enum<?>> enumClass) {
        Enum<?>[] constants = enumClass.getEnumConstants();
        String itemList;
        if (constants.length > MAX_ARG_FULLPRINT) {
            itemList = "an element of " + enumClass.getSimpleName();
        } else {
            boolean first = true;
            StringBuilder build = new StringBuilder();
            for (Enum<?> e : constants) {
                if (!first) {
                    build.append(", ");
                }
                build.append("'").append(e.name()).append("'");
                first = false;
            }
            itemList = build.toString();
        }
        return "Invalid " + enumClass.getSimpleName() + "; Must be 0-" + constants.length + " or " + itemList + ".";
    }
}
