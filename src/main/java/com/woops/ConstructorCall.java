package com.woops;
import java.lang.reflect.Constructor;
import java.util.List;

public class ConstructorCall extends Statement {
  private final Constructor<?> constructor;
  private final List<Object> args; 

  public ConstructorCall(Constructor<?> constructor, List<Object> args) {
    this.constructor = constructor;
    this.args = args;
  }

  @Override
  public void execute() throws Exception {
      result = constructor.newInstance(args.toArray());
  }
  
  @Override
  public String toCode() { 
    return "";
  }
}
