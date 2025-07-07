package com.woops;


public interface Contract {

  // Return true if this contract can be tested on the given objects.
  boolean isApplicable(Object... objs);

  // Perform the check
  ContractOutcome check(Object... objs);
}
