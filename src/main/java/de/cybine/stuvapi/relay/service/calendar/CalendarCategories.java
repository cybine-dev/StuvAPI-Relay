package de.cybine.stuvapi.relay.service.calendar;

import lombok.*;

@Getter
@AllArgsConstructor
public enum CalendarCategories
{
    LECTURE("Lecture"), ONLINE("Online"), PRESENCE("Presence"), HYBRID("Hybrid"), EXAM("Exam"), HOLIDAY("Holiday"), BLOCKER("Blocker");

    private final String displayName;
}
