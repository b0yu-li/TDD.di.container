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


class DependencyDependedOnComponent implements Dependency {
    Component component;

    @Inject
    public DependencyDependedOnComponent(Component component) {
        this.component = component;
    }
}

class DependencyDependedOnAnotherDep implements Dependency {
    AnotherDependency anotherDependency;

    @Inject
    public DependencyDependedOnAnotherDep(AnotherDependency anotherDependency) {
        this.anotherDependency = anotherDependency;
    }

}

class DependencyDependedOnDep implements Dependency {
    Dependency dependency;

    @Inject
    public DependencyDependedOnDep(Dependency dependency) {
        this.dependency = dependency;
    }
}

class DependencyWithFieldInjection implements Dependency {
    @Inject
    ComponentWithFieldInjection component;
}
