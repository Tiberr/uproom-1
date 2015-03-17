package ru.uproom.gate.tindenetlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.uproom.gate.tindenetlib.driver.TindenetSerialPort;

/**
 * Created by osipenko on 18.02.15.
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

        TindenetSerialPort serialPort = (TindenetSerialPort) ctx.getBean("tindenetSerialPortImpl");
        if (serialPort != null) serialPort.open();
        else LOG.error("Spring not found bean");

//        // to check quality of Raspberry UART
//        SendMessage sender = new SendMessage(serialPort);
//        new Thread(sender).start();

    }

//    public static class SendMessage implements Runnable {
//
//        RkJamSerialPort serialPort;
//
//        SendMessage(RkJamSerialPort serialPort) {
//            this.serialPort = serialPort;
//        }
//
//        @Override
//        public void run() {
//            byte[] data = new byte[2];
//            data[0] = 0x00;
//            data[1] = 0x15;
//            for (int i = 0; i < 20; i++) {
//                serialPort.sendRequest(data);
//                synchronized (this) {
//                    try {
//                        wait(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        break;
//                    }
//                }
//            }
//        }
//    }

}
