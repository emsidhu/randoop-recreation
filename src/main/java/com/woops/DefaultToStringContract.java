package com.woops;

public final class DefaultToStringContract implements Contract {

  @Override
  public boolean isApplicable(Object... objs) {
    return objs.length == 1 && objs[0] != null;
  }

  @Override
  public ContractOutcome check(Object... objs) {
    Object o = objs[0];
    try {
      o.toString();  // check if it throws exception
      return ContractOutcome.pass();
    } catch (Throwable t) {
      return ContractOutcome.error("toString() threw " + t);
    }
  }
}
