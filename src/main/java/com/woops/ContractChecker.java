package com.woops;

import java.util.List;

// Thrown when a contract outcome is FAIL or ERROR
public final class ContractViolationException extends RuntimeException {
  public ContractViolationException(String msg) { super(msg); }
}

// Runs the default set of contracts and throws on any violation.
public final class ContractChecker {

  private static final List<Contract> DEFAULT_CONTRACTS =
      List.of(new ReflexiveEqualsContract());

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
