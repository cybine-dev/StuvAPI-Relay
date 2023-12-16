package de.cybine.stuvapi.relay.service.action.data;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.*;

public class ActionDataSerializer implements Converter<ActionData<?>, String>
{
    @Override
    public JavaType getInputType(TypeFactory typeFactory)
    {
        return typeFactory.constructParametricType(ActionData.class, Object.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory)
    {
        return typeFactory.constructType(String.class);
    }

    @Override
    public String convert(ActionData<?> value)
    {
        return value.toBase64();
    }
}
