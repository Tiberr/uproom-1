package ru.uproom.libraries.zwave.functions;

import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by osipenko on 14.09.14.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RkZWaveFunctionHandlerAnnotation {
    RkZWaveFunctionID value();
}
