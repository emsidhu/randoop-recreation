package com.woops;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Sequence {
  public final List<Statement> statements = new ArrayList<>();

  // For filters
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
        lastResult = stmt.getResult();
      }
      threwException = false;
    } catch (Exception e) {
      threwException = true;
    }
  }

  // Getter for filter
  public Object getLastResult() {
    return lastResult;
  }

  public boolean throwsException() {
    return threwException;
  }

  // Fingerprint for EquivalenceFilter
  public String getSignatureFingerprint() {
    StringBuilder sb = new StringBuilder();
    for (Statement stmt : statements) {
      sb.append(stmt.getSignature()).append(";");
    }
    return sb.toString();
  }

  // Formats each test case
  public String toCode() {
    StringBuilder code = new StringBuilder();

    // Unique method name
    String methodName = "generatedTest_" + Math.abs(hashCode());

    code.append("  @org.junit.jupiter.api.Test\n");
    code.append("  public void ").append(methodName).append("() throws Throwable {\n");
    code.append("    com.demo.TestClass obj = new com.demo.TestClass();\n");
    // we should probably assert that invalid tests throw and error but i'm not sure how
    // if(valid ){
    for (int i = 0; i < statements.size(); i++) {
      Statement stmt = statements.get(i);
      // Give the statement a corresponding variable name if needed
      if (stmt.getType() != void.class) {
        stmt.setVariableName("var" + i);
      }

      code.append("    ").append(stmt.toCode()).append(";\n");
    }

    // } else {
  // @org.junit.jupiter.api.Test
  // public void generatedInvalidTest_1897789231() throws Throwable {
  //   Assertions.assertThrows(Throwable.class, () -> {
  //     com.demo.TestClass.crash();
  // });
    // }

    code.append("  }\n");
    return code.toString();
  }
}
