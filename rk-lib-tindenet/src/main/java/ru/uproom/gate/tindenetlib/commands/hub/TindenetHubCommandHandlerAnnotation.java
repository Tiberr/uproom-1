package ru.uproom.gate.tindenetlib.commands.hub;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by osipenko on 14.09.14.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TindenetHubCommandHandlerAnnotation {
    TindenetHubCommandID value();
}
