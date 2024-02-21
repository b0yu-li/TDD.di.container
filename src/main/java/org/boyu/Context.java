package org.boyu;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.boyu.exception.CyclicDependenciesFoundException;
import org.boyu.exception.DependencyNotFoundException;
import org.boyu.exception.IllegalComponentException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;

public class Context {
    private final Map<Class<?>, Provider<?>> components = new HashMap<>();

    public <T> void bind(Class<T> type, T instance) {
        components.put(type, () -> instance);
    }

    public <T, U extends T> void bind(Class<T> type, Class<U> impl) {
        final Constructor<U> constructor = getConstructor(impl);

        components.put(type, getProvider(constructor));
    }

    private <U> Provider<U> getProvider(Constructor<U> constructor) {
        return new ConstructionInjectionProvider(constructor);
    }

    class ConstructionInjectionProvider<U> implements Provider<U> {
        private boolean constructing = false;
        private Constructor<U> constructor;

        public ConstructionInjectionProvider(Constructor<U> constructor) {
            this.constructor = constructor;
        }

        @Override
        public U get() {
            if (constructing) throw new CyclicDependenciesFoundException();
            try {
                constructing = true;

                final Object[] objects = Arrays.stream(constructor.getParameters())
                        .map(Parameter::getType)
                        .map(typeKey -> Context.this.get(typeKey).orElseThrow(DependencyNotFoundException::new))
                        .toArray();
                return constructor.newInstance(objects);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            } finally {
                constructing = false;
            }
        }
    }

    private <U> Constructor<U> getConstructor(Class<U> impl) {
        final List<Constructor<?>> injectConstructors = Arrays.stream(impl.getConstructors())
                .filter(it -> it.isAnnotationPresent(Inject.class))
                .toList();

        if (injectConstructors.size() > 1) throw new IllegalComponentException();

        return (Constructor<U>) injectConstructors.stream()
                .findFirst()
                .orElseGet(() -> {
                    try {
                        return impl.getConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new IllegalComponentException();
                    }
                });
    }

    public <T> Optional<T> get(Class<T> typeKey) {
        return Optional.ofNullable(components.get(typeKey))
                .map(it -> (T) it.get());
    }
}
