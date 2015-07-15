package libraries.api;

import java.util.List;

/**
 * Created by osipenko on 04.04.15.
 */
public interface RkLibraryDriver {

    public int create();

    public void destroy();

    public void setLibraryManager(RkLibraryManager libraryManager);

    public List<RkLibraryDevice> getDeviceList();

    public void applyDeviceParameters();

    public void toggleControllerToAddingMode();

    public void toggleControllerToRemovingMode();

    public void interruptCurrentCommandInController();

    public void removeFailedDevice(RkLibraryDevice device);
}
