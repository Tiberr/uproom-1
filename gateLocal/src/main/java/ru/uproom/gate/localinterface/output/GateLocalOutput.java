package ru.uproom.gate.localinterface.output;

import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevicePool;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveSerialDataHandler;
import ru.uproom.gate.transport.command.Command;
import ru.uproom.gate.transport.dto.DeviceDTO;

import java.util.List;

/**
 * Created by osipenko on 29.12.14.
 */
public interface GateLocalOutput {

    public void setListDTO(List<DeviceDTO> devices);

    public void setCommandFromUnit(Command command);

    public ZWaveSerialDataHandler getDataHandler();

    public ZWaveDevicePool getDevicePool();

}
