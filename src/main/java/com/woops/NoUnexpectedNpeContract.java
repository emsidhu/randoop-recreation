package com.woops;

public final class NoUnexpectedNpeContract implements Contract {

  @Override
  public boolean isApplicable(Object... objs) {
    return objs.length == 1
        && objs[0] instanceof Invocation inv
        && inv.getExecutable() instanceof java.lang.reflect.Method;  // only applies to methods, not constructors
  }

  @Override
  public ContractOutcome check(Object... objs) {
    Invocation inv = (Invocation) objs[0];
    Throwable ex = inv.getException();
    if (ex == null || !(ex instanceof NullPointerException)) {
      return ContractOutcome.pass();
    }

    // An NPE was thrown, check for null input parameters
    for (Object arg : inv.getArgs()) {
      if (arg == null) return ContractOutcome.pass();
    }
    return ContractOutcome.error(
        "Method " + inv.getExecutable()
        + " threw NullPointerException with no null arguments");
  }
}
