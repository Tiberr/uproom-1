package ru.uproom.gate.localinterface.zwave.functions;

import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by osipenko on 14.09.14.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ZWaveFunctionHandlerAnnotation {
    ZWaveFunctionID value();
}
