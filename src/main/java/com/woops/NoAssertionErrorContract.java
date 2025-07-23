package com.woops;

public final class NoAssertionErrorContract implements Contract {

  @Override
  public boolean isApplicable(Object... objs) {
    return objs.length == 1
        && objs[0] instanceof Invocation inv
        && inv.getExecutable() instanceof java.lang.reflect.Method; // only applies to methods, not constructors
  }

  @Override
  public ContractOutcome check(Object... objs) {
    Invocation inv = (Invocation) objs[0];
    if (inv.getException() instanceof AssertionError) {
      return ContractOutcome.error(
          "Method " + inv.getExecutable() + " threw AssertionError");
    }
    return ContractOutcome.pass();
  }
}
