package libraries.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by osipenko on 05.04.15.
 */
public class RkLibraryDeviceInfo {

    private Map<RkLibraryDeviceParameterName, Object> parameters = new HashMap<>();

    public Map<RkLibraryDeviceParameterName, Object> getParameters() {
        return parameters;
    }

}
