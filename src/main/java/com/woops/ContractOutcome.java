package com.woops;

// PASS / FAIL / ERROR result of a contract evaluation.
public final class ContractOutcome {

  public enum Status { PASS, FAIL, ERROR }

  private final Status status;
  private final String message;

  private ContractOutcome(Status status, String message) {
    this.status = status;
    this.message = message;
  }

  public static ContractOutcome pass(){ 
    return new ContractOutcome(Status.PASS,  null); 
  }

  public static ContractOutcome fail(String msg) { 
    return new ContractOutcome(Status.FAIL,  msg); 
  }

  public static ContractOutcome error(String msg) { 
    return new ContractOutcome(Status.ERROR, msg); 
   }

  public boolean isViolation() { 
    return status != Status.PASS; 
  }

  public Status  getStatus() { 
    return status; 
  }

  public String  getMessage() {
     return message; 
  }
}
