package org.boyu;

import java.util.HashMap;
import java.util.Map;

public class Context {
    private Map<Class<Component>, Component> components = new HashMap<>();

    public void bind(Class<Component> componentClass, Component instance) {
        components.put(componentClass, instance);
    }

    public Component get(Class<Component> componentClass) {
        return components.get(componentClass);
    }
}
