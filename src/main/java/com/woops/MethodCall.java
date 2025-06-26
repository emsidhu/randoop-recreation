package com.woops;

import java.lang.reflect.Method;

/** 
 * Represents a method call within a sequence.
 * * Includes its arguments and return value (result).
 */
public final class MethodCall {
    private final Method method;
    private final Object[] args;
    private Object result;

    public MethodCall(Method method, Object[] args) {
        this.method = method;
        this.args = args;
    }

    public Object getResult() {
        return result;
    }

    
}
