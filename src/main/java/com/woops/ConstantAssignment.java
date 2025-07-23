package com.woops;

public class ConstantAssignment extends Statement {

  public ConstantAssignment(Object value, Class<?> type) {
    super(type); // initialize type
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

  @Override
  public Object getReturnValue() {
    return result;
  }

  // For equivalence filtering
  @Override
  public String getSignature() {
    return "const(" + (result == null ? "null" : result.getClass().getSimpleName()) + ")";
  }
}
