package org.grouplens.grapht;

import junit.framework.Test;

import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Engine;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.V8Engine;
import org.atinject.tck.auto.accessories.SpareTire;
import org.grouplens.grapht.spi.CachePolicy;

public class TckTest {
    
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
