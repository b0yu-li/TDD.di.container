package org.boyu;

public class DependencyWithInjectorConstructor implements Dependency {
    private String dependency;

    public DependencyWithInjectorConstructor(String dependency) {
        this.dependency = dependency;
    }

    public String getDependency() {
        return dependency;
    }
}
