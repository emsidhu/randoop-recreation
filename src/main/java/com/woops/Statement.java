package com.woops;

/**
 * Abstract base class for all statements (methodCall, constructorCall, constantAssignment).
 */
public abstract class Statement {
  protected Object result;
  protected final Class<?> type;

  protected Statement(Class<?> type) {
    this.type = type;
  }

  public abstract void execute() throws Exception;

  public abstract String toCode();

  public Object getResult() {
      return result;
  }

  public Class<?> getType() {
    return type;
  }
}
