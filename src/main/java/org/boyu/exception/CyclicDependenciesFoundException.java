package org.boyu.exception;

public class CyclicDependenciesFoundException extends RuntimeException {
    @Override
    public String getMessage() {
        return "found cyclic dependencies which are not allowed";
    }
}
