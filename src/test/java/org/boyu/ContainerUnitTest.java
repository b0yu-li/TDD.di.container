package org.boyu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        }
    }

    @Nested
    public class DependenciesSelection {

    }

    @Nested
    public class LifecycleManagement {

    }

}
