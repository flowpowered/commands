package com.flowpowered.commands.converters;

import com.flowpowered.commands.ArgumentParseException;
import com.flowpowered.commands.CommandArguments;
import com.flowpowered.math.vector.Vector3f;

public class Vector3Converter implements ArgumentConverter {

    @Override
    public Vector3f convert(CommandArguments args, String argName, Class<?> type) throws ArgumentParseException {
        if (!Vector3f.class.isAssignableFrom(type)) {
            return null;
        }
        String arg = args.currentArgument(argName);
        float x, y, z;
        if (arg.contains(",")) {
            String[] els = arg.split(",");
            if (els.length < 3) {
                throw args.failure(argName, "Must provide 3 coordinates", false);
            }
            int i = 0;
            try {
                x = Float.parseFloat(els[0]);
                ++i;
                y = Float.parseFloat(els[1]);
                ++i;
                z = Float.parseFloat(els[2]);
            } catch (NumberFormatException e) {
                throw args.failure(argName, "Value '" + els[i] + "' is not a coordinate you silly! ", false);
            }
        } else {
            x = args.popFloat(argName + ":x");
            y = args.popFloat(argName + ":y");
            z = args.popFloat(argName + ":z");
        }
        return new Vector3f(x, y, z);
    }

}
