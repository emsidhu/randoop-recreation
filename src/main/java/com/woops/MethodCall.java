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
  private final List<Argument> args; 
  private final boolean isStatic;

  public MethodCall(Method method, List<Argument> args) {
    super(method.getReturnType()); // initialize type
    this.method = method;
    this.args = args;
    isStatic = Modifier.isStatic(method.getModifiers());
  }

  @Override
  public void execute() throws Exception {
    // Get the receiver from the first argument if the method isn't static
    Object receiver = isStatic ? null : args.get(0).getValue();
    
    Object[] actualArgs = args.stream()
      .skip(isStatic ? 0 : 1)  // Skip first argument (the receiver) if not static
      .map(Argument::getValue)  // Extract values from Arguments
      .toArray();
      
    result = method.invoke(receiver, actualArgs);
  }


  
  @Override
  public String toCode() {
    StringBuilder code = new StringBuilder();

    // If it returns something, save it to a variable 
    if (method.getReturnType() != void.class) {
      code.append(method.getReturnType().getSimpleName())
        .append(" ")
        .append(getVariableName())
        .append(" = ");
    }

    // If the Method is static, it's called on the class name
    if (isStatic) {
      code.append(method.getDeclaringClass().getName());
    } else { // Otherwise, it's called on an instance
      Argument receiverArg = args.get(0);
      code.append(receiverArg.getStatement().getVariableName());
    }
    code.append(".").append(method.getName()).append("(");

    // Don't include the first arg (the receiver) if the method isn't static
    for (int i = (isStatic ? 0 : 1); i < args.size(); i++) {
      Argument arg = args.get(i);
      if (arg.hasStatement()) {
        code.append(arg.getStatement().getVariableName());
      } else {
        code.append(addQuotes(arg.getValue()));
      }
      if (i < args.size() - 1) code.append(", ");
    }

    code.append(")");
    return code.toString();
  }

  // Added for equivalence filtering
  @Override
  public String getSignature() {
    return method.getName() + "(" + Arrays.toString(method.getParameterTypes()) + ")";
  }
}
