package ru.uproom.gate.localinterface.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveDriver;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveMessageTypes;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@ZWaveFunctionHandlerAnnotation(value = ZWaveFunctionID.GET_VERSION)
public class ZWaveFunctionGetVersionHandler implements ZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveFunctionGetVersionHandler.class);


    private String createStringFromByteSequence(byte[] src, int begin) {
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
    public boolean execute(ZWaveMessageTypes messageType, byte[] parameters, ZWaveFunctionHandlePool pool) {

        ZWaveDriver driver = pool.getDriver();
        driver.setControllerLibraryVersion(createStringFromByteSequence(parameters, 0));

        ZWaveFunctionID functionID = getClass().getAnnotation(ZWaveFunctionHandlerAnnotation.class).value();
        LOG.debug("execute function : {}", functionID.name());

        driver.currentRequestReceived(functionID);

        return false;
    }

}
