package org.boyu;

import jakarta.inject.Inject;
import lombok.NoArgsConstructor;

public interface Component {
}

@NoArgsConstructor
class ComponentWithDefaultConstructor implements Component {
}

class ComponentWithInjectConstructor implements Component {
    private Dependency dependency;

    @Inject
    public ComponentWithInjectConstructor(Dependency dependency) {
        this.dependency = dependency;
    }

    public Dependency getDependency() {
        return dependency;
    }
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

class ComponentWithFieldInjection {
    @Inject
    Dependency dependency;

    public Dependency getDependency() {
        return dependency;
    }
}

class SubComponentWithFieldInjection extends ComponentWithFieldInjection {

}

class InjectMethodWithNoDependency {
    boolean called = false;

    @Inject
    void install() {
        this.called = true;
    }
}

class InjectMethodWithDependency {
    Dependency dependency;

    @Inject
    void install(Dependency dependency) {
        this.dependency = dependency;
    }
}

class SuperWithInjectMethod {
    boolean superCalled = false;

    @Inject
    public void install() {
        superCalled = true;
    }
}

class SubWithInjectMethod extends SuperWithInjectMethod {
    boolean subCalled = false;

    @Inject
    public void installAnother() {
        subCalled = true;
    }
}
