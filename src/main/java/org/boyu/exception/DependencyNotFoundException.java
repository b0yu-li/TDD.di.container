package org.boyu.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class DependencyNotFoundException extends RuntimeException {
    private final String message;

    @Override
    public String getMessage() {
        return message;
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
