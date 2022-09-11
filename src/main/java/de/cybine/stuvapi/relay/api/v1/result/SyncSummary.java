package de.cybine.stuvapi.relay.api.v1.result;

import java.time.ZonedDateTime;
import java.util.UUID;

public record SyncSummary(UUID id, ZonedDateTime startedAt, ZonedDateTime finishedAt)
{ }
