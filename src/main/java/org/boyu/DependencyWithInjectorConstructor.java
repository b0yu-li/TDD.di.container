package org.boyu;

import jakarta.inject.Inject;

public class DependencyWithInjectorConstructor implements Dependency {
    private String dependency;

    @Inject
    public DependencyWithInjectorConstructor(String dependency) {
        this.dependency = dependency;
    }

    public String getDependency() {
        return dependency;
    }
}
