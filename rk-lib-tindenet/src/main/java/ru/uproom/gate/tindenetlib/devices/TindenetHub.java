package ru.uproom.gate.tindenetlib.devices;

import ru.uproom.gate.tindenetlib.driver.TindenetSerialPortDataHandler;

/**
 * Created by osipenko on 21.03.15.
 */
public interface TindenetHub {

    public TindenetSerialPortDataHandler getSerialPortDataHandler();

//    public void addModule();
//    public void removeModule(int moduleId);
//
//    public void applyModuleParametersFromDTO(DeviceDTO dto);

}
