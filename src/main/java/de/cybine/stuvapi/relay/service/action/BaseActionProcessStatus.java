package de.cybine.stuvapi.relay.service.action;

public enum BaseActionProcessStatus implements ActionProcessStatus
{
    INITIALIZED, TERMINATED;

    @Override
    public String getName( )
    {
        return this.name();
    }
}
