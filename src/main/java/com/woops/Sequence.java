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
    newSeq.methodCalls.add(new MethodCall(m, args.toArray()));
    return newSeq;
  }

  // TODO: Properly format as a testcase
  public String toCode() {
    StringBuilder code = new StringBuilder();
    for (MethodCall s : methodCalls) {
      code.append(s.toCode()).append(";\n");
    }
    return code.toString();
  }
}
