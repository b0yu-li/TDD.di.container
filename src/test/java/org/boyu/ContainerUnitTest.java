package org.boyu;

import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ContainerUnitTest {

    @NoArgsConstructor
    static class ComponentWithDefaultConstructor implements Component {
    }

    @Nested
    class ComponentConstruction {
        @Nested
        class BindTypeToInstance {
            @Test
            void should_bind_type_to_a_specific_instance() {
                // given
                Context context = new Context();
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
                final Context context = new Context();

                // when
                context.bind(Component.class, ComponentWithDefaultConstructor.class);

                // then
                final Component actual = context.get(Component.class);
                assertThat(actual).isNotNull();
                assertThat(actual).isInstanceOf(ComponentWithDefaultConstructor.class);
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
