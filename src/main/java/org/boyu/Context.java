package org.boyu;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Context {
    private Map<Class<?>, Provider<?>> components = new HashMap<>();

    public <T> void bind(Class<T> type, T instance) {
        components.put(type, () -> instance);
    }

    public <T, U extends T> void bind(Class<T> type, Class<U> impl) {
        components.put(type, () -> {
            try {
                final Constructor<U> constructor = getConstructor(impl);
                final Object[] objects = Arrays.stream(constructor.getParameters())
                        .map(p -> p.getType())
                        .map(t -> {
                            return get(t);
                        })
                        .toArray();
                return constructor.newInstance(objects);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <U> Constructor<U> getConstructor(Class<U> impl) throws NoSuchMethodException {
        return (Constructor<U>) Arrays.stream(impl.getConstructors())
                .filter(c -> {
                    if (c.isAnnotationPresent(Inject.class)) {
                        return true;
                    }
                    return false;
                })
                .findFirst()
                .orElseGet(() -> {
                    try {
                        return impl.getConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });
    }


    public <T> T get(Class<T> type) {
        return (T) components.get(type).get();
    }
}
