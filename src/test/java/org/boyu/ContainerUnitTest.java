package org.boyu;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class ContainerUnitTest {

    @Nested
    public class ComponentConstruction {
        // TODO: bind instance
        // TODO: - constructor injection
        @Test
        void should_bind_type_to_a_specific_instance() {
            // given
            Context context = new Context();
            Component instance = new Component() {
            };

            // when
            context.bind(Component.class, instance);

            // then
            assertSame(instance, context.get(Component.class));
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
