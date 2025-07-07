package com.woops;

// Thrown when a contract outcome is FAIL or ERROR
public final class ContractViolationException extends RuntimeException {
  public ContractViolationException(String msg) { super(msg); }
}
