package de.cybine.stuvapi.relay.service.calendar;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CalendarCategories
{
    LECTURE("Lecture"), ONLINE("Online"), PRESENCE("Presence"), HYBRID("Hybrid"), EXAM("Exam"), HOLIDAY("Holiday");

    private final String displayName;
}
