package org.boyu.exception;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CyclicDependenciesFoundException extends RuntimeException {
    private Set<Class<?>> componentTypes = new HashSet<>();

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
        return componentTypes.stream().toList();
    }
}
