package org.grouplens.inject.spi.reflect.types;

import org.grouplens.inject.annotation.InheritsRole;
import org.grouplens.inject.annotation.Role;

@Role
@InheritsRole(RoleA.class)
public @interface RoleB { }
