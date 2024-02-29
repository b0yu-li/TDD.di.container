package org.boyu;

import org.boyu.exception.CyclicDependenciesFoundException;
import org.boyu.exception.DependencyNotFoundException;

import java.util.*;

public class ContextConfig {
    private final Map<Class<?>, ComponentProvider<?>> providers = new HashMap<>();

    public <T> void bind(Class<T> type, T instance) {
        providers.put(type, new ComponentProvider<T>() {
            @Override
            public T get(Context context) {
                return instance;
            }

            @Override
            public List<Class<?>> getDependencies() {
                return List.of();
            }
        });
    }

    public <T, U extends T> void bind(Class<T> type, Class<U> impl) {
        // TODO: HOW weird is it that the code below wouldn't work!
        // providers.put(type, context -> new ConstructionInjectionProvider<>(type, constructor));
        providers.put(type, new ConstructionInjectionProvider<>(impl));
    }

    public Context getContext() {
        providers.keySet().forEach(key -> checkDependencies(key, new Stack<>()));

        return new Context() {
            @Override
            public <T> Optional<T> get(Class<T> typeKey) {
                return Optional.ofNullable(providers.get(typeKey))
                        .map(it -> (T) it.get(this));
            }
        };
    }

    private void checkDependencies(Class<?> key, Stack<Class<?>> visiting) {
        providers.get(key).getDependencies().forEach(dep -> {
            if (!providers.containsKey(dep)) throw new DependencyNotFoundException(key, dep);
            if (visiting.contains(dep)) throw new CyclicDependenciesFoundException(visiting);
            visiting.push(dep);
            checkDependencies(dep, visiting);
            visiting.pop();
        });
    }

}
