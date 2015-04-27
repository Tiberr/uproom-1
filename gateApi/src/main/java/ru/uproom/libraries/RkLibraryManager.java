package ru.uproom.libraries;

import ru.uproom.gate.transport.dto.DeviceDTO;

/**
 * Created by osipenko on 04.04.15.
 */
public interface RkLibraryManager {

    public int create();

    public void destroy();

    public void addEventListener(RkLibraryEventListener listener);

    public void removeEventListener(RkLibraryEventListener listener);

    public void requestDeviceParameters(DeviceDTO device);

    public void applyDeviceParameters(DeviceDTO device);

}
