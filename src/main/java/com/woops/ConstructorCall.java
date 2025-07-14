package com.woops;
import java.lang.reflect.Constructor;
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

      // Checks contracts on new object
      ContractChecker.checkAll(result);
  }

  @Override
  public String toCode() { 
    StringBuilder code = new StringBuilder();
    code.append("new ");
    code.append(constructor.getName());
    code.append("(");
    for (int i = 0; i < args.size(); i++) {
      code.append(addQuotes(args.get(i).getValue()));
      if (i < args.size() - 1) code.append(", ");
    }
    code.append(")");
    return code.toString();
  }
}
