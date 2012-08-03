/*
 * Grapht, an open source dependency injector.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.grapht.spi.reflect;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.grouplens.grapht.InvalidBindingException;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectionPoint;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

/**
 * ReflectionDesire is an implementation of desire that contains all necessary
 * implementation to represent a desire, except that the point of injection is
 * abstracted by an {@link InjectionPoint}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ReflectionDesire implements Desire, Externalizable {
    /**
     * Return a list of desires that must satisfied in order to instantiate the
     * given type.
     *
     * @param type The class type whose dependencies will be queried
     * @return The dependency desires for the given type
     * @throws NullPointerException if the type is null
     */
    public static List<ReflectionDesire> getDesires(Class<?> type) {
        List<ReflectionDesire> desires = new ArrayList<ReflectionDesire>();

        boolean ctorFound = false;
        for (Constructor<?> ctor: type.getDeclaredConstructors()) {
            if (ctor.getAnnotation(Inject.class) != null) {
                if (!ctorFound) {
                    ctorFound = true;
                    for (int i = 0; i < ctor.getParameterTypes().length; i++) {
                        desires.add(new ReflectionDesire(new ConstructorParameterInjectionPoint(ctor, i)));
                    }
                } else {
                    // at the moment there can only be one injectable constructor
                    throw new InvalidBindingException(type, "More than one constructor with @Inject is not allowed");
                }
            }
        }
        
        // JSR 330 mandates that super class methods are injected first, so we
        // collect method injection points into a separate list and then reverse
        // it to get the ordering correct.
        List<ReflectionDesire> groupDesires = new ArrayList<ReflectionDesire>();
        
        // Must also keep track of methods overridden in the subtypes.
        Set<Signature> visitedMethods = new HashSet<Signature>();
        while(type != null) {
            for (Method m: type.getDeclaredMethods()) {
                Signature s = new Signature(m);
                if (!visitedMethods.contains(s) && m.getAnnotation(Inject.class) != null
                    && !Modifier.isStatic(m.getModifiers())) {
                    // have not seen this signature, and its an injection point
                    if (m.getParameterTypes().length > 0) {
                        for (int i = 0; i < m.getParameterTypes().length; i++) {
                            groupDesires.add(new ReflectionDesire(new SetterInjectionPoint(m, i)));
                        }
                    } else {
                        // hack to invoke no-argument injectable methods required by JSR 330
                        groupDesires.add(new ReflectionDesire(new NoArgumentInjectionPoint(m)));
                    }
                }
                // always add signature, because a subclass without @Inject
                // overrides any @Inject on the superclass's method declaration
                visitedMethods.add(s);
            }
            for (Field f: type.getDeclaredFields()) {
                if (f.getAnnotation(Inject.class) != null && !Modifier.isStatic(f.getModifiers())) {
                    // have not seen this field
                    groupDesires.add(new ReflectionDesire(new FieldInjectionPoint(f)));
                }
            }
            
            type = type.getSuperclass();
        }
        
        // after reversing this list, fields will be injected 
        // before methods as required
        Collections.reverse(groupDesires);
        desires.addAll(groupDesires);
        
        return Collections.unmodifiableList(desires);
    }
    
    // all are "final"
    private Class<?> desiredType;
    private InjectionPoint injectPoint;
    private Satisfaction satisfaction;

    /**
     * Create a ReflectionDesire that immediately wraps the given
     * InjectionPoint. The desired type equals the type declared by the
     * injection point. The created desire will have a satisfaction if the
     * injection point's type is satisfiable.
     * 
     * @param injectPoint The injection point to wrap
     * @throws NullPointerException if injectPoint is null
     */
    public ReflectionDesire(InjectionPoint injectPoint) {
        this(injectPoint.getErasedType(), injectPoint, null);
    }

    /**
     * Create a ReflectionDesire that represents the dependency for
     * <tt>desiredType</tt> that will be injected into the given InjectionPoint.
     * The optional satisfaction will satisfy this desire. If null is provided,
     * and the desired type instantiable, a ClassSatisfaction is created.
     * 
     * @param desiredType The desired type of the dependency
     * @param injectPoint The injection point of the desire
     * @param satisfaction The satisfaction satisfying this desire, if there is
     *            one
     * @throws NullPointerException if desiredType, injectPoint, or dfltSource is null
     * @throws IllegalArgumentException if desiredType is not assignable to the
     *             type of the injection point, or if the satisfaction's type is
     *             not assignable to the desired type
     */
    public ReflectionDesire(Class<?> desiredType, InjectionPoint injectPoint,
                            Satisfaction satisfaction) {
        Preconditions.notNull("desired type", desiredType);
        Preconditions.notNull("injection point", injectPoint);

        desiredType = Types.box(desiredType);
        Preconditions.isAssignable(injectPoint.getErasedType(), desiredType);
        if (satisfaction != null) {
            Preconditions.isAssignable(desiredType, satisfaction.getErasedType());
        }

        // try and find a satisfaction
        if (satisfaction == null) {
            if (Types.isInstantiable(desiredType)) {
                satisfaction = new ClassSatisfaction(desiredType);
            } else if (injectPoint.isNullable()) {
                satisfaction = new NullSatisfaction(desiredType);
            }
        }

        this.desiredType = desiredType;
        this.injectPoint = injectPoint;
        this.satisfaction = satisfaction;
    }
    
    /**
     * Constructor required by {@link Externalizable}.
     */
    public ReflectionDesire() { }
    
    @Override
    public Class<?> getDesiredType() {
        return desiredType;
    }

    @Override
    public InjectionPoint getInjectionPoint() {
        return injectPoint;
    }

    @Override
    public boolean isInstantiable() {
        return satisfaction != null;
    }

    @Override
    public Satisfaction getSatisfaction() {
        return satisfaction;
    }
    
    @Override
    public Desire restrict(Class<?> type) {
        return new ReflectionDesire(type, injectPoint, null);
    }
    
    @Override
    public Desire restrict(Satisfaction satis) {
        return new ReflectionDesire(satis.getErasedType(), injectPoint, satis);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReflectionDesire)) {
            return false;
        }
        ReflectionDesire r = (ReflectionDesire) o;
        return (r.desiredType.equals(desiredType) && 
                r.injectPoint.equals(injectPoint) && 
                (r.satisfaction == null ? satisfaction == null : r.satisfaction.equals(satisfaction)));
    }

    @Override
    public int hashCode() {
        return desiredType.hashCode() ^ injectPoint.hashCode() ^ (satisfaction == null ? 0 : satisfaction.hashCode());
    }

    @Override
    public String toString() {
        return "Desire(" + desiredType.getSimpleName() + ", " + injectPoint + ")";
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        desiredType = Types.readClass(in);
        
        injectPoint = (InjectionPoint) in.readObject();
        satisfaction = (Satisfaction) in.readObject();
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Types.writeClass(out, desiredType);
        
        out.writeObject(injectPoint);
        out.writeObject(satisfaction);
    }
    
    /*
     * Internal class to track a methods signature. Java's default reflection
     * doesn't give us a convenient way to record just this information.
     */
    public static class Signature {
        private final String name;
        private final Type[] args;
        
        public Signature(Method m) {
            int mods = m.getModifiers();
            if (Modifier.isPublic(mods) || Modifier.isProtected(mods)) {
                // method overrides depends solely on method name
                name = m.getName();
            } else if (Modifier.isPrivate(mods)) {
                // method overrides depend on method name and class name
                name = m.getName() + m.getDeclaringClass().getCanonicalName();
            } else {
                // method overrides depend on method name and package,
                // since it is package-private
                name = m.getName() + m.getDeclaringClass().getPackage().getName();
            }
            args = m.getGenericParameterTypes();
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Signature)) {
                return false;
            }
            Signature s = (Signature) o;
            return s.name.equals(name) && Arrays.equals(args, s.args);
        }
        
        @Override
        public int hashCode() {
            return (name.hashCode() ^ Arrays.hashCode(args));
        }
    }
}
