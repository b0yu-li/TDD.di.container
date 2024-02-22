package org.boyu.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DependencyNotFoundException extends RuntimeException {
    private final Class<?> componentType;
    private final Class<?> dependencyType;

    @Override
    public String getMessage() {
        return Reason.NO_DEPENDENCY_FOUND.getValue();
    }

    @Getter
    public enum Reason {
        NO_DEPENDENCY_FOUND("cannot find dependency for given implementation");

        private String value;

        private Reason(String value) {
            this.value = value;
        }
    }
}
