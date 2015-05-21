package libraries.api;

import java.util.List;

/**
 * Created by osipenko on 05.05.15.
 */
public interface RkLibraryManager {

    public void eventLibraryReady(boolean ready);

    public void eventSendDeviceList(List<RkLibraryDevice> devices);

}
