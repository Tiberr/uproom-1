package ru.uproom.libraries;

import ru.uproom.libraries.events.RkLibraryEvent;

/**
 * This interface must be implemented all classes that receive messages
 * <p/>
 * Created by osipenko on 19.03.15.
 */
public interface RkLibraryEventListener {

    public void handleEvent(RkLibraryEvent event);

}
