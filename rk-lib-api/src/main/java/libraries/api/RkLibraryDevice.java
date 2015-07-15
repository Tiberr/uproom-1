package libraries.api;

import java.util.Map;

/**
 * Created by osipenko on 05.05.15.
 */
public interface RkLibraryDevice {

    public int getDeviceId();

    public RkLibraryDeviceType getDeviceType();

    public Map<RkLibraryDeviceParameterName, String> getParameterList();

    public void applyParameterList(Map<RkLibraryDeviceParameterName, String> inputParams);

}
