package ru.uproom.libraries.events;

/**
 * Created by osipenko on 05.04.15.
 */
public class RkLibraryEvent {

    private RkLibraryEventType eventType;

    public RkLibraryEvent(RkLibraryEventType eventType) {
        this.eventType = eventType;
    }

    public RkLibraryEventType getEventType() {
        return eventType;
    }
}
