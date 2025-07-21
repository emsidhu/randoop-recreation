package com.woops;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

public class ConstructorCall extends Statement {
  private final Constructor<?> constructor;
  private final List<Object> args; 

  public ConstructorCall(Constructor<?> constructor, List<Object> args) {
    super(constructor.getDeclaringClass()); // initialize type
    this.constructor = constructor;
    this.args = args;
  }

  @Override
  public void execute() throws Exception {
    result = constructor.newInstance(args.toArray());
  }

  @Override
  public String toCode() {
    StringBuilder code = new StringBuilder();
    code.append(constructor.getDeclaringClass().getSimpleName()).append(" obj = new ");
    code.append(constructor.getDeclaringClass().getSimpleName()).append("(");
    for (int i = 0; i < args.size(); i++) {
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

  // For equivalence filtering
  @Override
  public String getSignature() {
    return "new " + constructor.getDeclaringClass().getSimpleName() +
           "(" + Arrays.toString(constructor.getParameterTypes()) + ")";
  }
}
