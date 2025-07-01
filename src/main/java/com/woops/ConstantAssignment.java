package com.woops;

public class ConstantAssignment extends Statement {
    private final Object value;

    public ConstantAssignment(Object value, Class<?> type) {
        this.value = value;
        this.result = value; // already known; no need to execute
    }

    @Override
    public void execute() {
        // no-op: already assigned at construction
    }

    @Override
    public String toCode() {
        return (value == null) ? "null" : value.toString();
    }
}
