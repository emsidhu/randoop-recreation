package com.woops.contracts;

// o.equals(o) must return true and must not throw execption
public final class DefaultEqualsContract implements Contract {

  @Override
  public boolean isApplicable(Object... objs) {
    // Needs exactly one non-null object.
    return objs.length == 1 && objs[0] != null;
  }

  @Override
  public ContractOutcome check(Object... objs) {
    Object o = objs[0];
    try {
      if (!o.equals(o)) {
        return ContractOutcome.fail("equals is not reflexive for " + o);
      }
      return ContractOutcome.pass();
    } catch (Throwable t) {
      return ContractOutcome.error("equals(o) threw " + t);
    }
  }
}
