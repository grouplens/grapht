package org.grouplens.grapht.solver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.Satisfaction;

public class InjectionContext implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final List<Pair<Satisfaction, Attributes>> context;
    private final List<Desire> desires;
    
    private final transient Map<String, Object> values;
    
    public InjectionContext() {
        // The default context starts out with an empty type path, no prior
        // desires and no stored values
        context = Collections.emptyList();
        desires = Collections.emptyList();
        values = new HashMap<String, Object>();
    }
    
    private InjectionContext(InjectionContext prior, Satisfaction satisfaction, Attributes attrs) {
        // A context with a pushed satisfaction inherits and updates the type
        // path, but resets the desires and stored values
        List<Pair<Satisfaction, Attributes>> newCtx = new ArrayList<Pair<Satisfaction, Attributes>>(prior.context);
        newCtx.add(Pair.of(satisfaction, attrs));
        
        context = Collections.unmodifiableList(newCtx);
        desires = Collections.emptyList();
        values = new HashMap<String, Object>();
    }
    
    private InjectionContext(InjectionContext prior, Desire desire) {
        // A context with a pushed desire reuses the same context and stored
        // values, but updates the prior desires list
        List<Desire> newDesires = new ArrayList<Desire>(prior.desires);
        newDesires.add(desire);
        
        context = prior.context;
        desires = Collections.unmodifiableList(newDesires);
        values = prior.values;
    }
    
    public InjectionContext push(Satisfaction satisfaction, Attributes attrs) {
        return new InjectionContext(this, satisfaction, attrs);
    }
    
    public InjectionContext push(Desire desire) {
        return new InjectionContext(this, desire);
    }
    
    public List<Pair<Satisfaction, Attributes>> getTypePath() {
        return context;
    }
    
    public List<Desire> getPriorDesires() {
        return desires;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        return (T) values.get(key);
    }
    
    public void putValue(String key, Object value) {
        values.put(key, value);
    }
}
