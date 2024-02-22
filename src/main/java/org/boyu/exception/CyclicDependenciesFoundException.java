package org.boyu.exception;

import java.util.ArrayList;
import java.util.List;

public class CyclicDependenciesFoundException extends RuntimeException {
    private List<Class<?>> componentTypes = new ArrayList<>();

    public CyclicDependenciesFoundException(Class<?> componentType) {
        this.componentTypes.add(componentType);
    }

    public CyclicDependenciesFoundException(Class<?> componentType, CyclicDependenciesFoundException e) {
        this.componentTypes.add(componentType);
        this.componentTypes.addAll(e.getComponentTypes());
    }

    @Override
    public String getMessage() {
        return "found cyclic dependencies which are not allowed";
    }

    public List<Class<?>> getComponentTypes() {
        return componentTypes;
    }
}
