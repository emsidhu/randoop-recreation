package com.woops;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Sequence {
  public final List<Statement> statements = new ArrayList<>();

  // for filters
  private Object lastResult = null;
  private boolean threwException = false;

  public Sequence() {
  }

  public void concat(Sequence seq) {
    this.statements.addAll(seq.statements);
  }

  public static Sequence extend(Method m, List<Sequence> seqs, List<Argument> args) {
    Sequence newSeq = new Sequence();
    for (Sequence seq : seqs) {
      newSeq.statements.addAll(seq.statements);
    }
    newSeq.statements.add(new MethodCall(m, args));
    return newSeq;
  }

  public void execute() {
    try {
      for (Statement stmt : statements) {
        stmt.execute();
        // Update lastResult： Statement.getResult
        lastResult = stmt.getResult();
      }
      threwException = false;
    } catch (Exception e) {
      threwException = true;
    }
  }

  //  getter for filter
  public Object getLastResult() {
    return lastResult;
  }

  public boolean throwsException() {
    return threwException;
  }

  // properly formats each test case
  public String toCode(boolean isValid) {
    StringBuilder code = new StringBuilder();

    // Unique method name
    String prefix = isValid ? "validGeneratedTest_" : "invalidGeneratedTest_";
    String methodName = prefix + Math.abs(hashCode());

    code.append("  @org.junit.jupiter.api.Test\n");
    code.append("  public void ").append(methodName).append("() throws Throwable {\n");
    if (isValid) {
      generateValidTest(code);
    } else {
      generateInvalidTest(code);
    }

    code.append("  }\n");
    return code.toString();
  }

  private void generateValidTest(StringBuilder code) {
    for (int i = 0; i < statements.size(); i++) {
      Statement stmt = statements.get(i);
      // Give the statement a corresponding variable name if needed
      if (stmt.getType() != void.class) {
        stmt.setVariableName("var" + i);
      }
      code.append("    ").append(stmt.toCode()).append(";\n");
    }
    
    // TODO: Add assertions for applicable contracts 
  }

  private void generateInvalidTest(StringBuilder code) {
    code.append("    Assertions.assertThrows(Throwable.class, () -> {\n");
    
    for (int i = 0; i < statements.size(); i++) {
      Statement stmt = statements.get(i);
      // Give the statement a corresponding variable name if needed
      if (stmt.getType() != void.class) {
        stmt.setVariableName("var" + i);
      }
      code.append("      ").append(stmt.toCode()).append(";\n");
    }
    
    code.append("    });\n");
  }
}

