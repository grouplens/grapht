package org.grouplens.grapht.util;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FrozenListTest {
    @SuppressWarnings("unchecked")
    @Test
    public void testEmptyList() {
        List<String> lst = new FrozenList<String>(Collections.EMPTY_LIST);
        assertThat(lst.isEmpty(), equalTo(true));
        assertThat(lst.size(), equalTo(0));
        assertThat(lst.iterator().hasNext(), equalTo(false));
        try {
            lst.get(0);
            fail("getting from empty list should throw");
        } catch (IndexOutOfBoundsException e) {
            /* expected */
        }
    }

    @Test
    public void testSimpleList() {
        List<String> lst = new FrozenList<String>(Arrays.asList("foo", "bar"));
        assertThat(lst.isEmpty(), equalTo(false));
        assertThat(lst.size(), equalTo(2));
        assertThat(lst.get(0), equalTo("foo"));
        assertThat(lst.get(1), equalTo("bar"));
        try {
            lst.get(2);
            fail("getting invalid index should throw");
        } catch (IndexOutOfBoundsException e) {
            /* expected */
        }

        Iterator<String> iter = lst.iterator();
        assertThat(iter.hasNext(), equalTo(true));
        String s = iter.next();
        assertThat(s, equalTo("foo"));
        assertThat(iter.hasNext(), equalTo(true));
        s = iter.next();
        assertThat(s, equalTo("bar"));
        assertThat(iter.hasNext(), equalTo(false));
    }

    @Test
    public void testListEquals() {
        List<String> lst = new FrozenList<String>(Arrays.asList("foo", "bar"));
        assertThat(lst, equalTo(Arrays.asList("foo", "bar")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSerializeRoundTrip() {
        FrozenList<String> lst = new FrozenList<String>(Arrays.asList("foo", "bar"));
        FrozenList<String> lst2 = SerializationUtils.clone(lst);
        assertThat(lst2, equalTo(lst));
        assertThat(lst2.size(), equalTo(2));

        FrozenList<String> empty =
                SerializationUtils.clone(new FrozenList<String>(Collections.EMPTY_LIST));
        assertThat(empty.isEmpty(), equalTo(true));
        assertThat(empty, hasSize(0));
    }
}
