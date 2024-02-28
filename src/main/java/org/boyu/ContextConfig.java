package org.boyu;

import jakarta.inject.Inject;
import org.boyu.exception.CyclicDependenciesFoundException;
import org.boyu.exception.DependencyNotFoundException;
import org.boyu.exception.IllegalComponentException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

import static org.boyu.exception.IllegalComponentException.Reason.MULTI_INJECT_CONSTRUCTORS;
import static org.boyu.exception.IllegalComponentException.Reason.NO_PROPER_CONSTRUCTOR_FOUND;

public class ContextConfig {
    private final Map<Class<?>, ComponentProvider<?>> providers = new HashMap<>();
    private final Map<Class<?>, List<Class<?>>> dependencies = new HashMap<>();

    public <T> void bind(Class<T> type, T instance) {
        providers.put(type, context -> instance);
        dependencies.put(type, List.of());
    }

    public <T, U extends T> void bind(Class<T> type, Class<U> impl) {
        final Constructor<U> constructor = getConstructor(impl);

        // TODO: HOW weird is it that the code below wouldn't work!
        // providers.put(type, context -> new ConstructionInjectionProvider<>(type, constructor));
        providers.put(type, new ConstructionInjectionProvider<>(type, constructor));
        dependencies.put(type, Arrays.stream(constructor.getParameters())
                .map(Parameter::getType)
                .collect(Collectors.toList()));
    }

    public Context getContext() {
        dependencies.keySet().forEach(key -> checkDependencies(key, new Stack<>()));

        return new Context() {
            @Override
            public <T> Optional<T> get(Class<T> typeKey) {
                return Optional.ofNullable(providers.get(typeKey))
                        .map(it -> (T) it.get(this));
            }
        };
    }

    private void checkDependencies(Class<?> key, Stack<Class<?>> visiting) {
        dependencies.get(key).forEach(dep -> {
            if (!dependencies.containsKey(dep)) throw new DependencyNotFoundException(key, dep);
            if (visiting.contains(dep)) throw new CyclicDependenciesFoundException(visiting);
            visiting.push(dep);
            checkDependencies(dep, visiting);
            visiting.pop();
        });
    }

    private class ConstructionInjectionProvider<T> implements ComponentProvider<T> {
        private final Class<?> componentType;
        private final Constructor<T> constructor;

        public ConstructionInjectionProvider(Class<?> componentType, Constructor<T> constructor) {
            this.componentType = componentType;
            this.constructor = constructor;
        }

        @Override
        public T get(Context context) {
            try {
                final Object[] objects = Arrays.stream(constructor.getParameters())
                        .map(Parameter::getType)
                        .map(typeKey -> context.get(typeKey).get())
                        .toArray();
                return constructor.newInstance(objects);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private <U> Constructor<U> getConstructor(Class<U> impl) {
        final List<Constructor<?>> injectConstructors = Arrays.stream(impl.getConstructors())
                .filter(it -> it.isAnnotationPresent(Inject.class))
                .toList();

        if (injectConstructors.size() > 1) throw new IllegalComponentException(MULTI_INJECT_CONSTRUCTORS.getValue());

        return (Constructor<U>) injectConstructors.stream()
                .findFirst()
                .orElseGet(() -> {
                    try {
                        return impl.getConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new IllegalComponentException(NO_PROPER_CONSTRUCTOR_FOUND.getValue());
                    }
                });
    }

}
