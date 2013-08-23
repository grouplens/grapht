package org.grouplens.grapht.solver;

import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.MockDesire;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class DesireChainTest {
    @Test
    public void testSingleDesire() {
        Desire desire = new MockDesire(String.class, null, null);
        DesireChain chain = DesireChain.singleton(desire);
        assertThat(chain.getCurrentDesire(),
                   equalTo(desire));
        assertThat(chain.getInitialDesire(),
                   equalTo(desire));
        assertThat(chain, contains(desire));
        assertThat(chain.getPreviousDesires(), hasSize(0));
    }

    @Test
    public void testMultipleDesires() {
        Desire d1 = new MockDesire(InputStream.class, null, null);
        Desire d2 = new MockDesire(FileInputStream.class, null, null);
        DesireChain chain = DesireChain.singleton(d1).extend(d2);
        assertThat(chain.getCurrentDesire(),
                   equalTo(d2));
        assertThat(chain.getInitialDesire(),
                   equalTo(d1));
        assertThat(chain.getPreviousDesires(),
                   equalTo((List<Desire>) DesireChain.singleton(d1)));
        assertThat(chain, contains(d1, d2));
    }
}
