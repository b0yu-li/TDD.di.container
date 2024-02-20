package org.boyu;

import jakarta.inject.Inject;
import org.boyu.exception.IllegalComponentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                assertThat(context.get(Component.class)).isEqualTo(instance);
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
                final Component actual = context.get(Component.class);
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
                final Component actual = context.get(Component.class);
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
                final Component instance = context.get(Component.class);
                assertThat(instance).isNotNull();
                final Dependency dependency = ((ComponentWithInjectConstructor) instance).getDependency();
                assertThat(dependency).isNotNull();
                final String innerDependency = ((DependencyWithInjectorConstructor) dependency).getDependency();
                assertThat(innerDependency).isNotNull().isEqualTo("indirect dependency");
            }

            @Nested
            class SadPath {
                @Test
                void should_throw_exception_when_bind_given_multi_inject_constructors() {
                    // when + then
                    assertThatThrownBy(() -> context.bind(Component.class, ComponentWithMultiInjectConstructors.class))
                            .isInstanceOf(IllegalComponentException.class)
                            .hasMessageContaining("cannot have multi @Inject constructors");
                }

                @Test
                void should_throw_exception_when_bind_given_no_inject_nor_default_constructor() {
                    // when + then
                    assertThatThrownBy(() -> context.bind(Component.class, ComponentWithNoInjectNorDefaultConstructor.class))
                            .isInstanceOf(IllegalComponentException.class)
                            .hasMessageContaining("cannot have multi @Inject constructors");
                }
            }
        }

        class ComponentWithMultiInjectConstructors implements Component {
            @Inject
            public ComponentWithMultiInjectConstructors(String dep1) {
            }

            @Inject
            public ComponentWithMultiInjectConstructors(String dep1, String dep2) {
            }
        }

        class ComponentWithNoInjectNorDefaultConstructor implements Component {
            public ComponentWithNoInjectNorDefaultConstructor(String dep1) {
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
