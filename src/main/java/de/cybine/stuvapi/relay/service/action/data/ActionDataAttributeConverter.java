package de.cybine.stuvapi.relay.service.action.data;

import jakarta.persistence.*;
import lombok.extern.slf4j.*;

@Slf4j
@Converter
public class ActionDataAttributeConverter implements AttributeConverter<ActionData<?>, String>
{
    @Override
    public String convertToDatabaseColumn(ActionData<?> attribute)
    {
        if (attribute == null)
            return null;

        return attribute.toJson();
    }

    @Override
    public ActionData<?> convertToEntityAttribute(String dbData)
    {
        if(dbData == null)
            return null;

        return ActionData.fromJson(dbData);
    }
}