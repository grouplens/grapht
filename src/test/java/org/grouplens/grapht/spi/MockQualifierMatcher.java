package org.grouplens.grapht.spi;

import java.util.HashSet;
import java.util.Set;

public class MockQualifierMatcher implements QualifierMatcher {
    private Set<MockQualifier> qualifiers;
    
    private MockQualifierMatcher() {
        qualifiers = null;
    }
    
    public static MockQualifierMatcher any() {
        return new MockQualifierMatcher();
    }
    
    public static MockQualifierMatcher none() {
        return match((MockQualifier) null);
    }
    
    public static MockQualifierMatcher match(MockQualifier... mockQualifiers) {
        MockQualifierMatcher q = new MockQualifierMatcher();
        q.qualifiers = new HashSet<MockQualifier>();
        for (MockQualifier m: mockQualifiers) {
            q.qualifiers.add(m);
        }
        return q;
    }
    
    @Override
    public int compareTo(QualifierMatcher o) {
        MockQualifierMatcher mq = (MockQualifierMatcher) o;
        
        if (qualifiers == null) {
            if (mq.qualifiers == null) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if (mq.qualifiers == null) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public boolean matches(Qualifier q) {
        return qualifiers == null || qualifiers.contains(q);
    }
}
