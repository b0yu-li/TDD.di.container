package org.boyu;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.boyu.exception.IllegalComponentException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {
    private final Map<Class<?>, Provider<?>> components = new HashMap<>();

    public <T> void bind(Class<T> type, T instance) {
        components.put(type, () -> instance);
    }

    public <T, U extends T> void bind(Class<T> type, Class<U> impl) {
        final List<Constructor<?>> injectConstructors = Arrays.stream(impl.getConstructors())
                .filter(it -> it.isAnnotationPresent(Inject.class))
                .toList();
        if (injectConstructors.size() > 1) {
            throw new IllegalComponentException();
        }

        final boolean doesHaveDefaultConstructor = Arrays.stream(impl.getConstructors())
                .filter(c -> 0 == c.getParameters().length)
                .findFirst()
                .map(c -> true)
                .orElse(false);
        if (injectConstructors.isEmpty() && !doesHaveDefaultConstructor) {
            throw new IllegalComponentException();
        }


        components.put(type, () -> {
            try {
                final Constructor<U> constructor = getConstructor(impl);
                final Object[] objects = Arrays.stream(constructor.getParameters())
                        .map(Parameter::getType)
                        .map(this::get)
                        .toArray();
                return constructor.newInstance(objects);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <U> Constructor<U> getConstructor(Class<U> impl) {
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
