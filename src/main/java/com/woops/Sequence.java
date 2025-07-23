package com.woops;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Sequence {
  public final List<Statement> statements = new ArrayList<>();

  // For filters
  private Object lastResult = null;
  private boolean threwException = false;
  private String violatedContract = null; // Track which contract was violated
  private Statement violatingStmt = null; // Track which statement caused the violation

  public Sequence() {
  }

  public void concat(Sequence seq) {
    this.statements.addAll(seq.statements);
  }

  public static Sequence extend(Method m, List<Sequence> seqs, List<Argument> args) {
    Sequence newSeq = new Sequence();
    for (Sequence seq : seqs) {
      newSeq.statements.addAll(seq.statements);
    }
    newSeq.statements.add(new MethodCall(m, args));
    return newSeq;
  }

  public void execute() {
    try {
      for (Statement stmt : statements) {
        stmt.execute();
        lastResult = stmt.getResult();
      }
      threwException = false;
    } catch (Exception e) {
      threwException = true;
    }
  }

  // Getter for filter
  public Object getLastResult() {
    return lastResult;
  }

  public boolean throwsException() {
    return threwException;
  }


  public void setViolatedContract(String contract) {
    this.violatedContract = contract;
  }

  public String getViolatedContract() {
    return violatedContract;
  }

  public void setViolatingStmt(Statement statement) {
    this.violatingStmt = statement;
  }

  public Statement getViolatingStmt() {
    return violatingStmt;
  }

  // properly formats each test case
  public String toCode(boolean isValid) {
    return toCode(isValid, this.violatedContract);
  }

  // Fingerprint for EquivalenceFilter
  public String getSignatureFingerprint() {
    StringBuilder sb = new StringBuilder();
    for (Statement stmt : statements) {
      sb.append(stmt.getSignature()).append(";");
    }
    return sb.toString();
  }

  // Formats each test case
  public String toCode(boolean isValid, String violatedContract) {
    StringBuilder code = new StringBuilder();

    // Unique method name
    String prefix = isValid ? "validGeneratedTest_" : "invalidGeneratedTest_";
    String methodName = prefix + Math.abs(hashCode());

    code.append("  @org.junit.jupiter.api.Test\n");
    code.append("  public void ").append(methodName).append("() throws Throwable {\n");
    if (isValid) {
      generateValidTest(code);
    } else {
      generateInvalidTest(code, violatedContract);
    }

    code.append("  }\n");
    return code.toString();
  }

  private void generateValidTest(StringBuilder code) {
    for (int i = 0; i < statements.size(); i++) {
      Statement stmt = statements.get(i);
      // Give the statement a corresponding variable name if needed
      if (stmt.getType() != void.class) {
        stmt.setVariableName("var" + i);
      }
      code.append("    ").append(stmt.toCode()).append(";\n");
    }

    // Comment this out if not needed
    code.append("\n").append("    // Contract Checks \n");
    addContractAssertions(code);
  }

  private void generateInvalidTest(StringBuilder code, String violatedContract) {
    code.append("    Assertions.assertThrows(Throwable.class, () -> {\n");
    
    for (int i = 0; i < statements.size(); i++) {
      Statement stmt = statements.get(i);
      // Give the statement a corresponding variable name if needed
      if (stmt.getType() != void.class) {
        stmt.setVariableName("var" + i);
      }
      code.append("      ").append(stmt.toCode()).append(";\n");
    }
    
    code.append("    });\n");
    
    // Add assertion that verifies the specific contract violation on the violating object
    if (violatedContract != null && !violatedContract.isEmpty()) {
      addContractViolationAssertion(code, violatedContract);
    }
  }

  
  private void addContractAssertions(StringBuilder code) {
    for (Statement stmt : statements) {
      if (stmt.getType() != void.class && stmt.getVariableName() != null) {
        String varName = stmt.getVariableName();
        Class<?> type = stmt.getType();

        // Add assertions for default contracts
        addDefaultAssertions(code, varName, type);
      }
    }
  }

  private void addDefaultAssertions(StringBuilder code, String varName, Class<?> type) {
    if (type.isPrimitive()) {
      return;
    }

    // o.equals(o) must return true (reflexivity)
    code.append("    Assertions.assertEquals(").append(varName).append(", ").append(varName).append(");\n");
    // hashCode() should not throw exception
    code.append("    Assertions.assertDoesNotThrow(() -> ").append(varName).append(".hashCode());\n");
    // toString() should not throw exception
    code.append("    Assertions.assertDoesNotThrow(() -> ").append(varName).append(".toString());\n");
  }


  private void addContractViolationAssertion(StringBuilder code, String violatedContract) {
    if (violatingStmt == null || violatingStmt.getVariableName() == null) return;
    
    String varName = violatingStmt.getVariableName();
    switch (violatedContract) {
      case "DefaultEqualsContract":
        code.append("    Assertions.assertEquals(").append(varName).append(", ").append(varName).append(");\n");
        break;
        
      case "DefaultHashCodeContract":
        code.append("    Assertions.assertDoesNotThrow(() -> ").append(varName).append(".hashCode());\n");
        break;
        
      case "DefaultToStringContract":
        code.append("    Assertions.assertDoesNotThrow(() -> ").append(varName).append(".toString());\n");
        break;
        
      default:
        code.append("    // Contract ").append(violatedContract).append(" was violated by ").append(varName).append("\n");
    }
  }


}