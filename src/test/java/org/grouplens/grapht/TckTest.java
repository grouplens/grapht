/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.grouplens.grapht;

import junit.framework.Test;
import junit.framework.TestCase;
import org.atinject.tck.Tck;
import org.atinject.tck.auto.*;
import org.atinject.tck.auto.accessories.SpareTire;

public class TckTest extends TestCase {
    
    public static Test suite() throws InjectionException {
        InjectorBuilder ib = InjectorBuilder.create()
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
