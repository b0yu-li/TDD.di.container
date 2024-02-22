package org.boyu;

import org.boyu.exception.CyclicDependenciesFoundException;
import org.boyu.exception.DependencyNotFoundException;
import org.boyu.exception.IllegalComponentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.boyu.exception.IllegalComponentException.Reason.MULTI_INJECT_CONSTRUCTORS;
import static org.boyu.exception.IllegalComponentException.Reason.NO_PROPER_CONSTRUCTOR_FOUND;

public class ContainerUnitTest {
    private Context context;

    @BeforeEach
    void setUp() {
        context = new Context();
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
                context.bind(Component.class, instance);

                // then
                assertThat(context.get(Component.class).get()).isEqualTo(instance);
            }
        }

        @Nested
        class BindTypeToClass {
            @Test
            void should_bind_type_to_a_class_with_default_constructor() {
                // given

                // when
                context.bind(Component.class, ComponentWithDefaultConstructor.class);

                // then
                final Component actual = context.get(Component.class).get();
                assertThat(actual)
                        .isNotNull()
                        .isInstanceOf(ComponentWithDefaultConstructor.class);
            }

            @Test
            void should_bind_type_to_a_class_with_inject_constructor() {
                // given
                final Dependency dependency = new Dependency() {
                };
                context.bind(Dependency.class, dependency);

                // when
                context.bind(Component.class, ComponentWithInjectConstructor.class);

                // then
                final Component actual = context.get(Component.class).get();
                assertThat(actual)
                        .isNotNull()
                        .isInstanceOf(ComponentWithInjectConstructor.class);
                assertThat(((ComponentWithInjectConstructor) actual).getDependency())
                        .isEqualTo(dependency);
            }

            @Test
            void should_bind_type_to_a_class_with_inject_constructor_with_transitive_dependencies() {
                // given
                context.bind(Dependency.class, DependencyWithInjectorConstructor.class);
                context.bind(String.class, "indirect dependency");

                // when
                context.bind(Component.class, ComponentWithInjectConstructor.class);

                // then
                final Component instance = context.get(Component.class).get();
                assertThat(instance).isNotNull();
                final Dependency dependency = ((ComponentWithInjectConstructor) instance).getDependency();
                assertThat(dependency).isNotNull();
                final String innerDependency = ((DependencyWithInjectorConstructor) dependency).getDependency();
                assertThat(innerDependency).isNotNull().isEqualTo("indirect dependency");
            }

            @Test
            void should_return_empty_when_get_given_component_undefined() {
                // when
                Optional<Component> component = context.get(Component.class);

                // then
                assertThat(component).isEmpty();
            }

            @Nested
            class SadPath {
                @Test
                void should_throw_exception_when_bind_given_multi_inject_constructors() {
                    // when + then
                    assertThatThrownBy(() -> context.bind(Component.class, ComponentWithMultiInjectConstructors.class))
                            .isInstanceOf(IllegalComponentException.class)
                            .hasMessageContaining(MULTI_INJECT_CONSTRUCTORS.getValue());
                }

                @Test
                void should_throw_exception_when_bind_given_no_inject_nor_default_constructor() {
                    // when + then
                    assertThatThrownBy(() -> context.bind(Component.class, ComponentWithNoInjectNorDefaultConstructor.class))
                            .isInstanceOf(IllegalComponentException.class)
                            .hasMessageContaining(NO_PROPER_CONSTRUCTOR_FOUND.getValue());
                }

                @Test
                void should_throw_exception_when_get_given_dependency_not_found() {
                    // given
                    context.bind(Component.class, ComponentWithInjectConstructor.class);

                    // when + then
                    final Throwable exception = catchThrowable(() -> context.get(Component.class));
                    assertThat(exception)
                            .isInstanceOf(DependencyNotFoundException.class)
                            .hasMessageContaining("cannot find dependency for given implementation");
                    assertThat(((DependencyNotFoundException) exception).getDependencyType()).isEqualTo(Dependency.class);
                }

                @Test
                void should_throw_exception_when_get_given_transitive_dependency_not_found() {
                    // given
                    context.bind(Component.class, ComponentWithInjectConstructor.class);
                    context.bind(Dependency.class, DependencyWithInjectorConstructor.class);

                    // when + then
                    final Throwable exception = catchThrowable(() -> context.get(Component.class));
                    assertThat(exception)
                            .isInstanceOf(DependencyNotFoundException.class)
                            .hasMessageContaining("cannot find dependency for given implementation");
                    assertThat(((DependencyNotFoundException) exception).getDependencyType()).isEqualTo(String.class);
                }

                @Test
                void should_throw_exception_given_cyclic_dependencies_found() {
                    // given
                    context.bind(Component.class, ComponentWithInjectConstructor.class);
                    context.bind(Dependency.class, DependencyDependedOnComponent.class);

                    // when + then
                    assertThatThrownBy(() -> context.get(Component.class))
                            .isInstanceOf(CyclicDependenciesFoundException.class)
                            .hasMessageContaining("found cyclic dependencies which are not allowed");
                }

                @Test
                void should_throw_exception_given_transitive_cyclic_dependencies_found() {
                    // given: A -> B -> C -> A
                    context.bind(Component.class, ComponentWithInjectConstructor.class);
                    context.bind(Dependency.class, DependencyDependedOnAnotherDep.class);
                    context.bind(AnotherDependency.class, AnotherDepDependedOnComp.class);

                    // when + then
                    assertThatThrownBy(() -> context.get(Component.class))
                            .isInstanceOf(CyclicDependenciesFoundException.class)
                            .hasMessageContaining("found cyclic dependencies which are not allowed");
                }

                @Test
                void should_throw_exception_given_self_cyclic_dependencies_found() {
                    // given: A -> B -> B
                    context.bind(Component.class, ComponentWithInjectConstructor.class);
                    context.bind(Dependency.class, DependencyDependedOnDep.class);

                    // when + then
                    assertThatThrownBy(() -> context.get(Component.class))
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
