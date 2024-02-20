package org.boyu.exception;

public class IllegalComponentException extends RuntimeException {
    @Override
    public String getMessage() {
        return "cannot have multi @Inject constructors";
    }
}
