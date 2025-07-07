package com.woops;

/**
 * Abstract base class for all statements (methodCall, constructorCall, constantAssignment).
 */
public abstract class Statement {
    protected Object result;

    public abstract void execute() throws Exception;

    public abstract String toCode();

    public Object getResult() {
        return result;
    }
    //update for filter
    public Object getReturnValue() {
        return null;
    }

}
