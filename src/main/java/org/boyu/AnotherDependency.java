package org.boyu;

import jakarta.inject.Inject;

public interface AnotherDependency {
}

class AnotherDepDependedOnComp implements AnotherDependency {
    Component component;

    @Inject
    public AnotherDepDependedOnComp(Component component) {
        this.component = component;
    }

}
