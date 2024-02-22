package org.boyu;

import jakarta.inject.Inject;

public interface AnotherDependency {
}

class DependencyC implements AnotherDependency {
    Component component;

    @Inject
    public DependencyC(Component component) {
        this.component = component;
    }

}
