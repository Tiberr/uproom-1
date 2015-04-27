package ru.uproom.libraries.zwave.commands;

import ru.uproom.libraries.zwave.enums.RkZWaveCommandClassNames;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by osipenko on 14.09.14.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RkZWaveCommandClassesAnnotation {
    RkZWaveCommandClassNames value();
}
