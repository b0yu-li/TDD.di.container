package org.boyu;

import jakarta.inject.Inject;
import org.boyu.exception.IllegalComponentException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.boyu.exception.IllegalComponentException.Reason.MULTI_INJECT_CONSTRUCTORS;
import static org.boyu.exception.IllegalComponentException.Reason.NO_PROPER_CONSTRUCTOR_FOUND;

class ConstructionInjectionProvider<T> implements ComponentProvider<T> {
    private final Constructor<T> constructor;

    public ConstructionInjectionProvider(Class<T> impl) {
        this.constructor = getConstructor(impl);
    }

    private static <U> Constructor<U> getConstructor(Class<U> impl) {
        final List<Constructor<?>> injectConstructors = Arrays.stream(impl.getConstructors())
                .filter(it -> it.isAnnotationPresent(Inject.class))
                .toList();

        if (injectConstructors.size() > 1)
            throw new IllegalComponentException(MULTI_INJECT_CONSTRUCTORS.getValue());

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

    @Override
    public List<Class<?>> getDependencies() {
        return Arrays.stream(constructor.getParameters())
                .map(Parameter::getType)
                .collect(Collectors.toList());
    }
}
