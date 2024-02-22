package org.boyu;

import jakarta.inject.Inject;

public interface Dependency {
}

class DependencyWithInjectorConstructor implements Dependency {
    private String dependency;

    @Inject
    public DependencyWithInjectorConstructor(String dependency) {
        this.dependency = dependency;
    }

    public String getDependency() {
        return dependency;
    }
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

    @Inject
    public DependencyB(AnotherDependency anotherDependency) {
        this.anotherDependency = anotherDependency;
    }

}
