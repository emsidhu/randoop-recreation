package com.woops;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

public class ConstructorCall extends Statement {
  private final Constructor<?> constructor;
  private final List<Argument> args; 

  public ConstructorCall(Constructor<?> constructor, List<Argument> args) {
    super(constructor.getDeclaringClass()); // initialize type
    this.constructor = constructor;
    this.args = args;
  }

  @Override
  public void execute() throws Exception {
      result = constructor.newInstance(args.stream().map(Argument::getValue).toArray());
  }

  // For equivalence filtering
  @Override
  public String getSignature() {
    return "new " + constructor.getDeclaringClass().getSimpleName() +
           "(" + Arrays.toString(constructor.getParameterTypes()) + ")";
  }
  
  public String toCode() { 
    StringBuilder code = new StringBuilder();

    // Save the result to a variable
    code.append(constructor.getName())
      .append(" ")
      .append(getVariableName())
      .append(" = ");


    code.append("new ");
    code.append(constructor.getName());
    code.append("(");
    for (int i = 0; i < args.size(); i++) {
      Argument arg = args.get(i);
      if (arg.hasStatement()) {
        code.append(arg.getStatement().getVariableName());
      } else {
        code.append(arg.getValue());
      }
      if (i < args.size() - 1) code.append(", ");
    }

    code.append(")");
    return code.toString();

  }
}
