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

  public Object getResult() {
    return result;
  }

  // for filters (may be overridden by subclasses)
  public Object getReturnValue() {
    return null;
  }

  public abstract String toCode();

  public Class<?> getType() {
    return type;
  }

  // ✅ for equivalence filtering
  public abstract String getSignature();
}
