package de.cybine.stuvapi.relay.service.action.data;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.*;

public class ActionDataDeserializer implements Converter<String, ActionData<?>>
{
    @Override
    public JavaType getInputType(TypeFactory typeFactory)
    {
        return typeFactory.constructType(String.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory)
    {
        return typeFactory.constructParametricType(ActionData.class, Object.class);
    }

    @Override
    public ActionData<?> convert(String value)
    {
        return ActionData.fromBase64(value);
    }
}
