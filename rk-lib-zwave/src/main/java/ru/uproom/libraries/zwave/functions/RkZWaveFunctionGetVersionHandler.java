package ru.uproom.libraries.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.zwave.driver.RkZWaveDriver;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;
import ru.uproom.libraries.zwave.enums.RkZWaveMessageTypes;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@RkZWaveFunctionHandlerAnnotation(value = RkZWaveFunctionID.GET_VERSION)
public class RkZWaveFunctionGetVersionHandler implements RkZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveFunctionGetVersionHandler.class);


    private String createStringFromIntSequence(int[] src, int begin) {
        if (src.length <= begin) return "";

        int i = begin;
        String str = "";
        while (i < src.length && src[i] != 0x00) {
            str += String.valueOf(src[i]);
            i++;
        }

        return str;
    }


    @Override
    public boolean execute(RkZWaveMessageTypes messageType, int[] parameters,
                           RkZWaveFunctionHandlePool pool, RkZWaveMessage request) {

        RkZWaveDriver driver = pool.getDriver();
        driver.setControllerLibraryVersion(createStringFromIntSequence(parameters, 0));

        RkZWaveFunctionID functionID = getClass().getAnnotation(RkZWaveFunctionHandlerAnnotation.class).value();
        LOG.debug("execute function : {}", functionID.name());

        driver.currentRequestReceived(functionID);

        return false;
    }

}
