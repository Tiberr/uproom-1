package ru.uproom.gate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.uproom.gate.devices.GateDevicesSet;

/**
 * Created by osipenko on 27.07.14.
 */
public class Main {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(Main.class);


    //##############################################################################################################
    //######    entry point


    public static void main(String[] args) {

        LOG.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LOG.info("Gate starting ... ");
        // spring initialization
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("applicationContext.xml");

        GateDevicesSet manager = (GateDevicesSet) ctx.getBean("deviceNetManager");
        //GateDevicesSet manager = (GateDevicesSet)ctx.getBean("zWaveHome");
        if (manager != null)
            manager.start();

    }
}
