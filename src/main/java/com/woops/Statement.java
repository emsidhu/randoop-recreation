package com.woops;

/**
 * Abstract base class for all statements (methodCall, constructorCall, constantAssignment).
 */
public abstract class Statement {
  protected Object result;
  protected final Class<?> type;
  private String variableName;

  protected Statement(Class<?> type) {
    this.type = type;
  }

  protected Object addQuotes(Object obj) {
    if (obj instanceof String) return "\"" + (String) obj + "\"";
    if (obj instanceof Character) return "'" + obj + "'";
    return obj; 
  }

  public abstract void execute() throws Exception;

  public Object getResult() {
    return result;
  }

  public abstract String toCode();

  public Class<?> getType() {
    return type;
  }

  public void setVariableName(String name) {
    this.variableName = name;
  }

  public String getVariableName() {
    return variableName;
  }
}
