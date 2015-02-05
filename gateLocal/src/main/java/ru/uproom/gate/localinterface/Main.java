package ru.uproom.gate.localinterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveSerialPort;

/**
 * Created by osipenko on 23.12.14.
 */
public class Main {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(Main.class);


    //##############################################################################################################
    //######    entry point


    public static void main(String[] args) {

        LOG.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LOG.info(">>>>\tGate Local starting ... ");
        // spring initialization
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("applicationContext.xml");
        LOG.info(">>>>\tGate Local started. ");

        ZWaveSerialPort serialPort = (ZWaveSerialPort) ctx.getBean("ZWaveSerialPortImpl");
        if (serialPort != null) serialPort.open();
        else LOG.error("Spring not found bean");

    }

}
