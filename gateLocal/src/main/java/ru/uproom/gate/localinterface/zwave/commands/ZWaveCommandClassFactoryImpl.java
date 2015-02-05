package ru.uproom.gate.localinterface.zwave.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveCommandClasses;
import ru.uproom.gate.transport.domain.ClassesSearcher;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;

/**
 * Created by osipenko on 29.01.15.
 */
@Service
public class ZWaveCommandClassFactoryImpl implements ZWaveCommandClassFactory {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(ZWaveCommandClassFactoryImpl.class);

    private Map<ZWaveCommandClasses, ZWaveCommandClass> commandClasses =
            new EnumMap<>(ZWaveCommandClasses.class);


    //##############################################################################################################
    //######    constructors / destructors


    @PostConstruct
    public void init() {
        registerCommandClasses();
    }


    //##############################################################################################################
    //######    methods


    private void registerCommandClasses() {
        for (Class<?> handler : ClassesSearcher.getAnnotatedClasses(
                ZWaveCommandClassesAnnotation.class
        )) {
            ZWaveCommandClassesAnnotation annotation =
                    handler.getAnnotation(ZWaveCommandClassesAnnotation.class);
            if (annotation == null) continue;
            ZWaveCommandClass commandClass = (ZWaveCommandClass) ClassesSearcher.instantiate(handler);
            commandClasses.put(annotation.value(), commandClass);
        }

    }

    @Override
    public ZWaveCommandClass getCommandClass(byte commandClassId) {
        return commandClasses.get(ZWaveCommandClasses.getByCode(commandClassId));
    }

}
