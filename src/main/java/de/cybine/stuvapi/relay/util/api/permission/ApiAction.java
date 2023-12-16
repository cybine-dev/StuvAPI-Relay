package de.cybine.stuvapi.relay.util.api.permission;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiAction
{
    String value( );
}
