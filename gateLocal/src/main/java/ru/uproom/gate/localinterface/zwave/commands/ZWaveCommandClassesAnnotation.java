package ru.uproom.gate.localinterface.zwave.commands;

import ru.uproom.gate.localinterface.zwave.enums.ZWaveCommandClassNames;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by osipenko on 14.09.14.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ZWaveCommandClassesAnnotation {
    ZWaveCommandClassNames value();
}
