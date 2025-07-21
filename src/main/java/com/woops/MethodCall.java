package com.woops;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/** 
  * Represents a method call within a sequence.
  * Includes its arguments and return value (result).
  */
public final class MethodCall extends Statement {
  private final Method method;
  private final List<Object> args; 
  private final boolean isStatic;

  public MethodCall(Method method, List<Object> args) {
    super(method.getReturnType()); // initialize type
    this.method = method;
    this.args = args;
    isStatic = Modifier.isStatic(method.getModifiers());
  }

  @Override
  public void execute() throws Exception {
    // Get the receiver from the first argument if the method is not static
    Object receiver = isStatic ? null : args.get(0);
    List<Object> actualArgs = isStatic ? args : args.subList(1, args.size());
    result = method.invoke(receiver, actualArgs.toArray());
  }

  @Override
  public String toCode() {
    StringBuilder code = new StringBuilder();
    // If the method is static, it's called on the class name
    if (isStatic) {
      code.append(method.getDeclaringClass().getName());
    } else {
      code.append("obj");
    }
    code.append(".").append(method.getName()).append("(");
    for (int i = (isStatic ? 0 : 1); i < args.size(); i++) {
      code.append(args.get(i));
      if (i < args.size() - 1) code.append(", ");
    }
    code.append(")");
    return code.toString();
  }

  @Override
  public Object getReturnValue() {
    return result;
  }

  // Added for equivalence filtering
  @Override
  public String getSignature() {
    return method.getName() + "(" + Arrays.toString(method.getParameterTypes()) + ")";
  }
}
