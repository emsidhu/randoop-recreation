package com.woops;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Sequence {
  public final List<Statement> statements = new ArrayList<>();

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

  public void execute() throws Exception {
    for (Statement stmt : statements) {
      stmt.execute();
    }
  }

  // properly formats each test case
  public String toCode() {
    StringBuilder code = new StringBuilder();

    // Unique method name
    String methodName = "generatedTest_" + Math.abs(hashCode());

    code.append("  @org.junit.jupiter.api.Test\n");
    code.append("  public void ").append(methodName).append("() throws Throwable {\n");
    code.append("    com.demo.TestClass obj = new com.demo.TestClass();\n");
    // we should probably assert that invalid tests throw and error but i'm not sure how
    // if(valid ){
      for (Statement stmt : statements) {
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
