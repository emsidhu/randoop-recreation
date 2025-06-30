package com.woops;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Sequence {
  public final List<MethodCall> methodCalls = new ArrayList<>();

  public Sequence() {
  }

  public static Sequence extend(Method m, List<Sequence> seqs, List<Object> args) {
    Sequence newSeq = new Sequence();
    for (Sequence seq : seqs) {
      newSeq.methodCalls.addAll(seq.methodCalls);
    }
    newSeq.methodCalls.add(new MethodCall(m, args));
    return newSeq;
  }

  public void execute() throws Exception {
    for (MethodCall m : methodCalls) {
      m.execute();
    }
  }

  // properly formats each test case
  public String toCode() {
    StringBuilder code = new StringBuilder();

    // Unique method name
    String methodName = "generatedTest_" + Math.abs(hashCode());

    code.append("  @org.junit.jupiter.api.Test\n");
    code.append("  public void ").append(methodName).append("() throws Throwable {\n");

    for (MethodCall call : methodCalls) {
      code.append("    ").append(call.toCode()).append(";\n");
    }

    code.append("  }\n");
    return code.toString();
  }
}
