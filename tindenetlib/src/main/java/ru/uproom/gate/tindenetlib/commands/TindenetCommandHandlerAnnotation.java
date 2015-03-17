package ru.uproom.gate.tindenetlib.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by osipenko on 14.09.14.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TindenetCommandHandlerAnnotation {
    TindenetCommandID value();
}
