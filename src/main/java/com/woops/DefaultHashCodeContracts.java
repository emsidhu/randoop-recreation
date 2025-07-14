package com.woops;

public final class DefaultHashCodeContract implements Contract {

  @Override
  public boolean isApplicable(Object... objs) {
    return objs.length == 1 && objs[0] != null;
  }

  @Override
  public ContractOutcome check(Object... objs) {
    Object o = objs[0];
    try {
      o.hashCode();  // check if it throws execption
      return ContractOutcome.pass();
    } catch (Throwable t) {
      return ContractOutcome.error("hashCode() threw " + t);
    }
  }
}
