package com.woops;

import java.util.List;


// Runs the default set of contracts and throws on any violation.
public final class ContractChecker {

  private static final List<Contract> DEFAULT_CONTRACTS = List.of(
      new DefaultEqualsContract(),
      new DefaultHashCodeContract(),
      new DefaultToStringContract(),
      new NoUnexpectedNpeContract(),
      new NoAssertionErrorContract()
  );

  private ContractChecker() {}

  public static String checkAll(Object... objs) {
    for (Contract c : DEFAULT_CONTRACTS) {
      if (c.isApplicable(objs)) {
        ContractOutcome out = c.check(objs);
        if (out.isViolation()) {
          return c.getClass().getSimpleName();
        }
      }
    }
    return null;
  }
}
