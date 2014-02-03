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
package org.grouplens.grapht;

import junit.framework.Test;
import junit.framework.TestCase;
import org.atinject.tck.Tck;
import org.atinject.tck.auto.*;
import org.atinject.tck.auto.accessories.SpareTire;

public class TckTest extends TestCase {
    
    public static Test suite() {
        InjectorBuilder ib = new InjectorBuilder()
            .setDefaultCachePolicy(CachePolicy.NEW_INSTANCE)
            .setProviderInjectionEnabled(true);
        
        ib.bind(Car.class).to(Convertible.class);
        ib.bind(Seat.class).withQualifier(Drivers.class).to(DriversSeat.class);
        ib.bind(Seat.class).to(Seat.class);
        ib.bind(Tire.class).to(Tire.class);
        ib.bind(Engine.class).to(V8Engine.class);
        ib.bind(Tire.class).withQualifier(Names.named("spare")).to(SpareTire.class);
        
        Car car = ib.build().getInstance(Car.class);
        
        // Support for private methods, but no support for static methods,
        // that doesn't make sense with grapht's design principles
        return Tck.testsFor(car, false, true);
    }
}
