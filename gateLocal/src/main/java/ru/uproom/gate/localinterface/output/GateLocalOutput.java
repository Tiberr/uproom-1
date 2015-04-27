package ru.uproom.gate.localinterface.output;

import ru.uproom.gate.transport.command.Command;
import ru.uproom.gate.transport.dto.DeviceDTO;
import ru.uproom.libraries.zwave.devices.RkZWaveDevicePool;
import ru.uproom.libraries.zwave.driver.RkZWaveDriver;

import java.util.List;

/**
 * Created by osipenko on 29.12.14.
 */
public interface GateLocalOutput {

    public void setListDTO(List<DeviceDTO> devices);

    public void setCommandFromUnit(Command command);

    public RkZWaveDriver getDataHandler();

    public RkZWaveDevicePool getDevicePool();

}
