package com.woops;

import java.util.List;


// Runs the default set of contracts and throws on any violation.
public final class ContractChecker {

  private static final List<Contract> DEFAULT_CONTRACTS = List.of(
      new DefaultEqualsContract(),
      new DefaultHashCodeContract(),
      new DefaultToStringContract()
  );

  private ContractChecker() {}

  public static void checkAll(Object... objs) {
    for (Contract c : DEFAULT_CONTRACTS) {
      if (c.isApplicable(objs)) {
        ContractOutcome out = c.check(objs);
        if (out.isViolation()) {
          throw new ContractViolationException(out.getMessage());
        }
      }
    }
  }
}
