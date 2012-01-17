package org.grouplens.inject.spi.reflect.types;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.grouplens.inject.annotation.InheritsRole;
import org.grouplens.inject.annotation.Role;

@Role
@InheritsRole(RoleB.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface RoleC { }
