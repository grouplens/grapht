package org.grouplens.grapht;

import org.grouplens.grapht.annotation.DefaultNull;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests for <a href="https://github.com/grouplens/grapht/issues/73">#73</a>.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class QualifiedDefaultNullTest {
    @Test
    public void testQualifiedDefaultNull() {
        InjectorBuilder bld = new InjectorBuilder();
        Injector inj = bld.build();

        // With no bindings, we should get a null inner class.
        MainC main = inj.getInstance(MainC.class);
        assertThat(main, notNullValue());
        assertThat(main.dependency, nullValue());
    }

    @Test
    public void testQualifiedDefaultNullWithBinding() {
        InjectorBuilder bld = new InjectorBuilder();
        bld.bind(TestQ.class, DepC.class)
           .to(DepC.class);
        Injector inj = bld.build();

        // With a bindings, we should get an instance
        MainC main = inj.getInstance(MainC.class);
        assertThat(main, notNullValue());
        assertThat(main.dependency,
                   allOf(notNullValue(),
                         instanceOf(DepC.class)));
    }

    /**
     * Qualifier with default of {@code null}.
     */
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @DefaultNull
    @Documented
    public static @interface TestQ {}

    /**
     * Component with qualified dependency on concrete class.
     */
    public static class MainC {
        private final DepC dependency;

        @Inject
        public MainC(@Nullable @TestQ DepC dep) {
            dependency = dep;
        }
    }

    public static class DepC {
        @Inject
        public DepC() {}
    }
}
