package org.boyu;

import org.boyu.exception.CyclicDependenciesFoundException;
import org.boyu.exception.DependencyNotFoundException;
import org.boyu.exception.IllegalComponentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.boyu.exception.IllegalComponentException.Reason.MULTI_INJECT_CONSTRUCTORS;
import static org.boyu.exception.IllegalComponentException.Reason.NO_PROPER_CONSTRUCTOR_FOUND;
import static org.mockito.BDDMockito.given;

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
                assertThat(config.getContext().get(Component.class).get()).isSameAs(instance);
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
                    final Throwable exception = catchThrowable(() -> config.getContext());
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
                    final Throwable exception = catchThrowable(() -> config.getContext());
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
                    final Throwable throwable = catchThrowable(() -> config.getContext());
                    assertThat(throwable).isInstanceOf(CyclicDependenciesFoundException.class);

                    final CyclicDependenciesFoundException exception = (CyclicDependenciesFoundException) throwable;
                    assertThat(exception).hasMessageContaining("found cyclic dependencies which are not allowed");

                    final List<Class<?>> classes = exception.getComponentTypes();
                    assertThat(classes)
                            .hasSize(2)
                            .containsExactlyInAnyOrder(Component.class, Dependency.class);
                }

                @Test
                void should_throw_exception_given_transitive_cyclic_dependencies_found() {
                    // given: A -> B -> C -> A
                    config.bind(Component.class, ComponentWithInjectConstructor.class);
                    config.bind(Dependency.class, DependencyDependedOnAnotherDep.class);
                    config.bind(AnotherDependency.class, AnotherDepDependedOnComp.class);

                    // when + then
                    final Throwable throwable = catchThrowable(() -> config.getContext());
                    assertThat(throwable).isInstanceOf(CyclicDependenciesFoundException.class);

                    final CyclicDependenciesFoundException exception = (CyclicDependenciesFoundException) throwable;
                    assertThat(exception).hasMessageContaining("found cyclic dependencies which are not allowed");

                    final List<Class<?>> classes = exception.getComponentTypes();
                    assertThat(classes)
                            .hasSize(3)
                            .containsExactlyInAnyOrder(Component.class, Dependency.class, AnotherDependency.class);
                }

                @Test
                void should_throw_exception_given_self_cyclic_dependencies_found() {
                    // given: A -> B -> B
                    config.bind(Component.class, ComponentWithInjectConstructor.class);
                    config.bind(Dependency.class, DependencyDependedOnDep.class);

                    // when + then
                    assertThatThrownBy(() -> config.getContext())
                            .isInstanceOf(CyclicDependenciesFoundException.class)
                            .hasMessageContaining("found cyclic dependencies which are not allowed");
                }
            }
        }

        @Nested
        class FieldInjection {
            @Test
            void should_inject_dependency_via_field() {
                // given
                final Dependency instance = new Dependency() {
                };
                config.bind(Dependency.class, instance);
                config.bind(ComponentWithFieldInjection.class, ComponentWithFieldInjection.class);

                // when
                final ComponentWithFieldInjection component = config.getContext().get(ComponentWithFieldInjection.class).get();

                // then
                assertThat(component.getDependency()).isSameAs(instance);
            }

            @Test
            void should_create_component_with_injection_field() {
                // given
                Context context = Mockito.mock(Context.class); // stub
                Dependency dependency = Mockito.mock(Dependency.class); // stub
                given(context.get(Dependency.class))
                        .willReturn(Optional.of(dependency));

                final ConstructionInjectionProvider<ComponentWithInjectConstructor> provider = new ConstructionInjectionProvider<>(ComponentWithInjectConstructor.class);

                // when
                final ComponentWithInjectConstructor component = provider.get(context);

                // then
                assertThat(component.getDependency()).isSameAs(dependency);
            }

            // TODO: exception if dependency not found
            @Test
            void should_throw_exception_when_field_dependency_missing() {
                // given
                config.bind(ComponentWithFieldInjection.class, ComponentWithFieldInjection.class);

                // when
                final Throwable exception = catchThrowable(() -> config.getContext());

                // then
                assertThat(exception)
                        .isInstanceOf(DependencyNotFoundException.class)
                        .hasMessageContaining("cannot find dependency for given implementation");
            }

            @Test
            void should_include_field_dependency_in_dependencies() {
                // given
                final ConstructionInjectionProvider<ComponentWithFieldInjection> provider = new ConstructionInjectionProvider<>(ComponentWithFieldInjection.class);

                // when

                // then
                assertThat(provider.getDependencies()).containsExactlyInAnyOrder(Dependency.class);
            }


            // TODO: exception if cyclic dependency found
            @Test
            void should_throw_exception_when_field_has_cyclic_dependencies() {
                // given
                config.bind(ComponentWithFieldInjection.class, ComponentWithFieldInjection.class);
                config.bind(Dependency.class, DependencyWithFieldInjection.class);

                // when
                final Throwable exception = catchThrowable(() -> config.getContext());

                // then
                assertThat(exception)
                        .isInstanceOf(CyclicDependenciesFoundException.class)
                        .hasMessageContaining("found cyclic dependencies which are not allowed");
            }

            // TODO: provide dependency information for field injection

            // TODO: exception if `final` field (final means filed could only be injected via constructor)
        }
    }

    @Nested
    public class DependenciesSelection {

    }

    @Nested
    public class LifecycleManagement {

    }

}
