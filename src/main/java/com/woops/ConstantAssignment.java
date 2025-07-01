package com.woops;

public class ConstantAssignment extends Statement {

    public ConstantAssignment(Object value) {
        this.result = value; 
    }

    @Override
    public void execute() {
        // no-op: already assigned at construction
    }

    @Override
    public String toCode() {
        return (result == null) ? "null" : result.toString();
    }
}
