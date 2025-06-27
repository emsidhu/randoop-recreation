package com.woops;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/** 
  * Represents a method call within a sequence.
  * * Includes its arguments and return value (result).
  */
public final class MethodCall {
  private final Method method;
  private final List<Object> args;
  private final boolean isStatic;
  private Object result;

  public MethodCall(Method method, List<Object> args) {
    this.method = method;
    this.args = args;
    isStatic = Modifier.isStatic(method.getModifiers());
  }

  public Object getResult() {
    return result;
  }
  
  public String toCode() {
    StringBuilder code = new StringBuilder();
    // If the Method is static, it's called on the class name
    if (isStatic) {
      code.append(method.getDeclaringClass().getName());
    } else { // Otherwise, it's called on an instance (always named obj for now)
      code.append("obj");
    }
    code.append(".").append(method.getName()).append("(");
    // Don't include the first arg (the receiver) if the method isn't static
    for (int i = (isStatic ? 0 : 1); i < args.size(); i++) {
      code.append(args.get(i));
      if (i < args.size() - 1) code.append(", ");
    }
    code.append(")");
    return code.toString();
  }
}
