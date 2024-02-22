package org.boyu.exception;

import java.util.List;

public class CyclicDependenciesFoundException extends RuntimeException {
    private List<Class<?>> componentTypes;

    @Override
    public String getMessage() {
        return "found cyclic dependencies which are not allowed";
    }

    public List<Class<?>> getComponentTypes() {
        return componentTypes;
    }
}
