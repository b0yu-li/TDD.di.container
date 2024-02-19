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
        // TODO: bind instance
        @Nested
        class ConstructorInjection {
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
        // TODO:    - w/ dependency:
        // TODO:        - one-level dep, e.g. A -depends-on-> B
        // TODO:        - multi-level dep, e.g. A -depends-on-> B -> C
        // TODO: - field injection
        // TODO: - method injection

        // TODO: bind abstract class

        // TODO: bind interface
    }

    @Nested
    public class DependenciesSelection {

    }

    @Nested
    public class LifecycleManagement {

    }

}
