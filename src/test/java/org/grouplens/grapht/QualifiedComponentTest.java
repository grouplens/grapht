package org.grouplens.grapht;

import org.grouplens.grapht.spi.reflect.types.TypeDftN;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Michael Ekstrand
 */
public class QualifiedComponentTest {
    @Test
    public void testDefaultNull() {
        InjectorBuilder b = new InjectorBuilder();
        Injector inj = b.build();
        OptionalDep obj = inj.getInstance(OptionalDep.class);
        assertThat(obj, not(nullValue()));
        assertThat(obj.getDep(), nullValue());
    }

    @Test(expected = InjectionException.class)
    public void testBadNull() {
        InjectorBuilder b = new InjectorBuilder();
        Injector inj = b.build();
        inj.getInstance(RequireDep.class);
    }

    private static class OptionalDep {
        private TypeDftN depend;
        @Inject
        public OptionalDep(@Nullable TypeDftN dep) {
            depend = dep;
        }
        public TypeDftN getDep() {
            return depend;
        }
    }

    private static class RequireDep {
        @Inject
        public RequireDep(TypeDftN dep) {
        }
    }
}
