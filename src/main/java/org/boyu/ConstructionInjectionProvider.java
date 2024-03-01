package org.boyu;

import jakarta.inject.Inject;
import org.boyu.exception.IllegalComponentException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.boyu.exception.IllegalComponentException.Reason.MULTI_INJECT_CONSTRUCTORS;
import static org.boyu.exception.IllegalComponentException.Reason.NO_PROPER_CONSTRUCTOR_FOUND;

class ConstructionInjectionProvider<T> implements ComponentProvider<T> {
    private final Constructor<T> injectConstructor;
    private final List<Field> injectFields;

    public ConstructionInjectionProvider(Class<T> impl) {
        this.injectConstructor = getConstructor(impl);
        this.injectFields = getInjectFields(impl);
    }

    @Override
    public T get(Context context) {
        try {
            final Object[] objects = Arrays.stream(injectConstructor.getParameters())
                    .map(Parameter::getType)
                    .map(typeKey -> context.get(typeKey).get())
                    .toArray();
            final T instance = injectConstructor.newInstance(objects);
            for (Field field : injectFields) {
                final Object fieldInstance = context.get(field.getType()).get();
                field.set(instance, fieldInstance);
            }

            return instance;
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Class<?>> getDependencies() {
        final List<Class<?>> depsInInjectConstructor = Arrays.stream(injectConstructor.getParameters())
                .map(Parameter::getType)
                .collect(Collectors.toList());
        final List<Class<?>> depsInInjectFields = injectFields.stream()
                .map(Field::getType)
                .collect(Collectors.toList());
        return Stream.concat(depsInInjectConstructor.stream(), depsInInjectFields.stream())
                .toList();
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
                        return impl.getDeclaredConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new IllegalComponentException(NO_PROPER_CONSTRUCTOR_FOUND.getValue());
                    }
                });
    }

    private static <T> List<Field> getInjectFields(Class<T> impl) {
        return Arrays.stream(impl.getDeclaredFields())
                .filter(it -> it.isAnnotationPresent(Inject.class))
                .toList();
    }
}
