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

import static org.boyu.exception.IllegalComponentException.Reason.MULTI_INJECT_CONSTRUCTORS;
import static org.boyu.exception.IllegalComponentException.Reason.NO_PROPER_CONSTRUCTOR_FOUND;

public class ContextConfig {
    private final Map<Class<?>, Provider<?>> components = new HashMap<>();
    private final Map<Class<?>, ComponentProvider<?>> components_ = new HashMap<>();

    public <T> void bind(Class<T> type, T instance) {
        components.put(type, () -> instance);
        components_.put(type, context -> instance);
    }

    public <T, U extends T> void bind(Class<T> type, Class<U> impl) {
        final Constructor<U> constructor = getConstructor(impl);

        components.put(type, new ConstructionInjectionProvider<>(type, constructor));

        // TODO: HOW weird is it that the code below wouldn't work!
        // components_.put(type, context -> new ConstructionInjectionProvider<>(type, constructor));
        components_.put(type, new ConstructionInjectionProvider<>(type, constructor));
    }

    public Context getContext() {
        return new Context() {
            @Override
            public <T> Optional<T> get(Class<T> typeKey) {
                return Optional.ofNullable(components_.get(typeKey))
                        .map(it -> (T) it.get(this));
            }
        };
    }

    private class ConstructionInjectionProvider<T> implements Provider<T>, ComponentProvider<T> {
        private boolean constructing = false;
        private final Class<?> componentType;
        private final Constructor<T> constructor;

        public ConstructionInjectionProvider(Class<?> componentType, Constructor<T> constructor) {
            this.componentType = componentType;
            this.constructor = constructor;
        }

        @Override
        public T get() {
            return get(getContext());
        }

        @Override
        public T get(Context context) {
            if (constructing) {
                throw new CyclicDependenciesFoundException(componentType);
            }
            try {
                constructing = true;

                final Object[] objects = Arrays.stream(constructor.getParameters())
                        .map(Parameter::getType)
                        .map(typeKey -> context.get(typeKey).orElseThrow(() -> new DependencyNotFoundException(componentType, typeKey)))
                        .toArray();
                return constructor.newInstance(objects);
            } catch (CyclicDependenciesFoundException e) { // TODO: Write a summary of this solution re:#recursive
                throw new CyclicDependenciesFoundException(componentType, e);
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
