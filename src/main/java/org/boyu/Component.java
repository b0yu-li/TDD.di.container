package org.boyu;

import jakarta.inject.Inject;

public interface Component {
}

class ComponentWithMultiInjectConstructors implements Component {
    @Inject
    public ComponentWithMultiInjectConstructors(String dep1) {
    }

    @Inject
    public ComponentWithMultiInjectConstructors(String dep1, String dep2) {
    }
}

class ComponentWithNoInjectNorDefaultConstructor implements Component {
    public ComponentWithNoInjectNorDefaultConstructor(String dep1) {
    }
}
