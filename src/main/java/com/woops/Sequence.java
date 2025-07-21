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

  public static Sequence extend(Method m, List<Sequence> seqs, List<Object> args) {
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
        // Update lastResult for use in filters
        lastResult = stmt.getReturnValue();
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

    for (Statement stmt : statements) {
      code.append("    ").append(stmt.toCode()).append(";\n");
    }

    code.append("  }\n");
    return code.toString();
  }
}
