package com.woops;

public class Argument {
  private final Object value;
  // The statement that value comes from (if applicable)
  private final Statement statement;

  public Argument(Object value) {
    this.value = value;
    this.statement = null;
  }
  
  public Argument(Statement statement) {
    this.statement = statement;
    this.value = null; // Don't capture result at construction time
  }
  
  public Object getValue() {
    // If we have a statement, get its current result; otherwise use the direct value
    return statement != null ? statement.getResult() : value;
  }
  
  public Statement getStatement() {
    return statement;
  }

  public boolean hasStatement() {
    return statement != null;
  }
}
