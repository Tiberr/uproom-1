package ru.uproom.libraries.zwave.commands;

import libraries.auxilliary.ClassesSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.zwave.enums.RkZWaveCommandClassNames;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by osipenko on 29.01.15.
 */
public class RkZWaveCommandClassFactory {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(RkZWaveCommandClassFactory.class);

    private Map<RkZWaveCommandClassNames, RkZWaveCommandClass> commandClasses =
            new EnumMap<>(RkZWaveCommandClassNames.class);


    //##############################################################################################################
    //######    constructors / destructors


    public void create() {
        registerCommandClasses();
    }

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


    //-----------------------------------------------------------------------------------------------------------

    public RkZWaveCommandClass getCommandClass(int commandClassId) {
        return commandClasses.get(RkZWaveCommandClassNames.getByCode(commandClassId));
    }

}
