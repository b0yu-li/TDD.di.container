package org.boyu.exception;

public class DependencyNotFoundException extends RuntimeException {
    @Override
    public String getMessage() {
        return "cannot find dependency for given implementation";
    }
}
