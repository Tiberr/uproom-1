package libraries.api;

/**
 * Created by osipenko on 04.04.15.
 */
public interface RkLibraryDriver {

    public int create();

    public void destroy();

    public void setLibraryManager(RkLibraryManager libraryManager);

    public void requestDeviceList();

    public void applyDeviceParameters();

}
