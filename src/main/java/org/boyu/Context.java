package org.boyu;

import jakarta.inject.Provider;

import java.util.HashMap;
import java.util.Map;

public class Context {
    private Map<Class<?>, Provider<?>> components = new HashMap<>();

    public <T> void bind(Class<T> type, T instance) {
        components.put(type, () -> instance);
    }

    public <T, U extends T> void bind(Class<T> type, Class<U> componentWithDefaultConstructorClass) {
        components.put(type, () -> {
            try {
                return componentWithDefaultConstructorClass.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    public <T> T get(Class<T> type) {
        return (T) components.get(type).get();
    }
}
