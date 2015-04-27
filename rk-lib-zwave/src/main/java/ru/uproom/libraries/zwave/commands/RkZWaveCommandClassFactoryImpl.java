package ru.uproom.libraries.zwave.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.auxilliary.ClassesSearcher;
import ru.uproom.libraries.zwave.enums.RkZWaveCommandClassNames;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by osipenko on 29.01.15.
 */
public class RkZWaveCommandClassFactoryImpl implements RkZWaveCommandClassFactory {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(RkZWaveCommandClassFactory.class);

    private Map<RkZWaveCommandClassNames, RkZWaveCommandClass> commandClasses =
            new EnumMap<>(RkZWaveCommandClassNames.class);


    //##############################################################################################################
    //######    constructors / destructors


    @Override
    public void create() {
        registerCommandClasses();
    }

    @Override
    public void destroy() {
        commandClasses.clear();
    }


    //##############################################################################################################
    //######    methods


    private void registerCommandClasses() {
        for (Class<?> handler : ClassesSearcher.getAnnotatedClasses(
                RkZWaveCommandClassesAnnotation.class
        )) {
            RkZWaveCommandClassesAnnotation annotation =
                    handler.getAnnotation(RkZWaveCommandClassesAnnotation.class);
            if (annotation == null) continue;
            RkZWaveCommandClass commandClass = (RkZWaveCommandClass) ClassesSearcher.instantiate(handler);
            commandClasses.put(annotation.value(), commandClass);
        }

    }

    @Override
    public RkZWaveCommandClass getCommandClass(int commandClassId) {
        return commandClasses.get(RkZWaveCommandClassNames.getByCode(commandClassId));
    }

}
