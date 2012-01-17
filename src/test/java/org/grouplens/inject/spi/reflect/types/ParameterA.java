package org.grouplens.inject.spi.reflect.types;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.grouplens.inject.annotation.Parameter;

@Parameter(Integer.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParameterA { }
