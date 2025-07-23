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
    StringBuilder code = new StringBuilder();

    // Save the result to a variable
    code.append(this.getType())
      .append(" ")
      .append(getVariableName())
      .append(" = ")
      .append((result == null) ? "null" : result.toString());

    return code.toString();
  }
}
