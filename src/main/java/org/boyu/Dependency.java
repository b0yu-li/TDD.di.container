package org.boyu;

import jakarta.inject.Inject;

public interface Dependency {
}

class DependentedOnDependency implements Dependency {
    Component component;

    @Inject
    public DependentedOnDependency(Component component) {
        this.component = component;
    }
}

class DependencyB implements Dependency {
    AnotherDependency anotherDependency;

    public DependencyB(AnotherDependency anotherDependency) {
        this.anotherDependency = anotherDependency;
    }

}
