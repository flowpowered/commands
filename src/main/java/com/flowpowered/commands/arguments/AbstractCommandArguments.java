package com.flowpowered.commands.arguments;

import com.flowpowered.math.vector.Vector3f;

public abstract class AbstractCommandArguments implements CommandArguments {

    @Override
    public String currentArgument(String argName) throws ArgumentParseException {
        return currentArgument(argName, false);
    }

    @Override
    public String currentArgument(String argName, boolean ignoreUnclosedQuote) throws ArgumentParseException {
        return currentArgument(argName, ignoreUnclosedQuote, true);
    }

    @Override
    public <T> T pop(String argName, Class<T> type, T def) throws ArgumentParseException {
        try {
            return pop(argName, type);
        } catch (ArgumentParseException e) {
            return potentialDefault(e, def);
        }
    }

    @Override
    public String popString(String argName) throws ArgumentParseException {
        return pop(argName, String.class);
    }

    @Override
    public String popString(String argName, String def) throws ArgumentParseException {
        return pop(argName, String.class, def);
    }

    @Override
    public int popInteger(String argName) throws ArgumentParseException {
        return pop(argName, Integer.class);
    }

    @Override
    public int popInteger(String argName, int def) throws ArgumentParseException {
        return pop(argName, Integer.class, def);
    }

    @Override
    public float popFloat(String argName) throws ArgumentParseException {
        return pop(argName, Float.class);
    }

    @Override
    public float popFloat(String argName, float def) throws ArgumentParseException {
        return pop(argName, Float.class, def);
    }

    @Override
    public double popDouble(String argName) throws ArgumentParseException {
        return pop(argName, Double.class);
    }

    @Override
    public double popDouble(String argName, double def) throws ArgumentParseException {
        return pop(argName, Double.class, def);
    }

    @Override
    public boolean popBoolean(String argName) throws ArgumentParseException {
        return pop(argName, Boolean.class);
    }

    @Override
    public boolean popBoolean(String argName, boolean def) throws ArgumentParseException {
        return pop(argName, Boolean.class, def);
    }

    @Override
    public Vector3f popVector3(String argName) throws ArgumentParseException {
        return pop(argName, Vector3f.class);
    }

    @Override
    public Vector3f popVector3(String argName, Vector3f def) throws ArgumentParseException {
        return pop(argName, Vector3f.class, def);
    }

    @Override
    public <T extends Enum<T>> T popEnumValue(String argName, Class<T> enumClass) throws ArgumentParseException {
        return pop(argName, enumClass);
    }

    @Override
    public <T extends Enum<T>> T popEnumValue(String argName, Class<T> enumClass, T def) throws ArgumentParseException {
        return pop(argName, enumClass, def);
    }

    @Override
    public String popRemainingStrings(String argName, String def) throws ArgumentParseException {
        try {
            return popRemainingStrings(argName);
        } catch (ArgumentParseException e) {
            return potentialDefault(e, def);
        }
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        return get(key, type, null);
    }

    @Override
    public String getString(String key) {
        return get(key, String.class);
    }

    @Override
    public String getString(String key, String def) {
        return get(key, String.class, def);
    }

    // TODO: Add more of these?

    @Override
    public int getInteger(String key, int def) {
        return get(key, Integer.class, def);
    }

    @Override
    public float getFloat(String key, float def) {
        return get(key, Float.class, def);
    }
}
