package org.boyu;

import org.boyu.exception.CyclicDependenciesFoundException;
import org.boyu.exception.DependencyNotFoundException;
import org.boyu.exception.IllegalComponentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.boyu.exception.IllegalComponentException.Reason.MULTI_INJECT_CONSTRUCTORS;
import static org.boyu.exception.IllegalComponentException.Reason.NO_PROPER_CONSTRUCTOR_FOUND;

public class ContainerUnitTest {
    private ContextConfig config;

    @BeforeEach
    void setUp() {
        config = new ContextConfig();
    }

    @Nested
    class ComponentConstruction {
        @Nested
        class BindTypeToInstance {
            @Test
            void should_bind_type_to_a_specific_instance() {
                // given
                Component instance = new Component() {
                };

                // when
                config.bind(Component.class, instance);

                // then
                assertThat(config.getContext().get(Component.class).get()).isEqualTo(instance);
            }
        }

        @Nested
        class BindTypeToClass {
            @Test
            void should_bind_type_to_a_class_with_default_constructor() {
                // given

                // when
                config.bind(Component.class, ComponentWithDefaultConstructor.class);

                // then
                final Component actual = config.getContext().get(Component.class).get();
                assertThat(actual)
                        .isNotNull()
                        .isInstanceOf(ComponentWithDefaultConstructor.class);
            }

            @Test
            void should_isolate_build_and_get() {
                // given

                // when
                config.bind(Component.class, ComponentWithDefaultConstructor.class);

                // then
                Context container = config.getContext();
                final Component actual = container.get(Component.class).get();
                assertThat(actual)
                        .isNotNull()
                        .isInstanceOf(ComponentWithDefaultConstructor.class);
            }


            @Test
            void should_bind_type_to_a_class_with_inject_constructor() {
                // given
                final Dependency dependency = new Dependency() {
                };
                config.bind(Dependency.class, dependency);

                // when
                config.bind(Component.class, ComponentWithInjectConstructor.class);

                // then
                final Component actual = config.getContext().get(Component.class).get();
                assertThat(actual)
                        .isNotNull()
                        .isInstanceOf(ComponentWithInjectConstructor.class);
                assertThat(((ComponentWithInjectConstructor) actual).getDependency())
                        .isEqualTo(dependency);
            }

            @Test
            void should_bind_type_to_a_class_with_inject_constructor_with_transitive_dependencies() {
                // given
                config.bind(Dependency.class, DependencyWithInjectorConstructor.class);
                config.bind(String.class, "indirect dependency");

                // when
                config.bind(Component.class, ComponentWithInjectConstructor.class);

                // then
                final Component instance = config.getContext().get(Component.class).get();
                assertThat(instance).isNotNull();
                final Dependency dependency = ((ComponentWithInjectConstructor) instance).getDependency();
                assertThat(dependency).isNotNull();
                final String innerDependency = ((DependencyWithInjectorConstructor) dependency).getDependency();
                assertThat(innerDependency).isNotNull().isEqualTo("indirect dependency");
            }

            @Test
            void should_return_empty_when_get_given_component_undefined() {
                // when
                Optional<Component> component = config.getContext().get(Component.class);

                // then
                assertThat(component).isEmpty();
            }

            @Nested
            class SadPath {
                @Test
                void should_throw_exception_when_bind_given_multi_inject_constructors() {
                    // when + then
                    assertThatThrownBy(() -> config.bind(Component.class, ComponentWithMultiInjectConstructors.class))
                            .isInstanceOf(IllegalComponentException.class)
                            .hasMessageContaining(MULTI_INJECT_CONSTRUCTORS.getValue());
                }

                @Test
                void should_throw_exception_when_bind_given_no_inject_nor_default_constructor() {
                    // when + then
                    assertThatThrownBy(() -> config.bind(Component.class, ComponentWithNoInjectNorDefaultConstructor.class))
                            .isInstanceOf(IllegalComponentException.class)
                            .hasMessageContaining(NO_PROPER_CONSTRUCTOR_FOUND.getValue());
                }

                @Test
                void should_throw_exception_when_get_given_dependency_not_found() {
                    // given
                    config.bind(Component.class, ComponentWithInjectConstructor.class);

                    // when + then
                    final Throwable exception = catchThrowable(() -> config.getContext().get(Component.class));
                    assertThat(exception)
                            .isInstanceOf(DependencyNotFoundException.class)
                            .hasMessageContaining("cannot find dependency for given implementation");
                    final DependencyNotFoundException notFoundException = (DependencyNotFoundException) exception;
                    assertThat(notFoundException.getDependencyType()).isEqualTo(Dependency.class);
                    assertThat(notFoundException.getComponentType()).isEqualTo(Component.class);
                }

                @Test
                void should_throw_exception_when_get_given_transitive_dependency_not_found() {
                    // given
                    config.bind(Component.class, ComponentWithInjectConstructor.class);
                    config.bind(Dependency.class, DependencyWithInjectorConstructor.class);

                    // when + then
                    final Throwable exception = catchThrowable(() -> config.getContext().get(Component.class));
                    assertThat(exception)
                            .isInstanceOf(DependencyNotFoundException.class)
                            .hasMessageContaining("cannot find dependency for given implementation");
                    final DependencyNotFoundException notFoundException = (DependencyNotFoundException) exception;
                    assertThat(notFoundException.getDependencyType()).isEqualTo(String.class);
                    assertThat(notFoundException.getComponentType()).isEqualTo(Dependency.class);
                }

                @Test
                void should_throw_exception_given_cyclic_dependencies_found() {
                    // given
                    config.bind(Component.class, ComponentWithInjectConstructor.class);
                    config.bind(Dependency.class, DependencyDependedOnComponent.class);

                    // when + then
                    final Throwable throwable = catchThrowable(() -> config.getContext().get(Component.class));
                    assertThat(throwable).isInstanceOf(CyclicDependenciesFoundException.class);

                    final CyclicDependenciesFoundException exception = (CyclicDependenciesFoundException) throwable;
                    assertThat(exception).hasMessageContaining("found cyclic dependencies which are not allowed");

                    final List<Class<?>> classes = exception.getComponentTypes();
                    assertThat(classes)
                            .hasSize(3)
                            .containsExactlyInAnyOrder(Component.class, Dependency.class, Component.class);
                }

                @Test
                void should_throw_exception_given_transitive_cyclic_dependencies_found() {
                    // given: A -> B -> C -> A
                    config.bind(Component.class, ComponentWithInjectConstructor.class);
                    config.bind(Dependency.class, DependencyDependedOnAnotherDep.class);
                    config.bind(AnotherDependency.class, AnotherDepDependedOnComp.class);

                    // when + then
                    final Throwable throwable = catchThrowable(() -> config.getContext().get(Component.class));
                    assertThat(throwable).isInstanceOf(CyclicDependenciesFoundException.class);

                    final CyclicDependenciesFoundException exception = (CyclicDependenciesFoundException) throwable;
                    assertThat(exception).hasMessageContaining("found cyclic dependencies which are not allowed");

                    final List<Class<?>> classes = exception.getComponentTypes();
                    assertThat(classes)
                            .hasSize(4)
                            .containsExactlyInAnyOrder(Component.class, Dependency.class, AnotherDependency.class, Component.class);
                }

                @Test
                void should_throw_exception_given_self_cyclic_dependencies_found() {
                    // given: A -> B -> B
                    config.bind(Component.class, ComponentWithInjectConstructor.class);
                    config.bind(Dependency.class, DependencyDependedOnDep.class);

                    // when + then
                    assertThatThrownBy(() -> config.getContext().get(Component.class))
                            .isInstanceOf(CyclicDependenciesFoundException.class)
                            .hasMessageContaining("found cyclic dependencies which are not allowed");
                }
            }
        }
    }

    @Nested
    public class DependenciesSelection {

    }

    @Nested
    public class LifecycleManagement {

    }

}
