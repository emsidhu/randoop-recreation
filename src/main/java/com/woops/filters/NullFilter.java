package com.woops.filters;

import com.woops.Sequence;
import com.woops.Statement;

public class NullFilter implements Filter {
  @Override
  public boolean isValid(Sequence sequence) {
    Object result = sequence.getLastResult();
    
    // If result is not null, it's valid
    if (result != null) return true;
    
    // If result is null, make sure last statement wasn't just a void method
    Statement lastStmt = sequence.statements.get(sequence.statements.size() - 1);
    return lastStmt.getType() == void.class;
  }

  @Override
  public String getName() {
    return "NullFilter";
  }
}
