package de.cybine.stuvapi.relay.util.api.permission;

import java.security.*;
import java.util.*;

public class ApiPermission extends Permission
{
    private ApiPermission(String permission)
    {
        super(permission);
    }

    @Override
    public boolean implies(Permission permission)
    {
        return false;
    }

    @Override
    public String getActions( )
    {
        return this.getName();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        if (obj == this)
            return true;

        if (!(obj instanceof ApiPermission permission))
            return false;

        return Objects.equals(this.getName(), permission.getName());
    }

    @Override
    public int hashCode( )
    {
        return this.getName().hashCode();
    }

    public static ApiPermission of(String permission)
    {
        return new ApiPermission(permission);
    }
}
