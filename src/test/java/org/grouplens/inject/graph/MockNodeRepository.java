package org.grouplens.inject.graph;

public class MockNodeRepository implements NodeRepository {
    @Override
    public Node resolve(Desire desire) {
        return null;
    }

    @Override
    public BindRule defaultBindRule() {
        return new DefaultBindRule();
    }

    private static class DefaultBindRule implements BindRule {
        @Override
        public boolean matches(Desire desire) {
            return false;
        }

        @Override
        public Desire apply(Desire desire) {
            return null;
        }
    }
}
