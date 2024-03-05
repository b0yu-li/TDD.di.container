package org.boyu.exception;

public class IllegalComponentException extends RuntimeException {
    private final String message;

    public IllegalComponentException(String message) {
        this.message = message;
    }

    public IllegalComponentException(Reason reason) {
        this.message = reason.getValue();
    }

    @Override
    public String getMessage() {
        return message;
    }

    public enum Reason {
        MULTI_INJECT_CONSTRUCTORS("cannot have multi @Inject constructors"),
        NO_PROPER_CONSTRUCTOR_FOUND("no @Inject nor no arg constructor found"),
        ABSTRACT_CLASS_NOT_ALLOWED("cannot bind non-instantiatable classes (i.e. abstract class or interface)");

        private String value;

        private Reason(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
