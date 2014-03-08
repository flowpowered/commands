package com.flowpowered.commands.converters;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.flowpowered.commands.arguments.ArgumentParseException;
import com.flowpowered.commands.arguments.CommandArguments;

public class ArgumentConverterSet {
    private Map<Class<?>, ArgumentConverter> cache = new ConcurrentHashMap<>();
    private final List<ArgumentConverter> converters;

    public ArgumentConverterSet() {
        this(Arrays.asList(new BooleanConverter(),
                new DoubleConverter(),
                new EnumConverter(),
                new FloatConverter(),
                new IntegerConverter(),
                new StringConverter(),
                new Vector3Converter()));
    }

    public ArgumentConverterSet(List<ArgumentConverter> converters) {
        this.converters = converters;
    }

    @SuppressWarnings("unchecked")
    public <T> T convert(CommandArguments args, String argName, Class<T> type) throws ArgumentParseException {
        ArgumentConverter converter = cache.get(type);
        if (converter != null) {
            return (T) converter.convert(args, argName, type);
        }
        for (ArgumentConverter cnv : converters) {
            T result = (T) cnv.convert(args, argName, type);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
