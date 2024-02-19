package org.boyu;

import java.util.HashMap;
import java.util.Map;

public class Context {
    private Map<Class<?>, Object> components = new HashMap<>();

    public <T> void bind(Class<T> componentClass, T instance) {
        components.put(componentClass, instance);
    }

    public <T> T get(Class<T> type) {
        return (T) components.get(type);
    }
}
